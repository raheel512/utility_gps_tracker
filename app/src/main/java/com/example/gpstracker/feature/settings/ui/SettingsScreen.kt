package com.example.gpstracker.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.data.DistanceUnit
import com.example.gpstracker.core.data.ThemeMode
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.component.RateAppDialog
import com.example.gpstracker.core.designsystem.component.SettingsChevron
import com.example.gpstracker.core.designsystem.component.SettingsExternalLinkIcon
import com.example.gpstracker.core.designsystem.component.SettingsRow
import com.example.gpstracker.core.designsystem.component.SettingsRowDivider
import com.example.gpstracker.core.designsystem.component.SettingsSection
import com.example.gpstracker.core.designsystem.component.SettingsSwitch
import com.example.gpstracker.core.designsystem.component.SettingsTrailingIcon
import com.example.gpstracker.core.designsystem.component.SettingsUnitSegmentedControl
import com.example.gpstracker.core.designsystem.component.SettingsValuePill
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.PillShape

/** Figma Theme mode Cancel border/text `#B3AEAE`. */
private val ThemeCancelBorder = Color(0xFFB3AEAE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val languageLabel = rememberLanguageDisplayName(uiState.languageCode)
    val screenBrush = Brush.verticalGradient(
        listOf(appColors.screenTintStart, appColors.screenTintEnd),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBrush),
    ) {
        AppTopBar(
            title = stringResource(R.string.screen_settings),
            containerColor = appColors.screenTintStart,
            statusBarAware = true,
            actions = {
                IconButton(
                    // TODO(route): help destination has no screenshot
                    onClick = { onEvent(SettingsEvent.HelpClick) },
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_settings_help),
                        contentDescription = stringResource(R.string.cd_settings_help),
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.contentSide)
                .padding(top = spacing.md, bottom = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            PreferencesSection(
                uiState = uiState,
                languageLabel = languageLabel,
                onEvent = onEvent,
            )
            AppExperienceSection(uiState = uiState, onEvent = onEvent)
            LegalSection(onEvent = onEvent)
        }
    }

    if (uiState.themePickerVisible) {
        ThemeModeSheet(
            draft = uiState.draftThemeMode,
            onEvent = onEvent,
        )
    }

    if (uiState.rateDialogVisible) {
        RateAppDialog(
            onRated = { stars -> onEvent(SettingsEvent.RateStarSelected(stars)) },
            onDismiss = { onEvent(SettingsEvent.RateDialogDismiss) },
        )
    }
}

@Composable
private fun PreferencesSection(
    uiState: SettingsUiState,
    languageLabel: String,
    onEvent: (SettingsEvent) -> Unit,
) {
    val appColors = LocalAppColors.current
    SettingsSection(title = stringResource(R.string.settings_section_preferences)) {
        SettingsRow(
            iconRes = R.drawable.ic_settings_moon,
            iconContentDescription = stringResource(R.string.cd_settings_theme),
            title = stringResource(R.string.settings_theme_title),
            subtitle = stringResource(R.string.settings_theme_subtitle),
            onClick = { onEvent(SettingsEvent.ThemeClick) },
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingsValuePill(text = themePillLabel(uiState.themeMode))
                    SettingsChevron()
                }
            },
        )
        SettingsRowDivider()
        SettingsRow(
            iconRes = R.drawable.ic_settings_globe,
            iconContentDescription = stringResource(R.string.cd_settings_language),
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_subtitle),
            onClick = { onEvent(SettingsEvent.LanguageClick) },
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = languageLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.textMuted,
                    )
                    SettingsChevron()
                }
            },
        )
        SettingsRowDivider()
        SettingsRow(
            iconRes = R.drawable.ic_settings_route,
            iconContentDescription = stringResource(R.string.cd_settings_routing),
            title = stringResource(R.string.settings_routing_title),
            subtitle = stringResource(R.string.settings_routing_subtitle),
            // TODO(route): Navigation Routing screen has no screenshot
            onClick = { onEvent(SettingsEvent.NavigationRoutingClick) },
            trailing = { SettingsChevron() },
        )
    }
}

@Composable
private fun AppExperienceSection(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_section_app_experience)) {
        SettingsRow(
            iconRes = R.drawable.ic_settings_bell,
            iconContentDescription = stringResource(R.string.cd_settings_road_alerts),
            title = stringResource(R.string.settings_road_alerts_title),
            subtitle = stringResource(R.string.settings_road_alerts_subtitle),
            trailing = {
                SettingsSwitch(
                    checked = uiState.roadAlertsEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.RoadAlertsToggle(it)) },
                    contentDescription = stringResource(R.string.cd_settings_road_alerts),
                )
            },
        )
        SettingsRowDivider()
        SettingsRow(
            iconRes = R.drawable.ic_settings_volume,
            iconContentDescription = stringResource(R.string.cd_settings_voice),
            title = stringResource(R.string.settings_voice_title),
            subtitle = stringResource(R.string.settings_voice_subtitle),
            trailing = {
                SettingsSwitch(
                    checked = uiState.voiceGuidanceEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.VoiceGuidanceToggle(it)) },
                    contentDescription = stringResource(R.string.cd_settings_voice),
                )
            },
        )
        SettingsRowDivider()
        SettingsRow(
            iconRes = R.drawable.ic_settings_gauge,
            iconContentDescription = stringResource(R.string.cd_settings_units),
            title = stringResource(R.string.settings_units_title),
            subtitle = stringResource(R.string.settings_units_subtitle),
            trailing = {
                SettingsUnitSegmentedControl(
                    selectedIsKm = uiState.distanceUnit == DistanceUnit.KM,
                    onSelectKm = { onEvent(SettingsEvent.DistanceUnitSelect(DistanceUnit.KM)) },
                    onSelectMi = { onEvent(SettingsEvent.DistanceUnitSelect(DistanceUnit.MI)) },
                    kmLabel = stringResource(R.string.settings_unit_km),
                    miLabel = stringResource(R.string.settings_unit_mi),
                    contentDescription = stringResource(R.string.cd_settings_units),
                )
            },
        )
    }
}

@Composable
private fun LegalSection(onEvent: (SettingsEvent) -> Unit) {
    SettingsSection(title = stringResource(R.string.settings_section_legal)) {
        SettingsRow(
            iconRes = R.drawable.ic_settings_shield,
            iconContentDescription = stringResource(R.string.cd_settings_privacy),
            title = stringResource(R.string.settings_privacy_title),
            subtitle = stringResource(R.string.settings_privacy_subtitle),
            onClick = { onEvent(SettingsEvent.PrivacyPolicyClick) },
            trailing = {
                SettingsExternalLinkIcon(
                    contentDescription = stringResource(R.string.cd_settings_open_external),
                )
            },
        )
        SettingsRowDivider()
        SettingsRow(
            iconRes = R.drawable.ic_settings_star,
            iconContentDescription = stringResource(R.string.cd_settings_rate),
            title = stringResource(R.string.settings_rate_title),
            subtitle = stringResource(R.string.settings_rate_subtitle),
            onClick = { onEvent(SettingsEvent.RateClick) },
            trailing = { SettingsChevron() },
        )
        SettingsRowDivider()
        SettingsRow(
            iconRes = R.drawable.ic_settings_share,
            iconContentDescription = stringResource(R.string.cd_settings_invite),
            title = stringResource(R.string.settings_invite_title),
            subtitle = stringResource(R.string.settings_invite_subtitle),
            onClick = { onEvent(SettingsEvent.InviteClick) },
            trailing = {
                SettingsTrailingIcon(
                    iconRes = R.drawable.ic_settings_copy,
                    contentDescription = stringResource(R.string.cd_settings_share),
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSheet(
    draft: ThemeMode,
    onEvent: (SettingsEvent) -> Unit,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val options = listOf(
        ThemeMode.Light to R.string.settings_theme_option_light,
        ThemeMode.Dark to R.string.settings_theme_option_dark,
        ThemeMode.Auto to R.string.settings_theme_option_auto,
    )

    ModalBottomSheet(
        onDismissRequest = { onEvent(SettingsEvent.ThemePickerDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(bottom = spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_sheet_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = appColors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onEvent(SettingsEvent.ThemePickerDismiss) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_settings_close),
                        contentDescription = stringResource(R.string.cd_settings_theme_close),
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = spacing.xs),
                thickness = 1.dp,
                color = Color(0xFFF2F3FF),
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { (mode, labelRes) ->
                    val selected = draft == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { onEvent(SettingsEvent.ThemeDraftSelect(mode)) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = appColors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        RadioButton(
                            selected = selected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = appColors.primary,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            // Figma: Cancel + Save side-by-side, 44dp tall, 26dp gap, pill shape
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(26.dp),
            ) {
                ThemeSheetButton(
                    text = stringResource(R.string.settings_theme_cancel),
                    onClick = { onEvent(SettingsEvent.ThemePickerDismiss) },
                    filled = false,
                    modifier = Modifier.weight(1f),
                )
                ThemeSheetButton(
                    text = stringResource(R.string.settings_theme_save),
                    onClick = { onEvent(SettingsEvent.ThemePickerSave) },
                    filled = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThemeSheetButton(
    text: String,
    onClick: () -> Unit,
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    val shape = PillShape
    val background = if (filled) appColors.primary else Color.Transparent
    val borderColor = if (filled) Color.Transparent else ThemeCancelBorder
    val textColor = if (filled) Color.White else ThemeCancelBorder
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = shape,
        color = background,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
            )
        }
    }
}

@Composable
private fun rememberLanguageDisplayName(code: String): String {
    val names = stringArrayResource(R.array.language_english_names)
    val codes = stringArrayResource(R.array.language_codes)
    return remember(code, names, codes) {
        val index = codes.indexOf(code)
        if (index >= 0) names.getOrElse(index) { code } else code
    }
}

@Composable
private fun themePillLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.Auto -> R.string.settings_theme_pill_auto
        ThemeMode.Light -> R.string.settings_theme_pill_light
        ThemeMode.Dark -> R.string.settings_theme_pill_dark
    },
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            uiState = SettingsUiState(),
            onEvent = {},
        )
    }
}
