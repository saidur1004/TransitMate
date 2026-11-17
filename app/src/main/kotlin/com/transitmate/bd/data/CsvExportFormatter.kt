package com.transitmate.bd.data

import com.transitmate.bd.core.nfc.TransitStationDirectory
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Pure Kotlin UTF-8 CSV generator (with UTF-8 BOM for spreadsheet compatibility)
 * supporting both English and Bangla localization for verified local transit history.
 */
object CsvExportFormatter {

    private const val UTF8_BOM = "\uFEFF"
    private val DHAKA_OFFSET: ZoneOffset = ZoneOffset.ofHours(6)
    private val TIMESTAMP_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun formatTransactionsCsv(
        cards: List<SavedTransitCard>,
        transactions: List<VerifiedTransaction>,
        isBangla: Boolean
    ): String {
        val cardsById = cards.associateBy { it.id }
        val builder = StringBuilder(UTF8_BOM)

        val headers = if (isBangla) {
            listOf(
                "কার্ডের নাম",
                "কার্ডের ধরন",
                "কার্ড IDm",
                "সময় (UTC+6)",
                "লেনদেনের ধরন",
                "শুরুর স্টেশন",
                "গন্তব্য স্টেশন",
                "পরিমাণ (টাকা)",
                "পরবর্তী ব্যালেন্স (টাকা)"
            )
        } else {
            listOf(
                "Card Name",
                "Card Type",
                "Card IDm",
                "Timestamp (UTC+6)",
                "Entry Type",
                "Origin Station",
                "Destination Station",
                "Amount (BDT)",
                "Post Balance (BDT)"
            )
        }
        builder.appendLine(headers.joinToString(",") { escapeCsv(it) })

        transactions.forEach { tx ->
            val card = cardsById[tx.cardId]
            val formattedTime = Instant.ofEpochMilli(tx.timestampMillis)
                .atOffset(DHAKA_OFFSET)
                .format(TIMESTAMP_FORMAT)

            val entryType = when (tx.type) {
                TransactionType.TRIP -> if (isBangla) "ভ্রমণ" else "Trip"
                TransactionType.TOP_UP -> if (isBangla) "রিচার্জ" else "Top-Up"
            }

            val origin = if (tx.type == TransactionType.TOP_UP) {
                "—"
            } else {
                TransitStationDirectory.resolveName(tx.originStationCode, tx.originStation, isBangla) ?: "—"
            }

            val destination = if (tx.type == TransactionType.TOP_UP) {
                "—"
            } else {
                TransitStationDirectory.resolveName(tx.destinationStationCode, tx.destinationStation, isBangla) ?: "—"
            }

            val signedAmount = when {
                tx.amountBdt == null -> ""
                tx.type == TransactionType.TRIP -> "-${tx.amountBdt}"
                else -> "+${tx.amountBdt}"
            }

            val row = listOf(
                card?.nickname ?: (if (isBangla) "অজানা কার্ড" else "Unknown Card"),
                card?.userSelectedType?.displayName ?: card?.recognitionStatus?.name ?: "",
                card?.cardIdm ?: "",
                formattedTime,
                entryType,
                origin,
                destination,
                signedAmount,
                tx.postBalanceBdt?.toString() ?: ""
            )
            builder.appendLine(row.joinToString(",") { escapeCsv(it) })
        }

        return builder.toString()
    }

    internal fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') ||
            value.contains('"') ||
            value.contains('\n') ||
            value.contains('\r')
        if (!needsQuotes) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}
