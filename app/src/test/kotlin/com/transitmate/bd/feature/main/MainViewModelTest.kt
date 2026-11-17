package com.transitmate.bd.feature.main

import com.transitmate.bd.core.common.DispatcherProvider
import com.transitmate.bd.core.nfc.NfcDiagnosticData
import com.transitmate.bd.domain.model.AppLanguage
import com.transitmate.bd.domain.model.AppThemeMode
import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import com.transitmate.bd.domain.repository.CardRepository
import com.transitmate.bd.domain.repository.NfcAvailabilityRepository
import com.transitmate.bd.domain.repository.PreferencesRepository
import com.transitmate.bd.domain.repository.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private class TestDispatcherProvider(val dispatcher: TestDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher get() = dispatcher
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
        override val unconfined: CoroutineDispatcher get() = dispatcher
    }

    private class FakeNfcRepo(var status: NfcHardwareStatus) : NfcAvailabilityRepository {
        override fun checkNfcStatus(): NfcHardwareStatus = status
        override val nfcStatus: Flow<NfcHardwareStatus> get() = flowOf(status)
    }

    private class FakePrefsRepo : PreferencesRepository {
        private val flow = MutableStateFlow(UserPreferences())
        override val preferences: Flow<UserPreferences> = flow

        override suspend fun setThemeMode(themeMode: AppThemeMode) {
            flow.value = flow.value.copy(themeMode = themeMode)
        }

        override suspend fun setLanguage(language: AppLanguage) {
            flow.value = flow.value.copy(language = language)
        }

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            flow.value = flow.value.copy(hasCompletedOnboarding = completed)
        }
    }

    private class FakeCardRepo : CardRepository {
        private val cardsFlow = MutableStateFlow<List<SavedTransitCard>>(emptyList())
        private val txFlow = MutableStateFlow<List<VerifiedTransaction>>(emptyList())
        private var nextId = 1L

        override val savedCards: Flow<List<SavedTransitCard>> = cardsFlow
        override val verifiedTransactions: Flow<List<VerifiedTransaction>> = txFlow

        override fun observeCard(cardId: Long): Flow<SavedTransitCard?> =
            cardsFlow.map { list -> list.firstOrNull { it.id == cardId } }

        override fun observeTransactionsForCard(cardId: Long): Flow<List<VerifiedTransaction>> =
            txFlow.map { list -> list.filter { it.cardId == cardId } }

        override suspend fun findByDigestKey(digestKey: String): SavedTransitCard? =
            cardsFlow.value.firstOrNull { it.cardDigestKey == digestKey }

        override suspend fun saveOrUpdateCard(card: SavedTransitCard): Long {
            val existing = cardsFlow.value.firstOrNull { it.cardDigestKey == card.cardDigestKey }
            val id = existing?.id ?: nextId++
            val saved = card.copy(id = id, isPrimary = existing?.isPrimary ?: cardsFlow.value.isEmpty() || card.isPrimary)
            cardsFlow.value = if (existing != null) {
                cardsFlow.value.map { if (it.id == id) saved else it }
            } else {
                cardsFlow.value + saved
            }
            return id
        }

        override suspend fun saveVerifiedTransactions(cardId: Long, transactions: List<VerifiedTransaction>) {
            val withCardId = transactions.mapIndexed { idx, tx -> tx.copy(id = (idx + 1).toLong(), cardId = cardId) }
            txFlow.value = (txFlow.value.filterNot { it.cardId == cardId } + withCardId)
        }

        override suspend fun renameCard(cardId: Long, newNickname: String, cardType: CardType) {
            cardsFlow.value = cardsFlow.value.map {
                if (it.id == cardId) it.copy(nickname = newNickname, userSelectedType = cardType) else it
            }
        }

        override suspend fun setPrimaryCard(cardId: Long) {
            cardsFlow.value = cardsFlow.value.map { it.copy(isPrimary = it.id == cardId) }
        }

        override suspend fun deleteCard(cardId: Long) {
            cardsFlow.value = cardsFlow.value.filterNot { it.id == cardId }
        }

        override suspend fun clearScanHistory() {
            txFlow.value = emptyList()
        }

        override suspend fun clearAllData() {
            cardsFlow.value = emptyList()
            txFlow.value = emptyList()
        }

        override suspend fun buildSanitizedExportJson(): String = """{"savedCardCount":${cardsFlow.value.size}}"""

        override suspend fun buildTransactionsCsv(isBangla: Boolean): String =
            com.transitmate.bd.data.CsvExportFormatter.formatTransactionsCsv(cardsFlow.value, txFlow.value, isBangla)
    }

    @Test
    fun consumerScanAndSave_persistsFullIdmBalanceAndTransactionsWhileKeepingDiagnosticsIsolated() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = MainViewModel(
            FakeNfcRepo(NfcHardwareStatus.AVAILABLE),
            FakeCardRepo(),
            FakePrefsRepo(),
            TestDispatcherProvider(dispatcher)
        )
        advanceUntilIdle()

        val outcome = CardScanOutcome.Detected(
            rfTechnology = "FeliCa (NFC-F)",
            cardIdm = "01:27:11:22:33:44:55:A0",
            cardDigestKey = "digest-1",
            recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
            cardType = CardType.RAPID_PASS,
            balanceAvailability = BalanceAvailability.Verified(amountBdt = 420, verifiedAtMillis = 5000L),
            parsedTransactions = listOf(
                VerifiedTransaction(
                    cardId = 0L,
                    originStation = "Uttara North",
                    destinationStation = "Motijheel",
                    originStationCode = 90,
                    destinationStationCode = 10,
                    amountBdt = 90,
                    postBalanceBdt = 420,
                    type = TransactionType.TRIP,
                    timestampMillis = 4900L,
                    isVerified = true,
                    rawBlockHex = "AABBCCDD"
                )
            ),
            scannedAtMillis = 5000L
        )
        vm.onConsumerTagOutcome(outcome)
        advanceUntilIdle()

        assertEquals(ScannerSessionPhase.DETECTED, vm.uiState.value.scannerPhase)
        assertNotNull(vm.uiState.value.latestScanOutcome)
        assertNull(vm.uiState.value.diagnosticData)

        vm.saveDetectedCard("My Rapid Card", CardType.RAPID_PASS)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.savedCards.size)
        val savedCard = vm.uiState.value.savedCards.first()
        assertEquals("My Rapid Card", savedCard.nickname)
        assertEquals("01:27:11:22:33:44:55:A0", savedCard.cardIdm)
        assertEquals(420, savedCard.lastVerifiedBalanceBdt)
        assertTrue(savedCard.isPrimary)
        assertEquals(1, vm.uiState.value.verifiedTransactions.size)
        assertEquals(90, vm.uiState.value.verifiedTransactions.first().amountBdt)
    }

    @Test
    fun unsupportedCardAndDismiss_resetsErrorSheetCleanly() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = MainViewModel(
            FakeNfcRepo(NfcHardwareStatus.AVAILABLE),
            FakeCardRepo(),
            FakePrefsRepo(),
            TestDispatcherProvider(dispatcher)
        )
        advanceUntilIdle()

        vm.onConsumerTagOutcome(CardScanOutcome.UnsupportedCard(listOf("IsoDep"), 100L))
        assertTrue(vm.uiState.value.activeScanErrorOutcome is CardScanOutcome.UnsupportedCard)

        vm.dismissScanErrorSheet()
        assertNull(vm.uiState.value.activeScanErrorOutcome)
        assertEquals(ScannerSessionPhase.WAITING, vm.uiState.value.scannerPhase)
    }

    @Test
    fun preferencesAndDiagnosticIsolation_updateIndependently() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = MainViewModel(
            FakeNfcRepo(NfcHardwareStatus.DISABLED),
            FakeCardRepo(),
            FakePrefsRepo(),
            TestDispatcherProvider(dispatcher)
        )
        advanceUntilIdle()

        vm.setThemeMode(AppThemeMode.DARK)
        vm.setLanguage(AppLanguage.BANGLA)
        vm.completeOnboarding()
        advanceUntilIdle()

        assertEquals(AppThemeMode.DARK, vm.uiState.value.preferences.themeMode)
        assertEquals(AppLanguage.BANGLA, vm.uiState.value.preferences.language)
        assertTrue(vm.uiState.value.preferences.hasCompletedOnboarding)

        vm.onDiagnosticTagScanned(NfcDiagnosticData(systemCodeHex = "90E3", scanCount = 1))
        assertEquals("90E3", vm.uiState.value.diagnosticData?.systemCodeHex)
        assertEquals(1, vm.uiState.value.totalDiagnosticScans)
    }
}
