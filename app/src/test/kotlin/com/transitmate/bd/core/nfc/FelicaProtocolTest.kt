package com.transitmate.bd.core.nfc

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FelicaProtocolTest {

    private val sampleIdm = byteArrayOf(
        0x01, 0x27, 0x11, 0x22, 0x33, 0x44, 0x55, 0xA0.toByte()
    )

    @Test
    fun buildReadWithoutEncryptionCommand_encodesStandardFeliCaPacket() {
        val cmd = FelicaProtocol.buildReadWithoutEncryptionCommand(
            idm = sampleIdm,
            serviceCode = FelicaProtocol.DTCA_HISTORY_SERVICE_CODE,
            startBlock = 10,
            blockCount = 2
        )

        val expectedLength = 14 + (2 * 2) // 18 bytes
        assertEquals(expectedLength, cmd.size)
        assertEquals(expectedLength.toByte(), cmd[0])
        assertEquals(0x06.toByte(), cmd[1])
        assertArrayEquals(sampleIdm, cmd.copyOfRange(2, 10))
        assertEquals(0x01.toByte(), cmd[10]) // Num services
        assertEquals(0x0F.toByte(), cmd[11]) // Service code 0x220F low byte
        assertEquals(0x22.toByte(), cmd[12]) // Service code 0x220F high byte
        assertEquals(0x02.toByte(), cmd[13]) // 2 blocks
        assertEquals(0x80.toByte(), cmd[14]) // Block 0 control
        assertEquals(10.toByte(), cmd[15])   // Block 10
        assertEquals(0x80.toByte(), cmd[16]) // Block 1 control
        assertEquals(11.toByte(), cmd[17])   // Block 11
    }

    @Test
    fun decodeReadResponse_extractsValidBlocks() {
        val block0 = ByteArray(16) { it.toByte() }
        val block1 = ByteArray(16) { (it + 16).toByte() }
        val frame = buildResponseFrame(
            idm = sampleIdm,
            sf1 = 0x00,
            sf2 = 0x00,
            blocks = listOf(block0, block1)
        )

        val decoded = FelicaProtocol.decodeReadResponse(frame, expectedIdm = sampleIdm)
        assertTrue(decoded.isSuccess)
        assertEquals(0, decoded.statusFlag1)
        assertEquals(0, decoded.statusFlag2)
        assertEquals(2, decoded.blocks.size)
        assertArrayEquals(block0, decoded.blocks[0])
        assertArrayEquals(block1, decoded.blocks[1])
    }

    @Test
    fun decodeReadResponse_handlesEmptyShortAndTruncatedFrames() {
        assertFalse(FelicaProtocol.decodeReadResponse(null).isSuccess)
        assertFalse(FelicaProtocol.decodeReadResponse(ByteArray(0)).isSuccess)
        assertFalse(FelicaProtocol.decodeReadResponse(ByteArray(12)).isSuccess)

        val valid = buildResponseFrame(sampleIdm, blocks = listOf(ByteArray(16)))
        val truncated = valid.copyOfRange(0, valid.size - 3)
        val decodedTruncated = FelicaProtocol.decodeReadResponse(truncated, sampleIdm)
        assertFalse(decodedTruncated.isSuccess)
        assertTrue(decodedTruncated.blocks.isEmpty())
    }

    @Test
    fun decodeReadResponse_rejectsWrongResponseCodeAndMismatchedIdm() {
        val wrongCode = buildResponseFrame(sampleIdm, blocks = listOf(ByteArray(16))).apply {
            this[1] = 0x05 // Not 0x07 Read Without Encryption Response
        }
        assertFalse(FelicaProtocol.decodeReadResponse(wrongCode, sampleIdm).isSuccess)

        val otherIdm = byteArrayOf(0x02, 0x27, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)
        val validForOther = buildResponseFrame(otherIdm, blocks = listOf(ByteArray(16)))
        assertFalse(FelicaProtocol.decodeReadResponse(validForOther, expectedIdm = sampleIdm).isSuccess)
    }

    @Test
    fun decodeReadResponse_surfacesNonZeroStatusFlagsAsProtectedFailure() {
        val protectedFrame = buildResponseFrame(
            idm = sampleIdm,
            sf1 = 0xFF,
            sf2 = 0xA8,
            blocks = emptyList()
        )
        val decoded = FelicaProtocol.decodeReadResponse(protectedFrame, expectedIdm = sampleIdm)
        assertTrue(decoded.isValidFrame)
        assertFalse(decoded.isSuccess)
        assertEquals(0xFF, decoded.statusFlag1)
        assertEquals(0xA8, decoded.statusFlag2)
        assertTrue(decoded.blocks.isEmpty())
    }

    companion object {
        fun buildResponseFrame(
            idm: ByteArray,
            sf1: Int = 0,
            sf2: Int = 0,
            blocks: List<ByteArray>
        ): ByteArray {
            val total = 13 + blocks.size * 16
            val out = ByteArray(total)
            out[0] = total.toByte()
            out[1] = 0x07
            idm.copyInto(out, 2)
            out[10] = sf1.toByte()
            out[11] = sf2.toByte()
            out[12] = blocks.size.toByte()
            var offset = 13
            blocks.forEach { block ->
                block.copyInto(out, offset)
                offset += 16
            }
            return out
        }
    }
}
