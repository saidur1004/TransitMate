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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.MetricStatTile
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
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
                showBack = false
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = allTime,
                        onClick = { allTime = true },
                        label = { Text(stringResource(R.string.insights_period_all)) },
                        leadingIcon = if (allTime) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null
                    )
                    FilterChip(
                        selected = !allTime,
                        onClick = { allTime = false },
                        label = { Text(stringResource(R.string.insights_period_month)) },
                        leadingIcon = if (!allTime) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatTile(
                        label = stringResource(R.string.insights_metric_trips),
                        value = if (trips.isNotEmpty()) "${trips.size}" else "—",
                        centered = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatTile(
                        label = stringResource(R.string.insights_metric_spent),
                        value = if (tripsWithFare.isNotEmpty()) "৳ $totalSpent" else "—",
                        centered = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatTile(
                        label = stringResource(R.string.insights_metric_topups),
                        value = if (topUps.isNotEmpty()) "৳ $totalTopUp" else "—",
                        centered = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatTile(
                        label = stringResource(R.string.insights_metric_avg),
                        value = if (tripsWithFare.isNotEmpty()) "৳ $avgFare" else "—",
                        centered = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                SurfaceSectionCard {
                    if (routeBreakdown.isEmpty()) {
                        val is30dFilteredOut = !allTime && transactions.isNotEmpty()
                        EmptyStateView(
                            icon = Icons.Rounded.BarChart,
                            title = if (is30dFilteredOut) {
                                stringResource(R.string.insights_empty_30d_title)
                            } else {
                                stringResource(R.string.insights_empty_title)
                            },
                            message = if (is30dFilteredOut) {
                                stringResource(R.string.insights_empty_30d_body)
                            } else {
                                stringResource(R.string.insights_empty_body)
                            },
                            actionLabel = if (is30dFilteredOut) stringResource(R.string.insights_switch_all_time) else null,
                            onAction = if (is30dFilteredOut) ({ allTime = true }) else null
                        )
                    } else {
                        val maxTrips = routeBreakdown.maxOf { it.tripCount }.coerceAtLeast(1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.insights_top_routes),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (allTime) {
                                Text(
                                    text = stringResource(R.string.insights_all_verified),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
