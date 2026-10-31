package com.example.gpstracker.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
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
import com.example.gpstracker.core.places.PlaceDetails
import com.example.gpstracker.core.places.PlaceSuggestion

@Composable
fun FindLocationScreen(
    uiState: FindLocationUiState,
    onEvent: (FindLocationEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(Unit) {
        onDispose { onEvent(FindLocationEvent.ScreenStopped) }
    }

    val spacing = LocalSpacing.current
    val density = LocalDensity.current
    val selected = uiState.selectedPlace
    var bottomChrome by remember { mutableStateOf(120.dp) }
    val reportBottomChrome = Modifier.onSizeChanged { size ->
        val needed = with(density) { size.height.toDp() } + spacing.md + 36.dp
        if (needed > bottomChrome) bottomChrome = needed
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (selected != null) {
            SelectedPlaceMap(
                place = selected,
                bottomChrome = bottomChrome,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CurrentLocationMap(
                latitude = uiState.userLatitude,
                longitude = uiState.userLongitude,
                locationRevision = uiState.locationRevision,
                bottomChrome = bottomChrome,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
        AppTopBar(
            title = stringResource(R.string.find_location_title),
            onNavigateUp = onNavigateUp,
            containerColor = Color.Transparent,
            showDivider = false,
            actions = {
                GpsLocationButton(
                    locating = uiState.locating,
                    onClick = { onEvent(FindLocationEvent.UseGpsClicked) },
                )
            },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val showingList = uiState.suggestions.isNotEmpty() ||
                uiState.isSearching ||
                uiState.emptyResults
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
                    .padding(top = spacing.sm)
                    .then(if (showingList) Modifier.fillMaxHeight() else Modifier),
            ) {
                SearchCard(
                    myLocationLine = uiState.myLocationLine
                        ?: stringResource(R.string.find_location_my_location_fallback),
                    destinationQuery = uiState.destinationQuery,
                    onDestinationChange = { onEvent(FindLocationEvent.DestinationQueryChanged(it)) },
                    onSwap = { onEvent(FindLocationEvent.SwapClicked) },
                    onVoice = { onEvent(FindLocationEvent.VoiceSearchClicked) },
                )
                if (showingList) {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    SuggestionsBlock(
                        suggestions = uiState.suggestions,
                        isSearching = uiState.isSearching,
                        emptyResults = uiState.emptyResults,
                        onSelect = { onEvent(FindLocationEvent.SuggestionSelected(it)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = spacing.md),
                    )
                }
            }

            if (!showingList && selected != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md)
                        .padding(bottom = spacing.md)
                        .then(reportBottomChrome),
                ) {
                    SelectedPlaceCard(
                        place = selected,
                        isSaved = uiState.isSelectedSaved,
                        onSave = { onEvent(FindLocationEvent.SaveClicked) },
                        onNavigate = { onEvent(FindLocationEvent.NavigateClicked) },
                    )
                }
            }
            if (!showingList && selected == null) {
                NavigateLocationButton(
                    onClick = { onEvent(FindLocationEvent.NavigateClicked) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = spacing.md)
                        .padding(bottom = spacing.md)
                        .then(reportBottomChrome),
                )
            }
        }
        }
    }
}

@Composable
private fun GpsLocationButton(
    locating: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    IconButton(
        onClick = onClick,
        enabled = !locating,
        modifier = Modifier.size(spacing.touchTarget),
    ) {
        if (locating) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalAppColors.current.primary,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_find_location_map_pinned),
                contentDescription = stringResource(R.string.cd_find_location_use_gps),
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun NavigateLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp),
        shape = RoundedCornerShape(percent = 50),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_find_location_navigate),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(R.string.find_location_navigate))
    }
}

@Composable
private fun SearchCard(
    myLocationLine: String,
    destinationQuery: String,
    onDestinationChange: (String) -> Unit,
    onSwap: () -> Unit,
    onVoice: () -> Unit,
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
            Text(
                text = stringResource(R.string.find_location_my_location_label),
                style = AppTextStyles.sectionLabel,
                color = colors.textMuted,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = ColorTokens.PinStartA,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(spacing.xs))
                Text(
                    text = myLocationLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                )
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = stringResource(R.string.cd_find_location_swap),
                        tint = colors.textMuted,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = spacing.xs),
                color = colors.outlineDefault,
            )

            Text(
                text = stringResource(R.string.find_location_destination_label),
                style = AppTextStyles.sectionLabel,
                color = colors.textMuted,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = ColorTokens.PinDestinationA,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(spacing.xs))
                BasicTextField(
                    value = destinationQuery,
                    onValueChange = onDestinationChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (destinationQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.find_location_search_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted,
                                )
                            }
                            inner()
                        }
                    },
                )
                IconButton(
                    onClick = onVoice,
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = stringResource(R.string.cd_find_location_voice),
                        tint = colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionsBlock(
    suggestions: List<PlaceSuggestion>,
    isSearching: Boolean,
    emptyResults: Boolean,
    onSelect: (PlaceSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val spacing = LocalSpacing.current
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        when {
            isSearching && suggestions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            emptyResults -> {
                Text(
                    text = stringResource(R.string.places_no_results),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    modifier = Modifier.padding(spacing.md),
                )
            }
            else -> {
                LazyColumn {
                    items(suggestions, key = { it.placeId }) { suggestion ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(suggestion) }
                                .padding(spacing.md),
                        ) {
                            Text(
                                text = suggestion.primaryText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = colors.textPrimary,
                            )
                            if (suggestion.secondaryText.isNotBlank()) {
                                Text(
                                    text = suggestion.secondaryText,
                                    style = AppTextStyles.chipText,
                                    color = colors.textMuted,
                                )
                            }
                        }
                        HorizontalDivider(color = colors.outlineDefault)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPlaceMap(
    place: PlaceDetails,
    bottomChrome: Dp,
    modifier: Modifier = Modifier,
) {
    val camera = rememberMapCameraState(place.latitude, place.longitude, DEFAULT_LOCATION_ZOOM)
    Box(modifier = modifier.fillMaxSize()) {
        AppMap(
            mapType = AppMapType.Normal,
            camera = camera,
            showUserLocation = false,
            pins = listOf(MapPin(place.placeId, place.latitude, place.longitude)),
            contentPadding = mapOverlayPadding(bottomChrome),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SelectedPlaceCard(
    place: PlaceDetails,
    isSaved: Boolean,
    onSave: () -> Unit,
    onNavigate: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.textPrimary,
                    )
                    place.formattedAddress?.let { address ->
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
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
            Spacer(modifier = Modifier.height(spacing.sm))
            NavigateLocationButton(onClick = onNavigate)
        }
    }
}

@Composable
private fun CurrentLocationMap(
    latitude: Double?,
    longitude: Double?,
    locationRevision: Int,
    bottomChrome: Dp,
    modifier: Modifier = Modifier,
) {
    val camera = rememberMapCameraState(zoom = 2f)
    LaunchedEffect(locationRevision, latitude, longitude) {
        if (latitude != null && longitude != null && locationRevision > 0) {
            camera.moveTo(latitude, longitude, DEFAULT_LOCATION_ZOOM)
        }
    }
    val pins = if (latitude != null && longitude != null) {
        listOf(MapPin(id = "my-location", latitude = latitude, longitude = longitude))
    } else {
        emptyList()
    }
    Box(modifier = modifier.fillMaxSize()) {
        AppMap(
            mapType = AppMapType.Normal,
            camera = camera,
            showUserLocation = latitude != null && longitude != null,
            userLatitude = latitude,
            userLongitude = longitude,
            pins = pins,
            contentPadding = mapOverlayPadding(bottomChrome),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun mapOverlayPadding(bottomChrome: Dp): PaddingValues {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return PaddingValues(top = statusTop + 64.dp + 180.dp, bottom = bottomChrome)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FindLocationEmptyPreview() {
    AppTheme {
        FindLocationScreen(
            uiState = FindLocationUiState(
                myLocationLine = "J3PW+FQ7, Muhammadi Colony Shakrial, Rawalpindi",
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
