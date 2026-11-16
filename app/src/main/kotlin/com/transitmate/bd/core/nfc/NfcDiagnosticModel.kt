package com.transitmate.bd.core.nfc

import java.security.MessageDigest

/**
 * Diagnostic scan data observed during physical NFC card discovery.
 * Strictly read-only and restricted to anticollision / discovery metadata.
 */
data class NfcDiagnosticData(
    val timestamp: Long = System.currentTimeMillis(),
    val techList: List<String> = emptyList(),
    val isNfcF: Boolean = false,
    val idmHex: String? = null,
    val idmRedacted: String? = null,
    val idmDigestKey: String? = null,
    val systemCodeHex: String? = null,
    val manufacturerPmmHex: String? = null,
    val scanCount: Int = 1,
    val rfLinkOk: Boolean = false,
    val isTagLost: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Formats a byte array into colon-separated uppercase hexadecimal string.
         */
        fun bytesToHex(bytes: ByteArray?): String? {
            if (bytes == null || bytes.isEmpty()) return null
            return bytes.joinToString(":") { "%02X".format(it) }
        }

        /**
         * Formats a 2-byte FeliCa System Code continuously without colons (e.g. "90E3").
         */
        fun formatSystemCodeHex(bytes: ByteArray?): String? {
            if (bytes == null || bytes.isEmpty()) return null
            return bytes.joinToString("") { "%02X".format(it) }
        }

        /**
         * Redacts the middle bytes of an 8-byte IDm for privacy in logs and UI.
         * Example: "01:2E:3D:4A:5B:6C:7D:9F" -> "01:2E:**:**:**:**:**:9F"
         */
        fun redactIdm(bytes: ByteArray?): String? {
            if (bytes == null || bytes.isEmpty()) return null
            if (bytes.size < 4) return bytesToHex(bytes)
            return bytes.mapIndexed { index, b ->
                if (index < 2 || index == bytes.size - 1) "%02X".format(b) else "**"
            }.joinToString(":")
        }

        /**
         * Computes a one-way SHA-256 hex digest of the raw tag identifier so saved cards
         * can be deduplicated locally without persisting raw card identifiers.
         */
        fun sha256Digest(bytes: ByteArray?): String? {
            if (bytes == null || bytes.isEmpty()) return null
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
