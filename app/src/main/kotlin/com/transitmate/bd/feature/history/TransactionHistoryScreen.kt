package com.transitmate.bd.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.component.VerifiedTransactionRow
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction
import java.text.SimpleDateFormat
import java.util.Date

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

    val locale = LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
    val monthFormatter = remember(locale) { SimpleDateFormat("LLLL yyyy", locale) }
    val groupedByMonth = remember(filtered, locale) {
        filtered.groupBy { tx -> monthFormatter.format(Date(tx.timestampMillis)).uppercase(locale) }
    }
    val hasActiveFilter = selectedFilter != HistoryFilter.ALL || selectedCardId != null

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
        ResponsiveScreenContainer(
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.history_verified_trips),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${filtered.size}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum")
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.history_recorded_spend),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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

            if (transactions.isEmpty()) {
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
                            actionLabel = stringResource(R.string.action_scan_card),
                            actionIcon = Icons.Rounded.Contactless,
                            onAction = onOpenScanner
                        )
                    }
                }
            } else if (filtered.isEmpty() && hasActiveFilter) {
                item {
                    SurfaceSectionCard {
                        EmptyStateView(
                            icon = Icons.Rounded.History,
                            title = stringResource(R.string.history_filter_empty_title),
                            message = stringResource(R.string.history_filter_empty_body),
                            actionLabel = stringResource(R.string.action_clear_filters),
                            actionIcon = Icons.Rounded.Close,
                            onAction = {
                                selectedFilter = HistoryFilter.ALL
                                selectedCardId = null
                            }
                        )
                    }
                }
            } else {
                groupedByMonth.forEach { (monthHeader, monthItems) ->
                    item(key = "hdr_$monthHeader") {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = monthHeader,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item(key = "grp_$monthHeader") {
                        SurfaceSectionCard(contentPadding = 16.dp) {
                            monthItems.forEachIndexed { idx, tx ->
                                VerifiedTransactionRow(
                                    transaction = tx,
                                    showDivider = idx < monthItems.lastIndex
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
