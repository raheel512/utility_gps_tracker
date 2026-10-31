package com.example.gpstracker.feature.places.nearby

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
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
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.places.NearbyCategory
import com.example.gpstracker.core.places.PlaceSummary

@Composable
fun NearbyResultsScreen(
    uiState: NearbyResultsUiState,
    onEvent: (NearbyResultsEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(Unit) {
        onDispose { onEvent(NearbyResultsEvent.ScreenStopped) }
    }

    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val title = uiState.category?.let { stringResource(it.labelRes) }
        ?: stringResource(R.string.screen_nearby_places)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = title,
            onNavigateUp = onNavigateUp,
            containerColor = colors.screenTintStart,
        )

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                NearbyResultsMap(
                    uiState = uiState,
                    onMarkerClick = { onEvent(NearbyResultsEvent.PlaceClicked(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                        .padding(horizontal = spacing.contentSide)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                ResultsList(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier
                        .weight(0.55f)
                        .padding(horizontal = spacing.contentSide),
                )
            }
        }
    }
}

@Composable
private fun NearbyResultsMap(
    uiState: NearbyResultsUiState,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val camera = rememberMapCameraState()
    LaunchedEffect(uiState.places, uiState.userLatitude, uiState.userLongitude) {
        if (uiState.places.isEmpty()) return@LaunchedEffect
        if (uiState.places.size == 1) {
            val place = uiState.places.first().place
            camera.moveTo(place.latitude, place.longitude, DEFAULT_LOCATION_ZOOM)
        } else {
            val points = buildList {
                uiState.places.forEach { add(com.example.gpstracker.core.map.MapPoint(it.place.latitude, it.place.longitude)) }
                val lat = uiState.userLatitude
                val lng = uiState.userLongitude
                if (lat != null && lng != null) add(com.example.gpstracker.core.map.MapPoint(lat, lng))
            }
            camera.moveToBounds(points, paddingPx = 80)
        }
    }

    AppMap(
        mapType = AppMapType.Normal,
        camera = camera,
        showUserLocation = true,
        pins = uiState.places.map { row ->
            MapPin(row.place.placeId, row.place.latitude, row.place.longitude, row.place.name)
        },
        userLatitude = uiState.userLatitude,
        userLongitude = uiState.userLongitude,
        onPinClick = onMarkerClick,
        modifier = modifier,
    )
}

@Composable
private fun ResultsList(
    uiState: NearbyResultsUiState,
    onEvent: (NearbyResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val spacing = LocalSpacing.current
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        when {
            uiState.emptyResults -> {
                Text(
                    text = stringResource(R.string.places_no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    modifier = Modifier.padding(spacing.md),
                )
            }
            else -> {
                LazyColumn {
                    items(uiState.places, key = { it.place.placeId }) { row ->
                        val selected = row.place.placeId == uiState.selectedPlaceId
                        PlaceRow(
                            row = row,
                            selected = selected,
                            isSaved = selected && uiState.isSelectedSaved,
                            onClick = { onEvent(NearbyResultsEvent.PlaceClicked(row.place.placeId)) },
                            onSave = { onEvent(NearbyResultsEvent.SaveClicked) },
                            onDirections = { onEvent(NearbyResultsEvent.DirectionsClicked) },
                        )
                        HorizontalDivider(color = colors.outlineDefault)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(
    row: NearbyPlaceRow,
    selected: Boolean,
    isSaved: Boolean,
    onClick: () -> Unit,
    onSave: () -> Unit,
    onDirections: () -> Unit,
) {
    val colors = LocalAppColors.current
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) ColorTokens.SelectionRowBg else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = ColorTokens.PinDestinationA,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(spacing.xs))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.place.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                )
                row.place.formattedAddress?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 2,
                    )
                }
            }
            if (selected) {
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        imageVector = if (isSaved) {
                            Icons.Filled.Bookmark
                        } else {
                            Icons.Filled.BookmarkBorder
                        },
                        contentDescription = stringResource(
                            if (isSaved) R.string.cd_saved_unsave else R.string.cd_saved_save,
                        ),
                        tint = colors.primary,
                    )
                }
            }
            Text(
                text = formatDistanceLabel(row.distanceDisplay),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.height(spacing.xs))
            AppButton(
                text = stringResource(R.string.nearby_directions),
                onClick = onDirections,
                fillMaxWidth = true,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                leadingIconContentDescription = null,
            )
        }
    }
}

@Composable
private fun formatDistanceLabel(display: DistanceFormatter.DistanceDisplay): String {
    val unit = when (display.unit) {
        DistanceFormatter.DistanceDisplayUnit.Km -> stringResource(R.string.settings_unit_km)
        DistanceFormatter.DistanceDisplayUnit.M -> stringResource(R.string.distance_unit_m)
        DistanceFormatter.DistanceDisplayUnit.Mi -> stringResource(R.string.settings_unit_mi)
        DistanceFormatter.DistanceDisplayUnit.Ft -> stringResource(R.string.distance_unit_ft)
    }
    return stringResource(R.string.nearby_distance_format, display.valueText, unit)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun NearbyResultsPreview() {
    AppTheme {
        NearbyResultsScreen(
            uiState = NearbyResultsUiState(
                category = NearbyCategory.Hotels,
                places = listOf(
                    NearbyPlaceRow(
                        place = PlaceSummary(
                            placeId = "1",
                            name = "Sample Hotel",
                            formattedAddress = "Muhammadi Colony Shakrial, Rawalpindi",
                            latitude = 33.6,
                            longitude = 73.0,
                        ),
                        distanceMeters = 1200.0,
                        distanceDisplay = DistanceFormatter.format(1200.0, com.example.gpstracker.core.data.DistanceUnit.KM),
                    ),
                ),
                selectedPlaceId = "1",
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
