package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.PillShape

@Composable
fun ProBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalAppColors.current.proBadge,
    contentColor: Color = Color.White,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = spacing.touchTarget / 2)
            .clip(PillShape)
            .background(backgroundColor)
            .padding(horizontal = spacing.sm, vertical = spacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTextStyles.tileLabel,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProBadgePreview() {
    AppTheme {
        ProBadge(text = "PRO")
    }
}
