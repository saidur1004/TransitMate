package com.transitmate.bd.feature.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.core.nfc.NfcDiagnosticData
import com.transitmate.bd.domain.model.NfcHardwareStatus
import kotlinx.coroutines.launch

@Composable
fun NfcDiagnosticsScreen(
    nfcStatus: NfcHardwareStatus,
    diagnosticData: NfcDiagnosticData?,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMsg = stringResource(R.string.diagnostics_copied_msg)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.diagnostics_title),
                showBack = true,
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = stringResource(R.string.diagnostics_clear)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusBannerCard(
                severity = BannerSeverity.INFO,
                icon = Icons.Rounded.Terminal,
                title = stringResource(R.string.diagnostics_banner_title),
                message = stringResource(R.string.diagnostics_banner_body)
            )

            SurfaceSectionCard {
                Text(
                    text = stringResource(R.string.diagnostics_adapter_header),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                val (hwLabel, hwColor) = when (nfcStatus) {
                    NfcHardwareStatus.AVAILABLE -> stringResource(R.string.nfc_status_ready) to semantic.success
                    NfcHardwareStatus.DISABLED -> stringResource(R.string.nfc_status_disabled) to semantic.warning
                    NfcHardwareStatus.NOT_SUPPORTED -> stringResource(R.string.nfc_status_unsupported) to MaterialTheme.colorScheme.error
                }
                InfoKeyValueRow(
                    label = "NFC Hardware",
                    value = hwLabel,
                    valueColor = hwColor
                )
                InfoKeyValueRow(
                    label = "Reader Flags",
                    value = "NFC_F | NFC_A | SKIP_NDEF",
                    monospaceValue = true,
                    showDivider = false
                )
            }

            SurfaceSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.diagnostics_last_scan_header),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (diagnosticData != null) {
                        StatusPill(
                            text = "Scan #${diagnosticData.scanCount}",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (diagnosticData == null) {
                    Text(
                        text = stringResource(R.string.diagnostics_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    InfoKeyValueRow(
                        label = "Tech List",
                        value = diagnosticData.techList.joinToString(", ").ifEmpty { "None" },
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = "IDm (Masked)",
                        value = diagnosticData.idmRedacted ?: "Unavailable",
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = "System Code",
                        value = diagnosticData.systemCodeHex?.let { "0x$it" } ?: "Not detected",
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = "Manufacturer (PMm)",
                        value = diagnosticData.manufacturerPmmHex ?: "Not detected",
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = "RF Link Check",
                        value = when {
                            diagnosticData.rfLinkOk -> "connect() / close() OK"
                            diagnosticData.errorMessage != null -> diagnosticData.errorMessage
                            else -> "Non-NfcF tag"
                        },
                        valueColor = if (diagnosticData.rfLinkOk) semantic.success else semantic.warning,
                        showDivider = false
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        val report = if (diagnosticData == null) {
                            "TransitMate NFC Diagnostics: hardware=$nfcStatus, no scan captured"
                        } else {
                            buildString {
                                appendLine("TransitMate NFC Diagnostics (Redacted)")
                                appendLine("Hardware: $nfcStatus")
                                appendLine("Scan #${diagnosticData.scanCount}")
                                appendLine("Tech: ${diagnosticData.techList.joinToString(", ")}")
                                appendLine("IDm (Redacted): ${diagnosticData.idmRedacted ?: "Unavailable"}")
                                appendLine("System Code: ${diagnosticData.systemCodeHex?.let { "0x$it" } ?: "None"}")
                                appendLine("PMm: ${diagnosticData.manufacturerPmmHex ?: "None"}")
                                appendLine("RF Link OK: ${diagnosticData.rfLinkOk}")
                                if (diagnosticData.errorMessage != null) {
                                    appendLine("Trace: ${diagnosticData.errorMessage}")
                                }
                            }
                        }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("TransitMate Diagnostics", report))
                        scope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.diagnostics_copy_log),
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.diagnostics_clear),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
