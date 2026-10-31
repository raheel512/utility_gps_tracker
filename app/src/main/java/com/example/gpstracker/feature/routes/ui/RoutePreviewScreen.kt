package com.example.gpstracker.feature.routes.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.core.map.AppMap
import com.example.gpstracker.core.map.AppMapType
import com.example.gpstracker.core.map.DEFAULT_LOCATION_ZOOM
import com.example.gpstracker.core.map.MapPin
import com.example.gpstracker.core.map.rememberMapCameraState
import com.example.gpstracker.core.map.toMapPoint
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.routes.DurationFormatter
import com.example.gpstracker.core.routes.RoutePlan

@Composable
fun RoutePreviewScreen(
    uiState: RoutePreviewUiState,
    onEvent: (RoutePreviewEvent) -> Unit,
    onNavigateUp: () -> Unit,
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
            title = uiState.destName.ifBlank { stringResource(R.string.find_location_title) },
            onNavigateUp = onNavigateUp,
            containerColor = colors.screenTintStart,
        )

        if (uiState.isLoading && uiState.plan == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.contentSide)
                    .padding(bottom = spacing.lg),
            ) {
                Spacer(Modifier.height(spacing.sm))
                RouteMapCard(uiState = uiState)
                Spacer(Modifier.height(spacing.md))
                DestinationCard(uiState = uiState, onEvent = onEvent)
            }
        }
    }

    if (uiState.addStopVisible) {
        AddStopDialog(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
private fun RouteMapCard(uiState: RoutePreviewUiState) {
    val colors = LocalAppColors.current
    val spacing = LocalSpacing.current
    val shape = RoundedCornerShape(16.dp)
    val points = uiState.plan?.points.orEmpty()
    val camera = rememberMapCameraState(uiState.destLatitude, uiState.destLongitude, DEFAULT_LOCATION_ZOOM)
    LaunchedEffect(uiState.plan?.encodedPolyline) {
        if (points.size >= 2) {
            camera.moveToBounds(points.map { it.toMapPoint() }, paddingPx = 80)
        }
    }
    val pins = buildList {
        add(MapPin("dest", uiState.destLatitude, uiState.destLongitude))
        val originLat = uiState.originLatitude
        val originLng = uiState.originLongitude
        if (originLat != null && originLng != null) {
            add(MapPin("origin", originLat, originLng))
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            AppMap(
                mapType = AppMapType.Normal,
                camera = camera,
                showUserLocation = true,
                pins = pins,
                route = points.map { it.toMapPoint() },
                userLatitude = uiState.originLatitude,
                userLongitude = uiState.originLongitude,
                modifier = Modifier.fillMaxSize(),
            )
            uiState.viaLabel?.let { via ->
                Surface(
                    modifier = Modifier
                        .padding(spacing.sm)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Place,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(spacing.xxs))
                        Text(
                            text = stringResource(R.string.routes_via, via),
                            style = AppTextStyles.chipText,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
            val duration = uiState.duration
            val distance = uiState.distance
            if (duration != null && distance != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(spacing.sm),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)) {
                        Text(
                            text = durationLabel(duration),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                        )
                        Text(
                            text = distanceLabel(distance),
                            style = AppTextStyles.chipText,
                            color = colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationCard(
    uiState: RoutePreviewUiState,
    onEvent: (RoutePreviewEvent) -> Unit,
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
        Column(modifier = Modifier.padding(spacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.routes_primary_destination),
                        style = AppTextStyles.sectionLabel,
                        color = colors.primary,
                    )
                    Text(
                        text = uiState.destName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                    )
                    if (uiState.destAddress.isNotBlank()) {
                        Text(
                            text = uiState.destAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
                IconButton(
                    onClick = { onEvent(RoutePreviewEvent.SaveClicked) },
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        imageVector = if (uiState.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(
                            if (uiState.isSaved) R.string.cd_routes_unsave else R.string.cd_routes_save,
                        ),
                        tint = colors.primary,
                    )
                }
            }
            Spacer(Modifier.height(spacing.md))
            AppButton(
                text = stringResource(R.string.routes_start_navigation),
                onClick = { onEvent(RoutePreviewEvent.StartNavigationClicked) },
                enabled = uiState.canStart && !uiState.isLoading,
                fillMaxWidth = true,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                leadingIconContentDescription = null,
            )
            Spacer(Modifier.height(spacing.sm))
            TextButton(
                onClick = { onEvent(RoutePreviewEvent.AddStopClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.touchTarget),
            ) {
                Icon(
                    Icons.Filled.AddLocation,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(spacing.xs))
                Text(
                    text = stringResource(R.string.routes_add_stop),
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AddStopDialog(
    uiState: RoutePreviewUiState,
    onEvent: (RoutePreviewEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(RoutePreviewEvent.DismissAddStop) },
        title = { Text(stringResource(R.string.routes_add_stop)) },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.addStopQuery,
                    onValueChange = { onEvent(RoutePreviewEvent.AddStopQueryChanged(it)) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.routes_add_stop_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(24.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                }
                LazyColumn(
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.suggestions, key = { it.placeId }) { suggestion ->
                        TextButton(
                            onClick = { onEvent(RoutePreviewEvent.ViaSuggestionSelected(suggestion)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(suggestion.primaryText, fontWeight = FontWeight.SemiBold)
                                Text(suggestion.secondaryText, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onEvent(RoutePreviewEvent.DismissAddStop) }) {
                Text(stringResource(R.string.settings_theme_cancel))
            }
        },
    )
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
private fun RoutePreviewScreenPreview() {
    AppTheme {
        RoutePreviewScreen(
            uiState = RoutePreviewUiState(
                destName = "Faisal Mosque",
                destAddress = "Shah Faisal Ave, E-8, Islamabad",
                destLatitude = 33.7295,
                destLongitude = 73.0383,
                viaLabel = "Blue Area Corridor",
                isLoading = false,
                canStart = true,
                duration = DurationFormatter.Display(14, 0, 14),
                distance = DistanceFormatter.DistanceDisplay("6.4", DistanceFormatter.DistanceDisplayUnit.Km),
                plan = RoutePlan(
                    encodedPolyline = "_p~iF~ps|U",
                    points = listOf(LatLngPoint(33.72, 73.03), LatLngPoint(33.73, 73.04)),
                    distanceMeters = 6400,
                    durationSeconds = 840,
                    viaLabel = "Blue Area Corridor",
                ),
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
