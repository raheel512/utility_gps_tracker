package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

/**
 * No-internet bottom sheet from Figma `ConnectionDroppedBottomSheet` (55463:4347).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoInternetDialog(
    onConnect: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.contentSide)
                .padding(bottom = spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(appColors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WifiOff,
                        contentDescription = stringResource(R.string.cd_no_internet_illustration),
                        tint = ColorTokens.Primary500,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(ColorTokens.Destructive),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "!",
                        color = ColorTokens.TextOnPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = stringResource(R.string.no_internet_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = appColors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = stringResource(R.string.no_internet_body),
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            AppButton(
                text = stringResource(R.string.no_internet_connect),
                onClick = onConnect,
                fillMaxWidth = true,
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
            ) {
                Text(
                    text = stringResource(R.string.no_internet_dismiss),
                    color = ColorTokens.Primary500,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoInternetDialogPreview() {
    AppTheme {
        // Content-only preview (sheet needs a host).
        Text(text = stringResource(R.string.no_internet_title))
    }
}
