package com.transitmate.bd.data

import com.transitmate.bd.core.nfc.NfcAvailabilityChecker
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.domain.repository.NfcAvailabilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Concrete implementation of [NfcAvailabilityRepository] using platform [NfcAvailabilityChecker].
 */
class DefaultNfcAvailabilityRepository(
    private val nfcChecker: NfcAvailabilityChecker
) : NfcAvailabilityRepository {

    override fun checkNfcStatus(): NfcHardwareStatus {
        return nfcChecker.checkStatus()
    }

    override val nfcStatus: Flow<NfcHardwareStatus> = flow {
        emit(nfcChecker.checkStatus())
    }
}
