package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

/**
 * GPS-off alert from Figma `55451:2268` (CANCEL / ENABLE GPS).
 */
@Composable
fun GpsDisabledDialog(
    onEnableGps: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onCancel,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Text(
                text = stringResource(R.string.gps_disabled_message),
                color = appColors.textPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onEnableGps,
                modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
            ) {
                Text(
                    text = stringResource(R.string.gps_disabled_enable),
                    color = ColorTokens.Primary500,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
            ) {
                Text(
                    text = stringResource(R.string.gps_disabled_cancel),
                    color = ColorTokens.Primary500,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}

@Preview
@Composable
private fun GpsDisabledDialogPreview() {
    AppTheme {
        GpsDisabledDialog(onEnableGps = {}, onCancel = {})
    }
}
