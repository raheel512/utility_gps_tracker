package com.example.gpstracker.feature.map.satellite

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.core.map.AppMap
import com.example.gpstracker.core.map.AppMapType
import com.example.gpstracker.core.map.CenterOnFirstFix
import com.example.gpstracker.core.map.DEFAULT_LOCATION_ZOOM
import com.example.gpstracker.core.map.MapCameraState
import com.example.gpstracker.core.map.MapControlButton
import com.example.gpstracker.core.map.MapPin
import com.example.gpstracker.core.map.TILTED_3D_DEGREES
import com.example.gpstracker.core.map.rememberMapCameraState
import com.example.gpstracker.core.map.toMapPoint

private val ControlSize = 40.dp
private val PillShape16 = RoundedCornerShape(16.dp)
private val SegmentShape = RoundedCornerShape(12.dp)
private val PillMaxWidth = 600.dp

@Composable
fun SatelliteScreen(
    uiState: SatelliteUiState,
    onEvent: (SatelliteEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    camera: MapCameraState = rememberMapCameraState(),
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val density = LocalDensity.current
    var pillHeightPx by remember { mutableStateOf(0) }
    val pillHeight = with(density) { pillHeightPx.toDp() }
    val userPoint = uiState.location?.toMapPoint()

    CenterOnFirstFix(camera, userPoint)

    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.satellite_view_title),
            onNavigateUp = onNavigateUp,
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AppMap(
                mapType = uiState.mapType,
                camera = camera,
                showUserLocation = true,
                compassEnabled = false,
                buildingsEnabled = uiState.is3d,
                contentPadding = PaddingValues(bottom = pillHeight + spacing.md),
                pins = listOfNotNull(
                    userPoint?.let {
                        MapPin(
                            id = "me",
                            latitude = it.latitude,
                            longitude = it.longitude,
                            label = uiState.coordinates,
                        )
                    },
                ),
                userLatitude = userPoint?.latitude,
                userLongitude = userPoint?.longitude,
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = spacing.md, bottom = pillHeight + spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                MapControlButton(
                    size = ControlSize,
                    containerColor = colors.mapStylePillBg,
                    onClick = {
                        camera.orient(tilt = 0f, bearing = 0f)
                        onEvent(SatelliteEvent.ResetOrientation)
                    },
                ) {
                    val description = stringResource(R.string.cd_satellite_north)
                    NorthNeedle(
                        bearing = camera.bearing,
                        modifier = Modifier.semantics { contentDescription = description },
                    )
                }
                MapControlButton(
                    size = ControlSize,
                    containerColor = colors.mapStylePillBg,
                    onClick = {
                        val next = !uiState.is3d
                        camera.orient(tilt = if (next) TILTED_3D_DEGREES else 0f)
                        onEvent(SatelliteEvent.Toggle3d)
                    },
                ) {
                    val description = stringResource(R.string.cd_satellite_toggle_3d)
                    Text(
                        text = stringResource(R.string.satellite_toggle_3d),
                        color = colors.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.semantics { contentDescription = description },
                    )
                }
                MapControlButton(
                    size = ControlSize,
                    containerColor = colors.mapStylePillBg,
                    onClick = {
                        userPoint?.let { camera.moveTo(it.latitude, it.longitude, DEFAULT_LOCATION_ZOOM) }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ZoomIn,
                        contentDescription = stringResource(R.string.cd_satellite_recenter),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            MapStylePill(
                selected = uiState.mapType,
                onSelect = { onEvent(SatelliteEvent.SelectMapType(it)) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = spacing.md)
                    .padding(bottom = spacing.md)
                    .widthIn(max = PillMaxWidth)
                    .fillMaxWidth()
                    .onSizeChanged { pillHeightPx = it.height },
            )
        }
    }
}

/** Red north / grey south needle; rotates against the camera bearing so red always points north. */
@Composable
private fun NorthNeedle(bearing: Float, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Canvas(modifier = modifier.size(width = 10.dp, height = 18.dp).rotate(-bearing)) {
        val midX = size.width / 2
        val midY = size.height / 2
        drawPath(
            Path().apply {
                moveTo(midX, 0f)
                lineTo(size.width, midY)
                lineTo(0f, midY)
                close()
            },
            color = colors.compassNorth,
        )
        drawPath(
            Path().apply {
                moveTo(0f, midY)
                lineTo(size.width, midY)
                lineTo(midX, size.height)
                close()
            },
            color = colors.compassSouth,
        )
    }
}

@StringRes
private fun AppMapType.styleLabel(): Int = when (this) {
    AppMapType.Satellite -> R.string.satellite_style_satellite
    AppMapType.Hybrid -> R.string.satellite_style_hybrid
    AppMapType.Terrain -> R.string.satellite_style_terrain
    AppMapType.Normal -> R.string.satellite_style_vector
}

@Composable
private fun MapStylePill(
    selected: AppMapType,
    onSelect: (AppMapType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val groupDescription = stringResource(R.string.cd_satellite_map_style)
    Row(
        modifier = modifier
            .appShadow(AppShadow.Tile, PillShape16)
            .clip(PillShape16)
            .background(colors.mapStylePillBg)
            .padding(horizontal = 7.dp)
            .semantics { contentDescription = groupDescription }
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SatelliteMapStyles.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(type) }),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(SegmentShape)
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .padding(horizontal = spacing.xxs),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isSelected && type == AppMapType.Satellite) {
                        Icon(
                            imageVector = Icons.Outlined.Public,
                            contentDescription = null,
                            tint = colors.textOnPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(spacing.xxs))
                    }
                    Text(
                        text = stringResource(type.styleLabel()),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) colors.textOnPrimary else colors.mapStyleInactiveText,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SatelliteScreenPreview() {
    AppTheme {
        SatelliteScreen(
            uiState = SatelliteUiState(),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
