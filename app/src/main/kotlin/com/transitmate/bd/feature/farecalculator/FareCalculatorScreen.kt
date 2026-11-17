package com.transitmate.bd.feature.farecalculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.TripOrigin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitHeroCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.MetroStation
import com.transitmate.bd.domain.repository.TransitInfoRepository

private enum class PickerTarget { ORIGIN, DESTINATION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareCalculatorScreen(
    transitInfoRepository: TransitInfoRepository,
    initialOriginCode: String?,
    onBack: () -> Unit
) {
    val stations = remember { transitInfoRepository.getStations() }
    var originCode by remember(initialOriginCode) { mutableStateOf(initialOriginCode) }
    var destinationCode by remember { mutableStateOf<String?>(null) }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    var swapTurns by remember { mutableIntStateOf(0) }
    val swapRotation by animateFloatAsState(
        targetValue = swapTurns * 180f,
        animationSpec = tween(220),
        label = "swapRotate"
    )

    val originStation = stations.firstOrNull { it.code == originCode }
    val destinationStation = stations.firstOrNull { it.code == destinationCode }
    val sameStation = originCode != null && originCode == destinationCode
    val result = remember(originCode, destinationCode) {
        if (originCode != null && destinationCode != null && originCode != destinationCode) {
            transitInfoRepository.calculateFare(originCode!!, destinationCode!!)
        } else {
            null
        }
    }

    val semantic = TransitMateThemeTokens.semantic
    val isBangla = LocalConfiguration.current.locales[0]?.language == "bn"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.fare_calc_title),
                showBack = true,
                onBackClick = onBack
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SurfaceSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StationPickerField(
                            label = stringResource(R.string.fare_calc_from),
                            station = originStation,
                            isBangla = isBangla,
                            icon = Icons.Rounded.TripOrigin,
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = { pickerTarget = PickerTarget.ORIGIN }
                        )
                        // Subtle 3-dot vertical route connector matching V3 mockup
                        Row(
                            modifier = Modifier.padding(start = 22.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.outline)
                                    )
                                }
                            }
                        }
                        StationPickerField(
                            label = stringResource(R.string.fare_calc_to),
                            station = destinationStation,
                            isBangla = isBangla,
                            icon = Icons.Rounded.Place,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            onClick = { pickerTarget = PickerTarget.DESTINATION }
                        )
                    }

                    // 48.dp Material 3 compliant swap button touch target
                    Surface(
                        onClick = {
                            val oldO = originCode
                            originCode = destinationCode
                            destinationCode = oldO
                            swapTurns++
                        },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = stringResource(R.string.cd_swap_stations),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(swapRotation)
                            )
                        }
                    }
                }
            }

            if (sameStation) {
                StatusBannerCard(
                    severity = BannerSeverity.WARNING,
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.fare_calc_title),
                    message = stringResource(R.string.fare_calc_same_station)
                )
            }

            AnimatedVisibility(visible = result != null) {
                result?.let { fare ->
                    TransitHeroCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.fare_calc_route_summary, fare.stationCount),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusPill(
                                text = stringResource(R.string.fare_calc_official_badge),
                                containerColor = semantic.successContainer,
                                contentColor = semantic.onSuccessContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = stringResource(R.string.fare_calc_pass_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳ ${fare.cardDiscountFareBdt}",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = stringResource(R.string.fare_calc_single_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "৳ ${fare.singleJourneyFareBdt}",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.fare_calc_discount_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (result == null && !sameStation) {
                SurfaceSectionCard {
                    EmptyStateView(
                        icon = Icons.Rounded.Calculate,
                        title = stringResource(R.string.fare_calc_empty_title),
                        message = stringResource(R.string.fare_calc_empty_body)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.fare_calc_offline_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }

    pickerTarget?.let { target ->
        ModalBottomSheet(
            onDismissRequest = { pickerTarget = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            StationPickerSheetContent(
                stations = stations,
                isBangla = isBangla,
                onSelect = { selected ->
                    if (target == PickerTarget.ORIGIN) originCode = selected.code
                    else destinationCode = selected.code
                    pickerTarget = null
                }
            )
        }
    }
}

@Composable
private fun StationPickerField(
    label: String,
    station: MetroStation?,
    isBangla: Boolean,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (station != null) {
                        if (isBangla) "${station.nameBn} · ${station.nameEn}"
                        else "${station.nameEn} · ${station.nameBn}"
                    } else {
                        stringResource(R.string.fare_calc_select_prompt)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (station != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun StationPickerSheetContent(
    stations: List<MetroStation>,
    isBangla: Boolean,
    onSelect: (MetroStation) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, stations) {
        val q = query.trim()
        if (q.isEmpty()) stations
        else stations.filter {
            it.nameEn.contains(q, ignoreCase = true) || it.nameBn.contains(q, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .padding(horizontal = 20.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.stations_search_hint)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cd_clear_search))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (filtered.isEmpty()) {
            EmptyStateView(
                icon = Icons.Rounded.Search,
                title = stringResource(R.string.stations_empty_title),
                message = stringResource(R.string.stations_empty_body, query),
                actionLabel = stringResource(R.string.action_clear_search),
                actionIcon = Icons.Rounded.Close,
                onAction = { query = "" }
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.code }) { station ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(station) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "%02d".format(station.order),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) station.nameBn else station.nameEn,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (isBangla) station.nameEn else station.nameBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
