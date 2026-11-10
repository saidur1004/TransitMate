package com.transitmate.bd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.transitmate.bd.core.designsystem.theme.TransitMateTheme
import com.transitmate.bd.core.nfc.AndroidNfcAvailabilityChecker
import com.transitmate.bd.data.DefaultNfcAvailabilityRepository
import com.transitmate.bd.feature.startup.StartupScreen
import com.transitmate.bd.feature.startup.StartupViewModel

class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val nfcChecker = AndroidNfcAvailabilityChecker(applicationContext)
                val nfcRepository = DefaultNfcAvailabilityRepository(nfcChecker)
                return StartupViewModel(nfcRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TransitMateTheme {
                StartupScreen(viewModel = startupViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check NFC status when returning to the app (e.g. from system settings)
        startupViewModel.checkHardware()
    }
}
