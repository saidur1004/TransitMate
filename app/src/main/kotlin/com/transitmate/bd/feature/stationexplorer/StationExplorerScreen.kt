package com.transitmate.bd.feature.stationexplorer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.EmptyStateView
import com.transitmate.bd.core.designsystem.component.InfoKeyValueRow
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitTopBar
import com.transitmate.bd.domain.model.MetroStation
import com.transitmate.bd.domain.repository.TransitInfoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationExplorerScreen(
    transitInfoRepository: TransitInfoRepository,
    onBack: () -> Unit,
    onCalculateFareFromStation: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var terminalsOnly by remember { mutableStateOf(false) }
    var selectedStation by remember { mutableStateOf<MetroStation?>(null) }

    val allStations = remember { transitInfoRepository.getStations() }
    val isBangla = LocalConfiguration.current.locales[0]?.language == "bn"
    val filteredStations = remember(query, terminalsOnly) {
        transitInfoRepository.searchStations(query)
            .let { list -> if (terminalsOnly) list.filter { it.isTerminal } else list }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TransitTopBar(
                title = stringResource(R.string.stations_title),
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
            item {
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
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !terminalsOnly,
                        onClick = { terminalsOnly = false },
                        label = { Text(stringResource(R.string.stations_filter_all, allStations.size)) },
                        leadingIcon = if (!terminalsOnly) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null
                    )
                    FilterChip(
                        selected = terminalsOnly,
                        onClick = { terminalsOnly = true },
                        label = { Text(stringResource(R.string.stations_filter_terminals)) },
                        leadingIcon = if (terminalsOnly) {
                            { Icon(Icons.Rounded.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            if (filteredStations.isEmpty()) {
                item {
                    SurfaceSectionCard {
                        EmptyStateView(
                            icon = Icons.Rounded.Search,
                            title = stringResource(R.string.stations_empty_title),
                            message = stringResource(R.string.stations_empty_body, query),
                            actionLabel = stringResource(R.string.action_clear_search),
                            actionIcon = Icons.Rounded.Close,
                            onAction = {
                                query = ""
                                terminalsOnly = false
                            }
                        )
                    }
                }
            } else {
                item {
                    SurfaceSectionCard(contentPadding = 12.dp) {
                        filteredStations.forEachIndexed { index, station ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStation = station }
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Vertical transit line rail + station order node
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(14.dp)
                                                .background(
                                                    if (index > 0 && !terminalsOnly) {
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                    } else {
                                                        androidx.compose.ui.graphics.Color.Transparent
                                                    }
                                                )
                                        )
                                        Surface(
                                            modifier = Modifier.size(30.dp),
                                            shape = CircleShape,
                                            color = if (station.isTerminal) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.primaryContainer
                                            }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "%02d".format(station.order),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (station.isTerminal) {
                                                        MaterialTheme.colorScheme.onPrimary
                                                    } else {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    }
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(14.dp)
                                                .background(
                                                    if (index < filteredStations.lastIndex && !terminalsOnly) {
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                    } else {
                                                        androidx.compose.ui.graphics.Color.Transparent
                                                    }
                                                )
                                        )
                                    }

                                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (station.isTerminal) {
                                        StatusPill(
                                            text = stringResource(R.string.stations_terminal_badge),
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < filteredStations.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
        }
    }

    selectedStation?.let { station ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedStation = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBangla) station.nameBn else station.nameEn,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${if (isBangla) station.nameEn else station.nameBn} · ${stringResource(R.string.stations_line_subtitle)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    StatusPill(
                        text = stringResource(R.string.stations_number_badge, station.order),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                SurfaceSectionCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentPadding = 16.dp
                ) {
                    Text(
                        text = stringResource(R.string.stations_landmarks_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBangla) station.landmarksBn else station.landmarksEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBangla) station.landmarksEn else station.landmarksBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    InfoKeyValueRow(
                        label = stringResource(R.string.stations_distance_label),
                        value = stringResource(R.string.stations_distance_km, station.distanceKmFromStart),
                        showDivider = false
                    )
                }

                PrimaryActionButton(
                    text = stringResource(R.string.stations_calc_from_here),
                    icon = Icons.Rounded.Calculate,
                    onClick = {
                        selectedStation = null
                        onCalculateFareFromStation(station.code)
                    }
                )
            }
        }
    }
}
