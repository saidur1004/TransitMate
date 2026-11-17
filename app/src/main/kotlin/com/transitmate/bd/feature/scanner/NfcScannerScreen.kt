package com.transitmate.bd.feature.scanner

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.ErrorStateView
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.SecondaryOutlinedButton
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.feature.main.ScannerSessionPhase
import com.transitmate.bd.feature.onboarding.NfcAlignmentGraphic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScannerScreen(
    nfcStatus: NfcHardwareStatus,
    scannerPhase: ScannerSessionPhase,
    activeErrorOutcome: CardScanOutcome?,
    onPauseScan: () -> Unit,
    onResumeScan: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenOfflineTools: () -> Unit,
    onDismissError: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.scanner_title),
                showBack = false,
                actions = {
                    IconButton(onClick = onOpenHelp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = stringResource(R.string.cd_help)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier.padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (nfcStatus) {
                NfcHardwareStatus.NOT_SUPPORTED -> {
                    Spacer(modifier = Modifier.weight(1f, fill = false))
                    SurfaceSectionCard(contentPadding = 12.dp) {
                        EmptyStateView(
                            icon = Icons.Rounded.WifiOff,
                            title = stringResource(R.string.nfc_banner_unsupported_title),
                            message = stringResource(R.string.nfc_banner_unsupported_body),
                            actionLabel = stringResource(R.string.fare_calc_title),
                            actionIcon = Icons.Rounded.Calculate,
                            onAction = onOpenOfflineTools
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f, fill = false))
                }

                NfcHardwareStatus.DISABLED -> {
                    SurfaceSectionCard(contentPadding = 12.dp) {
                        ErrorStateView(
                            icon = Icons.Rounded.WarningAmber,
                            title = stringResource(R.string.nfc_banner_disabled_title),
                            message = stringResource(R.string.nfc_banner_disabled_body),
                            isWarning = true,
                            primaryActionLabel = stringResource(R.string.action_open_nfc_settings),
                            onPrimaryAction = onOpenNfcSettings,
                            secondaryActionLabel = stringResource(R.string.fare_calc_title),
                            onSecondaryAction = onOpenOfflineTools
                        )
                    }
                    SupportedCardsTipCard()
                }

                NfcHardwareStatus.AVAILABLE -> {
                    val pulseAlpha = if (scannerPhase == ScannerSessionPhase.WAITING ||
                        scannerPhase == ScannerSessionPhase.READING
                    ) {
                        val transition = rememberInfiniteTransition(label = "nfcPulse")
                        val anim by transition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 0.45f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1600, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )
                        anim
                    } else {
                        1f
                    }

                    StatusPill(
                        text = stringResource(R.string.scanner_ready_pill),
                        containerColor = semantic.successContainer,
                        contentColor = semantic.onSuccessContainer,
                        dotColor = semantic.success
                    )

                    SurfaceSectionCard(contentPadding = 24.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.alpha(pulseAlpha)) {
                                NfcAlignmentGraphic()
                            }

                            when (scannerPhase) {
                                ScannerSessionPhase.WAITING -> {
                                    Text(
                                        text = stringResource(R.string.scanner_waiting_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = stringResource(R.string.scanner_waiting_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    StatusPill(
                                        text = stringResource(R.string.scanner_waiting_chip),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        dotColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                                ScannerSessionPhase.READING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = stringResource(R.string.scanner_reading_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = stringResource(R.string.scanner_reading_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                ScannerSessionPhase.DETECTED -> {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = semantic.success,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.scanner_detected_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = semantic.success,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = stringResource(R.string.scanner_detected_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ScannerSessionPhase.PAUSED -> {
                                    Text(
                                        text = stringResource(R.string.scanner_paused_chip),
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = stringResource(R.string.scanner_waiting_subtitle),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    SupportedCardsTipCard()

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    if (scannerPhase == ScannerSessionPhase.PAUSED) {
                        PrimaryActionButton(
                            text = stringResource(R.string.scanner_resume),
                            icon = Icons.Rounded.Contactless,
                            onClick = onResumeScan
                        )
                    } else {
                        SecondaryOutlinedButton(
                            text = stringResource(R.string.scanner_cancel),
                            icon = Icons.Rounded.Close,
                            onClick = onPauseScan,
                            enabled = scannerPhase == ScannerSessionPhase.WAITING
                        )
                    }
                }
            }
        }
        }
    }

    if (activeErrorOutcome != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissError,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ScannerErrorSheetContent(
                outcome = activeErrorOutcome,
                onRetry = onDismissError,
                onOpenHelp = {
                    onDismissError()
                    onOpenHelp()
                }
            )
        }
    }
}

@Composable
private fun ScannerErrorSheetContent(
    outcome: CardScanOutcome,
    onRetry: () -> Unit,
    onOpenHelp: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    val icon: ImageVector
    val badgeBg: androidx.compose.ui.graphics.Color
    val badgeFg: androidx.compose.ui.graphics.Color
    val title: String
    val body: String

    when (outcome) {
        is CardScanOutcome.UnsupportedCard -> {
            icon = Icons.Rounded.CreditCard
            badgeBg = MaterialTheme.colorScheme.errorContainer
            badgeFg = MaterialTheme.colorScheme.onErrorContainer
            title = stringResource(R.string.scanner_error_unsupported_title)
            val techLabel = outcome.detectedTechs.joinToString(", ").ifBlank { "ISO 14443" }
            body = stringResource(R.string.scanner_error_unsupported_body, techLabel)
        }
        is CardScanOutcome.CardRemovedEarly -> {
            icon = Icons.Rounded.WarningAmber
            badgeBg = semantic.warningContainer
            badgeFg = semantic.onWarningContainer
            title = stringResource(R.string.scanner_error_tag_lost_title)
            body = stringResource(R.string.scanner_error_tag_lost_body)
        }
        is CardScanOutcome.IdentifierUnavailable -> {
            icon = Icons.Rounded.VisibilityOff
            badgeBg = semantic.warningContainer
            badgeFg = semantic.onWarningContainer
            title = stringResource(R.string.scanner_error_id_title)
            body = stringResource(R.string.scanner_error_id_body)
        }
        is CardScanOutcome.CommunicationFailure -> {
            icon = Icons.Rounded.ErrorOutline
            badgeBg = MaterialTheme.colorScheme.errorContainer
            badgeFg = MaterialTheme.colorScheme.onErrorContainer
            title = stringResource(R.string.scanner_error_comm_title)
            body = stringResource(R.string.scanner_error_comm_body)
        }
        is CardScanOutcome.Detected -> {
            icon = Icons.Rounded.Contactless
            badgeBg = semantic.warningContainer
            badgeFg = semantic.onWarningContainer
            title = stringResource(R.string.scanner_error_unknown_card_title)
            body = stringResource(R.string.scanner_error_unknown_card_body)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = badgeBg,
                border = BorderStroke(1.dp, badgeFg.copy(alpha = 0.25f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = badgeFg)
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PrimaryActionButton(
            text = stringResource(R.string.action_try_again),
            onClick = onRetry
        )
        SecondaryOutlinedButton(
            text = stringResource(R.string.scanner_antenna_guide),
            onClick = onOpenHelp
        )
    }
}

@Composable
private fun SupportedCardsTipCard() {
    SurfaceSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 16.dp
    ) {
        Text(
            text = stringResource(R.string.scanner_supported_header),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.scanner_supported_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.scanner_supported_tip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
