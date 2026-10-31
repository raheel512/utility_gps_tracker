package com.example.gpstracker.feature.compass.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.RobotoFamily
import com.example.gpstracker.feature.compass.CompassHemisphere
import com.example.gpstracker.feature.compass.formatAltitudeMeters
import com.example.gpstracker.feature.compass.formatDms
import com.example.gpstracker.feature.compass.formatHeading
import com.example.gpstracker.feature.compass.latitudeHemisphere
import com.example.gpstracker.feature.compass.longitudeHemisphere

private val MutedInk = Color(0xFF94A3B8)
private val ThemeIdleInk = Color(0xFF99A2AF)
private val PinWell = Color(0xFFF8FAFC)
private val CardShape = RoundedCornerShape(12.dp)

@Composable
fun CompassScreen(
    uiState: CompassUiState,
    onEvent: (CompassEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = stringResource(R.string.screen_compass),
            onNavigateUp = onNavigateUp,
            containerColor = colors.screenTintStart,
            showDivider = false,
            actions = {
                IconButton(
                    onClick = { onEvent(CompassEvent.RefreshLocationClicked) },
                    enabled = !uiState.locating,
                    modifier = Modifier.defaultMinSize(
                        minWidth = spacing.touchTarget,
                        minHeight = spacing.touchTarget,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PinWell),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.locating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = colors.primary,
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_find_location_map_pinned),
                                contentDescription = stringResource(R.string.cd_compass_refresh_location),
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified,
                            )
                        }
                    }
                }
            },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = headingText(uiState),
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                color = colors.textPrimary,
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.9).sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val dial = minOf(304.dp, maxWidth)
                CompassDial(
                    headingDegrees = uiState.headingDegrees,
                    theme = uiState.theme,
                    modifier = Modifier.size(dial),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            ReadingsCard(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                altitudeMeters = uiState.altitudeMeters,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            ThemeRow(
                selected = uiState.theme,
                onSelect = { onEvent(CompassEvent.ThemeSelected(it)) },
            )
        }
    }
}

@Composable
private fun headingText(uiState: CompassUiState): String {
    val degrees = uiState.headingDegrees
    val cardinal = uiState.cardinal
    return if (degrees == null || cardinal == null) {
        stringResource(R.string.compass_no_reading)
    } else {
        formatHeading(degrees, cardinal)
    }
}

@Composable
private fun ReadingsCard(
    latitude: Double?,
    longitude: Double?,
    altitudeMeters: Double?,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .height(76.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, colors.outlineDefault, CardShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoordinateColumn(
            value = latitude?.let(::formatDms),
            hemisphere = latitude?.let(::latitudeHemisphere),
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(colors.outlineDefault),
        )
        AltitudeColumn(
            meters = altitudeMeters,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(colors.outlineDefault),
        )
        CoordinateColumn(
            value = longitude?.let(::formatDms),
            hemisphere = longitude?.let(::longitudeHemisphere),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CoordinateColumn(
    value: String?,
    hemisphere: CompassHemisphere?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value ?: stringResource(R.string.compass_no_reading),
            color = LocalAppColors.current.textPrimary,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 17.sp,
            letterSpacing = (-0.425).sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (hemisphere != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hemisphere.label(),
                color = MutedInk,
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 1.1.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AltitudeColumn(
    meters: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_compass_altitude_down),
            contentDescription = stringResource(R.string.cd_compass_altitude),
            modifier = Modifier.size(16.dp),
            tint = LocalAppColors.current.textSecondary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = meters?.let(::formatAltitudeMeters) ?: stringResource(R.string.compass_no_reading),
            color = LocalAppColors.current.textSecondary,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = (-0.3).sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun ThemeRow(
    selected: CompassTheme,
    onSelect: (CompassTheme) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CompassTheme.entries.forEach { theme ->
            ThemeChip(
                theme = theme,
                selected = theme == selected,
                onClick = { onSelect(theme) },
            )
        }
    }
}

@Composable
private fun ThemeChip(
    theme: CompassTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val label = theme.label()
    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.outlineDefault,
                shape = CardShape,
            )
            .clickable(onClick = onClick)
            .semantics { this.selected = selected }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(theme.thumbnailRes()),
            contentDescription = null,
            modifier = Modifier.size(width = 72.dp, height = 74.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) LocalAppColors.current.textPrimary else ThemeIdleInk,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = (-0.3).sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CompassHemisphere.label(): String = when (this) {
    CompassHemisphere.North -> stringResource(R.string.compass_hemisphere_north)
    CompassHemisphere.South -> stringResource(R.string.compass_hemisphere_south)
    CompassHemisphere.East -> stringResource(R.string.compass_hemisphere_east)
    CompassHemisphere.West -> stringResource(R.string.compass_hemisphere_west)
}

@Composable
private fun CompassTheme.label(): String = when (this) {
    CompassTheme.Classic -> stringResource(R.string.compass_theme_classic)
    CompassTheme.Night -> stringResource(R.string.compass_theme_night)
    CompassTheme.Terrain -> stringResource(R.string.compass_theme_terrain)
    CompassTheme.Nautical -> stringResource(R.string.compass_theme_nautical)
}

private fun CompassTheme.thumbnailRes(): Int = when (this) {
    CompassTheme.Classic -> R.drawable.compass_face_classic
    CompassTheme.Night -> R.drawable.compass_face_night
    CompassTheme.Terrain -> R.drawable.compass_face_terrain
    CompassTheme.Nautical -> R.drawable.compass_face_nautical
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CompassScreenPreview() {
    AppTheme {
        CompassScreen(
            uiState = CompassUiState(
                headingDegrees = 0f,
                cardinal = "N",
                latitude = 40.0 + 5.0 / 60.0 + 35.0 / 3600.0,
                longitude = 120.0 + 2.0 / 60.0 + 1.0 / 3600.0,
                altitudeMeters = 991.0,
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
