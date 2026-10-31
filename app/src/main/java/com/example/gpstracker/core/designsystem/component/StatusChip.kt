package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.PillShape

/**
 * Generic pill chip using design tokens (connectedChip colors by default).
 */
@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val appColors = LocalAppColors.current
    AssistChip(
        onClick = onClick ?: {},
        enabled = onClick != null,
        label = { Text(text = label, style = AppTextStyles.chipText) },
        shape = PillShape,
        modifier = modifier.defaultMinSize(minHeight = 32.dp),
        border = BorderStroke(0.5.dp, appColors.connectedChipBorder),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = appColors.connectedChipBg,
            labelColor = appColors.connectedChipText,
            disabledContainerColor = appColors.connectedChipBg,
            disabledLabelColor = appColors.connectedChipText,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    AppTheme {
        StatusChip(label = "Connected")
    }
}
