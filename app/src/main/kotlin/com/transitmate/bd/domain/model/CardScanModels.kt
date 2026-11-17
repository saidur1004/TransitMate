package com.transitmate.bd.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    BANGLA("bn");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

enum class CardRecognitionStatus {
    VERIFIED_RAPID_PASS,
    VERIFIED_MRT_PASS,
    UNVERIFIED
}

enum class BalanceUnavailableReason {
    UNVERIFIED_CARD_FORMAT,
    PROTECTED_OR_UNREADABLE,
    READ_EMPTY_NO_HISTORY
}

sealed interface BalanceAvailability {
    data class Unavailable(
        val reason: BalanceUnavailableReason = BalanceUnavailableReason.UNVERIFIED_CARD_FORMAT
    ) : BalanceAvailability

    data class Verified(
        val amountBdt: Int,
        val verifiedAtMillis: Long
    ) : BalanceAvailability
}

/**
 * Production user-facing NFC scan outcomes.
 * Enforces strict separation: Detection != Recognition != Balance Reading.
 */
sealed interface CardScanOutcome {
    data class Detected(
        val rfTechnology: String,
        val cardIdm: String?,
        val cardDigestKey: String?,
        val recognitionStatus: CardRecognitionStatus,
        val cardType: CardType,
        val balanceAvailability: BalanceAvailability,
        val parsedTransactions: List<VerifiedTransaction> = emptyList(),
        val scannedAtMillis: Long
    ) : CardScanOutcome

    data class UnsupportedCard(
        val detectedTechs: List<String>,
        val scannedAtMillis: Long
    ) : CardScanOutcome

    data class CardRemovedEarly(
        val scannedAtMillis: Long
    ) : CardScanOutcome

    data class IdentifierUnavailable(
        val rfTechnology: String,
        val scannedAtMillis: Long
    ) : CardScanOutcome

    data class CommunicationFailure(
        val reason: String?,
        val scannedAtMillis: Long
    ) : CardScanOutcome
}

data class SavedTransitCard(
    val id: Long = 0L,
    val cardDigestKey: String,
    val nickname: String,
    val userSelectedType: CardType,
    val recognitionStatus: CardRecognitionStatus,
    val rfTechnology: String,
    val cardIdm: String,
    val isPrimary: Boolean,
    val addedAtMillis: Long,
    val lastScannedAtMillis: Long,
    val lastVerifiedBalanceBdt: Int? = null,
    val lastVerifiedBalanceAtMillis: Long? = null
)

enum class TransactionType {
    TRIP,
    TOP_UP
}

data class VerifiedTransaction(
    val id: Long = 0L,
    val cardId: Long,
    val originStation: String?,
    val destinationStation: String?,
    val originStationCode: Int? = null,
    val destinationStationCode: Int? = null,
    val amountBdt: Int?,
    val postBalanceBdt: Int? = null,
    val type: TransactionType,
    val timestampMillis: Long,
    val isVerified: Boolean,
    val rawBlockHex: String? = null,
    val sequenceOrder: Long = 0L
)
