package com.example.gpstracker.feature.emergency.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.feature.country.ui.CountryFlag

private val CardShape = RoundedCornerShape(16.dp)
private val SoftRose = Color(0xFFFFF1F2)
private val SoftRoseBorder = Color(0xFFFFE4E6)
private val EmergencyInk = Color(0xFFBE123C)
private val ServiceWell = Color(0xFFF8FAFC)

@Composable
fun EmergencyScreen(
    uiState: EmergencyUiState,
    onEvent: (EmergencyEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val screenBrush = Brush.verticalGradient(
        listOf(appColors.screenTintStart, appColors.screenTintEnd),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBrush),
    ) {
        AppTopBar(
            title = stringResource(R.string.screen_emergency),
            onNavigateUp = onNavigateUp,
            containerColor = appColors.screenTintStart,
            showDivider = false,
            actions = {
                IconButton(
                    onClick = { onEvent(EmergencyEvent.Refresh) },
                    enabled = !uiState.loading,
                    modifier = Modifier.defaultMinSize(
                        minWidth = spacing.touchTarget,
                        minHeight = spacing.touchTarget,
                    ),
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = appColors.primary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.cd_emergency_refresh),
                            tint = appColors.textPrimary,
                        )
                    }
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.contentSide)
                .padding(bottom = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            CountryBanner(uiState = uiState)

            Text(
                text = stringResource(R.string.emergency_help_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textMuted,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            SosButton(
                number = uiState.generalNumber ?: uiState.policeNumber,
                enabled = true,
                onClick = { onEvent(EmergencyEvent.DialGeneral) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                ServiceDialCard(
                    title = stringResource(R.string.emergency_police),
                    number = uiState.policeNumber,
                    icon = Icons.Filled.LocalPolice,
                    onClick = { onEvent(EmergencyEvent.DialPolice) },
                    modifier = Modifier.weight(1f),
                )
                ServiceDialCard(
                    title = stringResource(R.string.emergency_ambulance),
                    number = uiState.ambulanceNumber,
                    icon = Icons.Filled.LocalHospital,
                    onClick = { onEvent(EmergencyEvent.DialAmbulance) },
                    modifier = Modifier.weight(1f),
                )
                ServiceDialCard(
                    title = stringResource(R.string.emergency_fire),
                    number = uiState.fireNumber,
                    icon = Icons.Filled.LocalFireDepartment,
                    onClick = { onEvent(EmergencyEvent.DialFire) },
                    modifier = Modifier.weight(1f),
                )
            }

            LocationCard(uiState = uiState, onEvent = onEvent)

            AppButton(
                text = stringResource(R.string.emergency_share_location),
                onClick = { onEvent(EmergencyEvent.ShareLocation) },
                enabled = uiState.hasFix,
                fillMaxWidth = true,
                leadingIcon = Icons.Filled.Share,
                leadingIconContentDescription = null,
            )

            NearbySection(onEvent = onEvent)

            Text(
                text = stringResource(R.string.emergency_dialer_note),
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CountryBanner(uiState: EmergencyUiState) {
    val appColors = LocalAppColors.current
    val spacing = LocalSpacing.current
    val title = when {
        !uiState.countryName.isNullOrBlank() -> uiState.countryName
        !uiState.countryIso.isNullOrBlank() -> uiState.countryIso
        else -> stringResource(R.string.emergency_country_unknown)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, CardShape),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (!uiState.countryIso.isNullOrBlank()) {
                CountryFlag(
                    iso = uiState.countryIso,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ServiceWell),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = appColors.primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.emergency_numbers_for),
                    style = MaterialTheme.typography.labelMedium,
                    color = appColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = appColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SosButton(
    number: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .appShadow(AppShadow.Card, RoundedCornerShape(percent = 50)),
        shape = RoundedCornerShape(percent = 50),
        color = EmergencyInk,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.size(spacing.sm))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.emergency_call_sos),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ServiceDialCard(
    title: String,
    number: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 112.dp)
            .appShadow(AppShadow.Card, CardShape),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftRoseBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftRose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmergencyInk,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = appColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = number,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = EmergencyInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LocationCard(
    uiState: EmergencyUiState,
    onEvent: (EmergencyEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Card, CardShape),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.emergency_my_location),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = appColors.textPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(
                    onClick = { onEvent(EmergencyEvent.CopyLocation) },
                    enabled = uiState.hasFix,
                    modifier = Modifier.size(spacing.touchTarget),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.cd_emergency_copy_location),
                        tint = if (uiState.hasFix) appColors.primary else appColors.textMuted,
                    )
                }
            }
            if (uiState.loading && !uiState.hasFix) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = appColors.primary,
                    )
                    Text(
                        text = stringResource(R.string.emergency_locating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textMuted,
                    )
                }
            } else if (!uiState.hasFix) {
                Text(
                    text = stringResource(R.string.emergency_location_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                uiState.placeLine?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                uiState.coordinates?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                uiState.accuracyLabel?.let {
                    Text(
                        text = stringResource(R.string.emergency_accuracy, it),
                        style = MaterialTheme.typography.labelMedium,
                        color = appColors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbySection(onEvent: (EmergencyEvent) -> Unit) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            text = stringResource(R.string.emergency_nearby_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = appColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            EmergencyNearbyKind.entries.forEach { kind ->
                NearbyChip(
                    kind = kind,
                    onClick = { onEvent(EmergencyEvent.OpenNearby(kind)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NearbyChip(
    kind: EmergencyNearbyKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val tint = when (kind) {
        EmergencyNearbyKind.Hospital -> appColors.hospitals
        EmergencyNearbyKind.Police -> appColors.banks
        EmergencyNearbyKind.Pharmacy -> appColors.emergency
        EmergencyNearbyKind.Petrol -> appColors.cafes
    }
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, appColors.cardBorder, CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = spacing.sm, horizontal = spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.background)
                .then(
                    tint.border?.let { borderColor ->
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    } ?: Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(kind.iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = stringResource(kind.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = appColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EmergencyScreenPreview() {
    AppTheme {
        EmergencyScreen(
            uiState = EmergencyUiState(
                loading = false,
                countryIso = "PK",
                countryName = "Pakistan",
                placeLine = "F-9, Islamabad, Pakistan",
                coordinates = "33.6938° N, 73.0489° E",
                accuracyLabel = "12 m",
                hasFix = true,
                policeNumber = "15",
                ambulanceNumber = "1122",
                fireNumber = "16",
                generalNumber = "1122",
            ),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
