package com.example.gpstracker.feature.saved.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.data.saved.SavedPlace
import com.example.gpstracker.core.data.saved.SavedPlaceIconKind
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow

@Composable
fun SavedScreen(
    uiState: SavedUiState,
    onEvent: (SavedEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        SavedTopBar(
            searchVisible = uiState.searchVisible,
            query = uiState.query,
            onEvent = onEvent,
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = spacing.contentSide,
                    vertical = spacing.sm,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "quick-save") {
                    QuickSavePill(
                        locationLine = savedLocationLine(
                            coordinates = uiState.currentLocationLine,
                            accuracyMeters = uiState.accuracyMeters,
                        ),
                        canSave = uiState.canSaveCurrent,
                        onSave = { onEvent(SavedEvent.SaveSpotClicked) },
                    )
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        text = stringResource(R.string.saved_section_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                }

                if (uiState.items.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.saved_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                            modifier = Modifier.padding(vertical = spacing.md),
                        )
                    }
                } else {
                    items(uiState.items, key = { it.key }) { item ->
                        SavedListCard(item = item, onEvent = onEvent)
                    }
                }
            }
        }
    }

    if (uiState.showCreateCollectionDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(SavedEvent.DismissCreateCollection) },
            title = { Text(stringResource(R.string.saved_create_collection_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.createCollectionName,
                    onValueChange = { onEvent(SavedEvent.CreateCollectionNameChanged(it)) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.saved_create_collection_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(SavedEvent.ConfirmCreateCollection) }) {
                    Text(stringResource(R.string.saved_create_collection_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(SavedEvent.DismissCreateCollection) }) {
                    Text(stringResource(R.string.settings_theme_cancel))
                }
            },
        )
    }
}

@Composable
private fun SavedTopBar(
    searchVisible: Boolean,
    query: String,
    onEvent: (SavedEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    // Top-level tabs omit Scaffold status-bar insets so bars can paint behind
    // the system bar (same as Home/Settings). Own WindowInsets.statusBars here
    // so content clears the status bar on API 24–35 (Android 7–15+) with
    // enableEdgeToEdge().
    if (searchVisible) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = spacing.appBarHorizontal, vertical = spacing.appBarTop),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = query,
                onValueChange = { onEvent(SavedEvent.QueryChanged(it)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.saved_search_hint),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textMuted,
                            )
                        }
                        inner()
                    }
                },
            )
            IconButton(
                onClick = { onEvent(SavedEvent.DismissSearch) },
                modifier = Modifier.size(spacing.touchTarget),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_saved_dismiss_search),
                )
            }
        }
    } else {
        AppTopBar(
            title = stringResource(R.string.saved_title),
            containerColor = colors.screenTintStart,
            statusBarAware = true,
            actions = {
                IconButton(
                    onClick = { onEvent(SavedEvent.SearchClicked) },
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = stringResource(R.string.cd_saved_search),
                    )
                }
                IconButton(
                    onClick = { onEvent(SavedEvent.AddCollectionClicked) },
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(R.string.cd_saved_add_collection),
                    )
                }
            },
        )
    }
}

@Composable
private fun QuickSavePill(
    locationLine: String?,
    canSave: Boolean,
    onSave: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ColorTokens.Primary0),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.saved_current_location),
                    style = AppTextStyles.tileLabel,
                    color = colors.textPrimary,
                )
                Text(
                    text = locationLine ?: stringResource(R.string.saved_location_unavailable),
                    style = AppTextStyles.chipText,
                    color = colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                onClick = onSave,
                enabled = canSave,
                shape = RoundedCornerShape(percent = 50),
                color = colors.primary,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = colors.textOnPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.saved_save_spot),
                        style = AppTextStyles.chipText.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textOnPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedListCard(
    item: SavedListItemUi,
    onEvent: (SavedEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(12.dp)
    val place = item.place
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Tile, shape)
            .clickable {
                when {
                    place != null -> onEvent(SavedEvent.PlaceClicked(place.id))
                    item.collection != null -> onEvent(SavedEvent.CollectionClicked(item.collection.id))
                }
            },
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorTokens.SelectionRowBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (item.collection != null) {
                        Icons.Filled.Folder
                    } else {
                        iconFor(item.iconKind)
                    },
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = savedPlaceTitle(item.title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = item.subtitle
                    ?: item.spotCountLabel?.let { count ->
                        stringResource(R.string.saved_spots_count, count)
                    }
                subtitle?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.chipText,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                item.distanceLabel?.let { distance ->
                    Text(
                        text = distance,
                        style = AppTextStyles.chipText.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (place != null) {
                    IconButton(
                        onClick = { onEvent(SavedEvent.UnsaveClicked(place.id)) },
                        modifier = Modifier.size(spacing.touchTarget),
                    ) {
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = stringResource(R.string.cd_saved_unsave),
                            tint = colors.primary,
                        )
                    }
                    Surface(
                        onClick = { onEvent(SavedEvent.StartTripClicked(place.id)) },
                        shape = RoundedCornerShape(12.dp),
                        color = ColorTokens.SelectionRowBg,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.saved_start_trip),
                                style = AppTextStyles.chipText.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun savedLocationLine(coordinates: String?, accuracyMeters: Int?): String? {
    if (coordinates == null) return null
    val meters = accuracyMeters ?: return coordinates
    return stringResource(R.string.saved_accuracy_line, coordinates, meters)
}

@Composable
private fun savedPlaceTitle(title: String): String =
    if (title == SAVED_CURRENT_SPOT_NAME) {
        stringResource(R.string.saved_current_location)
    } else {
        title
    }

private fun iconFor(kind: SavedPlaceIconKind): ImageVector = when (kind) {
    SavedPlaceIconKind.HOME -> Icons.Filled.Home
    SavedPlaceIconKind.WORK -> Icons.Filled.Business
    SavedPlaceIconKind.PLACE -> Icons.Filled.Place
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun SavedScreenPreview() {
    AppTheme {
        SavedScreen(
            uiState = SavedUiState(
                isLoading = false,
                currentLocationLine = "33.6844° N, 73.0479° E • Accuracy 3m",
                canSaveCurrent = true,
                items = listOf(
                    SavedListItemUi(
                        key = "place-1",
                        place = SavedPlace(
                            id = 1,
                            placeId = null,
                            name = "Home",
                            address = "Sector F-7/2, Islamabad",
                            latitude = 33.72,
                            longitude = 73.05,
                            collectionId = null,
                            createdAtMillis = 0,
                            iconKind = SavedPlaceIconKind.HOME,
                        ),
                        title = "Home",
                        subtitle = "Sector F-7/2, Islamabad",
                        distanceLabel = "8.4 km",
                        iconKind = SavedPlaceIconKind.HOME,
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}
