package com.transitmate.bd.core.nfc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NfcDiagnosticModelTest {

    @Test
    fun bytesToHex_formatsCorrectly() {
        val bytes = byteArrayOf(0x01.toByte(), 0x2E.toByte(), 0x88.toByte(), 0xB4.toByte())
        assertEquals("01:2E:88:B4", NfcDiagnosticData.bytesToHex(bytes))
    }

    @Test
    fun bytesToHex_handlesNullAndEmpty() {
        assertNull(NfcDiagnosticData.bytesToHex(null))
        assertNull(NfcDiagnosticData.bytesToHex(byteArrayOf()))
    }

    @Test
    fun redactIdm_redactsMiddleBytesForPrivacy() {
        // Standard 8-byte FeliCa IDm
        val idm = byteArrayOf(
            0x01.toByte(), 0x2E.toByte(), 0x3A.toByte(), 0x4B.toByte(),
            0x5C.toByte(), 0x6D.toByte(), 0x7E.toByte(), 0x9F.toByte()
        )
        val redacted = NfcDiagnosticData.redactIdm(idm)
        assertEquals("01:2E:**:**:**:**:**:9F", redacted)
    }

    @Test
    fun redactIdm_handlesShortArrays() {
        val shortBytes = byteArrayOf(0x01.toByte(), 0x02.toByte())
        assertEquals("01:02", NfcDiagnosticData.redactIdm(shortBytes))
    }
}

