package com.transitmate.bd.core.nfc

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
    val systemCodeHex: String? = null,
    val manufacturerPmmHex: String? = null,
    val scanCount: Int = 1,
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
    }
}
