package com.transitmate.bd.core.nfc

import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTagScanMapperTest {

    private val mapper = NfcTagScanMapper()

    @Test
    fun mapToOutcome_validNfcF_returnsDetectedWithUnverifiedAndUnavailableBalance() {
        val diag = NfcDiagnosticData(
            techList = listOf("NfcF"),
            isNfcF = true,
            idmRedacted = "01:27:**:**:**:**:**:A0",
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
        assertEquals("01:27:**:**:**:**:**:A0", detected.maskedIdentifier)
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
            idmRedacted = null,
            rfLinkOk = true
        )
        assertTrue(mapper.mapToOutcome(diag) is CardScanOutcome.IdentifierUnavailable)
    }
}
