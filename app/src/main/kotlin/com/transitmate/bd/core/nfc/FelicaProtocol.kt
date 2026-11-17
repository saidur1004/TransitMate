package com.transitmate.bd.core.nfc

/**
 * Decoded result of a single FeliCa Read Without Encryption (0x06 -> 0x07) response frame.
 */
data class FelicaReadFrameResult(
    val statusFlag1: Int,
    val statusFlag2: Int,
    val isValidFrame: Boolean,
    val blocks: List<ByteArray>
) {
    val isSuccess: Boolean
        get() = isValidFrame && statusFlag1 == 0 && statusFlag2 == 0
}

/**
 * Clean-room implementation of FeliCa (JIS X 6319-4) Read Without Encryption command
 * framing and response decoding for DTCA transit cards.
 */
object FelicaProtocol {

    const val COMMAND_READ_WITHOUT_ENCRYPTION: Byte = 0x06
    const val RESPONSE_READ_WITHOUT_ENCRYPTION: Byte = 0x07
    const val DTCA_HISTORY_SERVICE_CODE: Int = 0x220F
    const val SYSTEM_CODE_DTCA_RAPID_PASS: String = "90E3"

    const val FELICA_IDM_LENGTH: Int = 8
    const val FELICA_BLOCK_SIZE: Int = 16
    const val DEFAULT_WINDOW_BLOCK_COUNT: Int = 10
    const val MIN_RESPONSE_HEADER_SIZE: Int = 13

    private const val BLOCK_LIST_2BYTE_CONTROL: Byte = 0x80.toByte()
    private const val INVALID_STATUS_FLAG: Int = -1

    /**
     * Builds a FeliCa Read Without Encryption (0x06) command frame for 1 service and
     * a contiguous range of blocks starting at [startBlock].
     */
    fun buildReadWithoutEncryptionCommand(
        idm: ByteArray,
        serviceCode: Int = DTCA_HISTORY_SERVICE_CODE,
        startBlock: Int = 0,
        blockCount: Int = DEFAULT_WINDOW_BLOCK_COUNT
    ): ByteArray {
        require(idm.size == FELICA_IDM_LENGTH) {
            "FeliCa IDm must be $FELICA_IDM_LENGTH bytes, got ${idm.size}"
        }
        require(startBlock in 0..255) {
            "2-byte block list elements support block numbers 0..255, got $startBlock"
        }
        require(blockCount in 1..255) {
            "Block count must be 1..255, got $blockCount"
        }
        require(startBlock + blockCount <= 256) {
            "Requested block range exceeds 255: $startBlock..${startBlock + blockCount - 1}"
        }

        // Frame format: [LEN(1), CMD=0x06(1), IDm(8), NumServices=1(1), ServiceCodeLE(2), NumBlocks(1), BlockList(2*N)]
        val totalLength = 14 + (blockCount * 2)
        val packet = ByteArray(totalLength)
        var cursor = 0

        packet[cursor++] = totalLength.toByte()
        packet[cursor++] = COMMAND_READ_WITHOUT_ENCRYPTION
        idm.copyInto(destination = packet, destinationOffset = cursor)
        cursor += FELICA_IDM_LENGTH

        packet[cursor++] = 0x01.toByte() // 1 service
        packet[cursor++] = (serviceCode and 0xFF).toByte() // Service code LSB first
        packet[cursor++] = ((serviceCode ushr 8) and 0xFF).toByte() // Service code MSB
        packet[cursor++] = blockCount.toByte()

        for (offset in 0 until blockCount) {
            packet[cursor++] = BLOCK_LIST_2BYTE_CONTROL
            packet[cursor++] = (startBlock + offset).toByte()
        }

        return packet
    }

    /**
     * Decodes a raw FeliCa response frame returned by [android.nfc.tech.NfcF.transceive].
     * Enforces minimum header bounds, response code 0x07, optional IDm match, status flags,
     * and exact declared payload size.
     */
    fun decodeReadResponse(
        frame: ByteArray?,
        expectedIdm: ByteArray? = null
    ): FelicaReadFrameResult {
        if (frame == null || frame.size < MIN_RESPONSE_HEADER_SIZE) {
            return FelicaReadFrameResult(
                statusFlag1 = INVALID_STATUS_FLAG,
                statusFlag2 = INVALID_STATUS_FLAG,
                isValidFrame = false,
                blocks = emptyList()
            )
        }

        val responseCode = frame[1].toInt() and 0xFF
        if (responseCode != RESPONSE_READ_WITHOUT_ENCRYPTION.toInt()) {
            return FelicaReadFrameResult(
                statusFlag1 = INVALID_STATUS_FLAG,
                statusFlag2 = INVALID_STATUS_FLAG,
                isValidFrame = false,
                blocks = emptyList()
            )
        }

        if (expectedIdm != null && expectedIdm.size == FELICA_IDM_LENGTH) {
            val responseIdm = frame.copyOfRange(2, 10)
            if (!responseIdm.contentEquals(expectedIdm)) {
                return FelicaReadFrameResult(
                    statusFlag1 = INVALID_STATUS_FLAG,
                    statusFlag2 = INVALID_STATUS_FLAG,
                    isValidFrame = false,
                    blocks = emptyList()
                )
            }
        }

        val statusFlag1 = frame[10].toInt() and 0xFF
        val statusFlag2 = frame[11].toInt() and 0xFF
        if (statusFlag1 != 0 || statusFlag2 != 0) {
            return FelicaReadFrameResult(
                statusFlag1 = statusFlag1,
                statusFlag2 = statusFlag2,
                isValidFrame = true,
                blocks = emptyList()
            )
        }

        val blockCount = frame[12].toInt() and 0xFF
        val availablePayloadBytes = frame.size - MIN_RESPONSE_HEADER_SIZE
        val requiredPayloadBytes = blockCount * FELICA_BLOCK_SIZE
        if (availablePayloadBytes < requiredPayloadBytes) {
            return FelicaReadFrameResult(
                statusFlag1 = statusFlag1,
                statusFlag2 = statusFlag2,
                isValidFrame = false,
                blocks = emptyList()
            )
        }

        val blocks = ArrayList<ByteArray>(blockCount)
        for (i in 0 until blockCount) {
            val start = MIN_RESPONSE_HEADER_SIZE + (i * FELICA_BLOCK_SIZE)
            blocks.add(frame.copyOfRange(start, start + FELICA_BLOCK_SIZE))
        }

        return FelicaReadFrameResult(
            statusFlag1 = statusFlag1,
            statusFlag2 = statusFlag2,
            isValidFrame = true,
            blocks = blocks
        )
    }
}
