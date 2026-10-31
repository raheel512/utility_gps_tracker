package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.PillShape

/**
 * Connectivity pill (docs/design.md): Connected = network available.
 * Connected: green bg / border / text. Offline: red bg / border / text.
 */
@Composable
fun ConnectivityChip(
    connected: Boolean,
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    val background = if (connected) appColors.connectedChipBg else appColors.offlineChipBg
    val contentColor = if (connected) appColors.connectedChipText else appColors.offlineChipText
    val borderColor = if (connected) appColors.connectedChipBorder else appColors.offlineChipBorder
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(background)
            .border(0.5.dp, borderColor, PillShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (connected) Icons.Outlined.Wifi else Icons.Outlined.WifiOff,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(13.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = AppTextStyles.chipText,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectivityChipConnectedPreview() {
    AppTheme {
        ConnectivityChip(connected = true, label = "Connected", contentDescription = "Connected")
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectivityChipOfflinePreview() {
    AppTheme {
        ConnectivityChip(connected = false, label = "Offline", contentDescription = "Offline")
    }
}
