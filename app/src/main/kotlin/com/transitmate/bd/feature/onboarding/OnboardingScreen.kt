package com.transitmate.bd.feature.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.PrimaryActionButton
import com.transitmate.bd.core.designsystem.component.ResponsiveScreenContainer
import com.transitmate.bd.core.designsystem.component.StatusPill
import com.transitmate.bd.core.designsystem.component.SurfaceSectionCard
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val titleRes: Int,
    val bodyRes: Int,
    val pillRes: Int,
    val icon: ImageVector,
    val pillIcon: ImageVector,
    val isPhoneDiagram: Boolean
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            titleRes = R.string.onboarding_slide_1_title,
            bodyRes = R.string.onboarding_slide_1_body,
            pillRes = R.string.offline_readonly_badge,
            icon = Icons.Rounded.Contactless,
            pillIcon = Icons.Rounded.Contactless,
            isPhoneDiagram = true
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_slide_2_title,
            bodyRes = R.string.onboarding_slide_2_body,
            pillRes = R.string.onboarding_pill_storage,
            icon = Icons.Rounded.Shield,
            pillIcon = Icons.Rounded.Shield,
            isPhoneDiagram = false
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_slide_3_title,
            bodyRes = R.string.onboarding_slide_3_body,
            pillRes = R.string.onboarding_pill_tools,
            icon = Icons.Rounded.DirectionsSubway,
            pillIcon = Icons.Rounded.Calculate,
            isPhoneDiagram = false
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onFinishOnboarding) {
                        Text(
                            text = stringResource(R.string.action_skip),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier.padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SurfaceSectionCard(
                        contentPadding = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (page.isPhoneDiagram) {
                                NfcAlignmentGraphic()
                            } else {
                                // Concentric orbital frame matching V3 slide visual parity
                                Box(
                                    modifier = Modifier.size(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.size(152.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.outline
                                        )
                                    ) {}
                                    Surface(
                                        modifier = Modifier.size(110.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = page.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(46.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            StatusPill(
                                text = stringResource(page.pillRes),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                dotColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(page.titleRes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(page.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateContentSize()
                ) {
                    repeat(pages.size) { idx ->
                        val active = idx == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (active) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                PrimaryActionButton(
                    text = if (isLastPage) {
                        stringResource(R.string.action_get_started)
                    } else {
                        stringResource(R.string.action_next)
                    },
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = {
                        if (isLastPage) {
                            onFinishOnboarding()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    }
                )
            }
        }
        }
    }
}

@Composable
fun NfcAlignmentGraphic(
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier.size(width = 210.dp, height = 160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Restrained guide circles (zero neon glow)
            drawCircle(
                color = primaryColor.copy(alpha = 0.14f),
                radius = h * 0.44f,
                center = Offset(w * 0.48f, h * 0.5f),
                style = Stroke(width = 3f)
            )

            // Transit card positioned behind upper-right of handset
            drawRoundRect(
                color = containerColor,
                topLeft = Offset(w * 0.46f, h * 0.22f),
                size = Size(w * 0.42f, h * 0.38f),
                cornerRadius = CornerRadius(18f, 18f)
            )
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(w * 0.46f, h * 0.22f),
                size = Size(w * 0.42f, h * 0.38f),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 3f)
            )

            // Handset silhouette
            drawRoundRect(
                color = surfaceColor,
                topLeft = Offset(w * 0.26f, h * 0.08f),
                size = Size(w * 0.36f, h * 0.84f),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(w * 0.26f, h * 0.08f),
                size = Size(w * 0.36f, h * 0.84f),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 3.5f)
            )
            // Antenna zone target
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.65f),
                topLeft = Offset(w * 0.34f, h * 0.28f),
                size = Size(w * 0.20f, h * 0.26f),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = 2.5f)
            )
        }
        Icon(
            imageVector = Icons.Rounded.Contactless,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier
                .padding(end = 12.dp, bottom = 22.dp)
                .size(24.dp)
        )
    }
}
