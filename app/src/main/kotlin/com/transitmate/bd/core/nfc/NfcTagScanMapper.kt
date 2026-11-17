package com.transitmate.bd.core.nfc

import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.BalanceUnavailableReason
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType

/**
 * Maps low-level NFC discovery and FeliCa block decoding results into honest,
 * user-facing [CardScanOutcome] states.
 *
 * Strictly enforces: Card Detection != Card Recognition != Balance Reading.
 */
class NfcTagScanMapper {

    fun mapToOutcome(data: NfcDiagnosticData): CardScanOutcome {
        if (!data.isNfcF) {
            return CardScanOutcome.UnsupportedCard(
                detectedTechs = data.techList,
                scannedAtMillis = data.timestamp
            )
        }

        if (data.isTagLost) {
            return CardScanOutcome.CardRemovedEarly(
                scannedAtMillis = data.timestamp
            )
        }

        if (!data.rfLinkOk && data.errorMessage != null) {
            return CardScanOutcome.CommunicationFailure(
                reason = data.errorMessage,
                scannedAtMillis = data.timestamp
            )
        }

        val fullIdm = data.idmHex
        if (fullIdm.isNullOrBlank()) {
            return CardScanOutcome.IdentifierUnavailable(
                rfTechnology = "FeliCa (NFC-F)",
                scannedAtMillis = data.timestamp
            )
        }

        val snapshot = data.decodedSnapshot
        val recognitionStatus = snapshot?.recognitionStatus ?: CardRecognitionStatus.UNVERIFIED
        val cardType = snapshot?.cardType ?: CardType.UNKNOWN

        val balanceAvailability = when {
            snapshot?.latestBalanceBdt != null -> BalanceAvailability.Verified(
                amountBdt = snapshot.latestBalanceBdt,
                verifiedAtMillis = data.timestamp
            )
            data.serviceProtected -> BalanceAvailability.Unavailable(
                BalanceUnavailableReason.PROTECTED_OR_UNREADABLE
            )
            data.blocksReadCount > 0 -> BalanceAvailability.Unavailable(
                BalanceUnavailableReason.READ_EMPTY_NO_HISTORY
            )
            else -> BalanceAvailability.Unavailable(
                BalanceUnavailableReason.UNVERIFIED_CARD_FORMAT
            )
        }

        return CardScanOutcome.Detected(
            rfTechnology = "FeliCa (NFC-F)",
            cardIdm = fullIdm,
            cardDigestKey = data.idmDigestKey,
            recognitionStatus = recognitionStatus,
            cardType = cardType,
            balanceAvailability = balanceAvailability,
            parsedTransactions = snapshot?.transactions.orEmpty(),
            scannedAtMillis = data.timestamp
        )
    }
}
