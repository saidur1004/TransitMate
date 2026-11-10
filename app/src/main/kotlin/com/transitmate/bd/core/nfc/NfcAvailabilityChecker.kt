package com.transitmate.bd.core.nfc

import android.content.Context
import android.nfc.NfcAdapter
import com.transitmate.bd.domain.model.NfcHardwareStatus

/**
 * Platform helper to safely query Android NFC hardware availability.
 * Designed to never crash on devices or emulators lacking NFC hardware.
 */
interface NfcAvailabilityChecker {
    fun checkStatus(): NfcHardwareStatus
}

/**
 * Standard Android implementation using [NfcAdapter].
 */
class AndroidNfcAvailabilityChecker(
    private val context: Context
) : NfcAvailabilityChecker {

    override fun checkStatus(): NfcHardwareStatus {
        val nfcAdapter: NfcAdapter? = try {
            NfcAdapter.getDefaultAdapter(context.applicationContext)
        } catch (_: Exception) {
            null
        }

        return when {
            nfcAdapter == null -> NfcHardwareStatus.NOT_SUPPORTED
            !nfcAdapter.isEnabled -> NfcHardwareStatus.DISABLED
            else -> NfcHardwareStatus.AVAILABLE
        }
    }
}

