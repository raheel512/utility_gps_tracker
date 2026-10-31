package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Small "+1"-style badge from docs/design.md section 1: blue fill, 1dp white
 * border, white Roboto Black text.
 */
@Composable
fun CountryBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(appColors.countryBadgeBg)
            .border(1.dp, appColors.textOnPrimary, shape)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTextStyles.badge,
            color = appColors.countryBadgeText,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CountryBadgePreview() {
    AppTheme {
        CountryBadge(text = "+1")
    }
}
