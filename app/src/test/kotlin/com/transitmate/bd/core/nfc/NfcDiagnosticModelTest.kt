package com.transitmate.bd.core.nfc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NfcDiagnosticModelTest {

    @Test
    fun bytesToHex_formatsFullIdmPreservingByteOrder() {
        val bytes = byteArrayOf(
            0x01.toByte(), 0x27.toByte(), 0x88.toByte(), 0xB4.toByte(),
            0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0xA0.toByte()
        )
        assertEquals("01:27:88:B4:12:34:56:A0", NfcDiagnosticData.bytesToHex(bytes))
    }

    @Test
    fun bytesToHex_handlesNullAndEmpty() {
        assertNull(NfcDiagnosticData.bytesToHex(null))
        assertNull(NfcDiagnosticData.bytesToHex(byteArrayOf()))
    }

    @Test
    fun formatSystemCodeHex_formatsContinuouslyWithoutColons() {
        val sysCode = byteArrayOf(0x90.toByte(), 0xE3.toByte())
        assertEquals("90E3", NfcDiagnosticData.formatSystemCodeHex(sysCode))
        assertNull(NfcDiagnosticData.formatSystemCodeHex(null))
    }

    @Test
    fun sha256Digest_producesDeterministicHashWithoutExposingRawBytes() {
        val raw = byteArrayOf(0x01.toByte(), 0x27.toByte(), 0xAA.toByte(), 0xA0.toByte())
        val h1 = NfcDiagnosticData.sha256Digest(raw)
        val h2 = NfcDiagnosticData.sha256Digest(raw)
        assertEquals(h1, h2)
        assertEquals(64, h1?.length)
    }
}
