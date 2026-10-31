package com.example.gpstracker.feature.map.mylocation

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.core.map.AppMap
import com.example.gpstracker.core.map.CenterOnFirstFix
import com.example.gpstracker.core.map.DEFAULT_LOCATION_ZOOM
import com.example.gpstracker.core.map.MapCameraState
import com.example.gpstracker.core.map.MapControlButton
import com.example.gpstracker.core.map.MapPin
import com.example.gpstracker.core.map.MapZoomControls
import com.example.gpstracker.core.map.rememberMapCameraState
import com.example.gpstracker.core.map.toMapPoint

private val CardShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val CardMaxWidth = 600.dp
private val ActionCircleSize = 52.dp
private val ActionWidth = 82.dp

@Composable
fun MyLocationScreen(
    uiState: MyLocationUiState,
    onEvent: (MyLocationEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    camera: MapCameraState = rememberMapCameraState(),
) {
    val spacing = LocalSpacing.current
    val density = LocalDensity.current
    var cardHeightPx by remember { mutableStateOf(0) }
    val cardHeight = with(density) { cardHeightPx.toDp() }
    val userPoint = uiState.location?.toMapPoint()

    CenterOnFirstFix(camera, userPoint)

    Box(modifier = modifier.fillMaxSize()) {
        AppMap(
            mapType = uiState.mapType,
            camera = camera,
            showUserLocation = false,
            contentPadding = PaddingValues(bottom = cardHeight),
            pins = listOfNotNull(
                userPoint?.let { MapPin(id = "me", latitude = it.latitude, longitude = it.longitude) },
            ),
            modifier = Modifier.fillMaxSize(),
        )

        MapControlButton(
            onClick = onNavigateUp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(spacing.md),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_navigate_up),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = spacing.md, bottom = cardHeight + spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            MapControlButton(
                onClick = {
                    userPoint?.let { camera.moveTo(it.latitude, it.longitude, DEFAULT_LOCATION_ZOOM) }
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.GpsFixed,
                    contentDescription = stringResource(R.string.cd_my_location_locate_me),
                    modifier = Modifier.size(20.dp),
                )
            }
            MapControlButton(onClick = { onEvent(MyLocationEvent.ToggleMapType) }) {
                Icon(
                    imageVector = Icons.Outlined.Layers,
                    contentDescription = stringResource(R.string.cd_my_location_map_layers),
                    modifier = Modifier.size(20.dp),
                )
            }
            MapZoomControls(
                onZoomIn = { camera.zoomBy(1f) },
                onZoomOut = { camera.zoomBy(-1f) },
                zoomInContent = {
                    Icon(Icons.Filled.Add, stringResource(R.string.cd_map_zoom_in), Modifier.size(18.dp))
                },
                zoomOutContent = {
                    Icon(Icons.Filled.Remove, stringResource(R.string.cd_map_zoom_out), Modifier.size(18.dp))
                },
            )
        }

        LocationCard(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = CardMaxWidth)
                .fillMaxWidth()
                .onSizeChanged { cardHeightPx = it.height },
        )
    }
}

@Composable
private fun LocationCard(
    uiState: MyLocationUiState,
    onEvent: (MyLocationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.appShadow(AppShadow.Card, CardShape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.contentSide)
                .padding(top = spacing.sm, bottom = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.mapPullHandle),
            )
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = uiState.locationLine.orEmpty(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(spacing.md))
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xl, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CardAction(
                    icon = Icons.Filled.Share,
                    label = stringResource(R.string.my_location_share),
                    contentDescription = stringResource(R.string.cd_my_location_share),
                    circleColor = colors.mapActionShare,
                    enabled = uiState.canShareOrCopy,
                    onClick = { onEvent(MyLocationEvent.ShareClicked) },
                )
                CardAction(
                    icon = Icons.Filled.ContentCopy,
                    label = stringResource(R.string.my_location_copy),
                    contentDescription = stringResource(R.string.cd_my_location_copy),
                    circleColor = colors.mapActionCopy,
                    enabled = uiState.canShareOrCopy,
                    onClick = { onEvent(MyLocationEvent.CopyClicked) },
                )
            }
        }
    }
}

@Composable
private fun CardAction(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    circleColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.widthIn(min = ActionWidth),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = spacing.xxs),
        ) {
            Box(
                modifier = Modifier
                    .size(ActionCircleSize)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = colors.mapActionIcon,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.textSecondary,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun MyLocationScreenPreview() {
    AppTheme {
        MyLocationScreen(
            uiState = MyLocationUiState(
                locationLine = "M2XP+RFW, 9th Ave, F-9, Islamabad, Pakistan.",
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
