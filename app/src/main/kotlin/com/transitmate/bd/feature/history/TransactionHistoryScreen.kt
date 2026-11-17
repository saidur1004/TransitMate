package com.transitmate.bd.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.component.VerifiedTransactionRow
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction

private enum class HistoryFilter { ALL, TRIPS, TOP_UPS }

@Composable
fun TransactionHistoryScreen(
    transactions: List<VerifiedTransaction>,
    savedCards: List<SavedTransitCard> = emptyList(),
    initialCardId: Long? = null,
    onBack: () -> Unit,
    onOpenScanner: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var selectedCardId by remember(initialCardId) { mutableStateOf(initialCardId) }

    val filtered = remember(transactions, selectedFilter, selectedCardId) {
        transactions
            .let { list -> if (selectedCardId != null) list.filter { it.cardId == selectedCardId } else list }
            .let { list ->
                when (selectedFilter) {
                    HistoryFilter.ALL -> list
                    HistoryFilter.TRIPS -> list.filter { it.type == TransactionType.TRIP }
                    HistoryFilter.TOP_UPS -> list.filter { it.type == TransactionType.TOP_UP }
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.history_title),
                showBack = true,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (savedCards.size > 1) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCardId == null,
                            onClick = { selectedCardId = null },
                            label = { Text(stringResource(R.string.history_filter_all)) },
                            leadingIcon = if (selectedCardId == null) {
                                { Icon(Icons.Rounded.Check, contentDescription = null) }
                            } else null
                        )
                        savedCards.forEach { card ->
                            val selected = selectedCardId == card.id
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCardId = if (selected) null else card.id },
                                label = { Text(card.nickname) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        HistoryFilter.ALL to stringResource(R.string.history_filter_all),
                        HistoryFilter.TRIPS to stringResource(R.string.history_filter_trips),
                        HistoryFilter.TOP_UPS to stringResource(R.string.history_filter_topups)
                    ).forEach { (filter, label) ->
                        val selected = selectedFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = filter },
                            label = { Text(label) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }

            item {
                val totalSpend = filtered.filter { it.type == TransactionType.TRIP }.sumOf { it.amountBdt ?: 0 }
                SurfaceSectionCard {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.history_verified_trips),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${filtered.size}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum")
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.history_recorded_spend),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (filtered.any { it.amountBdt != null }) "৳ $totalSpend" else "—",
                                style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                                color = if (filtered.isNotEmpty()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    StatusBannerCard(
                        severity = BannerSeverity.INFO,
                        icon = Icons.Rounded.Info,
                        title = stringResource(R.string.history_unavailable_title),
                        message = stringResource(R.string.history_unavailable_body)
                    )
                }
                item {
                    SurfaceSectionCard {
                        EmptyStateView(
                            icon = Icons.Rounded.History,
                            title = stringResource(R.string.history_empty_title),
                            message = stringResource(R.string.history_empty_body),
                            actionLabel = if ((selectedFilter != HistoryFilter.ALL || selectedCardId != null) && transactions.isNotEmpty()) {
                                stringResource(R.string.action_clear_filters)
                            } else {
                                stringResource(R.string.action_scan_card)
                            },
                            actionIcon = Icons.Rounded.Contactless,
                            onAction = {
                                if ((selectedFilter != HistoryFilter.ALL || selectedCardId != null) && transactions.isNotEmpty()) {
                                    selectedFilter = HistoryFilter.ALL
                                    selectedCardId = null
                                } else {
                                    onOpenScanner()
                                }
                            }
                        )
                    }
                }
            } else {
                items(filtered, key = { it.id }) { tx ->
                    SurfaceSectionCard(contentPadding = 12.dp) {
                        VerifiedTransactionRow(
                            transaction = tx,
                            showDivider = false
                        )
                    }
                }
            }
        }
    }
}
