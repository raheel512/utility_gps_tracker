package com.example.gpstracker.feature.paywall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppCard
import com.example.gpstracker.core.designsystem.component.AssetSlot
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.PillShape

@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    onEvent: (PaywallEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        appColors.screenTintStart,
                        appColors.screenTintEnd,
                        appColors.screenTintStart,
                    ),
                ),
            ),
    ) {
        PaywallTopBar(
            onClose = { onEvent(PaywallEvent.Close) },
            onUpgrade = { onEvent(PaywallEvent.PurchaseClicked) },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = spacing.contentSide),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(spacing.sm))

            // TODO(asset): add drawable/ic_paywall_crown.xml (multi-color; never tint)
            AssetSlot(
                drawableRes = R.drawable.cd_paywall_crown,
                contentDescription = stringResource(R.string.cd_paywall_crown),
                modifier = Modifier.size(140.dp),
                placeholderSize = 140.dp,
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = appColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) {
                        append(stringResource(R.string.paywall_title_go))
                        append(' ')
                    }
                    withStyle(
                        SpanStyle(
                            color = ColorTokens.Primary500,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) {
                        append(stringResource(R.string.paywall_title_pro))
                    }
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = spacing.sm),
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                uiState.features.forEachIndexed { index, feature ->
                    PaywallFeatureRow(feature = feature)
                    if (index < uiState.features.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = appColors.cardBorder,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.contentSide)
                .padding(bottom = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppButton(
                text = stringResource(R.string.paywall_get_pro),
                onClick = { onEvent(PaywallEvent.PurchaseClicked) },
                enabled = !uiState.isPurchasing,
                fillMaxWidth = true,
                leadingIcon = ImageVector.vectorResource(id = R.drawable.ic_crown_white),
                leadingIconContentDescription = stringResource(R.string.cd_paywall_get_pro_crown),
            )

            TextButton(
                onClick = { onEvent(PaywallEvent.PurchaseClicked) },
                modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
                enabled = !uiState.isPurchasing,
            ) {
                Text(
                    text = stringResource(R.string.paywall_try_for_free),
                    style = AppTextStyles.sectionLink,
                    color = ColorTokens.Primary500,
                )
            }

            // Compliance additions — not in mockup; flag for design review.
            uiState.priceText?.let { price ->
                Text(
                    text = price,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
            uiState.trialText?.let { trial ->
                Text(
                    text = trial,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onEvent(PaywallEvent.OpenTerms) },
                    modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
                ) {
                    Text(
                        text = stringResource(R.string.paywall_terms),
                        style = MaterialTheme.typography.labelMedium,
                        color = appColors.textMuted,
                    )
                }
                Text(
                    text = stringResource(R.string.paywall_legal_separator),
                    style = MaterialTheme.typography.labelMedium,
                    color = appColors.textMuted,
                )
                TextButton(
                    onClick = { onEvent(PaywallEvent.OpenPrivacy) },
                    modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
                ) {
                    Text(
                        text = stringResource(R.string.paywall_privacy),
                        style = MaterialTheme.typography.labelMedium,
                        color = appColors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallTopBar(
    onClose: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xs)
            .padding(top = spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.defaultMinSize(
                minWidth = spacing.touchTarget,
                minHeight = spacing.touchTarget,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_paywall_close),
                tint = appColors.textPrimary,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = onUpgrade,
            modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
        ) {
            Text(
                text = stringResource(R.string.paywall_upgrade),
                style = AppTextStyles.sectionLink,
                color = ColorTokens.Primary500,
            )
        }
    }
}

@Composable
private fun PaywallFeatureRow(
    feature: PaywallFeature,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val visuals = feature.icon.visuals()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = spacing.touchTarget)
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(visuals.containerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = visuals.imageVector,
                contentDescription = null,
                tint = visuals.iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(spacing.sm))
        Text(
            text = stringResource(feature.titleRes),
            style = AppTextStyles.toolCardLabel,
            color = appColors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(spacing.xs))
        PaywallStatusChip(isPro = feature.isPro)
    }
}

@Composable
private fun PaywallStatusChip(
    isPro: Boolean,
    modifier: Modifier = Modifier,
) {
    val appColors = LocalAppColors.current
    val spacing = LocalSpacing.current
    val label = if (isPro) {
        stringResource(R.string.paywall_chip_pro)
    } else {
        stringResource(R.string.paywall_chip_free)
    }
    val background = if (isPro) appColors.proBadgeBg else appColors.successChipBg
    val content = if (isPro) appColors.proBadge else appColors.successChipText

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(background)
            .padding(horizontal = spacing.sm, vertical = spacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AppTextStyles.chipText,
            color = content,
        )
    }
}

/**
 * Row icon + container tints are approximate from the screenshot.
 * // VERIFY against Figma
 */
private data class FeatureIconVisuals(
    val imageVector: ImageVector,
    val iconTint: Color,
    val containerColor: Color,
)

private fun PaywallFeatureIcon.visuals(): FeatureIconVisuals = when (this) {
    // VERIFY against Figma
    PaywallFeatureIcon.LocationSharing -> FeatureIconVisuals(
        imageVector = Icons.Outlined.LocationOn,
        iconTint = Color(0xFFE11D48),
        containerColor = Color(0xFFFFE4E6),
    )
    PaywallFeatureIcon.GpsCamera -> FeatureIconVisuals(
        imageVector = Icons.Outlined.CameraAlt,
        iconTint = Color(0xFFF59E0B),
        containerColor = Color(0xFFFEF3C7),
    )
    PaywallFeatureIcon.SatelliteMap -> FeatureIconVisuals(
        imageVector = Icons.Outlined.Public,
        iconTint = Color(0xFF0D9488),
        containerColor = Color(0xFFCCFBF1),
    )
    PaywallFeatureIcon.StreetMap -> FeatureIconVisuals(
        imageVector = Icons.Outlined.Map,
        iconTint = Color(0xFF16A34A),
        containerColor = Color(0xFFDCFCE7),
    )
    PaywallFeatureIcon.Traffic -> FeatureIconVisuals(
        imageVector = Icons.Outlined.Traffic,
        iconTint = Color(0xFF16A34A),
        containerColor = Color(0xFFDCFCE7),
    )
    PaywallFeatureIcon.Compass -> FeatureIconVisuals(
        imageVector = Icons.Outlined.Explore,
        iconTint = Color(0xFF2563EB),
        containerColor = Color(0xFFDBEAFE),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PaywallScreenPreview() {
    AppTheme {
        PaywallScreen(
            uiState = PaywallUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaywallScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        PaywallScreen(
            uiState = PaywallUiState(),
            onEvent = {},
        )
    }
}
