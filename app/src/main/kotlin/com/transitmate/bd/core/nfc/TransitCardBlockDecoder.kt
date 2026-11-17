package com.transitmate.bd.core.nfc

import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

data class TransitStationLabel(
    val code: Int,
    val nameEn: String,
    val nameBn: String
)

/**
 * Station directory matching on-card numeric codes stored inside DTCA transaction blocks.
 */
object TransitStationDirectory {

    private val stationsByCode: Map<Int, TransitStationLabel> = mapOf(
        // Dhaka Metro Rail MRT Line 6
        10 to TransitStationLabel(10, "Motijheel", "মতিঝিল"),
        20 to TransitStationLabel(20, "Bangladesh Secretariat", "বাংলাদেশ সচিবালয়"),
        25 to TransitStationLabel(25, "Dhaka University", "ঢাকা বিশ্ববিদ্যালয়"),
        30 to TransitStationLabel(30, "Shahbagh", "শাহবাগ"),
        35 to TransitStationLabel(35, "Karwan Bazar", "কারওয়ান বাজার"),
        40 to TransitStationLabel(40, "Farmgate", "ফার্মগেট"),
        45 to TransitStationLabel(45, "Bijoy Sarani", "বিজয় সরণি"),
        50 to TransitStationLabel(50, "Agargaon", "আগারগাঁও"),
        55 to TransitStationLabel(55, "Shewrapara", "শেওড়াপাড়া"),
        60 to TransitStationLabel(60, "Kazipara", "কাজীপাড়া"),
        65 to TransitStationLabel(65, "Mirpur 10", "মিরপুর ১০"),
        70 to TransitStationLabel(70, "Mirpur 11", "মিরপুর ১১"),
        75 to TransitStationLabel(75, "Pallabi", "পল্লবী"),
        80 to TransitStationLabel(80, "Uttara South", "উত্তরা দক্ষিণ"),
        85 to TransitStationLabel(85, "Uttara Center", "উত্তরা সেন্টার"),
        90 to TransitStationLabel(90, "Uttara North", "উত্তরা উত্তর"),
        // Hatirjheel Bus (Rapid Pass)
        13 to TransitStationLabel(13, "Mohanagar (HJ)", "মহানগর (হাতিরঝিল)"),
        16 to TransitStationLabel(16, "Rampura (HJ)", "রামপুরা (হাতিরঝিল)"),
        17 to TransitStationLabel(17, "Badda (HJ)", "বাড্ডা (হাতিরঝিল)"),
        19 to TransitStationLabel(19, "Police Plaza (HJ)", "পুলিশ প্লাজা (হাতিরঝিল)"),
        28 to TransitStationLabel(28, "FDC (HJ)", "এফডিসি (হাতিরঝিল)")
    )

    fun resolveName(code: Int?, fallbackName: String?, isBangla: Boolean): String? {
        if (code != null) {
            stationsByCode[code]?.let { return if (isBangla) it.nameBn else it.nameEn }
        }
        if (!fallbackName.isNullOrBlank()) {
            val match = stationsByCode.values.firstOrNull {
                it.nameEn.equals(fallbackName, ignoreCase = true) || it.nameBn == fallbackName
            }
            if (match != null) return if (isBangla) match.nameBn else match.nameEn
            return fallbackName
        }
        return code?.takeIf { it > 0 }?.let { "Station #$it" }
    }

    fun getEnglishNameOrNull(code: Int): String? = stationsByCode[code]?.nameEn
}

data class DecodedTransactionBlock(
    val headerHex: String,
    val timestampMillis: Long,
    val fromStationCode: Int,
    val toStationCode: Int,
    val postBalanceBdt: Int,
    val rawBlockHex: String
)

data class DecodedCardSnapshot(
    val cardType: CardType,
    val recognitionStatus: CardRecognitionStatus,
    val latestBalanceBdt: Int?,
    val transactions: List<VerifiedTransaction>,
    val validBlockCount: Int
)

/**
 * Decodes 16-byte FeliCa transaction blocks from DTCA Rapid Pass & MRT Pass cards.
 */
object TransitCardBlockDecoder {

    private const val HEADER_METRO_TRIP = "08 52 10 00"
    private const val HEADER_HJ_BUS_BOARD = "08 D2 20 00"
    private const val HEADER_HJ_BUS_ALIGHT = "42 D6 30 00"
    private const val HEADER_RAPID_TOPUP = "42 60 02 00"
    private const val HEADER_MRT_TOPUP = "1D 60 02 01"

    private const val MIN_VALID_YEAR = 2020
    private val DHAKA_ZONE_OFFSET: ZoneOffset = ZoneOffset.ofHours(6)

    /**
     * Decodes a single 16-byte FeliCa history block. Returns null if the block is blank,
     * malformed, carries an invalid calendar timestamp, or predates 2020.
     */
    fun decodeBlock(
        block: ByteArray,
        baseYear: Int = 2000
    ): DecodedTransactionBlock? {
        if (block.size != FelicaProtocol.FELICA_BLOCK_SIZE) return null
        if (block.all { it == 0.toByte() }) return null

        val headerHex = formatSpacedHex(block, 0, 4)

        // Bytes 4..6 carry a 24-bit big-endian bit-packed timestamp
        val packedTime = ((block[4].toInt() and 0xFF) shl 16) or
            ((block[5].toInt() and 0xFF) shl 8) or
            (block[6].toInt() and 0xFF)

        val yearOffset = (packedTime ushr 17) and 0x1F
        val month = (packedTime ushr 13) and 0x0F
        val day = (packedTime ushr 8) and 0x1F
        val hour = (packedTime ushr 3) and 0x1F
        val fullYear = baseYear + yearOffset

        if (fullYear < MIN_VALID_YEAR) return null
        if (month !in 1..12 || hour !in 0..23) return null
        val maxDay = YearMonth.of(fullYear, month).lengthOfMonth()
        if (day !in 1..maxDay) return null

        val timestampMillis = LocalDateTime.of(fullYear, month, day, hour, 0, 0)
            .toInstant(DHAKA_ZONE_OFFSET)
            .toEpochMilli()

        val fromStationCode = block[8].toInt() and 0xFF
        val toStationCode = block[10].toInt() and 0xFF

        // Bytes 11..13 store post-transaction purse balance in 24-bit little-endian BDT
        val balanceBdt = (block[11].toInt() and 0xFF) or
            ((block[12].toInt() and 0xFF) shl 8) or
            ((block[13].toInt() and 0xFF) shl 16)

        return DecodedTransactionBlock(
            headerHex = headerHex,
            timestampMillis = timestampMillis,
            fromStationCode = fromStationCode,
            toStationCode = toStationCode,
            postBalanceBdt = balanceBdt,
            rawBlockHex = formatCompactHex(block)
        )
    }

    /**
     * Decodes a sequence of blocks read in card ring-buffer order (newest first at block 0)
     * into recognized card identity, verified purse balance, and computed transaction history.
     */
    fun decodeCardData(
        idmBytes: ByteArray?,
        systemCodeHex: String?,
        rawBlocks: List<ByteArray>,
        readSucceeded: Boolean,
        baseYear: Int = 2000
    ): DecodedCardSnapshot {
        val validBlocks = rawBlocks.mapNotNull { decodeBlock(it, baseYear) }

        val (cardType, recognitionStatus) = classifyCard(
            idmBytes = idmBytes,
            systemCodeHex = systemCodeHex,
            decodedBlocks = validBlocks,
            readSucceeded = readSucceeded
        )

        val latestBalance = validBlocks.firstOrNull()?.postBalanceBdt

        val transactions = validBlocks.mapIndexed { index, entry ->
            val olderEntry = validBlocks.getOrNull(index + 1)
            val delta = olderEntry?.let { entry.postBalanceBdt - it.postBalanceBdt }

            val isTopUpHeader = entry.headerHex == HEADER_RAPID_TOPUP ||
                entry.headerHex == HEADER_MRT_TOPUP

            val txType = when {
                isTopUpHeader -> TransactionType.TOP_UP
                delta != null && delta > 0 -> TransactionType.TOP_UP
                else -> TransactionType.TRIP
            }

            val amountBdt = when {
                delta == null -> null
                delta < 0 -> -delta
                else -> delta
            }

            val fromName = if (isTopUpHeader) null else TransitStationDirectory.getEnglishNameOrNull(entry.fromStationCode)
            val toName = if (isTopUpHeader) null else TransitStationDirectory.getEnglishNameOrNull(entry.toStationCode)

            VerifiedTransaction(
                cardId = 0L,
                originStation = fromName,
                destinationStation = toName,
                originStationCode = if (isTopUpHeader) null else entry.fromStationCode.takeIf { it > 0 },
                destinationStationCode = if (isTopUpHeader) null else entry.toStationCode.takeIf { it > 0 },
                amountBdt = amountBdt,
                postBalanceBdt = entry.postBalanceBdt,
                type = txType,
                timestampMillis = entry.timestampMillis,
                isVerified = true,
                rawBlockHex = entry.rawBlockHex,
                sequenceOrder = (validBlocks.size - index).toLong()
            )
        }

        return DecodedCardSnapshot(
            cardType = cardType,
            recognitionStatus = recognitionStatus,
            latestBalanceBdt = latestBalance,
            transactions = transactions,
            validBlockCount = validBlocks.size
        )
    }

    internal fun classifyCard(
        idmBytes: ByteArray?,
        systemCodeHex: String?,
        decodedBlocks: List<DecodedTransactionBlock>,
        readSucceeded: Boolean
    ): Pair<CardType, CardRecognitionStatus> {
        val hasRapidTopUp = decodedBlocks.any { it.headerHex == HEADER_RAPID_TOPUP }
        val hasHatirjheel = decodedBlocks.any {
            it.headerHex == HEADER_HJ_BUS_BOARD || it.headerHex == HEADER_HJ_BUS_ALIGHT
        }
        val hasMrtTopUp = decodedBlocks.any { it.headerHex == HEADER_MRT_TOPUP }
        val hasMetroTrip = decodedBlocks.any { it.headerHex == HEADER_METRO_TRIP }

        val isRapidIdmPrefix = idmBytes != null &&
            idmBytes.size >= 2 &&
            idmBytes[0] == 0x01.toByte() &&
            idmBytes[1] == 0x27.toByte()

        val isRapidSystemCode = systemCodeHex.equals(FelicaProtocol.SYSTEM_CODE_DTCA_RAPID_PASS, ignoreCase = true)

        return when {
            hasMrtTopUp && !hasRapidTopUp && !hasHatirjheel -> {
                CardType.MRT_PASS to CardRecognitionStatus.VERIFIED_MRT_PASS
            }
            hasRapidTopUp || hasHatirjheel -> {
                CardType.RAPID_PASS to CardRecognitionStatus.VERIFIED_RAPID_PASS
            }
            isRapidIdmPrefix && (readSucceeded || isRapidSystemCode) -> {
                CardType.RAPID_PASS to CardRecognitionStatus.VERIFIED_RAPID_PASS
            }
            !isRapidIdmPrefix && hasMetroTrip -> {
                CardType.MRT_PASS to CardRecognitionStatus.VERIFIED_MRT_PASS
            }
            else -> CardType.UNKNOWN to CardRecognitionStatus.UNVERIFIED
        }
    }

    private fun formatSpacedHex(bytes: ByteArray, start: Int, endExclusive: Int): String =
        (start until endExclusive).joinToString(" ") { idx -> "%02X".format(bytes[idx]) }

    private fun formatCompactHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02X".format(it) }
}
