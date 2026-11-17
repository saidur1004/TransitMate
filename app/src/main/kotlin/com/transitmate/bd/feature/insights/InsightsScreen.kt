package com.transitmate.bd.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.nfc.TransitStationDirectory
import com.transitmate.bd.domain.model.TransactionType
import com.transitmate.bd.domain.model.VerifiedTransaction

private data class RouteAggregate(
    val routeLabel: String,
    val tripCount: Int,
    val spentBdt: Int
)

@Composable
fun InsightsScreen(
    transactions: List<VerifiedTransaction>,
    onOpenHistory: () -> Unit,
    onOpenFareCalculator: () -> Unit,
    onOpenStationExplorer: () -> Unit
) {
    // Default to All Time so verified trips older than 30 days are immediately visible
    var allTime by remember { mutableStateOf(true) }
    val isBangla = LocalConfiguration.current.locales[0]?.language == "bn"

    val scoped = remember(transactions, allTime) {
        if (allTime) {
            transactions
        } else {
            val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            transactions.filter { it.timestampMillis >= cutoff }
        }
    }

    val trips = scoped.filter { it.type == TransactionType.TRIP }
    val topUps = scoped.filter { it.type == TransactionType.TOP_UP }
    val tripsWithFare = trips.mapNotNull { it.amountBdt }
    val totalSpent = tripsWithFare.sum()
    val totalTopUp = topUps.mapNotNull { it.amountBdt }.sum()
    val avgFare = if (tripsWithFare.isNotEmpty()) totalSpent / tripsWithFare.size else 0

    val unknownStationLabel = stringResource(R.string.tx_unknown_station)
    val routeBreakdown = remember(trips, isBangla, unknownStationLabel) {
        trips.groupBy { tx ->
            val from = TransitStationDirectory.resolveName(tx.originStationCode, tx.originStation, isBangla)
                ?: unknownStationLabel
            val to = TransitStationDirectory.resolveName(tx.destinationStationCode, tx.destinationStation, isBangla)
                ?: unknownStationLabel
            "$from → $to"
        }.map { (route, group) ->
            RouteAggregate(
                routeLabel = route,
                tripCount = group.size,
                spentBdt = group.sumOf { it.amountBdt ?: 0 }
            )
        }.sortedWith(compareByDescending<RouteAggregate> { it.tripCount }.thenByDescending { it.spentBdt })
            .take(5)
    }

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
                            value = if (tripsWithFare.isNotEmpty()) "৳ $totalSpent" else "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = stringResource(R.string.insights_metric_topups),
                            value = if (topUps.isNotEmpty()) "৳ $totalTopUp" else "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = stringResource(R.string.insights_metric_avg),
                            value = if (tripsWithFare.isNotEmpty()) "৳ $avgFare" else "—",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                SurfaceSectionCard {
                    if (routeBreakdown.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.BarChart,
                            title = stringResource(R.string.insights_empty_title),
                            message = stringResource(R.string.insights_empty_body)
                        )
                    } else {
                        val maxTrips = routeBreakdown.maxOf { it.tripCount }.coerceAtLeast(1)
                        Text(
                            text = stringResource(R.string.insights_top_routes),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            routeBreakdown.forEachIndexed { idx, item ->
                                val ratio = (item.tripCount.toFloat() / maxTrips.toFloat()).coerceIn(0.12f, 1f)
                                val barColor = if (idx % 2 == 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.routeLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.insights_route_trips,
                                                item.tripCount,
                                                item.spentBdt
                                            ),
                                            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = ratio)
                                                .fillMaxHeight()
                                                .clip(CircleShape)
                                                .background(barColor)
                                        )
                                    }
                                }
                            }
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
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
