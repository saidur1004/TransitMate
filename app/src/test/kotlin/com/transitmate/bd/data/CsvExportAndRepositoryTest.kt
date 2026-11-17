package com.transitmate.bd.data

import com.transitmate.bd.core.database.SavedCardEntity
import com.transitmate.bd.core.database.TransactionEntity
import com.transitmate.bd.core.database.TransitCardDao
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportAndRepositoryTest {

    private class InMemoryDao : TransitCardDao {
        private val cards = MutableStateFlow<List<SavedCardEntity>>(emptyList())
        private val txs = MutableStateFlow<List<TransactionEntity>>(emptyList())
        private var nextCardId = 1L
        private var nextTxId = 1L

        override fun observeAllCards(): Flow<List<SavedCardEntity>> = cards
        override suspend fun getAllCardsOnce(): List<SavedCardEntity> = cards.value
        override fun observeCard(cardId: Long): Flow<SavedCardEntity?> =
            cards.map { list -> list.firstOrNull { it.id == cardId } }
        override suspend fun findByDigestKey(digestKey: String): SavedCardEntity? =
            cards.value.firstOrNull { it.cardDigestKey == digestKey }

        override suspend fun insertCard(card: SavedCardEntity): Long {
            val id = if (card.id > 0L) card.id else nextCardId++
            val saved = card.copy(id = id)
            cards.value = cards.value.filterNot { it.id == id } + saved
            return id
        }

        override suspend fun updateCard(card: SavedCardEntity) {
            cards.value = cards.value.map { if (it.id == card.id) card else it }
        }

        override suspend fun clearPrimaryFlags() {
            cards.value = cards.value.map { it.copy(isPrimary = false) }
        }

        override suspend fun setPrimaryFlag(cardId: Long) {
            cards.value = cards.value.map { if (it.id == cardId) it.copy(isPrimary = true) else it }
        }

        override suspend fun updateCardLabel(cardId: Long, nickname: String, cardType: String) {
            cards.value = cards.value.map {
                if (it.id == cardId) it.copy(nickname = nickname, userSelectedType = cardType) else it
            }
        }

        override suspend fun deleteCardById(cardId: Long) {
            cards.value = cards.value.filterNot { it.id == cardId }
            txs.value = txs.value.filterNot { it.cardId == cardId }
        }

        override suspend fun deleteAllCards() {
            cards.value = emptyList()
        }

        override fun observeAllTransactions(): Flow<List<TransactionEntity>> = txs
        override suspend fun getAllTransactionsOnce(): List<TransactionEntity> = txs.value
        override fun observeTransactionsForCard(cardId: Long): Flow<List<TransactionEntity>> =
            txs.map { list -> list.filter { it.cardId == cardId } }

        override suspend fun getMaxSequenceOrder(cardId: Long): Long? =
            txs.value.filter { it.cardId == cardId }.maxOfOrNull { it.sequenceOrder }

        override suspend fun insertTransactions(transactions: List<TransactionEntity>) {
            val existingKeys = txs.value.mapNotNull { t -> t.rawBlockHex?.let { t.cardId to it } }.toMutableSet()
            val added = transactions.filter { entity ->
                val blockHex = entity.rawBlockHex
                blockHex == null || existingKeys.add(entity.cardId to blockHex)
            }.map { it.copy(id = nextTxId++) }
            txs.value = (added.sortedByDescending { it.sequenceOrder } + txs.value)
        }

        override suspend fun deleteAllTransactions() {
            txs.value = emptyList()
        }
    }

    @Test
    fun formatTransactionsCsv_writesUtf8BomBanglaStationNamesAndProperEscaping() {
        val card = SavedTransitCard(
            id = 1L,
            cardDigestKey = "digest-abc",
            nickname = "Daily \"Rapid\", Card",
            userSelectedType = CardType.RAPID_PASS,
            recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
            rfTechnology = "FeliCa (NFC-F)",
            cardIdm = "01:27:AA:BB:CC:DD:EE:A0",
            isPrimary = true,
            addedAtMillis = 1000L,
            lastScannedAtMillis = 2000L,
            lastVerifiedBalanceBdt = 14
        )
        val trip = VerifiedTransaction(
            id = 10L,
            cardId = 1L,
            originStation = "Motijheel",
            destinationStation = "Shewrapara",
            originStationCode = 10,
            destinationStationCode = 55,
            amountBdt = 45,
            postBalanceBdt = 14,
            type = TransactionType.TRIP,
            timestampMillis = 1791028800000L, // 2026-10-03
            isVerified = true
        )
        val topUp = VerifiedTransaction(
            id = 11L,
            cardId = 1L,
            originStation = null,
            destinationStation = null,
            amountBdt = 100,
            postBalanceBdt = 104,
            type = TransactionType.TOP_UP,
            timestampMillis = 1771761600000L,
            isVerified = true
        )

        val csvBn = CsvExportFormatter.formatTransactionsCsv(listOf(card), listOf(trip, topUp), isBangla = true)
        assertTrue(csvBn.startsWith("\uFEFF"))
        assertTrue(csvBn.contains("কার্ডের নাম,কার্ডের ধরন"))
        assertTrue(csvBn.contains("\"Daily \"\"Rapid\"\", Card\""))
        assertTrue(csvBn.contains("মতিঝিল,শেওড়াপাড়া,-45,14"))
        assertTrue(csvBn.contains("রিচার্জ,—,—,+100,104"))

        val csvEn = CsvExportFormatter.formatTransactionsCsv(listOf(card), listOf(trip, topUp), isBangla = false)
        assertTrue(csvEn.contains("Card Name,Card Type"))
        assertTrue(csvEn.contains("Motijheel,Shewrapara,-45,14"))
        assertTrue(csvEn.contains("Top-Up,—,—,+100,104"))
    }

    @Test
    fun roomCardRepository_deduplicatesRescannedTransactionsAndUpdatesSavedCard() = runTest {
        val dao = InMemoryDao()
        val repo = RoomCardRepository(dao)

        val cardId = repo.saveOrUpdateCard(
            SavedTransitCard(
                cardDigestKey = "digest-1",
                nickname = "Rapid Pass",
                userSelectedType = CardType.RAPID_PASS,
                recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
                rfTechnology = "FeliCa (NFC-F)",
                cardIdm = "01:27:AA:BB:CC:DD:EE:A0",
                isPrimary = true,
                addedAtMillis = 1000L,
                lastScannedAtMillis = 1000L,
                lastVerifiedBalanceBdt = 104
            )
        )

        val tx1 = VerifiedTransaction(
            cardId = 0L,
            originStation = "Shahbagh",
            destinationStation = "Shewrapara",
            originStationCode = 30,
            destinationStationCode = 55,
            amountBdt = 36,
            postBalanceBdt = 4,
            type = TransactionType.TRIP,
            timestampMillis = 2000L,
            isVerified = true,
            rawBlockHex = "08521000AABBCCDD"
        )
        repo.saveVerifiedTransactions(cardId, listOf(tx1))
        assertEquals(1, dao.getAllTransactionsOnce().size)

        // Repeated scan with same block + 1 new block must NOT duplicate tx1
        val tx2 = tx1.copy(timestampMillis = 3000L, postBalanceBdt = 14, amountBdt = 45, rawBlockHex = "0852100011223344")
        repo.saveVerifiedTransactions(cardId, listOf(tx2, tx1))
        assertEquals(2, dao.getAllTransactionsOnce().size)

        val json = repo.buildSanitizedExportJson()
        assertTrue(json.contains("\"verifiedTransactionCount\":2"))
        assertTrue(!json.contains("01:27:AA"))
    }
}
