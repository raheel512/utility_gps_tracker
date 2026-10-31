package com.example.gpstracker.feature.onboarding.ui

import android.media.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.example.gpstracker.core.designsystem.component.AssetSlot
import com.example.gpstracker.core.designsystem.component.drawableIdOrNull
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

@Composable
fun ConsentScreen(
    uiState: ConsentUiState,
    onEvent: (ConsentEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    // TODO(asset): add res/drawable/ic_gps_logo.webp (or png/xml)
    val logoRes = context.drawableIdOrNull("ic_gps_logo")

    Box(
        modifier = modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.splash_bkg),
                contentScale = ContentScale.Crop
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.contentSide),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            AssetSlot(
                drawableRes = logoRes,
                contentDescription = stringResource(R.string.cd_app_logo),
                modifier = Modifier.size(96.dp),
                placeholderSize = 96.dp,
            )
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                color = ColorTokens.TextPrimary,
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = stringResource(R.string.consent_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = ColorTokens.Primary500,
            )

            Spacer(modifier = Modifier.weight(1f))

            PageDots(total = 3, selectedIndex = 0)
            Spacer(modifier = Modifier.height(spacing.lg))

            AppButton(
                text = stringResource(R.string.consent_get_started),
                onClick = { onEvent(ConsentEvent.Continue) },
                enabled = uiState.canContinue,
                fillMaxWidth = true,
            )
            Spacer(modifier = Modifier.height(spacing.md))

            ConsentFooter(
                checked = uiState.termsAccepted,
                onCheckedChange = { onEvent(ConsentEvent.ToggleTerms) },
                onTermsClick = { onEvent(ConsentEvent.OpenTerms) },
                onPrivacyClick = { onEvent(ConsentEvent.OpenPrivacy) },
            )
            Spacer(modifier = Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun ConsentFooter(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val termsLabel = stringResource(R.string.consent_terms_link)
    val privacyLabel = stringResource(R.string.consent_privacy_link)

    val annotated = buildAnnotatedString {
        append(stringResource(R.string.consent_agree_prefix))
        append(' ')
        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(SpanStyle(color = ColorTokens.Primary500, fontWeight = FontWeight.Medium)) {
            append(termsLabel)
        }
        pop()
        append(' ')
        append(stringResource(R.string.consent_agree_and))
        append(' ')
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(SpanStyle(color = ColorTokens.Primary500, fontWeight = FontWeight.Medium)) {
            append(privacyLabel)
        }
        pop()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.defaultMinSize(
                minWidth = spacing.touchTarget,
                minHeight = spacing.touchTarget,
            ),
            colors = CheckboxDefaults.colors(
                checkedColor = ColorTokens.Primary500,
            ),
        )
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.bodySmall.copy(
                color = ColorTokens.TextSecondary,
                textAlign = TextAlign.Start,
            ),
            onClick = { offset ->
                annotated.getStringAnnotations("TERMS", offset, offset)
                    .firstOrNull()
                    ?.let { onTermsClick() }
                annotated.getStringAnnotations("PRIVACY", offset, offset)
                    .firstOrNull()
                    ?.let { onPrivacyClick() }
            },
        )
    }
}

@Composable
fun PageDots(
    total: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(
                        width = if (selected) 18.dp else 8.dp,
                        height = 8.dp,
                    )
                    .background(
                        color = if (selected) ColorTokens.Primary500 else ColorTokens.Primary0,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ConsentScreenPreview() {
    AppTheme {
        ConsentScreen(
            uiState = ConsentUiState(termsAccepted = false),
            onEvent = {},
        )
    }
}
