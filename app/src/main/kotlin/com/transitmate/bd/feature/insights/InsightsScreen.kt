package com.transitmate.bd.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.component.VerifiedTransactionRow
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction

@Composable
fun InsightsScreen(
    transactions: List<VerifiedTransaction>,
    onOpenHistory: () -> Unit,
    onOpenFareCalculator: () -> Unit,
    onOpenStationExplorer: () -> Unit
) {
    var allTime by remember { mutableStateOf(false) }

    val scoped = remember(transactions, allTime) {
        if (allTime) transactions
        else {
            val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            transactions.filter { it.timestampMillis >= cutoff }
        }
    }

    val trips = scoped.filter { it.type == TransactionType.TRIP }
    val tripsWithFare = trips.mapNotNull { it.amountBdt }
    val totalSpent = tripsWithFare.sum()
    val avgFare = if (tripsWithFare.isNotEmpty()) totalSpent / tripsWithFare.size else 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.insights_title),
                showBack = false,
                actions = {
                    FilterChip(
                        selected = allTime,
                        onClick = { allTime = !allTime },
                        label = {
                            Text(
                                text = if (allTime) {
                                    stringResource(R.string.insights_period_all)
                                } else {
                                    stringResource(R.string.insights_period_month)
                                }
                            )
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SurfaceSectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            label = stringResource(R.string.insights_metric_trips),
                            value = if (trips.isNotEmpty()) "${trips.size}" else "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = stringResource(R.string.insights_metric_spent),
                            value = if (trips.isNotEmpty()) "৳ $totalSpent" else "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = stringResource(R.string.insights_metric_avg),
                            value = if (trips.isNotEmpty()) "৳ $avgFare" else "—",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                SurfaceSectionCard {
                    if (trips.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.BarChart,
                            title = stringResource(R.string.insights_empty_title),
                            message = stringResource(R.string.insights_empty_body)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.insights_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val previewTrips = trips.take(5)
                        previewTrips.forEachIndexed { index, tx ->
                            VerifiedTransactionRow(
                                transaction = tx,
                                showDivider = index < previewTrips.lastIndex
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.insights_tools_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                SurfaceSectionCard(contentPadding = 4.dp) {
                    ToolRow(
                        icon = Icons.Rounded.History,
                        title = stringResource(R.string.history_title),
                        subtitle = stringResource(R.string.history_verified_trips),
                        badgeBg = MaterialTheme.colorScheme.primaryContainer,
                        badgeFg = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onOpenHistory
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ToolRow(
                        icon = Icons.Rounded.Calculate,
                        title = stringResource(R.string.fare_calc_title),
                        subtitle = stringResource(R.string.home_fare_calculator_sub),
                        badgeBg = MaterialTheme.colorScheme.secondaryContainer,
                        badgeFg = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onOpenFareCalculator
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ToolRow(
                        icon = Icons.Rounded.DirectionsSubway,
                        title = stringResource(R.string.stations_title),
                        subtitle = stringResource(R.string.home_station_explorer_sub),
                        badgeBg = MaterialTheme.colorScheme.surfaceVariant,
                        badgeFg = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenStationExplorer
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ToolRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeBg: Color,
    badgeFg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = badgeFg, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
