package com.example.gpstracker.feature.voice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppButtonStyle
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
import com.example.gpstracker.core.map.toMapPoint
import com.example.gpstracker.core.places.DistanceFormatter
import com.example.gpstracker.core.routes.DurationFormatter

@Composable
fun VoiceScreen(
    uiState: VoiceUiState,
    onEvent: (VoiceEvent) -> Unit,
    modifier: Modifier = Modifier,
    floating: Boolean = false,
) {
    when (uiState.phase) {
        VoicePhase.Results -> VoiceResults(
            uiState = uiState,
            onEvent = onEvent,
            modifier = modifier,
        )
        VoicePhase.Idle, VoicePhase.Listening, VoicePhase.Resolving -> VoiceListening(
            uiState = uiState,
            onEvent = onEvent,
            floating = floating,
            modifier = modifier,
        )
    }
}

@Composable
private fun VoiceListening(
    uiState: VoiceUiState,
    onEvent: (VoiceEvent) -> Unit,
    floating: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (floating) Color.Black.copy(alpha = 0.45f) else colors.screenTintStart)
            .then(
                if (floating) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onEvent(VoiceEvent.CloseClicked) },
                    )
                } else {
                    Modifier
                },
            )
            .padding(if (floating) 0.dp else spacing.contentSide),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(if (floating) spacing.contentSide else 0.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .then(
                    if (floating) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                    } else {
                        Modifier
                    },
                )
                .clip(shape)
                .border(1.dp, colors.outlineDefault, shape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { onEvent(VoiceEvent.CloseClicked) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(spacing.touchTarget),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_voice_close),
                        tint = colors.textPrimary,
                    )
                }
            }
            ListeningRings(
                level = uiState.level,
                onMic = { onEvent(VoiceEvent.MicClicked) },
            )
            Spacer(Modifier.height(spacing.lg))
            if (uiState.phase == VoicePhase.Resolving) {
                CircularProgressIndicator()
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        text = stringResource(R.string.voice_listening),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(spacing.sm))
                Waveform(
                    level = uiState.level,
                    modifier = Modifier.height(28.dp),
                )
            }
        }
    }
}

@Composable
private fun ListeningRings(
    level: Float,
    onMic: () -> Unit,
) {
    val colors = LocalAppColors.current
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val ring = minOf(288.dp, maxWidth)
        Box(
            modifier = Modifier
                .size(ring)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.08f + 0.08f * level)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(ring * 0.72f)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(ring * 0.5f)
                        .clip(CircleShape)
                        .background(ColorTokens.Primary0),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        onClick = onMic,
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = colors.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = stringResource(R.string.cd_voice_mic),
                                tint = ColorTokens.TextOnPrimary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Waveform(
    level: Float,
    modifier: Modifier = Modifier,
) {
    val weights = listOf(0.35f, 0.95f, 0.5f, 1f, 0.5f, 0.45f, 0.55f)
    val colors = LocalAppColors.current
    val waveformDescription = stringResource(R.string.cd_voice_waveform)
    Row(
        modifier = modifier
            .semantics { contentDescription = waveformDescription }
            .clip(RoundedCornerShape(percent = 50))
            .background(ColorTokens.ScreenTintStart)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        weights.forEach { weight ->
            val height = (6 + (18 * level.coerceIn(0f, 1f) * weight)).dp
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(colors.primary),
            )
        }
    }
}

@Composable
private fun VoiceResults(
    uiState: VoiceUiState,
    onEvent: (VoiceEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val destination = uiState.destination
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = stringResource(R.string.voice_results_title),
            onNavigateUp = { onEvent(VoiceEvent.CloseClicked) },
            containerColor = colors.screenTintStart,
            actions = {
                IconButton(
                    onClick = { onEvent(VoiceEvent.CloseClicked) },
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_voice_close),
                    )
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.contentSide)
                .padding(bottom = spacing.lg),
        ) {
            TranscriptChip(
                transcript = uiState.transcript,
                onMic = { onEvent(VoiceEvent.MicClicked) },
                onClear = { onEvent(VoiceEvent.ClearTranscript) },
            )
            Spacer(Modifier.height(spacing.sm))
            if (destination != null) {
                ResultsMap(uiState = uiState)
                Spacer(Modifier.height(spacing.md))
                DestinationCard(uiState = uiState, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun TranscriptChip(
    transcript: String,
    onMic: () -> Unit,
    onClear: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(percent = 50)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMic, modifier = Modifier.size(spacing.touchTarget)) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.cd_voice_mic),
                    tint = colors.primary,
                )
            }
            Text(
                text = stringResource(R.string.voice_transcript_quote, transcript),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
            IconButton(onClick = onClear, modifier = Modifier.size(spacing.touchTarget)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_voice_clear),
                    tint = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun ResultsMap(uiState: VoiceUiState) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val destination = uiState.destination ?: return
    val points = uiState.plan?.points.orEmpty()
    val camera = rememberMapCameraState(destination.latitude, destination.longitude, DEFAULT_LOCATION_ZOOM)
    LaunchedEffect(uiState.plan?.encodedPolyline) {
        if (points.size >= 2) {
            camera.moveToBounds(points.map { it.toMapPoint() }, paddingPx = 48)
        }
    }
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            AppMap(
                mapType = AppMapType.Normal,
                camera = camera,
                showUserLocation = false,
                pins = listOf(
                    MapPin("dest", destination.latitude, destination.longitude),
                ),
                route = points.map { it.toMapPoint() },
                modifier = Modifier.fillMaxSize(),
            )
            uiState.via?.let { via ->
                Surface(
                    modifier = Modifier
                        .padding(spacing.sm)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Text(
                        text = stringResource(R.string.routes_via, via.name),
                        style = AppTextStyles.chipText,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xxs),
                    )
                }
            }
            val duration = uiState.duration
            val distance = uiState.distance
            if (duration != null && distance != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(spacing.sm),
                ) {
                    Text(
                        text = durationLabel(duration),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = ColorTokens.TextOnPrimary,
                    )
                    Text(
                        text = distanceLabel(distance),
                        style = AppTextStyles.chipText,
                        color = ColorTokens.TextOnPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationCard(
    uiState: VoiceUiState,
    onEvent: (VoiceEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current
    val destination = uiState.destination ?: return
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(spacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Place,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(spacing.xs))
                        Text(
                            text = stringResource(R.string.routes_primary_destination),
                            style = AppTextStyles.chipText,
                            color = colors.primary,
                        )
                    }
                    Text(
                        text = destination.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                    if (destination.address.isNotBlank()) {
                        Text(
                            text = destination.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                    }
                }
                IconButton(
                    onClick = { onEvent(VoiceEvent.SaveClicked) },
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        imageVector = if (uiState.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = stringResource(
                            if (uiState.isSaved) R.string.cd_routes_unsave else R.string.cd_routes_save,
                        ),
                        tint = colors.primary,
                    )
                }
            }
            Spacer(Modifier.height(spacing.md))
            AppButton(
                text = stringResource(R.string.routes_start_navigation),
                onClick = { onEvent(VoiceEvent.StartNavigationClicked) },
                fillMaxWidth = true,
                enabled = uiState.canStart,
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                leadingIconContentDescription = stringResource(R.string.routes_start_navigation),
            )
            Spacer(Modifier.height(spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                AppButton(
                    text = stringResource(R.string.voice_preview_route),
                    onClick = { onEvent(VoiceEvent.PreviewClicked) },
                    style = AppButtonStyle.Outlined,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = spacing.touchTarget),
                )
                AppButton(
                    text = stringResource(R.string.routes_add_stop),
                    onClick = { onEvent(VoiceEvent.AddStopClicked) },
                    style = AppButtonStyle.Outlined,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = spacing.touchTarget),
                )
            }
        }
    }
}

@Composable
private fun durationLabel(duration: DurationFormatter.Display): String = when {
    duration.hours == 0 -> stringResource(R.string.routes_duration_min, duration.totalMinutes)
    duration.remainingMinutes == 0 -> stringResource(R.string.routes_duration_hours, duration.hours)
    else -> stringResource(
        R.string.routes_duration_hours_min,
        duration.hours,
        duration.remainingMinutes,
    )
}

@Composable
private fun distanceLabel(distance: DistanceFormatter.DistanceDisplay): String {
    val unit = when (distance.unit) {
        DistanceFormatter.DistanceDisplayUnit.Km -> stringResource(R.string.settings_unit_km)
        DistanceFormatter.DistanceDisplayUnit.M -> stringResource(R.string.distance_unit_m)
        DistanceFormatter.DistanceDisplayUnit.Mi -> stringResource(R.string.settings_unit_mi)
        DistanceFormatter.DistanceDisplayUnit.Ft -> stringResource(R.string.distance_unit_ft)
    }
    return stringResource(R.string.nearby_distance_format, distance.valueText, unit)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 700)
@Composable
private fun VoiceListeningPreview() {
    AppTheme {
        VoiceScreen(
            uiState = VoiceUiState(phase = VoicePhase.Listening, level = 0.6f),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 800)
@Composable
private fun VoiceResultsPreview() {
    AppTheme {
        VoiceScreen(
            uiState = VoiceUiState(
                phase = VoicePhase.Results,
                transcript = "Navigate to Faisal Mosque via Blue Area",
                destination = VoicePlace(
                    name = "Faisal Mosque",
                    address = "Shah Faisal Ave, E-8, Islamabad",
                    placeId = "pid",
                    latitude = 33.7295,
                    longitude = 73.0372,
                ),
                via = VoicePlace(
                    name = "Blue Area Corridor",
                    address = "",
                    placeId = "via",
                    latitude = 33.71,
                    longitude = 73.06,
                ),
                duration = DurationFormatter.Display(totalMinutes = 14, hours = 0, remainingMinutes = 14),
                distance = DistanceFormatter.DistanceDisplay("6.4", DistanceFormatter.DistanceDisplayUnit.Km),
                canStart = true,
            ),
            onEvent = {},
        )
    }
}
