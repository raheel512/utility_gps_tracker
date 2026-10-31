package com.example.gpstracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.PillShape
import com.example.gpstracker.core.designsystem.theme.appShadow

/**
 * Hero "Route Finder" card (docs/design.md section 5).
 *
 * Height wraps the content row only (16dp padding + 96dp illustration). The
 * decorative white circle is drawn — not composed — so it cannot inflate
 * the card. The CTA is a compact pill; the left column is the click target.
 */
@Composable
fun HeroCard(
    title: String,
    subtitle: String,
    ctaLabel: String,
    ctaContentDescription: String,
    onCtaClick: () -> Unit,
    @DrawableRes illustrationRes: Int?,
    illustrationContentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    val gradientColors = listOf(
        appColors.heroGradientStart,
        appColors.heroGradientMid,
        appColors.heroGradientEnd,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .appShadow(AppShadow.Hero, shape)
            .clip(shape)
            .drawWithCache {
                val brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to gradientColors[0],
                        0.5f to gradientColors[1],
                        1f to gradientColors[2],
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.35f, size.height),
                )
                // Decorative circle (176dp) drawn here so it never affects layout.
                val circleRadius = 88.dp.toPx()
                val circleCenter = Offset(
                    x = size.width + 8.dp.toPx(),
                    y = (-16).dp.toPx(),
                )
                onDrawBehind {
                    drawRect(brush)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = circleRadius,
                        center = circleCenter,
                    )
                }
            }
            .padding(spacing.heroPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onCtaClick),
            ) {
                Text(
                    text = title,
                    style = AppTextStyles.heroTitle,
                    color = appColors.textOnPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = subtitle,
                    style = AppTextStyles.heroSubtitle,
                    color = appColors.primaryContainer.copy(alpha = 0.9f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.size(12.dp))
                HeroCtaVisual(
                    label = ctaLabel,
                    chevronContentDescription = ctaContentDescription,
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            AssetSlot(
                drawableRes = illustrationRes,
                contentDescription = illustrationContentDescription,
                modifier = Modifier.size(width = 112.dp, height = 96.dp),
            )
        }
    }
}

/** Compact white pill — visual only; parent column handles the click. */
@Composable
private fun HeroCtaVisual(
    label: String,
    chevronContentDescription: String,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = modifier
            .clip(PillShape)
            .appShadow(AppShadow.Cta, PillShape)
            .background(appColors.textOnPrimary)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            style = AppTextStyles.heroCta,
            color = appColors.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = chevronContentDescription,
            tint = appColors.primary,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroCardPreview() {
    AppTheme {
        HeroCard(
            title = "Route Finder",
            subtitle = "Find the Route to destination",
            ctaLabel = "Find Route",
            ctaContentDescription = "Find Route",
            onCtaClick = {},
            illustrationRes = null,
            illustrationContentDescription = null,
        )
    }
}
