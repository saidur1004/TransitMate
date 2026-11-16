package com.transitmate.bd.core.nfc

import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.BalanceUnavailableReason
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType

/**
 * Maps low-level NFC discovery data into privacy-safe, user-facing [CardScanOutcome] states.
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

        if (data.idmRedacted.isNullOrBlank()) {
            return CardScanOutcome.IdentifierUnavailable(
                rfTechnology = "FeliCa (NFC-F)",
                scannedAtMillis = data.timestamp
            )
        }

        // Phase 03 verified NFC-F discovery and anticollision metadata,
        // while balance/service-level authority verification remains pending Phase 06.
        return CardScanOutcome.Detected(
            rfTechnology = "FeliCa (NFC-F)",
            maskedIdentifier = data.idmRedacted,
            cardDigestKey = data.idmDigestKey,
            recognitionStatus = CardRecognitionStatus.UNVERIFIED,
            cardType = CardType.UNKNOWN,
            balanceAvailability = BalanceAvailability.Unavailable(
                BalanceUnavailableReason.UNVERIFIED_CARD_FORMAT
            ),
            scannedAtMillis = data.timestamp
        )
    }
}
