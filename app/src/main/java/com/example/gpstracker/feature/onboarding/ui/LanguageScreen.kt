package com.example.gpstracker.feature.onboarding.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppButton
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

@Composable
fun LanguageScreen(
    uiState: LanguageUiState,
    onEvent: (LanguageEvent) -> Unit,
    modifier: Modifier = Modifier,
    fromSettings: Boolean = false,
) {
    val spacing = LocalSpacing.current
    val names = stringArrayResource(R.array.language_english_names)
    val codes = stringArrayResource(R.array.language_codes)
    val labelByCode = remember(names, codes) {
        codes.mapIndexed { index, code -> code to names.getOrElse(index) { code } }.toMap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorTokens.Surface),
    ) {
        if (fromSettings) {
            AppTopBar(
                title = stringResource(R.string.screen_language),
                onNavigateUp = { onEvent(LanguageEvent.NavigateUp) },
                statusBarAware = false,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md),
        ) {
            if (!fromSettings) {
                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    text = stringResource(R.string.language_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorTokens.TextPrimary,
                )
                Spacer(modifier = Modifier.height(spacing.md))
            } else {
                Spacer(modifier = Modifier.height(spacing.md))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(bottom = spacing.md),
            ) {
                itemsIndexed(
                    items = uiState.options,
                    key = { _, option -> option.code },
                ) { _, option ->
                    val label = labelByCode[option.code] ?: option.code
                    LanguageRow(
                        label = label,
                        selected = option.code == uiState.selectedCode,
                        flagRes = option.flagRes,
                        onClick = { onEvent(LanguageEvent.Select(option.code)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))
            AppButton(
                text = stringResource(
                    if (fromSettings) R.string.action_continue else R.string.action_next,
                ),
                onClick = {
                    onEvent(
                        if (fromSettings) LanguageEvent.NavigateUp else LanguageEvent.Continue,
                    )
                },
                enabled = !uiState.isSaving,
                fillMaxWidth = true,
                trailingIcon = if (fromSettings) null else Icons.AutoMirrored.Filled.ArrowForward,
                trailingIconContentDescription = if (fromSettings) {
                    null
                } else {
                    stringResource(R.string.cd_next)
                },
            )
            Spacer(modifier = Modifier.height(spacing.md))
        }
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    @DrawableRes flagRes: Int,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = spacing.touchTarget)
            .clip(shape)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) ColorTokens.Primary500 else ColorTokens.TileBorder,
                shape = shape,
            )
            .clickable(onClick = onClick),
        color = if (selected) ColorTokens.SelectionRowBg else ColorTokens.Surface,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(flagRes),
                contentDescription = label,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = ColorTokens.TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            LanguageSelectionIndicator(selected = selected)
        }
    }
}

/**
 * Matches design: selected = filled primary circle + white tick;
 * unselected = hollow dark outline circle.
 */
@Composable
private fun LanguageSelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val description = stringResource(
        if (selected) R.string.cd_language_selected else R.string.cd_language_unselected,
    )
    Box(
        modifier = modifier
            .size(spacing.touchTarget)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(ColorTokens.Primary500),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(
                        width = 1.5.dp,
                        color = ColorTokens.TextPrimary,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LanguageScreenPreview() {
    AppTheme {
        LanguageScreen(
            uiState = LanguageUiState(selectedCode = "en"),
            onEvent = {},
        )
    }
}
