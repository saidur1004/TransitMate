package com.transitmate.bd.core.nfc

import android.nfc.TagLostException
import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.BalanceUnavailableReason
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NfcFCardReaderTest {

    private val rapidIdm = byteArrayOf(0x01, 0x27, 0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xA0.toByte())
    private val mapper = NfcTagScanMapper()
    private val reader = NfcDiagnosticReader(mapper)

    private class FakeTransceiver(
        override val idm: ByteArray,
        private val onConnect: (() -> Unit)? = null,
        private val onWindow: (Int) -> ByteArray
    ) : FelicaBlockTransceiver {
        var closed = false
            private set

        override fun connect() {
            onConnect?.invoke()
        }

        override fun transceive(command: ByteArray): ByteArray {
            val startBlock = command[15].toInt() and 0xFF
            return onWindow(startBlock)
        }

        override fun close() {
            closed = true
        }
    }

    @Test
    fun readWithTransceiver_readsBothWindowsAndProducesFullIdmAndVerifiedBalance() {
        val recentBlock = buildSampleMetroBlock(yearOffset = 26, month = 11, day = 17, hour = 8, from = 90, to = 10, balance = 350)
        val olderBlock = buildSampleMetroBlock(yearOffset = 26, month = 11, day = 16, hour = 18, from = 10, to = 90, balance = 440)
        val transceiver = FakeTransceiver(rapidIdm) { startBlock ->
            if (startBlock == 0) {
                FelicaProtocolTest.buildResponseFrame(
                    idm = rapidIdm,
                    blocks = listOf(recentBlock, olderBlock) + List(8) { ByteArray(16) }
                )
            } else {
                FelicaProtocolTest.buildResponseFrame(
                    idm = rapidIdm,
                    blocks = List(10) { ByteArray(16) }
                )
            }
        }

        val diag = reader.readWithTransceiver(
            transceiver = transceiver,
            techs = listOf("NfcF"),
            isNfcF = true,
            rawId = rapidIdm,
            systemCodeHex = "90E3",
            pmmHex = "01:46:42:45:45:46:9B:FF",
            scanCount = 1,
            timestampMillis = 9999L
        )

        assertTrue(transceiver.closed)
        assertTrue(diag.rfLinkOk)
        assertFalse(diag.isTagLost)
        assertEquals("01:27:12:34:56:78:9A:A0", diag.idmHex)
        assertEquals("90E3", diag.systemCodeHex)
        assertEquals(20, diag.blocksReadCount)
        assertEquals(2, diag.validTransactionCount)

        val outcome = mapper.mapToOutcome(diag) as CardScanOutcome.Detected
        assertEquals("01:27:12:34:56:78:9A:A0", outcome.cardIdm)
        assertEquals(CardType.RAPID_PASS, outcome.cardType)
        assertEquals(CardRecognitionStatus.VERIFIED_RAPID_PASS, outcome.recognitionStatus)
        val bal = outcome.balanceAvailability as BalanceAvailability.Verified
        assertEquals(350, bal.amountBdt)
        assertEquals(2, outcome.parsedTransactions.size)
        assertEquals(90, outcome.parsedTransactions[0].amountBdt)
    }

    @Test
    fun readWithTransceiver_preservesWindow1WhenWindow2LosesTag() {
        val recentBlock = buildSampleMetroBlock(26, 11, 17, 8, 90, 40, 210)
        val transceiver = FakeTransceiver(rapidIdm) { startBlock ->
            if (startBlock == 0) {
                FelicaProtocolTest.buildResponseFrame(
                    idm = rapidIdm,
                    blocks = listOf(recentBlock) + List(9) { ByteArray(16) }
                )
            } else {
                throw TagLostException("Card moved during window 2")
            }
        }

        val diag = reader.readWithTransceiver(
            transceiver = transceiver,
            techs = listOf("NfcF"),
            isNfcF = true,
            rawId = rapidIdm,
            systemCodeHex = "90E3",
            pmmHex = null,
            scanCount = 2
        )

        assertTrue(diag.rfLinkOk)
        assertFalse(diag.isTagLost)
        assertEquals(10, diag.blocksReadCount)
        assertEquals(210, (mapper.mapToOutcome(diag) as CardScanOutcome.Detected).let {
            (it.balanceAvailability as BalanceAvailability.Verified).amountBdt
        })
    }

    @Test
    fun readWithTransceiver_handlesProtectedStatusFlagsHonestly() {
        val otherIdm = byteArrayOf(0x02, 0xFE.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
        val transceiver = FakeTransceiver(otherIdm) {
            FelicaProtocolTest.buildResponseFrame(
                idm = otherIdm,
                sf1 = 0xFF,
                sf2 = 0xA1,
                blocks = emptyList()
            )
        }

        val diag = reader.readWithTransceiver(
            transceiver = transceiver,
            techs = listOf("NfcF"),
            isNfcF = true,
            rawId = otherIdm,
            systemCodeHex = "0003",
            pmmHex = null,
            scanCount = 3
        )

        assertTrue(diag.rfLinkOk)
        assertTrue(diag.serviceProtected)
        val outcome = mapper.mapToOutcome(diag) as CardScanOutcome.Detected
        assertEquals(CardRecognitionStatus.UNVERIFIED, outcome.recognitionStatus)
        val unavailable = outcome.balanceAvailability as BalanceAvailability.Unavailable
        assertEquals(BalanceUnavailableReason.PROTECTED_OR_UNREADABLE, unavailable.reason)
    }

    @Test
    fun readWithTransceiver_mapsInitialTagLostAndIoFailure() {
        val tagLostTransceiver = FakeTransceiver(
            idm = rapidIdm,
            onConnect = { throw TagLostException("Moved away") },
            onWindow = { ByteArray(0) }
        )
        val lostDiag = reader.readWithTransceiver(tagLostTransceiver, listOf("NfcF"), true, rapidIdm, "90E3", null, 4)
        assertTrue(lostDiag.isTagLost)
        assertTrue(mapper.mapToOutcome(lostDiag) is CardScanOutcome.CardRemovedEarly)

        val ioTransceiver = FakeTransceiver(
            idm = rapidIdm,
            onConnect = { throw IOException("Transceive timeout") },
            onWindow = { ByteArray(0) }
        )
        val ioDiag = reader.readWithTransceiver(ioTransceiver, listOf("NfcF"), true, rapidIdm, "90E3", null, 5)
        assertFalse(ioDiag.rfLinkOk)
        assertTrue(mapper.mapToOutcome(ioDiag) is CardScanOutcome.CommunicationFailure)
    }

    private fun buildSampleMetroBlock(
        yearOffset: Int,
        month: Int,
        day: Int,
        hour: Int,
        from: Int,
        to: Int,
        balance: Int
    ): ByteArray {
        val block = ByteArray(16)
        byteArrayOf(0x08, 0x52, 0x10, 0x00).copyInto(block, 0)
        val packed = (yearOffset shl 17) or (month shl 13) or (day shl 8) or (hour shl 3)
        block[4] = (packed ushr 16).toByte()
        block[5] = (packed ushr 8).toByte()
        block[6] = packed.toByte()
        block[8] = from.toByte()
        block[10] = to.toByte()
        block[11] = (balance and 0xFF).toByte()
        block[12] = ((balance ushr 8) and 0xFF).toByte()
        block[13] = ((balance ushr 16) and 0xFF).toByte()
        return block
    }
}
