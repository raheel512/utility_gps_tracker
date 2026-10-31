package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.PillShape

/** Figma Setting Screen icon container fill `#E2E7FF`. */
private val SettingsIconContainerBg = Color(0xFFE2E7FF)

/** Figma theme value pill / unit track `#EAEDFF`. */
private val SettingsChipBg = Color(0xFFEAEDFF)

/** Figma selected unit / icon stroke accent `#004AC6`. */
private val SettingsAccentBlue = Color(0xFF004AC6)

/**
 * Settings list row from Figma Setting Screen: 40dp icon container, 12dp
 * padding, 64dp min height, title 16/24 + subtitle 12/16.
 */
@Composable
fun SettingsRow(
    @DrawableRes iconRes: Int,
    iconContentDescription: String?,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val rowModifier = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 64.dp)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(SettingsIconContainerBg),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = iconContentDescription,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.width(spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = appColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.24.sp,
                ),
                color = appColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(spacing.xs))
        trailing()
    }
}

@Composable
fun SettingsChevron(
    contentDescription: String? = null,
) {
    val appColors = LocalAppColors.current
    Icon(
        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
        contentDescription = contentDescription,
        tint = appColors.textMuted,
        modifier = Modifier.size(16.dp),
    )
}

@Composable
fun SettingsValuePill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    Text(
        text = text,
        style = AppTextStyles.chipText.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.33.sp,
        ),
        color = appColors.textPrimary,
        modifier = modifier
            .clip(PillShape)
            .background(SettingsChipBg)
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

@Composable
fun SettingsExternalLinkIcon(
    contentDescription: String,
) {
    Image(
        painter = painterResource(R.drawable.ic_settings_external_link),
        contentDescription = contentDescription,
        modifier = Modifier.size(width = 18.dp, height = 15.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun SettingsTrailingIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        modifier = modifier.size(16.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
) {
    val appColors = LocalAppColors.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.semantics { this.contentDescription = contentDescription },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = SettingsAccentBlue,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = appColors.textMuted.copy(alpha = 0.4f),
        ),
    )
}

@Composable
fun SettingsUnitSegmentedControl(
    selectedIsKm: Boolean,
    onSelectKm: () -> Unit,
    onSelectMi: () -> Unit,
    kmLabel: String,
    miLabel: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    val segmentShape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .selectableGroup()
            .clip(shape)
            .background(SettingsChipBg)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UnitSegment(
            label = kmLabel,
            selected = selectedIsKm,
            onClick = onSelectKm,
            shape = segmentShape,
        )
        UnitSegment(
            label = miLabel,
            selected = !selectedIsKm,
            onClick = onSelectMi,
            shape = segmentShape,
        )
    }
}

@Composable
private fun UnitSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
) {
    val appColors = LocalAppColors.current
    Text(
        text = label,
        style = AppTextStyles.chipText.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.24.sp,
        ),
        color = if (selected) SettingsAccentBlue else appColors.textSecondary,
        modifier = Modifier
            .defaultMinSize(minHeight = 24.dp)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(Color.White)
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsRowPreview() {
    AppTheme {
        SettingsRow(
            iconRes = R.drawable.ic_settings_moon,
            iconContentDescription = null,
            title = "Theme",
            subtitle = "Follow system appearance",
            onClick = {},
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingsValuePill(text = "Auto")
                    SettingsChevron()
                }
            },
        )
    }
}
