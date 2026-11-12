package com.transitmate.bd.feature.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitmate.bd.core.common.DefaultDispatcherProvider
import com.transitmate.bd.core.common.DispatcherProvider
import com.transitmate.bd.core.nfc.NfcDiagnosticData
import com.transitmate.bd.domain.repository.NfcAvailabilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing technical startup state, hardware verification,
 * and Phase 03 physical card validation events.
 */
class StartupViewModel(
    private val nfcRepository: NfcAvailabilityRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StartupUiState(isLoading = true))
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    init {
        checkHardware()
    }

    fun checkHardware() {
        viewModelScope.launch(dispatchers.io) {
            val status = nfcRepository.checkNfcStatus()
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    nfcStatus = status
                )
            }
        }
    }

    /**
     * Receives raw diagnostic data from the isolated NFC Reader Mode harness.
     */
    fun onDiagnosticTagScanned(data: NfcDiagnosticData) {
        _uiState.update { current ->
            current.copy(
                diagnosticData = data,
                totalScansObserved = current.totalScansObserved + 1
            )
        }
    }

    fun clearDiagnosticData() {
        _uiState.update { current ->
            current.copy(diagnosticData = null)
        }
    }
}
