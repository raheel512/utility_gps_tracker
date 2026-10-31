package com.example.gpstracker.feature.weather.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.RobotoFamily

private val FairSky = listOf(
    Color(0xFF136CC2),
    Color(0xFF298CEE),
    Color(0xFF66B5F8),
    Color(0xFFB3DCFF),
    Color(0xFFD8EDFF),
)
private val RainSky = listOf(Color(0xFF458ECC), Color(0xFF449CF1))
private val ConditionYellow = Color(0xFFFFDE43)
private val MetricLabel = Color(0xFF64748B)
private val MetricValue = Color(0xFF1E293B)
private val CardFill = Color(0xF0F6FAFF)
private val Page = Color(0xFFF4F7FB)
private val CardShape = RoundedCornerShape(28.dp)
private val SkyShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
private val CardOverlap = 72.dp

@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onEvent: (WeatherEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val raining = uiState.reading?.kind?.rainSky == true
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Page)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            WeatherSky(
                raining = raining,
                modifier = Modifier
                    .matchParentSize()
                    .clip(SkyShape),
            )
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(bottom = 96.dp),
            ) {
                WeatherHeader(
                    loading = uiState.loading,
                    onNavigateUp = onNavigateUp,
                    onRefresh = { onEvent(WeatherEvent.RefreshClicked) },
                )
                WeatherHero(
                    uiState = uiState,
                    modifier = Modifier.padding(top = 28.dp, bottom = 20.dp),
                )
            }
        }
        WeatherMetricsCard(
            reading = uiState.reading,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .pullUp(CardOverlap)
                .padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun WeatherSky(
    raining: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(if (raining) RainSky else FairSky),
        ),
    ) {
        if (raining) {
            Image(
                painter = painterResource(R.drawable.ic_weather_rain_cloud),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 56.dp)
                    .size(128.dp)
                    .graphicsLayer { alpha = 0.8f },
                contentScale = ContentScale.Fit,
            )
            Image(
                painter = painterResource(R.drawable.ic_weather_rain_cloud),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 28.dp)
                    .size(128.dp)
                    .graphicsLayer { alpha = 0.8f },
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun WeatherHeader(
    loading: Boolean,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp)
            .defaultMinSize(minHeight = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateUp,
            modifier = Modifier.defaultMinSize(spacing.touchTarget, spacing.touchTarget),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_navigate_up),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.screen_weather),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = Color.White,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp,
        )
        IconButton(
            onClick = onRefresh,
            enabled = !loading,
            modifier = Modifier.defaultMinSize(spacing.touchTarget, spacing.touchTarget),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_find_location_map_pinned),
                    contentDescription = stringResource(R.string.cd_weather_refresh),
                    modifier = Modifier.size(24.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun WeatherHero(
    uiState: WeatherUiState,
    modifier: Modifier = Modifier,
) {
    val reading = uiState.reading
    val raining = reading?.kind?.rainSky == true
    val empty = stringResource(R.string.compass_no_reading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (uiState.loading && reading == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(vertical = 28.dp)
                    .size(36.dp),
                color = Color.White,
                strokeWidth = 3.dp,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Image(
                        painter = painterResource(R.drawable.ic_weather_cloud),
                        contentDescription = null,
                        modifier = Modifier.size(width = 64.dp, height = 48.dp),
                        contentScale = ContentScale.Fit,
                    )
                    if (raining) {
                        Image(
                            painter = painterResource(R.drawable.ic_weather_humidity),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 8.dp)
                                .size(width = 12.dp, height = 16.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                Text(
                    text = reading?.temperature ?: empty,
                    modifier = Modifier.padding(start = 12.dp),
                    color = Color.White,
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 60.sp,
                    lineHeight = 60.sp,
                    letterSpacing = (-3).sp,
                )
                if (!raining) {
                    SunGlyph(modifier = Modifier.padding(start = 10.dp))
                }
            }
            Text(
                text = reading?.let { stringResource(it.kind.labelRes) } ?: empty,
                modifier = Modifier.padding(top = 6.dp),
                color = ConditionYellow,
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.45.sp,
            )
        }
        Text(
            text = uiState.placeName ?: empty,
            modifier = Modifier.padding(top = 2.dp),
            color = Color.White,
            fontFamily = RobotoFamily,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = reading?.let { stringResource(R.string.weather_last_update, it.updatedAt) } ?: empty,
            color = Color.White.copy(alpha = 0.8f),
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.3.sp,
        )
        if (uiState.failed && reading == null) {
            Text(
                text = stringResource(R.string.weather_unavailable),
                modifier = Modifier.padding(top = 8.dp),
                color = Color.White,
                fontFamily = RobotoFamily,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SunGlyph(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .requiredSize(150.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xCCFFF1B4), Color(0x55FFF2A8), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xBFFFF2A8),
                    spotColor = Color(0xBFFFF2A8),
                )
                .background(Color(0xFFFFFBEB), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFDE68A), Color.White)),
                        CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun WeatherMetricsCard(
    reading: WeatherReading?,
    modifier: Modifier = Modifier,
) {
    val empty = stringResource(R.string.compass_no_reading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 28.dp,
                shape = CardShape,
                ambientColor = Color(0x73003078),
                spotColor = Color(0x73003078),
            )
            .clip(CardShape)
            .background(CardFill)
            .border(1.dp, Color.White.copy(alpha = 0.8f), CardShape)
            .padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell(
                icon = R.drawable.ic_weather_wind,
                iconWidth = 20.dp,
                iconHeight = 20.dp,
                well = Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF3B82F6))),
                label = stringResource(R.string.weather_wind),
                value = reading?.let { stringResource(R.string.weather_wind_value, it.wind) } ?: empty,
                modifier = Modifier.weight(1f),
            )
            MetricCell(
                icon = R.drawable.ic_weather_humidity,
                iconWidth = 15.dp,
                iconHeight = 20.dp,
                well = Brush.verticalGradient(listOf(Color(0xFF22D3EE), Color(0xFF3B82F6))),
                label = stringResource(R.string.weather_humidity),
                value = reading?.let { stringResource(R.string.weather_percent_value, it.humidityPercent) } ?: empty,
                modifier = Modifier.weight(1f),
            )
            MetricCell(
                icon = R.drawable.ic_weather_pressure,
                iconWidth = 18.dp,
                iconHeight = 18.dp,
                rotateDegrees = 45f,
                well = Brush.verticalGradient(listOf(Color(0xFFA78BFA), Color(0xFF4F46E5))),
                label = stringResource(R.string.weather_pressure),
                value = reading?.let { stringResource(R.string.weather_pressure_value, it.pressure) } ?: empty,
                modifier = Modifier.weight(1f),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCell(
                icon = R.drawable.ic_weather_cloudiness,
                iconWidth = 22.5.dp,
                iconHeight = 15.75.dp,
                well = Brush.verticalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF0EA5E9))),
                label = stringResource(R.string.weather_cloudiness),
                value = reading?.let { stringResource(R.string.weather_percent_value, it.cloudPercent) } ?: empty,
                modifier = Modifier.weight(1f),
            )
            MetricCell(
                icon = R.drawable.ic_weather_sunrise,
                iconWidth = 20.dp,
                iconHeight = 20.dp,
                well = Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFFB923C))),
                label = stringResource(R.string.weather_sunrise),
                value = reading?.sunrise ?: empty,
                modifier = Modifier.weight(1f),
            )
            MetricCell(
                icon = R.drawable.ic_weather_sunset,
                iconWidth = 22.5.dp,
                iconHeight = 18.dp,
                well = Brush.verticalGradient(listOf(Color(0xFFFB7185), Color(0xFFA855F7))),
                label = stringResource(R.string.weather_sunset),
                value = reading?.sunset ?: empty,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricCell(
    icon: Int,
    iconWidth: Dp,
    iconHeight: Dp,
    well: Brush,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    rotateDegrees: Float = 0f,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(56.dp)
                .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(well),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(width = iconWidth, height = iconHeight)
                    .graphicsLayer { rotationZ = rotateDegrees },
                contentScale = ContentScale.Fit,
            )
        }
        Text(
            text = label,
            color = MetricLabel,
            fontFamily = RobotoFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 2.dp),
            color = MetricValue,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 19.5.sp,
            letterSpacing = (-0.325).sp,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Modifier.pullUp(overlap: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val overlapPx = overlap.roundToPx()
    val height = (placeable.height - overlapPx).coerceAtLeast(0)
    layout(placeable.width, height) {
        placeable.placeRelative(0, -overlapPx)
    }
}
