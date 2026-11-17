package com.transitmate.bd.feature.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.AppLogoMark
import com.transitmate.bd.core.designsystem.component.BannerSeverity
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.StatusBannerCard
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import com.transitmate.bd.core.designsystem.component.TransitHeroCard
import com.transitmate.bd.core.designsystem.component.VerifiedTransactionRow
import com.transitmate.bd.core.designsystem.theme.TransitMateThemeTokens
import com.transitmate.bd.domain.model.CardRecognitionStatus
import com.transitmate.bd.domain.model.NfcHardwareStatus
import com.transitmate.bd.domain.model.SavedTransitCard
import com.transitmate.bd.domain.model.VerifiedTransaction
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    nfcStatus: NfcHardwareStatus,
    primaryCard: SavedTransitCard?,
    savedCardCount: Int,
    recentTransactions: List<VerifiedTransaction>,
    onScanClick: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onPrimaryCardClick: (Long) -> Unit,
    onOpenMyCards: () -> Unit,
    onOpenFareCalculator: () -> Unit,
    onOpenStationExplorer: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val semantic = TransitMateThemeTokens.semantic

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Brand Header (Zero profile avatar, zero fake greeting)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppLogoMark(size = 32.dp, cornerRadius = 8.dp)
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                when (nfcStatus) {
                    NfcHardwareStatus.AVAILABLE -> StatusPill(
                        text = stringResource(R.string.nfc_status_ready),
                        containerColor = semantic.successContainer,
                        contentColor = semantic.onSuccessContainer,
                        dotColor = semantic.success
                    )
                    NfcHardwareStatus.DISABLED -> StatusPill(
                        text = stringResource(R.string.nfc_status_disabled),
                        containerColor = semantic.warningContainer,
                        contentColor = semantic.onWarningContainer,
                        dotColor = semantic.warning
                    )
                    NfcHardwareStatus.NOT_SUPPORTED -> StatusPill(
                        text = stringResource(R.string.nfc_status_unsupported),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        dotColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Conditional NFC Disabled / Unsupported Banner
        if (nfcStatus == NfcHardwareStatus.DISABLED) {
            item {
                StatusBannerCard(
                    severity = BannerSeverity.WARNING,
                    icon = Icons.Rounded.WarningAmber,
                    title = stringResource(R.string.nfc_banner_disabled_title),
                    message = stringResource(R.string.nfc_banner_disabled_body),
                    actionLabel = stringResource(R.string.action_open_nfc_settings),
                    onActionClick = onOpenNfcSettings
                )
            }
        } else if (nfcStatus == NfcHardwareStatus.NOT_SUPPORTED) {
            item {
                StatusBannerCard(
                    severity = BannerSeverity.INFO,
                    icon = Icons.Rounded.WifiOff,
                    title = stringResource(R.string.nfc_banner_unsupported_title),
                    message = stringResource(R.string.nfc_banner_unsupported_body)
                )
            }
        }

        // Primary Transit Card Hero
        item {
            if (primaryCard != null) {
                val isVerifiedCard = primaryCard.recognitionStatus != CardRecognitionStatus.UNVERIFIED
                val recognitionLabel = when (primaryCard.recognitionStatus) {
                    CardRecognitionStatus.VERIFIED_RAPID_PASS -> "Rapid Pass · DTCA"
                    CardRecognitionStatus.VERIFIED_MRT_PASS -> "MRT Pass · DMTCL"
                    CardRecognitionStatus.UNVERIFIED -> stringResource(R.string.result_row_recognition_pending)
                }
                val accentColor = when (primaryCard.recognitionStatus) {
                    CardRecognitionStatus.VERIFIED_RAPID_PASS -> MaterialTheme.colorScheme.secondary
                    CardRecognitionStatus.VERIFIED_MRT_PASS -> MaterialTheme.colorScheme.primary
                    CardRecognitionStatus.UNVERIFIED -> MaterialTheme.colorScheme.outline
                }
                TransitHeroCard(
                    accentColor = accentColor,
                    onClick = { onPrimaryCardClick(primaryCard.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = primaryCard.nickname,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = recognitionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (primaryCard.recognitionStatus == CardRecognitionStatus.VERIFIED_RAPID_PASS) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusPill(
                            text = stringResource(R.string.home_primary_badge),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (primaryCard.lastVerifiedBalanceBdt != null) {
                                    stringResource(R.string.result_balance_verified_label)
                                } else {
                                    stringResource(R.string.home_balance_label)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            if (primaryCard.lastVerifiedBalanceBdt != null) {
                                Text(
                                    text = "৳ ${primaryCard.lastVerifiedBalanceBdt}",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 34.sp,
                                        lineHeight = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.home_balance_unavailable),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.home_balance_unavailable_note),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (primaryCard.lastVerifiedBalanceBdt != null && isVerifiedCard) {
                            StatusPill(
                                text = stringResource(R.string.result_badge_verified),
                                containerColor = semantic.successContainer,
                                contentColor = semantic.onSuccessContainer,
                                icon = Icons.Rounded.Verified
                            )
                        } else if (primaryCard.lastVerifiedBalanceBdt == null) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = semantic.warning,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = primaryCard.cardIdm,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                .format(Date(primaryCard.lastScannedAtMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (savedCardCount > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onOpenMyCards) {
                            Text(text = stringResource(R.string.home_manage_all_cards, savedCardCount))
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                TransitHeroCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_no_card_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_no_card_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Primary Scan Action
        item {
            PrimaryActionButton(
                text = stringResource(R.string.action_scan_card),
                icon = Icons.Rounded.Contactless,
                onClick = onScanClick
            )
        }

        // Quick Offline Tools Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SurfaceSectionCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp,
                    onClick = onOpenFareCalculator
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.fare_calc_title),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.home_fare_calculator_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                SurfaceSectionCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp,
                    onClick = onOpenStationExplorer
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsSubway,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.stations_title),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.home_station_explorer_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Recent Activity Preview
        item {
            SurfaceSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_recent_activity),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onOpenHistory) {
                        Text(text = stringResource(R.string.action_view_all))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (recentTransactions.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_recent_empty_title),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(R.string.home_recent_empty_body),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val previewTx = recentTransactions.take(3)
                    previewTx.forEachIndexed { index, tx ->
                        VerifiedTransactionRow(
                            transaction = tx,
                            showDivider = index < previewTx.lastIndex
                        )
                    }
                }
            }
        }

        // Offline Privacy Footnote
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 8.dp),
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
                    text = stringResource(R.string.home_offline_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
