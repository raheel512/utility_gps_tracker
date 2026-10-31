package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow

/** Figma Setting Screen row divider `#F2F3FF`. */
private val SettingsDivider = Color(0xFFF2F3FF)

/**
 * Settings section: uppercase label above a 12dp-radius white card of rows
 * separated by Figma dividers (no extra card inner padding).
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.sectionLabelGap),
    ) {
        Text(
            text = title,
            style = AppTextStyles.sectionLabel,
            color = appColors.textSecondary,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .appShadow(AppShadow.Card, shape),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = shape,
            /*border = BorderStroke(1.dp, appColors.cardBorder),*/
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )
        }
    }
}

@Composable
fun SettingsRowDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = SettingsDivider,
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsSectionPreview() {
    AppTheme {
        SettingsSection(title = "PREFERENCES") {
            Text(text = "Row placeholder", modifier = Modifier)
        }
    }
}
