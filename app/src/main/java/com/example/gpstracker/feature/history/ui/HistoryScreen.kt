package com.example.gpstracker.feature.history.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.data.history.RouteHistoryEntry
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.feature.history.HistoryRange

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    menuState: HistoryMenuState,
    onEvent: (HistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = stringResource(R.string.history_title),
            containerColor = colors.screenTintStart,
            statusBarAware = true,
        )
        FilterRow(
            selected = uiState.range,
            onSelect = { onEvent(HistoryEvent.RangeSelected(it)) },
        )
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (uiState.isEmpty) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                modifier = Modifier.padding(spacing.contentSide),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = spacing.contentSide,
                    vertical = spacing.sm,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                uiState.groups.forEach { group ->
                    item(key = "header-${group.key}") {
                        DayHeader(group = group)
                    }
                    items(group.items, key = { it.entry.id }) { item ->
                        HistoryCard(
                            item = item,
                            menuOpen = menuState.entryId == item.entry.id,
                            onEvent = onEvent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selected: HistoryRange,
    onSelect: (HistoryRange) -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.contentSide, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        HistoryRange.entries.forEach { range ->
            val isSelected = range == selected
            Text(
                text = rangeLabel(range),
                modifier = Modifier
                    .defaultMinSize(minHeight = 32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.primary else Color.White)
                    .clickable { onSelect(range) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = if (isSelected) colors.textOnPrimary else colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.24.sp,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun rangeLabel(range: HistoryRange): String = when (range) {
    HistoryRange.All -> stringResource(R.string.history_filter_all)
    HistoryRange.Today -> stringResource(R.string.history_filter_today)
    HistoryRange.Yesterday -> stringResource(R.string.history_filter_yesterday)
    HistoryRange.ThisWeek -> stringResource(R.string.history_filter_this_week)
    HistoryRange.ThisMonth -> stringResource(R.string.history_filter_this_month)
}

@Composable
private fun DayHeader(group: HistoryDayGroupUi) {
    val colors = LocalAppColors.current
    val heading = when {
        group.isToday -> stringResource(R.string.history_heading_today)
        group.isYesterday -> stringResource(R.string.history_heading_yesterday)
        else -> group.dateLabel
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = ColorTokens.Primary0,
        ) {
            Text(
                text = stringResource(R.string.history_trips_count, group.tripCount),
                style = AppTextStyles.chipText,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = group.dateLabel,
            style = AppTextStyles.chipText,
            color = colors.textMuted,
        )
    }
}

@Composable
private fun HistoryCard(
    item: HistoryListItemUi,
    menuOpen: Boolean,
    onEvent: (HistoryEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorTokens.NavIndicator),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_history_route),
                        contentDescription = null,
                        modifier = Modifier.size(width = 12.5.dp, height = 15.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(Modifier.width(spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.startedLabel,
                        style = AppTextStyles.chipText,
                        color = colors.textMuted,
                    )
                }
                Box {
                    IconButton(
                        onClick = { onEvent(HistoryEvent.MenuClicked(item.entry.id)) },
                        modifier = Modifier.size(spacing.touchTarget),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_history_more),
                            contentDescription = stringResource(R.string.cd_history_menu),
                            modifier = Modifier.size(width = 3.33333.dp, height = 13.3333.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { onEvent(HistoryEvent.DismissMenu) },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.history_delete)) },
                            onClick = { onEvent(HistoryEvent.DeleteClicked(item.entry.id)) },
                        )
                    }
                }
            }
            if (item.entry.hasRouteStats && item.entry.encodedPolyline.isNotBlank()) {
                Spacer(Modifier.height(spacing.sm))
                MiniRouteCanvas(
                    encodedPolyline = item.entry.encodedPolyline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorTokens.ScreenTintStart),
                )
            }
            Spacer(Modifier.height(spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val duration = item.duration
                val distance = item.distance
                if (duration != null) {
                    Image(
                        painter = painterResource(R.drawable.ic_history_clock),
                        contentDescription = null,
                        modifier = Modifier.size(13.3333.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = durationLabel(duration),
                        style = AppTextStyles.chipText,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (distance != null) {
                    Spacer(Modifier.width(12.dp))
                    Image(
                        painter = painterResource(R.drawable.ic_history_distance),
                        contentDescription = null,
                        modifier = Modifier.size(width = 13.3333.dp, height = 8.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = distanceLabel(distance),
                        style = AppTextStyles.chipText,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 32.dp)
                        .clip(CircleShape)
                        .background(ColorTokens.NavIndicator)
                        .clickable { onEvent(HistoryEvent.ViewRouteClicked(item.entry.id)) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.history_view_route),
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.24.sp,
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_history_chevron),
                        contentDescription = null,
                        modifier = Modifier.size(width = 4.93333.dp, height = 8.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun durationLabel(duration: DurationFormatter.Display): String = when {
    duration.hours == 0 -> stringResource(R.string.routes_duration_min, duration.totalMinutes)
    duration.remainingMinutes == 0 -> stringResource(R.string.routes_duration_hours, duration.hours)
    else -> stringResource(
        R.string.routes_duration_hours_min,
        duration.hours,
        duration.remainingMinutes,
    )
}

@Composable
private fun distanceLabel(distance: DistanceFormatter.DistanceDisplay): String {
    val unit = when (distance.unit) {
        DistanceFormatter.DistanceDisplayUnit.Km -> stringResource(R.string.settings_unit_km)
        DistanceFormatter.DistanceDisplayUnit.M -> stringResource(R.string.distance_unit_m)
        DistanceFormatter.DistanceDisplayUnit.Mi -> stringResource(R.string.settings_unit_mi)
        DistanceFormatter.DistanceDisplayUnit.Ft -> stringResource(R.string.distance_unit_ft)
    }
    return stringResource(R.string.nearby_distance_format, distance.valueText, unit)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HistoryScreenPreview() {
    val entry = RouteHistoryEntry(
        id = 1,
        originLabel = "Home",
        destLabel = "Blue Area",
        destPlaceId = null,
        destLatitude = 33.7,
        destLongitude = 73.05,
        viaLabel = null,
        viaPlaceId = null,
        viaLatitude = null,
        viaLongitude = null,
        plannedDurationSeconds = 1440,
        plannedDistanceMeters = 8700,
        encodedPolyline = "_p~iF~ps|U_ulLnnqC_mqNvxq`@",
        startedAtMillis = 0L,
    )
    AppTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                isLoading = false,
                range = HistoryRange.Today,
                groups = listOf(
                    HistoryDayGroupUi(
                        key = "today",
                        isToday = true,
                        isYesterday = false,
                        dateLabel = "Oct 24, 2024",
                        tripCount = 1,
                        items = listOf(
                            HistoryListItemUi(
                                entry = entry,
                                title = "Home → Blue Area",
                                startedLabel = "8:15 AM",
                                duration = DurationFormatter.Display(24, 0, 24),
                                distance = DistanceFormatter.DistanceDisplay(
                                    "8.7",
                                    DistanceFormatter.DistanceDisplayUnit.Km,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            menuState = HistoryMenuState(),
            onEvent = {},
        )
    }
}
