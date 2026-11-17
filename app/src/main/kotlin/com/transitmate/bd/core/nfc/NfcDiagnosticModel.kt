package com.transitmate.bd.core.nfc

import java.security.MessageDigest

/**
 * Diagnostic and decoded scan metadata observed during physical NFC card discovery and reading.
 */
data class NfcDiagnosticData(
    val timestamp: Long = System.currentTimeMillis(),
    val techList: List<String> = emptyList(),
    val isNfcF: Boolean = false,
    val idmHex: String? = null,
    val idmDigestKey: String? = null,
    val systemCodeHex: String? = null,
    val manufacturerPmmHex: String? = null,
    val scanCount: Int = 1,
    val rfLinkOk: Boolean = false,
    val isTagLost: Boolean = false,
    val blocksReadCount: Int = 0,
    val validTransactionCount: Int = 0,
    val statusFlag1: Int? = null,
    val statusFlag2: Int? = null,
    val serviceProtected: Boolean = false,
    val decodedSnapshot: DecodedCardSnapshot? = null,
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Formats a byte array into colon-separated uppercase hexadecimal string
         * preserving exact byte order (e.g., "01:27:XX:XX:XX:XX:XX:A0").
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
         * Computes a one-way SHA-256 hex digest of the raw tag identifier so saved cards
         * can be deduplicated cleanly inside the local database.
         */
        fun sha256Digest(bytes: ByteArray?): String? {
            if (bytes == null || bytes.isEmpty()) return null
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
