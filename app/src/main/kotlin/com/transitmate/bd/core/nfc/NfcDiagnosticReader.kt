package com.transitmate.bd.core.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.NfcF
import android.os.Bundle
import com.transitmate.bd.domain.model.CardScanOutcome
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Isolated NFC Reader Mode harness for both physical FeliCa card reading and diagnostics.
 *
 * Uses modern Android Reader Mode (API 19+) to execute read-only FeliCa Read Without
 * Encryption (0x06) queries on a system background binder thread without triggering
 * system chime dialogs or modifying card memory.
 */
class NfcDiagnosticReader(
    private val scanMapper: NfcTagScanMapper = NfcTagScanMapper()
) {

    private val scanCounter = AtomicInteger(0)

    @Volatile
    private var lastTagDigest: String? = null

    @Volatile
    private var lastTagTimestamp: Long = 0L

    /**
     * Enables Reader Mode on the host activity if an NFC adapter is available.
     *
     * @param activity Host Activity.
     * @param debounceMs Suppresses rapid duplicate callbacks for the exact same resting tag.
     * @param onReadingStarted Optional callback invoked right when an NFC tag triggers a session.
     * @param onOutcome Optional consumer-facing outcome callback mapped from the physical read.
     * @param onTagScanned Callback invoked on a background thread with full diagnostic details.
     */
    fun enable(
        activity: Activity,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        onReadingStarted: (() -> Unit)? = null,
        onOutcome: ((CardScanOutcome) -> Unit)? = null,
        onTagScanned: (NfcDiagnosticData) -> Unit
    ) {
        val adapter = try {
            NfcAdapter.getDefaultAdapter(activity)
        } catch (_: Exception) {
            null
        } ?: return

        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        val options = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        try {
            adapter.enableReaderMode(
                activity,
                { tag ->
                    val now = System.currentTimeMillis()
                    val digest = NfcDiagnosticData.sha256Digest(tag.id)
                    if (debounceMs > 0 && digest != null && digest == lastTagDigest && (now - lastTagTimestamp) < debounceMs) {
                        return@enableReaderMode
                    }
                    lastTagDigest = digest
                    lastTagTimestamp = now

                    onReadingStarted?.invoke()
                    val result = processTag(tag)
                    onTagScanned(result)
                    onOutcome?.invoke(scanMapper.mapToOutcome(result))
                },
                flags,
                options
            )
        } catch (_: Exception) {
            // Handset OEM security restriction or adapter disabled
        }
    }

    /**
     * Disables Reader Mode when host activity pauses or scanner stops.
     */
    fun disable(activity: Activity) {
        val adapter = try {
            NfcAdapter.getDefaultAdapter(activity)
        } catch (_: Exception) {
            null
        } ?: return

        try {
            adapter.disableReaderMode(activity)
        } catch (_: Exception) {
            // Ignored on teardown
        }
    }

    /**
     * Connects to a discovered [Tag] and executes read-only FeliCa Read Without Encryption
     * against DTCA history service 0x220F when [android.nfc.tech.NfcF] is present.
     */
    fun processTag(tag: Tag): NfcDiagnosticData {
        val count = scanCounter.incrementAndGet()
        val techs = tag.techList.map { it.substringAfterLast('.') }
        val isNfcF = tag.techList.contains(TECH_NFC_F)
        val rawId = tag.id
        val idmHex = NfcDiagnosticData.bytesToHex(rawId)
        val idmDigest = NfcDiagnosticData.sha256Digest(rawId)

        if (!isNfcF) {
            return NfcDiagnosticData(
                timestamp = System.currentTimeMillis(),
                techList = techs,
                isNfcF = false,
                idmHex = idmHex,
                idmDigestKey = idmDigest,
                scanCount = count
            )
        }

        val nfcF = NfcF.get(tag) ?: return NfcDiagnosticData(
            timestamp = System.currentTimeMillis(),
            techList = techs,
            isNfcF = true,
            idmHex = idmHex,
            idmDigestKey = idmDigest,
            scanCount = count,
            errorMessage = "NfcF technology listed but NfcF.get(tag) returned null"
        )

        val systemCodeHex = NfcDiagnosticData.formatSystemCodeHex(nfcF.systemCode)
        val pmmHex = NfcDiagnosticData.bytesToHex(nfcF.manufacturer)
        val transceiver = AndroidFelicaBlockTransceiver(nfcF, rawId ?: ByteArray(0))

        return readWithTransceiver(
            transceiver = transceiver,
            techs = techs,
            isNfcF = true,
            rawId = rawId,
            systemCodeHex = systemCodeHex,
            pmmHex = pmmHex,
            scanCount = count
        )
    }

    /**
     * Core FeliCa RF read pipeline over [FelicaBlockTransceiver], kept platform-agnostic so
     * unit tests can verify window reads, partial recovery, status flags, and error handling.
     */
    internal fun readWithTransceiver(
        transceiver: FelicaBlockTransceiver,
        techs: List<String>,
        isNfcF: Boolean,
        rawId: ByteArray?,
        systemCodeHex: String?,
        pmmHex: String?,
        scanCount: Int,
        timestampMillis: Long = System.currentTimeMillis()
    ): NfcDiagnosticData {
        val idmHex = NfcDiagnosticData.bytesToHex(rawId)
        val idmDigest = NfcDiagnosticData.sha256Digest(rawId)

        var rfLinkOk = false
        var isTagLost = false
        var serviceProtected = false
        var statusFlag1: Int? = null
        var statusFlag2: Int? = null
        var errorMsg: String? = null
        val collectedBlocks = mutableListOf<ByteArray>()

        try {
            transceiver.connect()
            rfLinkOk = true

            if (rawId != null && rawId.size == FelicaProtocol.FELICA_IDM_LENGTH) {
                for ((windowIndex, startBlock) in READ_WINDOW_STARTS.withIndex()) {
                    try {
                        val cmd = FelicaProtocol.buildReadWithoutEncryptionCommand(
                            idm = rawId,
                            serviceCode = FelicaProtocol.DTCA_HISTORY_SERVICE_CODE,
                            startBlock = startBlock,
                            blockCount = FelicaProtocol.DEFAULT_WINDOW_BLOCK_COUNT
                        )
                        val responseBytes = transceiver.transceive(cmd)
                        val decodedFrame = FelicaProtocol.decodeReadResponse(responseBytes, expectedIdm = rawId)
                        statusFlag1 = decodedFrame.statusFlag1
                        statusFlag2 = decodedFrame.statusFlag2

                        if (!decodedFrame.isValidFrame) {
                            if (windowIndex == 0) {
                                errorMsg = "Malformed FeliCa read response frame"
                            }
                            break
                        }

                        if (!decodedFrame.isSuccess) {
                            if (windowIndex == 0) {
                                serviceProtected = true
                                errorMsg = "FeliCa service 0x220F returned status SF1=0x%02X SF2=0x%02X"
                                    .format(decodedFrame.statusFlag1, decodedFrame.statusFlag2)
                            }
                            break
                        }

                        collectedBlocks += decodedFrame.blocks
                    } catch (e: TagLostException) {
                        if (collectedBlocks.isEmpty()) {
                            isTagLost = true
                            rfLinkOk = false
                            errorMsg = "Tag removed too early before RF read completed"
                        } else {
                            errorMsg = "Window ${windowIndex + 1} interrupted early; preserved ${collectedBlocks.size} blocks"
                        }
                        break
                    } catch (e: IOException) {
                        if (collectedBlocks.isEmpty()) {
                            rfLinkOk = false
                            errorMsg = "RF read failed: ${e.message ?: "IO error"}"
                        } else {
                            errorMsg = "Window ${windowIndex + 1} IO warning; preserved ${collectedBlocks.size} blocks"
                        }
                        break
                    }
                }
            } else {
                errorMsg = "IDm length invalid for FeliCa read (${rawId?.size ?: 0} bytes)"
            }
        } catch (_: TagLostException) {
            isTagLost = true
            rfLinkOk = false
            errorMsg = "Tag removed too early before RF link completed"
        } catch (e: IOException) {
            rfLinkOk = false
            errorMsg = "RF connection failed: ${e.message ?: "IO error"}"
        } finally {
            transceiver.close()
        }

        val readSucceeded = rfLinkOk && !serviceProtected && collectedBlocks.isNotEmpty()
        val decodedSnapshot = if (rfLinkOk && !isTagLost) {
            TransitCardBlockDecoder.decodeCardData(
                idmBytes = rawId,
                systemCodeHex = systemCodeHex,
                rawBlocks = collectedBlocks,
                readSucceeded = readSucceeded
            )
        } else {
            null
        }

        return NfcDiagnosticData(
            timestamp = timestampMillis,
            techList = techs,
            isNfcF = isNfcF,
            idmHex = idmHex,
            idmDigestKey = idmDigest,
            systemCodeHex = systemCodeHex,
            manufacturerPmmHex = pmmHex,
            scanCount = scanCount,
            rfLinkOk = rfLinkOk,
            isTagLost = isTagLost,
            blocksReadCount = collectedBlocks.size,
            validTransactionCount = decodedSnapshot?.validBlockCount ?: 0,
            statusFlag1 = statusFlag1,
            statusFlag2 = statusFlag2,
            serviceProtected = serviceProtected,
            decodedSnapshot = decodedSnapshot,
            errorMessage = errorMsg
        )
    }

    fun resetCounter() {
        scanCounter.set(0)
        lastTagDigest = null
        lastTagTimestamp = 0L
    }

    companion object {
        private const val TECH_NFC_F = "android.nfc.tech.NfcF"
        private const val DEFAULT_DEBOUNCE_MS = 1200L
        private val READ_WINDOW_STARTS = listOf(0, 10)
    }
}
