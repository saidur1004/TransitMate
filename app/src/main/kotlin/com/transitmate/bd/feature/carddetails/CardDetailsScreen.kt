package com.transitmate.bd.feature.carddetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.DeleteCardConfirmationDialog
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.RenameCardDialog
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitHeroCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.VerifiedTransaction
import java.text.DateFormat
import java.util.Date

@Composable
fun CardDetailsScreen(
    card: SavedTransitCard?,
    transactions: List<VerifiedTransaction>,
    onBack: () -> Unit,
    onRefreshScan: () -> Unit,
    onSetPrimary: (Long) -> Unit,
    onRenameCard: (Long, String, CardType) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onOpenCardHistory: (Long) -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = card?.nickname ?: stringResource(R.string.card_details_title),
                showBack = true,
                onBackClick = onBack,
                actions = {
                    if (card != null) {
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.dialog_rename_card_title)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = stringResource(R.string.card_details_remove),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (card == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Rounded.CreditCard,
                    title = stringResource(R.string.card_details_title),
                    message = stringResource(R.string.card_details_not_found),
                    actionLabel = stringResource(R.string.cd_navigate_back),
                    onAction = onBack
                )
            }
            return@Scaffold
        }

        val recognitionText = when (card.recognitionStatus) {
            CardRecognitionStatus.VERIFIED_RAPID_PASS -> CardType.RAPID_PASS.displayName
            CardRecognitionStatus.VERIFIED_MRT_PASS -> CardType.MRT_PASS.displayName
            CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_row_recognition_pending)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TransitHeroCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.nickname,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${card.rfTechnology} · $recognitionText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (card.isPrimary) {
                        StatusPill(
                            text = stringResource(R.string.home_primary_badge),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            icon = Icons.Rounded.Star
                        )
                    } else {
                        StatusPill(
                            text = stringResource(R.string.result_badge_unverified),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.result_balance_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (card.lastVerifiedBalanceBdt != null) {
                                Text(
                                    text = "৳ ${card.lastVerifiedBalanceBdt}",
                                    style = MaterialTheme.typography.displayMedium
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.result_balance_unavailable),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = semantic.warning,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.maskedIdentifier,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatusPill(
                        text = stringResource(R.string.result_masked_id),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = Icons.Rounded.Shield
                    )
                }
            }

            PrimaryActionButton(
                text = stringResource(R.string.card_details_refresh),
                icon = Icons.Rounded.Contactless,
                onClick = onRefreshScan
            )

            SurfaceSectionCard {
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_technology),
                    value = card.rfTechnology
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_recognition),
                    value = recognitionText
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.card_details_added_on),
                    value = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(card.addedAtMillis))
                )
                InfoKeyValueRow(
                    label = stringResource(R.string.result_row_last_scanned),
                    value = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(card.lastScannedAtMillis))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Switch) { onSetPrimary(card.id) }
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.card_details_set_primary),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = card.isPrimary,
                        onCheckedChange = { if (it) onSetPrimary(card.id) }
                    )
                }
            }

            SurfaceSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.card_details_activity_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { onOpenCardHistory(card.id) }) {
                        Text(stringResource(R.string.action_view_all))
                    }
                }
                if (transactions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.card_details_activity_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    transactions.take(3).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${tx.originStation ?: "—"} → ${tx.destinationStation ?: "—"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "৳ ${tx.amountBdt}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.card_details_remove),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (showRenameDialog && card != null) {
        RenameCardDialog(
            title = stringResource(R.string.dialog_rename_card_title),
            initialName = card.nickname,
            onSave = { newName ->
                showRenameDialog = false
                onRenameCard(card.id, newName, card.userSelectedType)
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog && card != null) {
        DeleteCardConfirmationDialog(
            cardName = card.nickname,
            onConfirmDelete = {
                showDeleteDialog = false
                onDeleteCard(card.id)
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
