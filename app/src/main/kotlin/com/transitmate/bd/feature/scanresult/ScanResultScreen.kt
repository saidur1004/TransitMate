package com.transitmate.bd.feature.scanresult

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.RenameCardDialog
import com.transitmate.bd.core.designsystem.component.SecondaryOutlinedButton
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitHeroCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import java.text.DateFormat
import java.util.Date

@Composable
fun ScanResultScreen(
    outcome: CardScanOutcome.Detected?,
    savedCardId: Long?,
    onSaveCard: (nickname: String, cardType: CardType) -> Unit,
    onViewSavedCard: (Long) -> Unit,
    onScanAnother: () -> Unit,
    onOpenHelp: () -> Unit,
    onBack: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.result_title),
                showBack = true,
                onBackClick = onBack,
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
        if (outcome == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Rounded.Contactless,
                    title = stringResource(R.string.scanner_waiting_title),
                    message = stringResource(R.string.scanner_waiting_subtitle),
                    actionLabel = stringResource(R.string.action_scan_card),
                    actionIcon = Icons.Rounded.Contactless,
                    onAction = onScanAnother
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusBannerCard(
                severity = BannerSeverity.SUCCESS,
                icon = Icons.Rounded.CheckCircle,
                title = stringResource(R.string.result_banner_detected),
                message = stringResource(R.string.result_row_technology) + ": " + outcome.rfTechnology
            )

            // Card Preview — Honest Recognition Separation
            val recognitionBadgeText = when (outcome.recognitionStatus) {
                CardRecognitionStatus.VERIFIED_RAPID_PASS -> CardType.RAPID_PASS.displayName
                CardRecognitionStatus.VERIFIED_MRT_PASS -> CardType.MRT_PASS.displayName
                CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_badge_unverified)
            }

            TransitHeroCard(accentColor = MaterialTheme.colorScheme.secondary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.result_card_detected_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = outcome.rfTechnology,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusPill(
                        text = recognitionBadgeText,
                        containerColor = if (outcome.recognitionStatus == CardRecognitionStatus.UNVERIFIED) {
                            semantic.warningContainer
                        } else {
                            semantic.successContainer
                        },
                        contentColor = if (outcome.recognitionStatus == CardRecognitionStatus.UNVERIFIED) {
                            semantic.onWarningContainer
                        } else {
                            semantic.onSuccessContainer
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.result_masked_id),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = outcome.maskedIdentifier ?: stringResource(R.string.home_balance_unavailable),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Honest Balance Availability Card
            SurfaceSectionCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (outcome.balanceAvailability is BalanceAvailability.Verified) {
                                    semantic.successContainer
                                } else {
                                    semantic.warningContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (outcome.balanceAvailability is BalanceAvailability.Verified) {
                                semantic.onSuccessContainer
                            } else {
                                semantic.onWarningContainer
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.result_balance_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        when (val bal = outcome.balanceAvailability) {
                            is BalanceAvailability.Verified -> {
                                Text(
                                    text = "৳ ${bal.amountBdt}",
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                            is BalanceAvailability.Unavailable -> {
                                Text(
                                    text = stringResource(R.string.result_balance_unavailable),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.result_balance_unavailable_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Clean Consumer Metadata Rows (Zero System Code / PMm / RF traces!)
            SurfaceSectionCard(contentPadding = 16.dp) {
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_detection),
                    value = stringResource(R.string.result_row_detection_ok),
                    valueColor = semantic.success
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_technology),
                    value = outcome.rfTechnology
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_recognition),
                    value = recognitionBadgeText
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_last_scanned),
                    value = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(outcome.scannedAtMillis)),
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (savedCardId != null) {
                PrimaryActionButton(
                    text = stringResource(R.string.result_saved_view_card),
                    icon = Icons.Rounded.CreditCard,
                    onClick = { onViewSavedCard(savedCardId) }
                )
            } else {
                PrimaryActionButton(
                    text = stringResource(R.string.result_save_card),
                    icon = Icons.Rounded.Add,
                    onClick = { showSaveDialog = true }
                )
            }

            SecondaryOutlinedButton(
                text = stringResource(R.string.result_scan_another),
                icon = Icons.Rounded.Contactless,
                onClick = onScanAnother
            )
        }
    }

    if (showSaveDialog) {
        RenameCardDialog(
            title = stringResource(R.string.dialog_save_card_title),
            initialName = stringResource(R.string.default_card_nickname),
            onSave = { nickname ->
                showSaveDialog = false
                onSaveCard(nickname, CardType.UNKNOWN)
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}
