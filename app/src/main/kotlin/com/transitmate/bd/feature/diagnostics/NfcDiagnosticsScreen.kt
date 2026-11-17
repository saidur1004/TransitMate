package com.transitmate.bd.feature.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.FullIdmRow
import com.transitmate.bd.core.designsystem.component.IdmPrivacyCaption
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.SecondaryOutlinedButton
import com.transitmate.bd.core.designsystem.component.SecondaryTonalButton
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.component.copyPlainTextToClipboard
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
    val idmCopiedMsg = stringResource(R.string.result_idm_copied)

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
        ResponsiveScreenContainer(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    label = stringResource(R.string.diagnostics_row_hardware),
                    value = hwLabel,
                    valueColor = hwColor
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.diagnostics_row_flags),
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
                            text = stringResource(R.string.diagnostics_scan_badge, diagnosticData.scanCount),
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
                    val fullIdm = diagnosticData.idmHex
                    if (fullIdm != null) {
                        FullIdmRow(
                            cardIdm = fullIdm,
                            label = stringResource(R.string.diagnostics_row_full_idm),
                            onCopy = {
                                copyPlainTextToClipboard(context, "TransitMate Card IDm", fullIdm)
                                scope.launch { snackbarHostState.showSnackbar(idmCopiedMsg) }
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        IdmPrivacyCaption()
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        InfoKeyValueRow(
                            label = stringResource(R.string.diagnostics_row_full_idm),
                            value = stringResource(R.string.diagnostics_val_unavailable),
                            monospaceValue = true
                        )
                    }
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_tech_list),
                        value = diagnosticData.techList.joinToString(", ")
                            .ifEmpty { stringResource(R.string.diagnostics_val_none) },
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_system_code),
                        value = diagnosticData.systemCodeHex?.let { "0x$it" }
                            ?: stringResource(R.string.diagnostics_val_not_detected),
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_pmm),
                        value = diagnosticData.manufacturerPmmHex
                            ?: stringResource(R.string.diagnostics_val_not_detected),
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_service_status),
                        value = if (diagnosticData.statusFlag1 != null && diagnosticData.statusFlag2 != null) {
                            "SF1=0x%02X SF2=0x%02X".format(diagnosticData.statusFlag1, diagnosticData.statusFlag2)
                        } else {
                            stringResource(R.string.diagnostics_val_not_queried)
                        },
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_blocks),
                        value = stringResource(
                            R.string.diagnostics_val_blocks,
                            diagnosticData.blocksReadCount,
                            diagnosticData.validTransactionCount
                        ),
                        monospaceValue = true
                    )
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_decoded),
                        value = buildString {
                            append(
                                diagnosticData.decodedSnapshot?.cardType?.displayName
                                    ?: stringResource(R.string.diagnostics_val_unverified)
                            )
                            val bal = diagnosticData.decodedSnapshot?.latestBalanceBdt
                            if (bal != null) append(" · ৳ $bal")
                        }
                    )
                    InfoKeyValueRow(
                        label = stringResource(R.string.diagnostics_row_rf_link),
                        value = when {
                            diagnosticData.rfLinkOk -> "connect() / transceive() / close() OK"
                            diagnosticData.errorMessage != null -> diagnosticData.errorMessage
                            else -> stringResource(R.string.diagnostics_val_rf_non_felica)
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
                SecondaryTonalButton(
                    text = stringResource(R.string.diagnostics_copy_log),
                    icon = Icons.Rounded.ContentCopy,
                    onClick = {
                        val report = if (diagnosticData == null) {
                            "TransitMate NFC Diagnostics: hardware=$nfcStatus, no scan captured"
                        } else {
                            buildString {
                                appendLine("TransitMate NFC Diagnostics")
                                appendLine("Hardware: $nfcStatus")
                                appendLine("Scan #${diagnosticData.scanCount}")
                                appendLine("Tech: ${diagnosticData.techList.joinToString(", ")}")
                                appendLine("IDm: ${diagnosticData.idmHex ?: "Unavailable"}")
                                appendLine("System Code: ${diagnosticData.systemCodeHex?.let { "0x$it" } ?: "None"}")
                                appendLine("PMm: ${diagnosticData.manufacturerPmmHex ?: "None"}")
                                appendLine("Service 0x220F Flags: SF1=${diagnosticData.statusFlag1}, SF2=${diagnosticData.statusFlag2}")
                                appendLine("Blocks Read: ${diagnosticData.blocksReadCount}, Valid Tx: ${diagnosticData.validTransactionCount}")
                                appendLine("Decoded Card: ${diagnosticData.decodedSnapshot?.cardType?.displayName}, Balance: ${diagnosticData.decodedSnapshot?.latestBalanceBdt}")
                                appendLine("RF Link OK: ${diagnosticData.rfLinkOk}")
                                if (diagnosticData.errorMessage != null) {
                                    appendLine("Trace: ${diagnosticData.errorMessage}")
                                }
                            }
                        }
                        copyPlainTextToClipboard(context, "TransitMate Diagnostics", report)
                        scope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                    },
                    modifier = Modifier.weight(1f)
                )

                SecondaryOutlinedButton(
                    text = stringResource(R.string.diagnostics_clear),
                    icon = Icons.Rounded.Close,
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        }
    }
}
