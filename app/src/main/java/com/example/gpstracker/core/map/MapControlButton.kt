package com.example.gpstracker.core.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.appShadow

private val ControlShape = RoundedCornerShape(12.dp)

/** Floating map control: 44dp in My Location, 40dp in Satellite View; touch target stays ≥ 48dp. */
@Composable
fun MapControlButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = LocalAppColors.current.mapControlSurface,
    showShadow: Boolean = true,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = ControlShape,
        color = containerColor,
        contentColor = LocalAppColors.current.mapControlIcon,
        modifier = modifier
            .size(size)
            .then(
                if (showShadow) Modifier.appShadow(AppShadow.MapControl, ControlShape)
                else Modifier,
            ),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

/** Stacked zoom in / zoom out pair with a hairline divider (My Location "Zoom Buttons Group"). */
@Composable
fun MapZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    zoomInContent: @Composable () -> Unit,
    zoomOutContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val colors = LocalAppColors.current
    Surface(
        shape = ControlShape,
        color = colors.mapControlSurface,
        contentColor = colors.mapControlIcon,
        modifier = modifier
            .width(size)
            .appShadow(AppShadow.MapControl, ControlShape),
    ) {
        Column {
            Surface(onClick = onZoomIn, color = Color.Transparent, modifier = Modifier.size(size)) {
                Box(contentAlignment = Alignment.Center) { zoomInContent() }
            }
            HorizontalDivider(color = colors.mapControlDivider)
            Surface(onClick = onZoomOut, color = Color.Transparent, modifier = Modifier.size(size)) {
                Box(contentAlignment = Alignment.Center) { zoomOutContent() }
            }
        }
    }
}
