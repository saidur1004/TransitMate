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
 * Isolated NFC Reader Mode harness for both physical card discovery and diagnostics.
 *
 * Uses modern Android Reader Mode (API 19+) to capture anticollision metadata
 * on a system background binder thread without triggering system chime dialogs.
 *
 * Strictly read-only: does NOT attempt service block reading, card writing, or balance parsing.
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
     * @param onTagScanned Callback invoked on a background thread when a tag is processed.
     */
    fun enable(
        activity: Activity,
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
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
     * Extracts discovery metadata from a newly detected [Tag] without executing unverified block reads.
     */
    fun processTag(tag: Tag): NfcDiagnosticData {
        val count = scanCounter.incrementAndGet()
        val techs = tag.techList.map { it.substringAfterLast('.') }
        val isNfcF = tag.techList.contains(TECH_NFC_F)
        val rawId = tag.id
        val idmHex = NfcDiagnosticData.bytesToHex(rawId)
        val idmRedacted = NfcDiagnosticData.redactIdm(rawId)
        val idmDigest = NfcDiagnosticData.sha256Digest(rawId)

        var systemCodeHex: String? = null
        var pmmHex: String? = null
        var rfLinkOk = false
        var isTagLost = false
        var errorMsg: String? = null

        if (isNfcF) {
            val nfcF = NfcF.get(tag)
            if (nfcF != null) {
                systemCodeHex = NfcDiagnosticData.formatSystemCodeHex(nfcF.systemCode)
                pmmHex = NfcDiagnosticData.bytesToHex(nfcF.manufacturer)

                try {
                    nfcF.connect()
                    rfLinkOk = true
                } catch (_: TagLostException) {
                    isTagLost = true
                    errorMsg = "Tag removed too early before RF link completed"
                } catch (e: IOException) {
                    errorMsg = "RF connection failed: ${e.message ?: "IO error"}"
                } finally {
                    try {
                        nfcF.close()
                    } catch (_: Exception) {
                        // Connection already closed
                    }
                }
            } else {
                errorMsg = "NfcF technology listed but NfcF.get(tag) returned null"
            }
        }

        return NfcDiagnosticData(
            timestamp = System.currentTimeMillis(),
            techList = techs,
            isNfcF = isNfcF,
            idmHex = idmHex,
            idmRedacted = idmRedacted,
            idmDigestKey = idmDigest,
            systemCodeHex = systemCodeHex,
            manufacturerPmmHex = pmmHex,
            scanCount = count,
            rfLinkOk = rfLinkOk,
            isTagLost = isTagLost,
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
    }
}
