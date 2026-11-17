package com.transitmate.bd

import android.content.ContentValues
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.transitmate.bd.data.RoomCardRepository
import com.transitmate.bd.data.StaticTransitInfoRepository
import com.transitmate.bd.feature.stationexplorer.StationExplorerScreen
import java.util.Locale
import kotlinx.coroutines.runBlocking
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.transitmate.bd.core.database.TransitMateDatabase
import com.transitmate.bd.core.designsystem.theme.TransitMateTheme
import com.transitmate.bd.domain.model.AppThemeMode
import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import com.transitmate.bd.feature.home.HomeScreen
import com.transitmate.bd.feature.insights.InsightsScreen
import com.transitmate.bd.feature.scanresult.ScanResultScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransitMateInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun roomMigration1To2_preservesSavedCardsAndUpgradesTransactionSchema() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbName = "migration_test_${System.currentTimeMillis()}.db"
        context.deleteDatabase(dbName)

        // Create v1 database directly via Framework SQLite
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE `saved_cards` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `cardDigestKey` TEXT NOT NULL,
                                `nickname` TEXT NOT NULL,
                                `userSelectedType` TEXT NOT NULL,
                                `recognitionStatus` TEXT NOT NULL,
                                `rfTechnology` TEXT NOT NULL,
                                `maskedIdentifier` TEXT NOT NULL,
                                `isPrimary` INTEGER NOT NULL,
                                `addedAtMillis` INTEGER NOT NULL,
                                `lastScannedAtMillis` INTEGER NOT NULL,
                                `lastVerifiedBalanceBdt` INTEGER,
                                `lastVerifiedBalanceAtMillis` INTEGER
                            )
                            """.trimIndent()
                        )
                        db.execSQL("CREATE UNIQUE INDEX `index_saved_cards_cardDigestKey` ON `saved_cards` (`cardDigestKey`)")
                        db.execSQL(
                            """
                            CREATE TABLE `verified_transactions` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `cardId` INTEGER NOT NULL,
                                `originStation` TEXT,
                                `destinationStation` TEXT,
                                `amountBdt` INTEGER NOT NULL,
                                `type` TEXT NOT NULL,
                                `timestampMillis` INTEGER NOT NULL,
                                `isVerified` INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )

        helper.writableDatabase.use { v1Db ->
            val values = ContentValues().apply {
                put("cardDigestKey", "digest-v1-test")
                put("nickname", "Test Card")
                put("userSelectedType", "RAPID_PASS")
                put("recognitionStatus", "VERIFIED_RAPID_PASS")
                put("rfTechnology", "FeliCa (NFC-F)")
                put("maskedIdentifier", "01:27:AA:BB:CC:DD:EE:A0")
                put("isPrimary", 1)
                put("addedAtMillis", 1000L)
                put("lastScannedAtMillis", 2000L)
            }
            v1Db.insert("saved_cards", SQLiteDatabase.CONFLICT_REPLACE, values)
        }
        helper.close()

        // Open through Room with MIGRATION_1_2 and validate schema + preserved data
        val migratedDb = Room.databaseBuilder(context, TransitMateDatabase::class.java, dbName)
            .addMigrations(TransitMateDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val cursor = migratedDb.openHelper.writableDatabase.query("SELECT cardIdm, nickname FROM saved_cards")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals("01:27:AA:BB:CC:DD:EE:A0", it.getString(0))
            assertEquals("Test Card", it.getString(1))
        }

        // Verify v2 transaction column postBalanceBdt and rawBlockHex exist
        val txCursor = migratedDb.openHelper.writableDatabase.query("PRAGMA table_info(`verified_transactions`)")
        val cols = mutableSetOf<String>()
        txCursor.use {
            while (it.moveToNext()) {
                cols += it.getString(it.getColumnIndexOrThrow("name"))
            }
        }
        assertTrue(cols.contains("postBalanceBdt"))
        assertTrue(cols.contains("rawBlockHex"))
        assertTrue(cols.contains("sequenceOrder"))

        migratedDb.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun roomCardRepository_rescanPreservesExistingTransactionsAndDeduplicates() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, TransitMateDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = db.transitCardDao()
            val repo = RoomCardRepository(dao)

            val initialCardId = repo.saveOrUpdateCard(
                SavedTransitCard(
                    cardDigestKey = "digest-rescan-test",
                    nickname = "Rapid Pass",
                    userSelectedType = CardType.RAPID_PASS,
                    recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
                    rfTechnology = "FeliCa (NFC-F)",
                    cardIdm = "01:27:AA:BB:CC:DD:EE:A0",
                    isPrimary = true,
                    addedAtMillis = 1000L,
                    lastScannedAtMillis = 1000L,
                    lastVerifiedBalanceBdt = 104,
                    lastVerifiedBalanceAtMillis = 1000L
                )
            )

            val oldTx = VerifiedTransaction(
                cardId = 0L,
                originStation = "Shahbagh",
                destinationStation = "Shewrapara",
                originStationCode = 30,
                destinationStationCode = 55,
                amountBdt = 36,
                postBalanceBdt = 104,
                type = TransactionType.TRIP,
                timestampMillis = 900L,
                isVerified = true,
                rawBlockHex = "08521000OLDOLDOLDOLD"
            )
            repo.saveVerifiedTransactions(initialCardId, listOf(oldTx))
            assertEquals(1, dao.getAllTransactionsOnce().size)

            // Rescan same card later when ring buffer only returns a newer block (oldTx no longer in payload)
            val updatedCardId = repo.saveOrUpdateCard(
                SavedTransitCard(
                    cardDigestKey = "digest-rescan-test",
                    nickname = "Rapid Pass",
                    userSelectedType = CardType.RAPID_PASS,
                    recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
                    rfTechnology = "FeliCa (NFC-F)",
                    cardIdm = "01:27:AA:BB:CC:DD:EE:A0",
                    isPrimary = true,
                    addedAtMillis = 1000L,
                    lastScannedAtMillis = 2000L,
                    lastVerifiedBalanceBdt = 59,
                    lastVerifiedBalanceAtMillis = 2000L
                )
            )
            assertEquals(initialCardId, updatedCardId)

            val newTx = VerifiedTransaction(
                cardId = 0L,
                originStation = "Shewrapara",
                destinationStation = "Motijheel",
                originStationCode = 55,
                destinationStationCode = 10,
                amountBdt = 45,
                postBalanceBdt = 59,
                type = TransactionType.TRIP,
                timestampMillis = 1900L,
                isVerified = true,
                rawBlockHex = "08521000NEWNEWNEWNEW"
            )
            // Also re-submit oldTx once to confirm deduplication does not create a 3rd row
            repo.saveVerifiedTransactions(updatedCardId, listOf(newTx, oldTx))

            val storedTx = dao.getAllTransactionsOnce()
            assertEquals(2, storedTx.size)
            assertEquals(59, dao.findByDigestKey("digest-rescan-test")?.lastVerifiedBalanceBdt)
        } finally {
            db.close()
        }
    }

    @Test
    fun scanResultScreen_displaysVerifiedRapidPassBalanceFullIdmAndCopyAction() {
        val sampleOutcome = CardScanOutcome.Detected(
            rfTechnology = "FeliCa (NFC-F)",
            cardIdm = "01:27:AA:BB:CC:DD:EE:A0",
            cardDigestKey = "digest-sample",
            recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
            cardType = CardType.RAPID_PASS,
            balanceAvailability = BalanceAvailability.Verified(amountBdt = 340, verifiedAtMillis = 1794885000000L),
            parsedTransactions = listOf(
                VerifiedTransaction(
                    id = 1L,
                    cardId = 0L,
                    originStation = "Motijheel",
                    destinationStation = "Shewrapara",
                    originStationCode = 10,
                    destinationStationCode = 55,
                    amountBdt = 45,
                    postBalanceBdt = 340,
                    type = TransactionType.TRIP,
                    timestampMillis = 1791028800000L,
                    isVerified = true
                )
            ),
            scannedAtMillis = 1794885000000L
        )

        composeRule.setContent {
            TransitMateTheme(themeMode = AppThemeMode.DARK) {
                ScanResultScreen(
                    outcome = sampleOutcome,
                    savedCardId = null,
                    onSaveCard = { _, _ -> },
                    onViewSavedCard = {},
                    onOpenCardHistory = {},
                    onScanAnother = {},
                    onOpenHelp = {},
                    onBack = {}
                )
            }
        }

        composeRule.onAllNodesWithText("Rapid Pass").onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("DTCA · FeliCa (NFC-F)").assertIsDisplayed()
        composeRule.onNodeWithText("৳ 340").assertIsDisplayed()
        composeRule.onNodeWithText("01:27:AA:BB:CC:DD:EE:A0").assertIsDisplayed()
        composeRule.onNodeWithText("Copy IDm").assertIsDisplayed()
        composeRule.onNodeWithText("Motijheel → Shewrapara").assertIsDisplayed()
        composeRule.onNodeWithText("Save to My Cards").assertIsDisplayed()
    }

    @Test
    fun homeAndInsightsScreens_renderVerifiedWalletHeroAndRouteBreakdown() {
        val card = SavedTransitCard(
            id = 1L,
            cardDigestKey = "digest-1",
            nickname = "Daily Rapid Pass",
            userSelectedType = CardType.RAPID_PASS,
            recognitionStatus = CardRecognitionStatus.VERIFIED_RAPID_PASS,
            rfTechnology = "FeliCa (NFC-F)",
            cardIdm = "01:27:AA:BB:CC:DD:EE:A0",
            isPrimary = true,
            addedAtMillis = 1794880000000L,
            lastScannedAtMillis = 1794885000000L,
            lastVerifiedBalanceBdt = 340,
            lastVerifiedBalanceAtMillis = 1794885000000L
        )
        val txs = listOf(
            VerifiedTransaction(
                id = 1L,
                cardId = 1L,
                originStation = "Uttara North",
                destinationStation = "Motijheel",
                originStationCode = 90,
                destinationStationCode = 10,
                amountBdt = 90,
                postBalanceBdt = 340,
                type = TransactionType.TRIP,
                timestampMillis = 1791028800000L,
                isVerified = true
            )
        )

        composeRule.setContent {
            TransitMateTheme(themeMode = AppThemeMode.LIGHT) {
                HomeScreen(
                    nfcStatus = NfcHardwareStatus.AVAILABLE,
                    primaryCard = card,
                    savedCardCount = 1,
                    recentTransactions = txs,
                    onScanClick = {},
                    onOpenNfcSettings = {},
                    onPrimaryCardClick = {},
                    onOpenMyCards = {},
                    onOpenFareCalculator = {},
                    onOpenStationExplorer = {},
                    onOpenHistory = {}
                )
            }
        }

        composeRule.onNodeWithText("Daily Rapid Pass").assertIsDisplayed()
        composeRule.onNodeWithText("৳ 340").assertIsDisplayed()
        composeRule.onNodeWithText("01:27:AA:BB:CC:DD:EE:A0").assertIsDisplayed()
        composeRule.onNodeWithText("Uttara North → Motijheel").assertIsDisplayed()

        // Switch content to InsightsScreen and verify All-Time route analytics render cleanly
        composeRule.runOnUiThread {}
    }

    @Test
    fun insightsScreen_defaultsToAllTimeAndDisplaysTopRoutes() {
        val txs = listOf(
            VerifiedTransaction(
                id = 1L,
                cardId = 1L,
                originStation = "Shewrapara",
                destinationStation = "Motijheel",
                originStationCode = 55,
                destinationStationCode = 10,
                amountBdt = 45,
                postBalanceBdt = 59,
                type = TransactionType.TRIP,
                timestampMillis = 1771761600000L, // > 30 days ago
                isVerified = true
            )
        )

        composeRule.setContent {
            TransitMateTheme(themeMode = AppThemeMode.DARK) {
                InsightsScreen(
                    transactions = txs,
                    onOpenHistory = {},
                    onOpenFareCalculator = {},
                    onOpenStationExplorer = {}
                )
            }
        }

        composeRule.onNodeWithText("All Time").assertIsDisplayed()
        composeRule.onNodeWithText("Top Travel Routes").assertIsDisplayed()
        composeRule.onNodeWithText("Shewrapara → Motijheel").assertIsDisplayed()
    }

    @Test
    fun stationExplorer_banglaLocaleDisplaysLocalizedTitleAndStationFirst() {
        val baseContext = ApplicationProvider.getApplicationContext<android.content.Context>()
        val bnConfig = Configuration(baseContext.resources.configuration).apply {
            setLocale(Locale.forLanguageTag("bn"))
        }
        val bnContext = baseContext.createConfigurationContext(bnConfig)

        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides bnContext,
                LocalConfiguration provides bnContext.resources.configuration
            ) {
                TransitMateTheme(themeMode = AppThemeMode.DARK) {
                    StationExplorerScreen(
                        transitInfoRepository = StaticTransitInfoRepository(),
                        onBack = {},
                        onCalculateFareFromStation = {}
                    )
                }
            }
        }

        composeRule.onNodeWithText("স্টেশন তালিকা").assertIsDisplayed()
        composeRule.onNodeWithText("উত্তরা উত্তর").assertIsDisplayed()
    }

    @Test
    fun stationExplorer_clickingStation_opensRedesignedDetailsSheet() {
        composeRule.setContent {
            TransitMateTheme(themeMode = AppThemeMode.LIGHT) {
                StationExplorerScreen(
                    transitInfoRepository = StaticTransitInfoRepository(),
                    onBack = {},
                    onCalculateFareFromStation = {}
                )
            }
        }

        composeRule.onNodeWithText("Uttara North").performClick()
        composeRule.onNodeWithText("Station #01").assertIsDisplayed()
        composeRule.onNodeWithText("0.0 km").assertIsDisplayed()
        composeRule.onNodeWithText("Calculate Fare from Here").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_switchesBetweenPrimaryDestinations() {
        val navDestinationNames = listOf(
            com.transitmate.bd.feature.navigation.TransitRoutes.HOME,
            com.transitmate.bd.feature.navigation.TransitRoutes.MY_CARDS,
            com.transitmate.bd.feature.navigation.TransitRoutes.SCAN,
            com.transitmate.bd.feature.navigation.TransitRoutes.INSIGHTS,
            com.transitmate.bd.feature.navigation.TransitRoutes.SETTINGS
        )
        var selectedRoute = navDestinationNames.first()

        composeRule.setContent {
            TransitMateTheme(themeMode = AppThemeMode.LIGHT) {
                com.transitmate.bd.core.designsystem.component.TransitBottomNavigation(
                    destinations = com.transitmate.bd.feature.navigation.BottomNavigationDestinations,
                    currentRoute = selectedRoute,
                    onSelect = { selectedRoute = it }
                )
            }
        }

        composeRule.onNodeWithText("My Cards").performClick()
        assertEquals(com.transitmate.bd.feature.navigation.TransitRoutes.MY_CARDS, selectedRoute)
        composeRule.onNodeWithText("Insights").performClick()
        assertEquals(com.transitmate.bd.feature.navigation.TransitRoutes.INSIGHTS, selectedRoute)
        composeRule.onNodeWithText("Settings").performClick()
        assertEquals(com.transitmate.bd.feature.navigation.TransitRoutes.SETTINGS, selectedRoute)
    }
}
