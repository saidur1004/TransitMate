package com.transitmate.bd.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SavedCardEntity::class, TransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TransitMateDatabase : RoomDatabase() {
    abstract fun transitCardDao(): TransitCardDao

    companion object {
        @Volatile
        private var instance: TransitMateDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_cards_v2` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cardDigestKey` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `userSelectedType` TEXT NOT NULL,
                        `recognitionStatus` TEXT NOT NULL,
                        `rfTechnology` TEXT NOT NULL,
                        `cardIdm` TEXT NOT NULL,
                        `isPrimary` INTEGER NOT NULL,
                        `addedAtMillis` INTEGER NOT NULL,
                        `lastScannedAtMillis` INTEGER NOT NULL,
                        `lastVerifiedBalanceBdt` INTEGER,
                        `lastVerifiedBalanceAtMillis` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `saved_cards_v2` (
                        `id`, `cardDigestKey`, `nickname`, `userSelectedType`,
                        `recognitionStatus`, `rfTechnology`, `cardIdm`, `isPrimary`,
                        `addedAtMillis`, `lastScannedAtMillis`,
                        `lastVerifiedBalanceBdt`, `lastVerifiedBalanceAtMillis`
                    )
                    SELECT
                        `id`, `cardDigestKey`, `nickname`, `userSelectedType`,
                        `recognitionStatus`, `rfTechnology`, `maskedIdentifier`, `isPrimary`,
                        `addedAtMillis`, `lastScannedAtMillis`,
                        `lastVerifiedBalanceBdt`, `lastVerifiedBalanceAtMillis`
                    FROM `saved_cards`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `saved_cards`")
                db.execSQL("ALTER TABLE `saved_cards_v2` RENAME TO `saved_cards`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_saved_cards_cardDigestKey` ON `saved_cards` (`cardDigestKey`)")

                db.execSQL("DROP TABLE IF EXISTS `verified_transactions`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `verified_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cardId` INTEGER NOT NULL,
                        `originStation` TEXT,
                        `originStationCode` INTEGER,
                        `destinationStation` TEXT,
                        `destinationStationCode` INTEGER,
                        `amountBdt` INTEGER,
                        `postBalanceBdt` INTEGER,
                        `type` TEXT NOT NULL,
                        `timestampMillis` INTEGER NOT NULL,
                        `isVerified` INTEGER NOT NULL,
                        `rawBlockHex` TEXT,
                        `sequenceOrder` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`cardId`) REFERENCES `saved_cards`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_verified_transactions_cardId` ON `verified_transactions` (`cardId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_verified_transactions_cardId_rawBlockHex` ON `verified_transactions` (`cardId`, `rawBlockHex`)")
            }
        }

        fun getInstance(context: Context): TransitMateDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TransitMateDatabase::class.java,
                    "transitmate_local.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
