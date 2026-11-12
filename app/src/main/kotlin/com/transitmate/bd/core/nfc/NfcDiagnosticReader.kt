package com.transitmate.bd.core.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import java.io.IOException

/**
 * Isolated diagnostic reader harness for Phase 03 physical card validation.
 *
 * Uses modern Android Reader Mode (API 19+) to capture anticollision metadata
 * on a dedicated background thread without triggering system chime dialogs.
 *
 * Strictly read-only: does NOT attempt service block reading, card writing, or balance parsing.
 */
class NfcDiagnosticReader {

    private var scanCounter = 0

    /**
     * Enables Reader Mode on the host activity if an NFC adapter is available.
     *
     * @param activity Host Activity.
     * @param onTagScanned Callback invoked on a background thread when a tag is detected.
     */
    fun enable(activity: Activity, onTagScanned: (NfcDiagnosticData) -> Unit) {
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
                    val result = processTag(tag)
                    onTagScanned(result)
                },
                flags,
                options
            )
        } catch (_: Exception) {
            // Handset OEM security restriction or adapter disabled
        }
    }

    /**
     * Disables Reader Mode when host activity pauses.
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
     * Extracts discovery metadata from a newly detected [Tag] without executing block reads.
     */
    fun processTag(tag: Tag): NfcDiagnosticData {
        scanCounter++
        val techs = tag.techList.map { it.substringAfterLast('.') }
        val isNfcF = tag.techList.contains("android.nfc.tech.NfcF")
        val rawId = tag.id
        val idmHex = NfcDiagnosticData.bytesToHex(rawId)
        val idmRedacted = NfcDiagnosticData.redactIdm(rawId)

        var systemCodeHex: String? = null
        var pmmHex: String? = null
        var errorMsg: String? = null

        if (isNfcF) {
            val nfcF = NfcF.get(tag)
            if (nfcF != null) {
                systemCodeHex = NfcDiagnosticData.bytesToHex(nfcF.systemCode)
                pmmHex = NfcDiagnosticData.bytesToHex(nfcF.manufacturer)

                // Establish test connection and immediately close safely to verify RF link
                try {
                    nfcF.connect()
                } catch (e: IOException) {
                    errorMsg = "RF connection failed: ${e.message}"
                } finally {
                    try {
                        nfcF.close()
                    } catch (_: Exception) {
                        // Connection already closed
                    }
                }
            } else {
                errorMsg = "NfcF technology present in techList but NfcF.get(tag) returned null"
            }
        }

        return NfcDiagnosticData(
            timestamp = System.currentTimeMillis(),
            techList = techs,
            isNfcF = isNfcF,
            idmHex = idmHex,
            idmRedacted = idmRedacted,
            systemCodeHex = systemCodeHex,
            manufacturerPmmHex = pmmHex,
            scanCount = scanCounter,
            errorMessage = errorMsg
        )
    }

    fun resetCounter() {
        scanCounter = 0
    }
}

