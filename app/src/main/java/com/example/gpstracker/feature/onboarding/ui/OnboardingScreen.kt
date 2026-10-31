package com.example.gpstracker.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AssetSlot
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { uiState.pageCount },
    )

    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                onEvent(OnboardingEvent.PageChanged(page))
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.contentSide),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onEvent(OnboardingEvent.Back) },
                modifier = Modifier.defaultMinSize(
                    minWidth = spacing.touchTarget,
                    minHeight = spacing.touchTarget,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_up),
                    tint = ColorTokens.TextSecondary,
                )
            }
            TextButton(
                onClick = { onEvent(OnboardingEvent.Skip) },
                enabled = !uiState.isCompleting,
                modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
            ) {
                Text(
                    text = stringResource(R.string.action_skip),
                    color = ColorTokens.Primary500,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val slide = uiState.slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AssetSlot(
                    drawableRes = slide.imageRes,
                    contentDescription = stringResource(slide.titleRes),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    placeholderSize = 260.dp,
                )
                Spacer(modifier = Modifier.height(spacing.lg))
                Text(
                    text = stringResource(slide.titleRes),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorTokens.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                Text(
                    text = stringResource(slide.bodyRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTokens.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        PageDots(total = uiState.pageCount, selectedIndex = uiState.currentPage)
        Spacer(modifier = Modifier.height(spacing.lg))
        AppButton(
            text = stringResource(R.string.action_next),
            onClick = { onEvent(OnboardingEvent.Next) },
            enabled = !uiState.isCompleting,
            fillMaxWidth = true,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            trailingIconContentDescription = stringResource(R.string.cd_next),
        )
        Spacer(modifier = Modifier.height(spacing.lg))
    }
}

@Preview(showBackground = true/*, heightDp = 800*/)
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(currentPage = 0),
            onEvent = {},
        )
    }
}
