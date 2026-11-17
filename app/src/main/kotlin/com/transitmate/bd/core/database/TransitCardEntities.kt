package com.transitmate.bd.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_cards",
    indices = [Index(value = ["cardDigestKey"], unique = true)]
)
data class SavedCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardDigestKey: String,
    val nickname: String,
    val userSelectedType: String,
    val recognitionStatus: String,
    val rfTechnology: String,
    val cardIdm: String,
    val isPrimary: Boolean,
    val addedAtMillis: Long,
    val lastScannedAtMillis: Long,
    val lastVerifiedBalanceBdt: Int? = null,
    val lastVerifiedBalanceAtMillis: Long? = null
)

@Entity(
    tableName = "verified_transactions",
    foreignKeys = [
        ForeignKey(
            entity = SavedCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cardId"]),
        Index(value = ["cardId", "rawBlockHex"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardId: Long,
    val originStation: String?,
    val originStationCode: Int? = null,
    val destinationStation: String?,
    val destinationStationCode: Int? = null,
    val amountBdt: Int?,
    val postBalanceBdt: Int? = null,
    val type: String,
    val timestampMillis: Long,
    val isVerified: Boolean,
    val rawBlockHex: String? = null,
    val sequenceOrder: Long = 0L
)
