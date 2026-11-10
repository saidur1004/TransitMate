package com.transitmate.bd.feature.startup

import com.transitmate.bd.core.common.DispatcherProvider
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.domain.repository.NfcAvailabilityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {

    private class TestDispatcherProvider(val dispatcher: TestDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher get() = dispatcher
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
        override val unconfined: CoroutineDispatcher get() = dispatcher
    }

    private class FakeNfcRepository(var status: NfcHardwareStatus) : NfcAvailabilityRepository {
        override fun checkNfcStatus(): NfcHardwareStatus = status
        override val nfcStatus: Flow<NfcHardwareStatus> get() = flowOf(status)
    }

    @Test
    fun init_checksNfcStatusAndUpdatesUiState() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = TestDispatcherProvider(testDispatcher)
        val repository = FakeNfcRepository(NfcHardwareStatus.AVAILABLE)

        val viewModel = StartupViewModel(repository, dispatchers)

        // Advance coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(NfcHardwareStatus.AVAILABLE, state.nfcStatus)
        assertEquals("TransitMate", state.appName)
    }

    @Test
    fun checkHardware_handlesDisabledStatus() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = TestDispatcherProvider(testDispatcher)
        val repository = FakeNfcRepository(NfcHardwareStatus.DISABLED)

        val viewModel = StartupViewModel(repository, dispatchers)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(NfcHardwareStatus.DISABLED, state.nfcStatus)
    }

    @Test
    fun checkHardware_handlesUnsupportedStatus() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = TestDispatcherProvider(testDispatcher)
        val repository = FakeNfcRepository(NfcHardwareStatus.NOT_SUPPORTED)

        val viewModel = StartupViewModel(repository, dispatchers)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(NfcHardwareStatus.NOT_SUPPORTED, state.nfcStatus)
    }
}

