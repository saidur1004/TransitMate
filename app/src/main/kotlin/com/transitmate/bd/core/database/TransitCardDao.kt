package com.transitmate.bd.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransitCardDao {

    @Query("SELECT * FROM saved_cards ORDER BY isPrimary DESC, lastScannedAtMillis DESC")
    fun observeAllCards(): Flow<List<SavedCardEntity>>

    @Query("SELECT * FROM saved_cards ORDER BY isPrimary DESC, lastScannedAtMillis DESC")
    suspend fun getAllCardsOnce(): List<SavedCardEntity>

    @Query("SELECT * FROM saved_cards WHERE id = :cardId LIMIT 1")
    fun observeCard(cardId: Long): Flow<SavedCardEntity?>

    @Query("SELECT * FROM saved_cards WHERE cardDigestKey = :digestKey LIMIT 1")
    suspend fun findByDigestKey(digestKey: String): SavedCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SavedCardEntity): Long

    @Query("UPDATE saved_cards SET isPrimary = 0")
    suspend fun clearPrimaryFlags()

    @Query("UPDATE saved_cards SET isPrimary = 1 WHERE id = :cardId")
    suspend fun setPrimaryFlag(cardId: Long)

    @Transaction
    suspend fun setPrimaryCard(cardId: Long) {
        clearPrimaryFlags()
        setPrimaryFlag(cardId)
    }

    @Query("UPDATE saved_cards SET nickname = :nickname, userSelectedType = :cardType WHERE id = :cardId")
    suspend fun updateCardLabel(cardId: Long, nickname: String, cardType: String)

    @Query("DELETE FROM saved_cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("DELETE FROM saved_cards")
    suspend fun deleteAllCards()

    @Query("SELECT * FROM verified_transactions ORDER BY timestampMillis DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM verified_transactions ORDER BY timestampMillis DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Query("SELECT * FROM verified_transactions WHERE cardId = :cardId ORDER BY timestampMillis DESC")
    fun observeTransactionsForCard(cardId: Long): Flow<List<TransactionEntity>>

    @Query("DELETE FROM verified_transactions")
    suspend fun deleteAllTransactions()
}
