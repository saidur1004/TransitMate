package com.transitmate.bd.data

import com.transitmate.bd.core.nfc.NfcAvailabilityChecker
import com.transitmate.bd.domain.model.NfcHardwareStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultNfcAvailabilityRepositoryTest {

    private class FakeNfcChecker(var status: NfcHardwareStatus) : NfcAvailabilityChecker {
        override fun checkStatus(): NfcHardwareStatus = status
    }

    @Test
    fun checkNfcStatus_returnsCheckerStatus() {
        val fakeChecker = FakeNfcChecker(NfcHardwareStatus.AVAILABLE)
        val repository = DefaultNfcAvailabilityRepository(fakeChecker)

        assertEquals(NfcHardwareStatus.AVAILABLE, repository.checkNfcStatus())

        fakeChecker.status = NfcHardwareStatus.DISABLED
        assertEquals(NfcHardwareStatus.DISABLED, repository.checkNfcStatus())

        fakeChecker.status = NfcHardwareStatus.NOT_SUPPORTED
        assertEquals(NfcHardwareStatus.NOT_SUPPORTED, repository.checkNfcStatus())
    }

    @Test
    fun nfcStatusFlow_emitsCurrentStatus() = runTest {
        val fakeChecker = FakeNfcChecker(NfcHardwareStatus.AVAILABLE)
        val repository = DefaultNfcAvailabilityRepository(fakeChecker)

        val emittedStatus = repository.nfcStatus.first()
        assertEquals(NfcHardwareStatus.AVAILABLE, emittedStatus)
    }
}

