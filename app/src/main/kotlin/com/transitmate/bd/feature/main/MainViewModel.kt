package com.transitmate.bd.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitmate.bd.core.common.DefaultDispatcherProvider
import com.transitmate.bd.core.common.DispatcherProvider
import com.transitmate.bd.core.nfc.NfcDiagnosticData
import com.transitmate.bd.domain.model.AppLanguage
import com.transitmate.bd.domain.model.AppThemeMode
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.VerifiedTransaction
import com.transitmate.bd.domain.repository.CardRepository
import com.transitmate.bd.domain.repository.NfcAvailabilityRepository
import com.transitmate.bd.domain.repository.PreferencesRepository
import com.transitmate.bd.domain.repository.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScannerSessionPhase {
    WAITING,
    PAUSED,
    READING,
    DETECTED
}

data class MainUiState(
    val isHydrated: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val nfcStatus: NfcHardwareStatus = NfcHardwareStatus.NOT_SUPPORTED,
    val savedCards: List<SavedTransitCard> = emptyList(),
    val verifiedTransactions: List<VerifiedTransaction> = emptyList(),
    val scannerPhase: ScannerSessionPhase = ScannerSessionPhase.WAITING,
    val latestScanOutcome: CardScanOutcome.Detected? = null,
    val activeScanErrorOutcome: CardScanOutcome? = null,
    val latestSavedCardIdForResult: Long? = null,
    val diagnosticData: NfcDiagnosticData? = null,
    val totalDiagnosticScans: Int = 0
)

class MainViewModel(
    private val nfcAvailabilityRepository: NfcAvailabilityRepository,
    private val cardRepository: CardRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val savedCardsFlow = cardRepository.savedCards.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        refreshNfcStatus()
        observePreferences()
        observeCardsAndTransactions()
    }

    private fun observePreferences() {
        viewModelScope.launch(dispatchers.main) {
            preferencesRepository.preferences.collect { prefs ->
                _uiState.update { it.copy(isHydrated = true, preferences = prefs) }
            }
        }
    }

    private fun observeCardsAndTransactions() {
        viewModelScope.launch(dispatchers.main) {
            cardRepository.savedCards.collect { cards ->
                _uiState.update { it.copy(savedCards = cards) }
            }
        }
        viewModelScope.launch(dispatchers.main) {
            cardRepository.verifiedTransactions.collect { tx ->
                _uiState.update { it.copy(verifiedTransactions = tx) }
            }
        }
    }

    fun refreshNfcStatus() {
        viewModelScope.launch(dispatchers.io) {
            val status = nfcAvailabilityRepository.checkNfcStatus()
            _uiState.update { it.copy(nfcStatus = status) }
        }
    }

    fun startScannerSession() {
        _uiState.update {
            it.copy(
                scannerPhase = ScannerSessionPhase.WAITING,
                activeScanErrorOutcome = null
            )
        }
    }

    fun pauseScannerSession() {
        _uiState.update { it.copy(scannerPhase = ScannerSessionPhase.PAUSED) }
    }

    fun resumeScannerSession() {
        _uiState.update {
            it.copy(
                scannerPhase = ScannerSessionPhase.WAITING,
                activeScanErrorOutcome = null
            )
        }
    }

    fun onConsumerTagOutcome(outcome: CardScanOutcome) {
        when (outcome) {
            is CardScanOutcome.Detected -> {
                viewModelScope.launch(dispatchers.io) {
                    val existing = outcome.cardDigestKey?.let { cardRepository.findByDigestKey(it) }
                    _uiState.update {
                        it.copy(
                            scannerPhase = ScannerSessionPhase.DETECTED,
                            latestScanOutcome = outcome,
                            activeScanErrorOutcome = null,
                            latestSavedCardIdForResult = existing?.id
                        )
                    }
                }
            }
            is CardScanOutcome.UnsupportedCard,
            is CardScanOutcome.CardRemovedEarly,
            is CardScanOutcome.IdentifierUnavailable,
            is CardScanOutcome.CommunicationFailure -> {
                _uiState.update {
                    it.copy(
                        scannerPhase = ScannerSessionPhase.WAITING,
                        activeScanErrorOutcome = outcome
                    )
                }
            }
        }
    }

    fun dismissScanErrorSheet() {
        _uiState.update {
            it.copy(
                activeScanErrorOutcome = null,
                scannerPhase = ScannerSessionPhase.WAITING
            )
        }
    }

    fun saveDetectedCard(nickname: String, userCardType: CardType) {
        val detected = _uiState.value.latestScanOutcome ?: return
        val digest = detected.cardDigestKey ?: return
        val masked = detected.maskedIdentifier ?: return
        viewModelScope.launch(dispatchers.io) {
            val savedId = cardRepository.saveOrUpdateCard(
                SavedTransitCard(
                    cardDigestKey = digest,
                    nickname = nickname.trim().ifEmpty { "Commute Card" },
                    userSelectedType = userCardType,
                    recognitionStatus = CardRecognitionStatus.UNVERIFIED,
                    rfTechnology = detected.rfTechnology,
                    maskedIdentifier = masked,
                    isPrimary = false,
                    addedAtMillis = detected.scannedAtMillis,
                    lastScannedAtMillis = detected.scannedAtMillis
                )
            )
            _uiState.update { it.copy(latestSavedCardIdForResult = savedId) }
        }
    }

    fun setPrimaryCard(cardId: Long) {
        viewModelScope.launch(dispatchers.io) {
            cardRepository.setPrimaryCard(cardId)
        }
    }

    fun renameCard(cardId: Long, nickname: String, cardType: CardType) {
        viewModelScope.launch(dispatchers.io) {
            cardRepository.renameCard(cardId, nickname, cardType)
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch(dispatchers.io) {
            cardRepository.deleteCard(cardId)
        }
    }

    fun clearScanHistory() {
        viewModelScope.launch(dispatchers.io) {
            cardRepository.clearScanHistory()
        }
    }

    fun clearAllLocalData() {
        viewModelScope.launch(dispatchers.io) {
            cardRepository.clearAllData()
            _uiState.update { it.copy(latestSavedCardIdForResult = null) }
        }
    }

    suspend fun getSanitizedExportJson(): String = cardRepository.buildSanitizedExportJson()

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch(dispatchers.io) {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch(dispatchers.io) {
            preferencesRepository.setLanguage(language)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch(dispatchers.io) {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun onDiagnosticTagScanned(data: NfcDiagnosticData) {
        _uiState.update {
            it.copy(
                diagnosticData = data,
                totalDiagnosticScans = it.totalDiagnosticScans + 1
            )
        }
    }

    fun clearDiagnosticData() {
        _uiState.update { it.copy(diagnosticData = null, totalDiagnosticScans = 0) }
    }
}
