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
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.FullIdmRow
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.RenameCardDialog
import com.transitmate.bd.core.designsystem.component.SecondaryOutlinedButton
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitHeroCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.component.VerifiedTransactionRow
import com.transitmate.bd.core.designsystem.component.copyPlainTextToClipboard
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.BalanceAvailability
import com.transitmate.bd.domain.model.BalanceUnavailableReason
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardScanOutcome
import com.transitmate.bd.domain.model.CardType
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSaveDialog by remember { mutableStateOf(false) }
    val copiedMsg = stringResource(R.string.result_idm_copied)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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

        val isVerifiedCard = outcome.recognitionStatus != CardRecognitionStatus.UNVERIFIED
        val cardHeadline = when (outcome.recognitionStatus) {
            CardRecognitionStatus.VERIFIED_RAPID_PASS -> CardType.RAPID_PASS.displayName
            CardRecognitionStatus.VERIFIED_MRT_PASS -> CardType.MRT_PASS.displayName
            CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_card_detected_title)
        }
        val cardSubheadline = when (outcome.recognitionStatus) {
            CardRecognitionStatus.VERIFIED_RAPID_PASS -> "DTCA · ${outcome.rfTechnology}"
            CardRecognitionStatus.VERIFIED_MRT_PASS -> "DMTCL · ${outcome.rfTechnology}"
            CardRecognitionStatus.UNVERIFIED -> outcome.rfTechnology
        }
        val recognitionBadgeText = when (outcome.recognitionStatus) {
            CardRecognitionStatus.VERIFIED_RAPID_PASS,
            CardRecognitionStatus.VERIFIED_MRT_PASS -> stringResource(R.string.result_badge_verified)
            CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_badge_unverified)
        }
        val cardStripeColor = when (outcome.recognitionStatus) {
            CardRecognitionStatus.VERIFIED_RAPID_PASS -> MaterialTheme.colorScheme.secondary
            CardRecognitionStatus.VERIFIED_MRT_PASS -> MaterialTheme.colorScheme.primary
            CardRecognitionStatus.UNVERIFIED -> MaterialTheme.colorScheme.outline
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Pass Card: Card Identity + Focal Balance + Full IDm
            TransitHeroCard(accentColor = cardStripeColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cardHeadline,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = cardSubheadline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusPill(
                        text = recognitionBadgeText,
                        containerColor = if (isVerifiedCard) semantic.successContainer else semantic.warningContainer,
                        contentColor = if (isVerifiedCard) semantic.onSuccessContainer else semantic.onWarningContainer,
                        icon = if (isVerifiedCard) Icons.Rounded.Verified else null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Focal Balance Showcase inside the Card Hero
                when (val bal = outcome.balanceAvailability) {
                    is BalanceAvailability.Verified -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.result_balance_verified_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳ ${bal.amountBdt}",
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 36.sp,
                                            lineHeight = 42.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(semantic.successContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = semantic.onSuccessContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                    is BalanceAvailability.Unavailable -> {
                        val reasonDesc = when (bal.reason) {
                            BalanceUnavailableReason.PROTECTED_OR_UNREADABLE ->
                                stringResource(R.string.result_balance_protected_desc)
                            BalanceUnavailableReason.READ_EMPTY_NO_HISTORY ->
                                stringResource(R.string.result_balance_empty_desc)
                            BalanceUnavailableReason.UNVERIFIED_CARD_FORMAT ->
                                stringResource(R.string.result_balance_unavailable_desc)
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(semantic.warningContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = semantic.onWarningContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.result_balance_unavailable),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = reasonDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                val idmText = outcome.cardIdm ?: stringResource(R.string.home_balance_unavailable)
                FullIdmRow(
                    cardIdm = idmText,
                    onCopy = outcome.cardIdm?.let { fullIdm ->
                        {
                            copyPlainTextToClipboard(context, "TransitMate Card IDm", fullIdm)
                            scope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                        }
                    }
                )
            }

            // Extracted Card Transactions Preview (Directly on Scan Result!)
            if (outcome.parsedTransactions.isNotEmpty()) {
                SurfaceSectionCard(contentPadding = 16.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.result_activity_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        StatusPill(
                            text = stringResource(R.string.result_activity_records, outcome.parsedTransactions.size),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val previewList = outcome.parsedTransactions.take(5)
                    previewList.forEachIndexed { index, tx ->
                        VerifiedTransactionRow(
                            transaction = tx,
                            showDivider = index < previewList.lastIndex
                        )
                    }
                }
            }

            // Concise Card Metadata Summary (No duplicate rows, zero raw RF tech trace)
            SurfaceSectionCard(contentPadding = 16.dp) {
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_recognition),
                    value = when (outcome.recognitionStatus) {
                        CardRecognitionStatus.VERIFIED_RAPID_PASS -> CardType.RAPID_PASS.displayName
                        CardRecognitionStatus.VERIFIED_MRT_PASS -> CardType.MRT_PASS.displayName
                        CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_row_recognition_pending)
                    },
                    valueColor = if (isVerifiedCard) semantic.success else MaterialTheme.colorScheme.onSurface
                )
                if (savedCardId != null) {
                    InfoKeyValueRow(
                        label = stringResource(R.string.card_details_title),
                        value = stringResource(R.string.result_saved_badge),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_last_scanned),
                    value = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(outcome.scannedAtMillis)),
                    showDivider = false
                )
            }

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

    if (showSaveDialog && outcome != null) {
        val suggestedName = when (outcome.cardType) {
            CardType.RAPID_PASS -> CardType.RAPID_PASS.displayName
            CardType.MRT_PASS -> CardType.MRT_PASS.displayName
            CardType.UNKNOWN -> stringResource(R.string.default_card_nickname)
        }
        RenameCardDialog(
            title = stringResource(R.string.dialog_save_card_title),
            initialName = suggestedName,
            onSave = { nickname ->
                showSaveDialog = false
                onSaveCard(nickname, outcome.cardType)
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}
