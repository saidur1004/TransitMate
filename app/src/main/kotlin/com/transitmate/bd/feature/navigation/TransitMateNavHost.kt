package com.transitmate.bd.feature.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.transitmate.bd.core.designsystem.component.TransitBottomNavigation
import com.transitmate.bd.domain.model.AppLanguage
import com.transitmate.bd.domain.repository.TransitInfoRepository
import com.transitmate.bd.feature.about.AboutScreen
import com.transitmate.bd.feature.carddetails.CardDetailsScreen
import com.transitmate.bd.feature.diagnostics.NfcDiagnosticsScreen
import com.transitmate.bd.feature.farecalculator.FareCalculatorScreen
import com.transitmate.bd.feature.help.HelpFaqScreen
import com.transitmate.bd.feature.history.TransactionHistoryScreen
import com.transitmate.bd.feature.home.HomeScreen
import com.transitmate.bd.feature.insights.InsightsScreen
import com.transitmate.bd.feature.main.MainViewModel
import com.transitmate.bd.feature.main.ScannerSessionPhase
import com.transitmate.bd.feature.mycards.MyCardsScreen
import com.transitmate.bd.feature.onboarding.OnboardingScreen
import com.transitmate.bd.feature.scanner.NfcScannerScreen
import com.transitmate.bd.feature.scanresult.ScanResultScreen
import com.transitmate.bd.feature.settings.DataManagementScreen
import com.transitmate.bd.feature.settings.SettingsScreen
import com.transitmate.bd.feature.splash.SplashScreen
import com.transitmate.bd.feature.stationexplorer.StationExplorerScreen
import kotlinx.coroutines.delay

@Composable
fun TransitMateAppShell(
    viewModel: MainViewModel,
    transitInfoRepository: TransitInfoRepository,
    onOpenNfcSettings: () -> Unit,
    onNfcPollingRequirementChanged: (shouldPoll: Boolean, diagnosticsActive: Boolean) -> Unit
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        TransitRoutes.HOME,
        TransitRoutes.MY_CARDS,
        TransitRoutes.SCAN,
        TransitRoutes.INSIGHTS,
        TransitRoutes.SETTINGS
    )

    // Notify host Activity cleanly whenever Reader Mode should be enabled or disabled
    val isScanRoute = currentRoute == TransitRoutes.SCAN
    val isDiagRoute = currentRoute == TransitRoutes.NFC_DIAGNOSTICS
    val shouldPollNfc = (isScanRoute && uiState.scannerPhase != ScannerSessionPhase.PAUSED) || isDiagRoute

    LaunchedEffect(shouldPollNfc, isDiagRoute) {
        onNfcPollingRequirementChanged(shouldPollNfc, isDiagRoute)
    }

    // Reset scanner session cleanly upon entering Scan tab
    LaunchedEffect(isScanRoute) {
        if (isScanRoute) {
            viewModel.startScannerSession()
        }
    }

    // Smooth transition to Scan Result upon confirmed physical tag detection
    LaunchedEffect(isScanRoute, uiState.scannerPhase) {
        if (isScanRoute && uiState.scannerPhase == ScannerSessionPhase.DETECTED) {
            delay(260L)
            navController.navigate(TransitRoutes.SCAN_RESULT) {
                launchSingleTop = true
            }
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(TransitRoutes.HOME) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                TransitBottomNavigation(
                    destinations = BottomNavigationDestinations,
                    currentRoute = currentRoute,
                    onSelect = navigateToTab
                )
            }
        }
    ) { outerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = outerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = TransitRoutes.SPLASH,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(180)) },
                popEnterTransition = { EnterTransition.None + fadeIn(animationSpec = tween(180)) },
                popExitTransition = { ExitTransition.None + fadeOut(animationSpec = tween(160)) }
            ) {
                composable(TransitRoutes.SPLASH) {
                    SplashScreen(
                        isHydrated = uiState.isHydrated,
                        hasCompletedOnboarding = uiState.preferences.hasCompletedOnboarding,
                        onNavigateToOnboarding = {
                            navController.navigate(TransitRoutes.ONBOARDING) {
                                popUpTo(TransitRoutes.SPLASH) { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(TransitRoutes.HOME) {
                                popUpTo(TransitRoutes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(TransitRoutes.ONBOARDING) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            viewModel.completeOnboarding()
                            navController.navigate(TransitRoutes.HOME) {
                                popUpTo(TransitRoutes.ONBOARDING) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(TransitRoutes.HOME) {
                    val primaryCard = uiState.savedCards.firstOrNull { it.isPrimary } ?: uiState.savedCards.firstOrNull()
                    HomeScreen(
                        nfcStatus = uiState.nfcStatus,
                        primaryCard = primaryCard,
                        savedCardCount = uiState.savedCards.size,
                        recentTransactions = uiState.verifiedTransactions,
                        onScanClick = { navigateToTab(TransitRoutes.SCAN) },
                        onOpenNfcSettings = onOpenNfcSettings,
                        onPrimaryCardClick = { cardId -> navController.navigate(TransitRoutes.cardDetails(cardId)) },
                        onOpenMyCards = { navigateToTab(TransitRoutes.MY_CARDS) },
                        onOpenFareCalculator = { navController.navigate(TransitRoutes.fareCalculator()) },
                        onOpenStationExplorer = { navController.navigate(TransitRoutes.STATION_EXPLORER) },
                        onOpenHistory = { navController.navigate(TransitRoutes.history()) }
                    )
                }

                composable(TransitRoutes.MY_CARDS) {
                    MyCardsScreen(
                        cards = uiState.savedCards,
                        onScanNewCard = { navigateToTab(TransitRoutes.SCAN) },
                        onCardClick = { cardId -> navController.navigate(TransitRoutes.cardDetails(cardId)) },
                        onSetPrimary = viewModel::setPrimaryCard,
                        onRenameCard = viewModel::renameCard,
                        onDeleteCard = viewModel::deleteCard
                    )
                }

                composable(TransitRoutes.SCAN) {
                    NfcScannerScreen(
                        nfcStatus = uiState.nfcStatus,
                        scannerPhase = uiState.scannerPhase,
                        activeErrorOutcome = uiState.activeScanErrorOutcome,
                        onPauseScan = viewModel::pauseScannerSession,
                        onResumeScan = viewModel::resumeScannerSession,
                        onOpenNfcSettings = onOpenNfcSettings,
                        onOpenHelp = { navController.navigate(TransitRoutes.HELP_FAQ) },
                        onOpenOfflineTools = { navController.navigate(TransitRoutes.fareCalculator()) },
                        onDismissError = viewModel::dismissScanErrorSheet
                    )
                }

                composable(TransitRoutes.SCAN_RESULT) {
                    ScanResultScreen(
                        outcome = uiState.latestScanOutcome,
                        savedCardId = uiState.latestSavedCardIdForResult,
                        onSaveCard = viewModel::saveDetectedCard,
                        onViewSavedCard = { cardId ->
                            navController.navigate(TransitRoutes.cardDetails(cardId)) {
                                popUpTo(TransitRoutes.SCAN_RESULT) { inclusive = true }
                            }
                        },
                        onOpenCardHistory = { cardId -> navController.navigate(TransitRoutes.history(cardId)) },
                        onScanAnother = {
                            navController.popBackStack()
                            viewModel.startScannerSession()
                        },
                        onOpenHelp = { navController.navigate(TransitRoutes.HELP_FAQ) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = TransitRoutes.CARD_DETAILS,
                    arguments = listOf(navArgument("cardId") { type = NavType.LongType })
                ) { entry ->
                    val cardId = entry.arguments?.getLong("cardId") ?: -1L
                    val card = uiState.savedCards.firstOrNull { it.id == cardId }
                    val cardTx = uiState.verifiedTransactions.filter { it.cardId == cardId }
                    CardDetailsScreen(
                        card = card,
                        transactions = cardTx,
                        onBack = { navController.popBackStack() },
                        onRefreshScan = { navigateToTab(TransitRoutes.SCAN) },
                        onSetPrimary = viewModel::setPrimaryCard,
                        onRenameCard = viewModel::renameCard,
                        onDeleteCard = viewModel::deleteCard,
                        onOpenCardHistory = { id -> navController.navigate(TransitRoutes.history(id)) }
                    )
                }

                composable(TransitRoutes.INSIGHTS) {
                    InsightsScreen(
                        transactions = uiState.verifiedTransactions,
                        onOpenHistory = { navController.navigate(TransitRoutes.history()) },
                        onOpenFareCalculator = { navController.navigate(TransitRoutes.fareCalculator()) },
                        onOpenStationExplorer = { navController.navigate(TransitRoutes.STATION_EXPLORER) }
                    )
                }

                composable(
                    route = TransitRoutes.HISTORY,
                    arguments = listOf(navArgument("cardId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { entry ->
                    val cardId = entry.arguments?.getString("cardId")?.toLongOrNull()
                    TransactionHistoryScreen(
                        transactions = uiState.verifiedTransactions,
                        savedCards = uiState.savedCards,
                        initialCardId = cardId,
                        onBack = { navController.popBackStack() },
                        onOpenScanner = { navigateToTab(TransitRoutes.SCAN) }
                    )
                }

                composable(
                    route = TransitRoutes.FARE_CALCULATOR,
                    arguments = listOf(navArgument("originCode") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { entry ->
                    val originCode = entry.arguments?.getString("originCode")
                    FareCalculatorScreen(
                        transitInfoRepository = transitInfoRepository,
                        initialOriginCode = originCode,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(TransitRoutes.STATION_EXPLORER) {
                    StationExplorerScreen(
                        transitInfoRepository = transitInfoRepository,
                        onBack = { navController.popBackStack() },
                        onCalculateFareFromStation = { code ->
                            navController.navigate(TransitRoutes.fareCalculator(code))
                        }
                    )
                }

                composable(TransitRoutes.SETTINGS) {
                    SettingsScreen(
                        preferences = uiState.preferences,
                        onSelectTheme = viewModel::setThemeMode,
                        onSelectLanguage = viewModel::setLanguage,
                        onOpenDataManagement = { navController.navigate(TransitRoutes.DATA_MANAGEMENT) },
                        onOpenHelp = { navController.navigate(TransitRoutes.HELP_FAQ) },
                        onOpenAbout = { navController.navigate(TransitRoutes.ABOUT) },
                        onOpenDiagnostics = { navController.navigate(TransitRoutes.NFC_DIAGNOSTICS) }
                    )
                }

                composable(TransitRoutes.DATA_MANAGEMENT) {
                    DataManagementScreen(
                        savedCardCount = uiState.savedCards.size,
                        historyCount = uiState.verifiedTransactions.size,
                        isBangla = uiState.preferences.language == AppLanguage.BANGLA,
                        onLoadExportPreview = viewModel::getSanitizedExportJson,
                        onBuildCsvExport = viewModel::getTransactionsCsv,
                        onClearHistory = viewModel::clearScanHistory,
                        onDeleteAllData = viewModel::clearAllLocalData,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(TransitRoutes.NFC_DIAGNOSTICS) {
                    NfcDiagnosticsScreen(
                        nfcStatus = uiState.nfcStatus,
                        diagnosticData = uiState.diagnosticData,
                        onClear = viewModel::clearDiagnosticData,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(TransitRoutes.HELP_FAQ) {
                    HelpFaqScreen(
                        language = uiState.preferences.language,
                        onReplayOnboarding = { navController.navigate(TransitRoutes.ONBOARDING) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(TransitRoutes.ABOUT) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
