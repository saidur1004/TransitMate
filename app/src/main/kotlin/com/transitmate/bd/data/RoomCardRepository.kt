package com.transitmate.bd.data

import com.transitmate.bd.core.database.SavedCardEntity
import com.transitmate.bd.core.database.TransactionEntity
import com.transitmate.bd.core.database.TransitCardDao
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import com.transitmate.bd.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCardRepository(
    private val dao: TransitCardDao
) : CardRepository {

    override val savedCards: Flow<List<SavedTransitCard>> =
        dao.observeAllCards().map { list -> list.map { it.toDomain() } }

    override val verifiedTransactions: Flow<List<VerifiedTransaction>> =
        dao.observeAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun observeCard(cardId: Long): Flow<SavedTransitCard?> =
        dao.observeCard(cardId).map { it?.toDomain() }

    override fun observeTransactionsForCard(cardId: Long): Flow<List<VerifiedTransaction>> =
        dao.observeTransactionsForCard(cardId).map { list -> list.map { it.toDomain() } }

    override suspend fun findByDigestKey(digestKey: String): SavedTransitCard? =
        dao.findByDigestKey(digestKey)?.toDomain()

    override suspend fun saveOrUpdateCard(card: SavedTransitCard): Long {
        val existing = dao.findByDigestKey(card.cardDigestKey)
        val hasCards = dao.getAllCardsOnce().isNotEmpty()
        val makePrimary = card.isPrimary || (!hasCards && existing == null) || (existing?.isPrimary == true)
        if (makePrimary) {
            dao.clearPrimaryFlags()
        }
        val resolvedType = if (card.userSelectedType != CardType.UNKNOWN) {
            card.userSelectedType
        } else {
            runCatching { CardType.valueOf(existing?.userSelectedType ?: CardType.UNKNOWN.name) }
                .getOrDefault(CardType.UNKNOWN)
        }
        val entity = SavedCardEntity(
            id = existing?.id ?: 0L,
            cardDigestKey = card.cardDigestKey,
            nickname = existing?.nickname ?: card.nickname,
            userSelectedType = resolvedType.name,
            recognitionStatus = card.recognitionStatus.name,
            rfTechnology = card.rfTechnology,
            cardIdm = card.cardIdm,
            isPrimary = makePrimary,
            addedAtMillis = existing?.addedAtMillis ?: card.addedAtMillis,
            lastScannedAtMillis = card.lastScannedAtMillis,
            lastVerifiedBalanceBdt = card.lastVerifiedBalanceBdt ?: existing?.lastVerifiedBalanceBdt,
            lastVerifiedBalanceAtMillis = card.lastVerifiedBalanceAtMillis ?: existing?.lastVerifiedBalanceAtMillis
        )
        return if (existing == null) {
            dao.insertCard(entity)
        } else {
            dao.updateCard(entity)
            existing.id
        }
    }

    override suspend fun saveVerifiedTransactions(cardId: Long, transactions: List<VerifiedTransaction>) {
        if (transactions.isEmpty() || cardId <= 0L) return
        val baseSequence = dao.getMaxSequenceOrder(cardId) ?: 0L
        // Assign ascending sequence numbers from oldest to newest so identical-hour
        // blocks sort in strict reverse-chronological card ring-buffer order.
        val entities = transactions
            .reversed()
            .mapIndexed { index, tx ->
                TransactionEntity(
                    cardId = cardId,
                    originStation = tx.originStation,
                    originStationCode = tx.originStationCode,
                    destinationStation = tx.destinationStation,
                    destinationStationCode = tx.destinationStationCode,
                    amountBdt = tx.amountBdt,
                    postBalanceBdt = tx.postBalanceBdt,
                    type = tx.type.name,
                    timestampMillis = tx.timestampMillis,
                    isVerified = tx.isVerified,
                    rawBlockHex = tx.rawBlockHex,
                    sequenceOrder = baseSequence + index + 1L
                )
            }
        dao.insertTransactions(entities)
    }

    override suspend fun renameCard(cardId: Long, newNickname: String, cardType: CardType) {
        val cleanName = newNickname.trim()
        if (cleanName.isNotEmpty()) {
            dao.updateCardLabel(cardId, cleanName, cardType.name)
        }
    }

    override suspend fun setPrimaryCard(cardId: Long) {
        dao.setPrimaryCard(cardId)
    }

    override suspend fun deleteCard(cardId: Long) {
        dao.deleteCardById(cardId)
    }

    override suspend fun clearScanHistory() {
        dao.deleteAllTransactions()
    }

    override suspend fun clearAllData() {
        dao.deleteAllTransactions()
        dao.deleteAllCards()
    }

    override suspend fun buildSanitizedExportJson(): String {
        val cards = dao.getAllCardsOnce()
        val tx = dao.getAllTransactionsOnce()
        val cardEntries = cards.joinToString(",") { c ->
            """{"nickname":"${escapeJson(c.nickname)}","type":"${c.userSelectedType}","recognition":"${c.recognitionStatus}","technology":"${c.rfTechnology}","digestPrefix":"${c.cardDigestKey.take(12)}","balanceBdt":${c.lastVerifiedBalanceBdt ?: "null"},"isPrimary":${c.isPrimary},"lastScannedAt":${c.lastScannedAtMillis}}"""
        }
        return """{"app":"TransitMate","mode":"offline-read-only","savedCardCount":${cards.size},"verifiedTransactionCount":${tx.size},"cards":[$cardEntries]}"""
    }

    override suspend fun buildTransactionsCsv(isBangla: Boolean): String {
        val cards = dao.getAllCardsOnce().map { it.toDomain() }
        val tx = dao.getAllTransactionsOnce().map { it.toDomain() }
        return CsvExportFormatter.formatTransactionsCsv(cards, tx, isBangla)
    }

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun SavedCardEntity.toDomain(): SavedTransitCard = SavedTransitCard(
        id = id,
        cardDigestKey = cardDigestKey,
        nickname = nickname,
        userSelectedType = runCatching { CardType.valueOf(userSelectedType) }.getOrDefault(CardType.UNKNOWN),
        recognitionStatus = runCatching { CardRecognitionStatus.valueOf(recognitionStatus) }
            .getOrDefault(CardRecognitionStatus.UNVERIFIED),
        rfTechnology = rfTechnology,
        cardIdm = cardIdm,
        isPrimary = isPrimary,
        addedAtMillis = addedAtMillis,
        lastScannedAtMillis = lastScannedAtMillis,
        lastVerifiedBalanceBdt = lastVerifiedBalanceBdt,
        lastVerifiedBalanceAtMillis = lastVerifiedBalanceAtMillis
    )

    private fun TransactionEntity.toDomain(): VerifiedTransaction = VerifiedTransaction(
        id = id,
        cardId = cardId,
        originStation = originStation,
        destinationStation = destinationStation,
        originStationCode = originStationCode,
        destinationStationCode = destinationStationCode,
        amountBdt = amountBdt,
        postBalanceBdt = postBalanceBdt,
        type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.TRIP),
        timestampMillis = timestampMillis,
        isVerified = isVerified,
        rawBlockHex = rawBlockHex,
        sequenceOrder = sequenceOrder
    )
}
