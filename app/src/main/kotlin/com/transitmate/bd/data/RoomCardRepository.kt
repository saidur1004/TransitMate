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
        val entity = SavedCardEntity(
            id = existing?.id ?: 0L,
            cardDigestKey = card.cardDigestKey,
            nickname = existing?.nickname ?: card.nickname,
            userSelectedType = existing?.userSelectedType ?: card.userSelectedType.name,
            recognitionStatus = card.recognitionStatus.name,
            rfTechnology = card.rfTechnology,
            maskedIdentifier = card.maskedIdentifier,
            isPrimary = makePrimary,
            addedAtMillis = existing?.addedAtMillis ?: card.addedAtMillis,
            lastScannedAtMillis = card.lastScannedAtMillis,
            lastVerifiedBalanceBdt = card.lastVerifiedBalanceBdt ?: existing?.lastVerifiedBalanceBdt,
            lastVerifiedBalanceAtMillis = card.lastVerifiedBalanceAtMillis ?: existing?.lastVerifiedBalanceAtMillis
        )
        return dao.insertCard(entity)
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
            """{"nickname":"${escapeJson(c.nickname)}","type":"${c.userSelectedType}","recognition":"${c.recognitionStatus}","technology":"${c.rfTechnology}","maskedId":"${escapeJson(c.maskedIdentifier)}","isPrimary":${c.isPrimary},"lastScannedAt":${c.lastScannedAtMillis}}"""
        }
        return """{"app":"TransitMate","mode":"offline-read-only","savedCardCount":${cards.size},"verifiedTransactionCount":${tx.size},"cards":[$cardEntries]}"""
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
        maskedIdentifier = maskedIdentifier,
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
        amountBdt = amountBdt,
        type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.TRIP),
        timestampMillis = timestampMillis,
        isVerified = isVerified
    )
}
