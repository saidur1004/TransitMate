package com.transitmate.bd.domain.repository

import com.transitmate.bd.domain.model.NfcHardwareStatus
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for observing and querying device NFC availability.
 */
interface NfcAvailabilityRepository {
    /**
     * Inspects and returns current device NFC hardware availability.
     */
    fun checkNfcStatus(): NfcHardwareStatus

    /**
     * Provides an observable flow of device NFC readiness.
     */
    val nfcStatus: Flow<NfcHardwareStatus>
}

