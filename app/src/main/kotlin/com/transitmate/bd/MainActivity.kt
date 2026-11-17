package com.transitmate.bd

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.transitmate.bd.core.designsystem.theme.TransitMateTheme
import com.transitmate.bd.feature.main.MainViewModel
import com.transitmate.bd.feature.navigation.TransitMateAppShell
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var isActivityResumed = false
    private var shouldPollNfc = false
    private var isDiagnosticsRouteActive = false

    private val appContainer: TransitMateContainer
        get() = (application as TransitMateApplication).container

    private val mainViewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    nfcAvailabilityRepository = appContainer.nfcAvailabilityRepository,
                    cardRepository = appContainer.cardRepository,
                    preferencesRepository = appContainer.preferencesRepository,
                    dispatchers = appContainer.dispatchers
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val baseContext = LocalContext.current
            val localizedContext = remember(baseContext, uiState.preferences.language.tag) {
                createLocalizedContext(baseContext, uiState.preferences.language.tag)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                TransitMateTheme(themeMode = uiState.preferences.themeMode) {
                    TransitMateAppShell(
                        viewModel = mainViewModel,
                        transitInfoRepository = appContainer.transitInfoRepository,
                        onOpenNfcSettings = ::openAndroidNfcSettings,
                        onNfcPollingRequirementChanged = { poll, diagActive ->
                            shouldPollNfc = poll
                            isDiagnosticsRouteActive = diagActive
                            syncNfcReaderMode()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
        mainViewModel.refreshNfcStatus()
        syncNfcReaderMode()
    }

    override fun onPause() {
        isActivityResumed = false
        appContainer.nfcDiagnosticReader.disable(this)
        super.onPause()
    }

    private fun syncNfcReaderMode() {
        if (!isActivityResumed || !shouldPollNfc) {
            appContainer.nfcDiagnosticReader.disable(this)
            return
        }

        val diagnosticsOnly = isDiagnosticsRouteActive
        appContainer.nfcDiagnosticReader.enable(
            activity = this,
            onReadingStarted = {
                if (!diagnosticsOnly) {
                    mainViewModel.onScannerReadingStarted()
                }
            },
            onOutcome = { outcome ->
                if (!diagnosticsOnly) {
                    mainViewModel.onConsumerTagOutcome(outcome)
                }
            },
            onTagScanned = { diagnosticData ->
                if (diagnosticsOnly) {
                    mainViewModel.onDiagnosticTagScanned(diagnosticData)
                }
            }
        )
    }

    private fun openAndroidNfcSettings() {
        val nfcIntent = Intent(Settings.ACTION_NFC_SETTINGS)
        val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
        try {
            startActivity(nfcIntent)
        } catch (_: Exception) {
            runCatching { startActivity(fallbackIntent) }
        }
    }

    private fun createLocalizedContext(base: Context, languageTag: String): Context {
        val locale = Locale.forLanguageTag(languageTag)
        val config = Configuration(base.resources.configuration).apply {
            setLocale(locale)
        }
        return base.createConfigurationContext(config)
    }
}
