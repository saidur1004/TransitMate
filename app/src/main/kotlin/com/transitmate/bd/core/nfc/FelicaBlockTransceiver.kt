package com.transitmate.bd.core.nfc

import android.nfc.tech.NfcF

/**
 * Hardware-independent seam over FeliCa block reads so card-read orchestration,
 * partial-window recovery, and error paths are unit-testable on the JVM.
 */
interface FelicaBlockTransceiver {
    val idm: ByteArray
    fun connect()
    fun transceive(command: ByteArray): ByteArray
    fun close()
}

/**
 * Android [NfcF] implementation of [FelicaBlockTransceiver].
 */
class AndroidFelicaBlockTransceiver(
    private val nfcF: NfcF,
    override val idm: ByteArray
) : FelicaBlockTransceiver {

    override fun connect() {
        nfcF.connect()
        runCatching { nfcF.timeout = TRANSCEIVE_TIMEOUT_MS }
    }

    override fun transceive(command: ByteArray): ByteArray = nfcF.transceive(command)

    override fun close() {
        try {
            nfcF.close()
        } catch (_: Exception) {
            // Already closed or disconnected
        }
    }

    private companion object {
        const val TRANSCEIVE_TIMEOUT_MS = 1500
    }
}
