package com.transitmate.bd.core.nfc

import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.BalanceUnavailableReason
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTagScanMapperTest {

    private val mapper = NfcTagScanMapper()

    @Test
    fun mapToOutcome_validNfcFWithoutDecodedBlocks_returnsFullIdmAndUnverified() {
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            idmHex = "01:27:11:22:33:44:55:A0",
            idmDigestKey = "abcd1234",
            systemCodeHex = "90E3",
            rfLinkOk = true,
            timestamp = 1000L
        )

        val outcome = mapper.mapToOutcome(diag)
        assertTrue(outcome is CardScanOutcome.Detected)
        val detected = outcome as CardScanOutcome.Detected
        assertEquals("FeliCa (NFC-F)", detected.rfTechnology)
        assertEquals(CardRecognitionStatus.UNVERIFIED, detected.recognitionStatus)
        assertTrue(detected.balanceAvailability is BalanceAvailability.Unavailable)
        assertEquals("01:27:11:22:33:44:55:A0", detected.cardIdm)
    }

    @Test
    fun mapToOutcome_verifiedRapidPassSnapshot_mapsVerifiedBalanceAndType() {
        val snapshot = DecodedCardSnapshot(
            cardType = CardType.RAPID_PASS,
            recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
            latestBalanceBdt = 520,
            transactions = emptyList(),
            validBlockCount = 4
        )
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            idmHex = "01:27:11:22:33:44:55:A0",
            idmDigestKey = "abcd1234",
            systemCodeHex = "90E3",
            rfLinkOk = true,
            blocksReadCount = 20,
            validTransactionCount = 4,
            statusFlag1 = 0,
            statusFlag2 = 0,
            decodedSnapshot = snapshot,
            timestamp = 2000L
        )

        val detected = mapper.mapToOutcome(diag) as CardScanOutcome.Detected
        assertEquals("01:27:11:22:33:44:55:A0", detected.cardIdm)
        assertEquals(CardType.RAPID_PASS, detected.cardType)
        assertEquals(CardRecognitionStatus.VERIFIED_RAPID_PASS, detected.recognitionStatus)
        val bal = detected.balanceAvailability as BalanceAvailability.Verified
        assertEquals(520, bal.amountBdt)
    }

    @Test
    fun mapToOutcome_emptyHistoryBlocks_reportsEmptyHistoryReason() {
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            idmHex = "01:27:11:22:33:44:55:A0",
            idmDigestKey = "abcd1234",
            systemCodeHex = "90E3",
            rfLinkOk = true,
            blocksReadCount = 20,
            validTransactionCount = 0,
            decodedSnapshot = DecodedCardSnapshot(
                cardType = CardType.RAPID_PASS,
                recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
                latestBalanceBdt = null,
                transactions = emptyList(),
                validBlockCount = 0
            )
        )
        val detected = mapper.mapToOutcome(diag) as CardScanOutcome.Detected
        val bal = detected.balanceAvailability as BalanceAvailability.Unavailable
        assertEquals(BalanceUnavailableReason.READ_EMPTY_NO_HISTORY, bal.reason)
    }

    @Test
    fun mapToOutcome_nonNfcF_returnsUnsupportedCard() {
        val diag = NfcDiagnosticData(
            techList = listOf("IsoDep", "NfcA"),
            isNfcF = false,
            timestamp = 2000L
        )
        val outcome = mapper.mapToOutcome(diag)
        assertTrue(outcome is CardScanOutcome.UnsupportedCard)
        assertEquals(listOf("IsoDep", "NfcA"), (outcome as CardScanOutcome.UnsupportedCard).detectedTechs)
    }

    @Test
    fun mapToOutcome_tagLost_returnsCardRemovedEarly() {
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            isTagLost = true,
            rfLinkOk = false,
            errorMessage = "Tag lost"
        )
        assertTrue(mapper.mapToOutcome(diag) is CardScanOutcome.CardRemovedEarly)
    }

    @Test
    fun mapToOutcome_ioError_returnsCommunicationFailure() {
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            isTagLost = false,
            rfLinkOk = false,
            errorMessage = "RF timeout"
        )
        val outcome = mapper.mapToOutcome(diag)
        assertTrue(outcome is CardScanOutcome.CommunicationFailure)
        assertEquals("RF timeout", (outcome as CardScanOutcome.CommunicationFailure).reason)
    }

    @Test
    fun mapToOutcome_missingIdentifier_returnsIdentifierUnavailable() {
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            idmHex = null,
            rfLinkOk = true
        )
        assertTrue(mapper.mapToOutcome(diag) is CardScanOutcome.IdentifierUnavailable)
    }
}
