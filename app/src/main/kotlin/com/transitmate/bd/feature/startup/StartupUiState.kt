package com.transitmate.bd.feature.startup

import com.transitmate.bd.core.nfc.NfcDiagnosticData
import com.transitmate.bd.domain.model.NfcHardwareStatus

/**
 * UI state for the technical startup smoke-test screen and Phase 03 diagnostic harness.
 */
data class StartupUiState(
    val isLoading: Boolean = true,
    val nfcStatus: NfcHardwareStatus = NfcHardwareStatus.NOT_SUPPORTED,
    val appName: String = "TransitMate",
    val appTagline: String = "Dhaka Metro & Rapid Pass Companion",
    val phaseBadge: String = "Phase 03 · NFC Research & Validation",
    val diagnosticData: NfcDiagnosticData? = null,
    val totalScansObserved: Int = 0
)
