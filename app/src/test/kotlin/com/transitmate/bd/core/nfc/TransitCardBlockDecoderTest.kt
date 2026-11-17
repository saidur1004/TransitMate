package com.transitmate.bd.core.nfc

import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransitCardBlockDecoderTest {

    private val rapidIdm = byteArrayOf(0x01, 0x27, 0x10, 0x20, 0x30, 0x40, 0x50, 0xA0.toByte())
    private val mrtIdm = byteArrayOf(0x01, 0x10, 0x10, 0x20, 0x30, 0x40, 0x50, 0xB1.toByte())

    @Test
    fun decodeBlock_verifiesBigEndianTimestampAndLittleEndianBalance() {
        // 2026-11-17 08:00 (+06:00)
        val packedTime = (26 shl 17) or (11 shl 13) or (17 shl 8) or (8 shl 3)
        val block = buildBlock(
            header = byteArrayOf(0x08, 0x52, 0x10, 0x00),
            packedTimestamp = packedTime,
            fromStation = 90, // Uttara North
            toStation = 10,   // Motijheel
            balanceBdt = 484  // 0x0001E4 -> little-endian bytes: E4 01 00
        )

        assertEquals(0xE4.toByte(), block[11])
        assertEquals(0x01.toByte(), block[12])
        assertEquals(0x00.toByte(), block[13])

        val decoded = TransitCardBlockDecoder.decodeBlock(block)
        assertNotNull(decoded)
        assertEquals("08 52 10 00", decoded?.headerHex)
        assertEquals(90, decoded?.fromStationCode)
        assertEquals(10, decoded?.toStationCode)
        assertEquals(484, decoded?.postBalanceBdt)
    }

    @Test
    fun decodeBlock_rejectsBlankMalformedPre2020AndInvalidCalendarDates() {
        assertNull(TransitCardBlockDecoder.decodeBlock(ByteArray(15)))
        assertNull(TransitCardBlockDecoder.decodeBlock(ByteArray(16))) // All zeros

        val pre2020 = buildBlock(
            header = byteArrayOf(0x08, 0x52, 0x10, 0x00),
            packedTimestamp = (19 shl 17) or (6 shl 13) or (10 shl 8) or (9 shl 3),
            fromStation = 90,
            toStation = 10,
            balanceBdt = 200
        )
        assertNull(TransitCardBlockDecoder.decodeBlock(pre2020))

        // Invalid date: Feb 30, 2026
        val invalidFeb30 = buildBlock(
            header = byteArrayOf(0x08, 0x52, 0x10, 0x00),
            packedTimestamp = (26 shl 17) or (2 shl 13) or (30 shl 8) or (9 shl 3),
            fromStation = 90,
            toStation = 10,
            balanceBdt = 200
        )
        assertNull(TransitCardBlockDecoder.decodeBlock(invalidFeb30))
    }

    @Test
    fun decodeCardData_calculatesTripAndTopUpAmountsAndLeavesOldestDeltaNull() {
        val newestTrip = buildBlock(
            header = byteArrayOf(0x08, 0x52, 0x10, 0x00),
            packedTimestamp = encodeTime(26, 11, 17, 9),
            fromStation = 90, // Uttara North
            toStation = 40,   // Farmgate
            balanceBdt = 446  // Spent 54 from 500
        )
        val topUp = buildBlock(
            header = byteArrayOf(0x42, 0x60, 0x02, 0x00), // Rapid Pass Recharge
            packedTimestamp = encodeTime(26, 11, 16, 18),
            fromStation = 0,
            toStation = 0,
            balanceBdt = 500  // Added 400 from 100
        )
        val oldestBus = buildBlock(
            header = byteArrayOf(0x42, 0xD6.toByte(), 0x30, 0x00), // Hatirjheel Bus
            packedTimestamp = encodeTime(26, 11, 15, 11),
            fromStation = 16, // Rampura (HJ)
            toStation = 28,   // FDC (HJ)
            balanceBdt = 100
        )

        val snapshot = TransitCardBlockDecoder.decodeCardData(
            idmBytes = rapidIdm,
            systemCodeHex = "90E3",
            rawBlocks = listOf(newestTrip, topUp, oldestBus, ByteArray(16)),
            readSucceeded = true
        )

        assertEquals(CardType.RAPID_PASS, snapshot.cardType)
        assertEquals(CardRecognitionStatus.VERIFIED_RAPID_PASS, snapshot.recognitionStatus)
        assertEquals(446, snapshot.latestBalanceBdt)
        assertEquals(3, snapshot.validBlockCount)
        assertEquals(3, snapshot.transactions.size)

        // Tx 0: Metro trip -54 BDT
        val tx0 = snapshot.transactions[0]
        assertEquals(TransactionType.TRIP, tx0.type)
        assertEquals("Uttara North", tx0.originStation)
        assertEquals("Farmgate", tx0.destinationStation)
        assertEquals(54, tx0.amountBdt)
        assertEquals(446, tx0.postBalanceBdt)

        // Tx 1: Rapid Pass top-up +400 BDT
        val tx1 = snapshot.transactions[1]
        assertEquals(TransactionType.TOP_UP, tx1.type)
        assertEquals(400, tx1.amountBdt)
        assertEquals(500, tx1.postBalanceBdt)

        // Tx 2: Oldest known block -> no prior reference, amount delta is null (not fabricated)
        val tx2 = snapshot.transactions[2]
        assertEquals(TransactionType.TRIP, tx2.type)
        assertEquals("Rampura (HJ)", tx2.originStation)
        assertEquals("FDC (HJ)", tx2.destinationStation)
        assertNull(tx2.amountBdt)
        assertEquals(100, tx2.postBalanceBdt)
    }

    @Test
    fun decodeCardData_recognizesMrtPassFromHeader() {
        val mrtTopUp = buildBlock(
            header = byteArrayOf(0x1D, 0x60, 0x02, 0x01),
            packedTimestamp = encodeTime(26, 11, 17, 10),
            fromStation = 0,
            toStation = 0,
            balanceBdt = 300
        )
        val snapshot = TransitCardBlockDecoder.decodeCardData(
            idmBytes = mrtIdm,
            systemCodeHex = null,
            rawBlocks = listOf(mrtTopUp),
            readSucceeded = true
        )
        assertEquals(CardType.MRT_PASS, snapshot.cardType)
        assertEquals(CardRecognitionStatus.VERIFIED_MRT_PASS, snapshot.recognitionStatus)
        assertEquals(300, snapshot.latestBalanceBdt)
    }

    private fun encodeTime(yearOffset: Int, month: Int, day: Int, hour: Int): Int =
        (yearOffset shl 17) or (month shl 13) or (day shl 8) or (hour shl 3)

    private fun buildBlock(
        header: ByteArray,
        packedTimestamp: Int,
        fromStation: Int,
        toStation: Int,
        balanceBdt: Int
    ): ByteArray {
        val out = ByteArray(16)
        header.copyInto(out, 0)
        out[4] = (packedTimestamp ushr 16).toByte()
        out[5] = (packedTimestamp ushr 8).toByte()
        out[6] = packedTimestamp.toByte()
        out[8] = fromStation.toByte()
        out[10] = toStation.toByte()
        out[11] = (balanceBdt and 0xFF).toByte()
        out[12] = ((balanceBdt ushr 8) and 0xFF).toByte()
        out[13] = ((balanceBdt ushr 16) and 0xFF).toByte()
        return out
    }
}
