package com.example.gpstracker.feature.camera.ui

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.permissions.CameraPermissionState
import com.example.gpstracker.feature.camera.stamp.StampRenderModel

private val ControlSize = 40.dp
private val TouchTarget = 48.dp
private val ScreenEdge = 16.dp

@Composable
fun GpsCameraScreen(
    uiState: GpsCameraUiState,
    onEvent: (GpsCameraEvent) -> Unit,
    onImageCaptureReady: (ImageCapture?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraReady = uiState.cameraPermission == CameraPermissionState.Granted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (cameraReady) {
            CameraPreviewHost(
                lensFacingFront = uiState.lensFacingFront,
                flashMode = uiState.flashMode,
                enabled = true,
                onFocusTap = { x, y -> onEvent(GpsCameraEvent.FocusTapped(x, y)) },
                onImageCaptureReady = onImageCaptureReady,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorTokens.CameraOnScrim)
            }
        }

        if (uiState.gridEnabled) {
            GridOverlay(Modifier.fillMaxSize())
        }

        Vignettes()

        uiState.focusPoint?.let { FocusReticle(it, Modifier.fillMaxSize()) }
            ?: DefaultReticle(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            TopBar(uiState = uiState, onEvent = onEvent)
            uiState.headingLine?.let { heading ->
                HeadingPill(
                    text = heading,
                    accurate = uiState.headingAccurate,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenEdge)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.stamp?.let { stamp -> StampOverlay(model = stamp) }
                ShutterDeck(uiState = uiState, onEvent = onEvent)
            }
        }

        if (uiState.isProcessing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(ColorTokens.CameraProcessingScrim),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorTokens.CameraOnScrim)
            }
        }

        if (uiState.settingsOpen) {
            CameraSettingsSheet(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun Vignettes() {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(statusBarTop + 72.dp)
                .background(
                    Brush.verticalGradient(
                        0f to ColorTokens.CameraVignetteTop,
                        0.5f to ColorTokens.CameraVignetteTop.copy(alpha = 0.4f),
                        1f to Color.Transparent,
                    ),
                ),
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(256.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.5f to ColorTokens.CameraVignetteBottom.copy(alpha = 0.6f),
                        1f to ColorTokens.CameraVignetteBottom,
                    ),
                ),
        )
    }
}

@Composable
private fun TopBar(
    uiState: GpsCameraUiState,
    onEvent: (GpsCameraEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ScreenEdge - 4.dp, end = ScreenEdge - 4.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {
            CircleControl(
                onClick = { onEvent(GpsCameraEvent.FlashClicked) },
                contentDescription = stringResource(R.string.cd_gps_camera_flash),
                stateDescription = stringResource(uiState.flashMode.labelRes()),
                enabled = !uiState.lensFacingFront,
            ) {
                FlashIcon(uiState.flashMode)
            }
            FlashMenu(
                expanded = uiState.flashMenuOpen,
                selected = uiState.flashMode,
                onEvent = onEvent,
            )
        }
        CircleControl(
            onClick = { onEvent(GpsCameraEvent.ToggleGrid) },
            contentDescription = stringResource(R.string.cd_gps_camera_grid),
            stateDescription = stringResource(
                if (uiState.gridEnabled) R.string.gps_camera_state_on else R.string.gps_camera_state_off,
            ),
            role = Role.Switch,
        ) {
            DesignIcon(
                R.drawable.ic_gps_cam_grid,
                modifier = Modifier.alpha(if (uiState.gridEnabled) 1f else 0.45f),
            )
        }
        CircleControl(
            onClick = { onEvent(GpsCameraEvent.ToggleLens) },
            contentDescription = stringResource(R.string.cd_gps_camera_flip),
        ) {
            DesignIcon(R.drawable.ic_gps_cam_lens)
        }
        val sats = uiState.satelliteCount
        val accuracy = uiState.accuracyLine
        if (sats != null || accuracy != null) {
            StatusPill(
                text = buildString {
                    if (sats != null) append(stringResource(R.string.gps_camera_sats, sats))
                    if (sats != null && accuracy != null) append(" · ")
                    if (accuracy != null) append(accuracy)
                },
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 4.dp),
            )
        }
        CircleControl(
            onClick = { onEvent(GpsCameraEvent.SettingsClicked) },
            contentDescription = stringResource(R.string.cd_gps_camera_settings),
        ) {
            DesignIcon(R.drawable.ic_gps_cam_settings)
        }
    }
}

@Composable
private fun FlashIcon(mode: FlashModeCycle) {
    when (mode) {
        FlashModeCycle.Auto -> DesignIcon(R.drawable.ic_gps_cam_flash)
        FlashModeCycle.On -> Icon(
            Icons.Filled.FlashOn,
            contentDescription = null,
            tint = ColorTokens.CameraOnScrim,
            modifier = Modifier.size(20.dp),
        )
        FlashModeCycle.Off -> Icon(
            Icons.Filled.FlashOff,
            contentDescription = null,
            tint = ColorTokens.CameraOnScrim,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun FlashModeCycle.labelRes(): Int = when (this) {
    FlashModeCycle.Auto -> R.string.gps_camera_flash_auto
    FlashModeCycle.On -> R.string.gps_camera_flash_on
    FlashModeCycle.Off -> R.string.gps_camera_flash_off
}

@Composable
private fun FlashMenu(
    expanded: Boolean,
    selected: FlashModeCycle,
    onEvent: (GpsCameraEvent) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onEvent(GpsCameraEvent.FlashMenuDismissed) },
        containerColor = ColorTokens.CameraStampScrim,
        shape = RoundedCornerShape(12.dp),
    ) {
        FlashModeCycle.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(mode.labelRes()),
                        fontWeight = if (mode == selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                leadingIcon = {
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { FlashIcon(mode) }
                },
                trailingIcon = if (mode == selected) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else {
                    null
                },
                colors = MenuDefaults.itemColors(
                    textColor = ColorTokens.CameraOnScrim,
                    trailingIconColor = ColorTokens.CameraHeadingLocked,
                ),
                onClick = { onEvent(GpsCameraEvent.FlashSelected(mode)) },
            )
        }
    }
}

@Composable
private fun DesignIcon(@DrawableRes res: Int, modifier: Modifier = Modifier) {
    // Figma icons carry their own fill colours.
    Icon(
        painter = painterResource(res),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier,
    )
}

@Composable
private fun HeadingPill(text: String, accurate: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(ColorTokens.CameraControlScrim)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DesignIcon(R.drawable.ic_gps_cam_compass)
        Text(
            text = text,
            color = ColorTokens.CameraOnScrim,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            Modifier
                .padding(start = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    if (accurate) ColorTokens.CameraHeadingLocked else ColorTokens.CameraHeadingCoarse,
                ),
        )
    }
}

@Composable
private fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(ColorTokens.CameraChipScrim)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DesignIcon(R.drawable.ic_gps_cam_sat)
        Text(
            text = text,
            color = ColorTokens.CameraOnScrim,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.33.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CircleControl(
    onClick: () -> Unit,
    contentDescription: String,
    stateDescription: String? = null,
    role: Role = Role.Button,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(TouchTarget)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = role, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                this.role = role
                if (stateDescription != null) this.stateDescription = stateDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(ControlSize)
                .clip(CircleShape)
                .background(ColorTokens.CameraControlScrim)
                .alpha(if (enabled) 1f else 0.45f),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}

@Composable
private fun ShutterDeck(uiState: GpsCameraUiState, onEvent: (GpsCameraEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            GalleryButton(
                uri = uiState.latestPhotoUri,
                badge = uiState.sessionPhotoCount,
                onClick = { onEvent(GpsCameraEvent.GalleryClicked) },
                onThumbnailUnavailable = { onEvent(GpsCameraEvent.LatestPhotoUnavailable) },
            )
        }
        ShutterButton(
            enabled = !uiState.isProcessing,
            onClick = { onEvent(GpsCameraEvent.ShutterClicked) },
        )
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            TemplateButton(onClick = { onEvent(GpsCameraEvent.TemplateClicked) })
        }
    }
}

@Composable
private fun DeckLabel(text: String) {
    Text(
        text = text,
        color = ColorTokens.CameraLabel,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.33.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun GalleryButton(
    uri: Uri?,
    badge: Int,
    onClick: () -> Unit,
    onThumbnailUnavailable: () -> Unit,
) {
    val cd = stringResource(R.string.cd_gps_camera_gallery)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = cd }
            .padding(4.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorTokens.CameraLabel),
                contentAlignment = Alignment.Center,
            ) {
                if (uri != null) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = { onThumbnailUnavailable() },
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(ColorTokens.CameraControlScrim.copy(alpha = 0.2f)),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = ColorTokens.CameraShutterCore.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            if (uri != null && badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(ColorTokens.CameraShutterCore),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badge.coerceAtMost(99).toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        DeckLabel(stringResource(R.string.gps_camera_gallery))
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    val cd = stringResource(R.string.cd_gps_camera_shutter)
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .border(4.dp, ColorTokens.CameraOnScrim.copy(alpha = 0.9f), CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = cd }
            .alpha(if (enabled) 1f else 0.6f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ColorTokens.CameraOnScrim),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ColorTokens.CameraShutterCore),
                contentAlignment = Alignment.Center,
            ) {
                DesignIcon(R.drawable.ic_gps_cam_shutter)
            }
        }
    }
}

@Composable
private fun TemplateButton(onClick: () -> Unit) {
    val cd = stringResource(R.string.cd_gps_camera_template)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = cd }
            .padding(4.dp),
    ) {
        Column(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorTokens.CameraTemplateScrim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            DesignIcon(R.drawable.ic_gps_cam_layers)
            Text(
                text = stringResource(R.string.gps_camera_template_standard),
                color = ColorTokens.CameraLabelAccent,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                letterSpacing = (-0.225).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        DeckLabel(stringResource(R.string.gps_camera_template))
    }
}

@Composable
private fun GridOverlay(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val stroke = 1.dp.toPx()
        val color = ColorTokens.CameraGridLine
        val thirdW = size.width / 3f
        val thirdH = size.height / 3f
        drawLine(color, Offset(thirdW, 0f), Offset(thirdW, size.height), stroke)
        drawLine(color, Offset(thirdW * 2, 0f), Offset(thirdW * 2, size.height), stroke)
        drawLine(color, Offset(0f, thirdH), Offset(size.width, thirdH), stroke)
        drawLine(color, Offset(0f, thirdH * 2), Offset(size.width, thirdH * 2), stroke)
    }
}

@Composable
private fun DefaultReticle(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(144.dp)) {
            val corner = 20.dp.toPx()
            val stroke = 2.dp.toPx()
            val half = stroke / 2f
            val w = size.width
            val h = size.height
            fun bracket(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(ColorTokens.CameraReticleCorner, Offset(x, y), Offset(x + dx * corner, y), stroke, StrokeCap.Square)
                drawLine(ColorTokens.CameraReticleCorner, Offset(x, y), Offset(x, y + dy * corner), stroke, StrokeCap.Square)
            }
            bracket(half, half, 1f, 1f)
            bracket(w - half, half, -1f, 1f)
            bracket(half, h - half, 1f, -1f)
            bracket(w - half, h - half, -1f, -1f)

            val cx = w / 2f
            val cy = h / 2f
            val tickInset = 8.dp.toPx()
            val tick = 12.dp.toPx()
            val tickColor = ColorTokens.CameraReticleTick
            drawLine(tickColor, Offset(tickInset, cy), Offset(tickInset + tick, cy), stroke)
            drawLine(tickColor, Offset(w - tickInset - tick, cy), Offset(w - tickInset, cy), stroke)
            drawLine(tickColor, Offset(cx, tickInset), Offset(cx, tickInset + tick), stroke)
            drawLine(tickColor, Offset(cx, h - tickInset - tick), Offset(cx, h - tickInset), stroke)

            val levelInset = 32.dp.toPx()
            val level = 8.dp.toPx()
            drawLine(ColorTokens.CameraLevelLine, Offset(levelInset, cy), Offset(levelInset + level, cy), stroke)
            drawLine(ColorTokens.CameraLevelLine, Offset(w - levelInset - level, cy), Offset(w - levelInset, cy), stroke)

            drawCircle(ColorTokens.CameraReticleDot, radius = 4.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

@Composable
private fun FocusReticle(point: FocusPoint, modifier: Modifier = Modifier) {
    Box(modifier) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val cx = size.width * point.xFraction
            val cy = size.height * point.yFraction
            val half = 36.dp.toPx()
            val len = 16.dp.toPx()
            val stroke = 2.dp.toPx()
            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(ColorTokens.CameraReticleCorner, Offset(x, y), Offset(x + dx * len, y), stroke)
                drawLine(ColorTokens.CameraReticleCorner, Offset(x, y), Offset(x, y + dy * len), stroke)
            }
            corner(cx - half, cy - half, 1f, 1f)
            corner(cx + half, cy - half, -1f, 1f)
            corner(cx - half, cy + half, 1f, -1f)
            corner(cx + half, cy + half, -1f, -1f)
            drawCircle(ColorTokens.CameraReticleDot, radius = 4.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Preview(showBackground = true, widthDp = 360, heightDp = 640, name = "Small")
@Composable
private fun GpsCameraScreenPreview() {
    AppTheme {
        GpsCameraScreen(
            uiState = GpsCameraUiState(
                cameraPermission = CameraPermissionState.Granted,
                satelliteCount = 18,
                accuracyLine = "± 5 m",
                headingLine = "342° NNW",
                headingAccurate = true,
                sessionPhotoCount = 1,
                stamp = StampRenderModel(
                    dateTimeLine = "14 Oct 2024 · 10:24:18 AM",
                    gmtBadge = "GMT+5",
                    addressLine = "Kashmir Highway, Sector G-9, Islamabad",
                    coordinatesLine = "33.6938° N, 73.0489° E",
                    altitudeLine = "518 m",
                    speedLine = "0 km/h",
                    pressureLine = "1012 hPa",
                    headingLine = "342° NNW",
                ),
            ),
            onEvent = {},
            onImageCaptureReady = {},
        )
    }
}
