package com.transitmate.bd.feature.mycards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.DeleteCardConfirmationDialog
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.RenameCardDialog
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.TransitHeroCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.CardType
import com.transitmate.bd.domain.model.SavedTransitCard

@Composable
fun MyCardsScreen(
    cards: List<SavedTransitCard>,
    onScanNewCard: () -> Unit,
    onCardClick: (Long) -> Unit,
    onSetPrimary: (Long) -> Unit,
    onRenameCard: (Long, String, CardType) -> Unit,
    onDeleteCard: (Long) -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic
    var renameTarget by remember { mutableStateOf<SavedTransitCard?>(null) }
    var deleteTarget by remember { mutableStateOf<SavedTransitCard?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.my_cards_title),
                showBack = false,
                actions = {
                    IconButton(onClick = onScanNewCard) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.my_cards_scan_add),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Rounded.CreditCard,
                    title = stringResource(R.string.my_cards_empty_title),
                    message = stringResource(R.string.my_cards_empty_body),
                    actionLabel = stringResource(R.string.my_cards_empty_cta),
                    actionIcon = Icons.Rounded.Contactless,
                    onAction = onScanNewCard
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.my_cards_saved_header, cards.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(cards, key = { it.id }) { card ->
                var menuExpanded by remember { mutableStateOf(false) }
                val isVerified = card.recognitionStatus != CardRecognitionStatus.UNVERIFIED
                val stripColor = when {
                    card.isPrimary -> MaterialTheme.colorScheme.primary
                    isVerified -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                }
                val recognitionText = when (card.recognitionStatus) {
                    CardRecognitionStatus.VERIFIED_RAPID_PASS -> CardType.RAPID_PASS.displayName
                    CardRecognitionStatus.VERIFIED_MRT_PASS -> CardType.MRT_PASS.displayName
                    CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_row_recognition_pending)
                }

                TransitHeroCard(
                    accentColor = stripColor,
                    onClick = { onCardClick(card.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = card.nickname,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (card.isPrimary) {
                                    StatusPill(
                                        text = stringResource(R.string.home_primary_badge),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else if (!isVerified) {
                                    StatusPill(
                                        text = stringResource(R.string.result_badge_unverified),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "$recognitionText · ${card.rfTechnology}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                if (!card.isPrimary) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.card_details_set_primary)) },
                                        onClick = {
                                            menuExpanded = false
                                            onSetPrimary(card.id)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.dialog_rename_card_title)) },
                                    onClick = {
                                        menuExpanded = false
                                        renameTarget = card
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.action_remove),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        deleteTarget = card
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.cardIdm,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (card.lastVerifiedBalanceBdt != null) {
                            Text(
                                text = "৳ ${card.lastVerifiedBalanceBdt}",
                                style = MaterialTheme.typography.titleSmall,
                                color = semantic.success
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.result_balance_unavailable),
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.warning
                            )
                        }
                    }
                }
            }

            item {
                FilledTonalButton(
                    onClick = onScanNewCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Contactless,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.my_cards_scan_add))
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.my_cards_privacy_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    renameTarget?.let { target ->
        RenameCardDialog(
            title = stringResource(R.string.dialog_rename_card_title),
            initialName = target.nickname,
            onSave = { newName ->
                onRenameCard(target.id, newName, target.userSelectedType)
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { target ->
        DeleteCardConfirmationDialog(
            cardName = target.nickname,
            onConfirmDelete = {
                onDeleteCard(target.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
