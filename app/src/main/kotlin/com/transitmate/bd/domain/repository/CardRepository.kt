package com.transitmate.bd.domain.repository

import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.VerifiedTransaction
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    val savedCards: Flow<List<SavedTransitCard>>
    val verifiedTransactions: Flow<List<VerifiedTransaction>>

    fun observeCard(cardId: Long): Flow<SavedTransitCard?>
    fun observeTransactionsForCard(cardId: Long): Flow<List<VerifiedTransaction>>

    suspend fun findByDigestKey(digestKey: String): SavedTransitCard?
    suspend fun saveOrUpdateCard(card: SavedTransitCard): Long
    suspend fun saveVerifiedTransactions(cardId: Long, transactions: List<VerifiedTransaction>)
    suspend fun renameCard(cardId: Long, newNickname: String, cardType: CardType)
    suspend fun setPrimaryCard(cardId: Long)
    suspend fun deleteCard(cardId: Long)
    suspend fun clearScanHistory()
    suspend fun clearAllData()
    suspend fun buildSanitizedExportJson(): String
    suspend fun buildTransactionsCsv(isBangla: Boolean): String
}
