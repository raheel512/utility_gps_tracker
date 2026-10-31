package com.example.gpstracker.feature.saved.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.data.saved.SavedPlace
import com.example.gpstracker.core.data.saved.SavedPlaceIconKind
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.core.map.AppMap
import com.example.gpstracker.core.map.AppMapType
import com.example.gpstracker.core.map.DEFAULT_LOCATION_ZOOM
import com.example.gpstracker.core.map.MapPin
import com.example.gpstracker.core.map.rememberMapCameraState

@Composable
fun SavedPlaceDetailScreen(
    uiState: SavedPlaceDetailUiState,
    onEvent: (SavedPlaceDetailEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val place = uiState.place

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = place?.name ?: stringResource(R.string.saved_title),
            onNavigateUp = onNavigateUp,
            containerColor = colors.screenTintStart,
        )

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            place == null -> {
                Text(
                    text = stringResource(R.string.saved_empty),
                    modifier = Modifier.padding(spacing.contentSide),
                    color = colors.textMuted,
                )
            }
            else -> {
                val camera = rememberMapCameraState(place.latitude, place.longitude, DEFAULT_LOCATION_ZOOM)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spacing.contentSide)
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    AppMap(
                        mapType = AppMapType.Normal,
                        camera = camera,
                        showUserLocation = false,
                        pins = listOf(MapPin(place.placeId ?: place.id.toString(), place.latitude, place.longitude)),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(spacing.sm))
                val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appShadow(AppShadow.Card, shape),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(spacing.md)) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = colors.textPrimary,
                        )
                        place.address?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.sm))
                        AppButton(
                            text = stringResource(R.string.saved_start_trip),
                            onClick = { onEvent(SavedPlaceDetailEvent.StartTripClicked) },
                            fillMaxWidth = true,
                            leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            leadingIconContentDescription = null,
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        AppButton(
                            text = stringResource(R.string.find_location_save),
                            onClick = { onEvent(SavedPlaceDetailEvent.UnsaveClicked) },
                            fillMaxWidth = true,
                            leadingIcon = Icons.Filled.Bookmark,
                            leadingIconContentDescription = stringResource(R.string.cd_saved_unsave),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun SavedPlaceDetailPreview() {
    AppTheme {
        SavedPlaceDetailScreen(
            uiState = SavedPlaceDetailUiState(
                isLoading = false,
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
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
