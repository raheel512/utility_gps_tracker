package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
 * Location permission rationale bottom sheet from the attached screenshot.
 *
 * When [permanentlyDenied] is true, the primary CTA becomes "Open settings"
 * (no dedicated mockup — flag for design review).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionRationale(
    permanentlyDenied: Boolean,
    onAllowOrOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    titleRes: Int = R.string.location_permission_title,
    bodyRes: Int = R.string.location_permission_body,
    allowRes: Int = R.string.location_permission_allow,
    illustrationContentDescriptionRes: Int = R.string.cd_location_permission_illustration,
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
            // TODO(asset): add drawable/ic_location_permission_radar (multi-color; never tint)
            AssetSlot(
                drawableRes = null,
                contentDescription = stringResource(illustrationContentDescriptionRes),
                modifier = Modifier.size(96.dp),
                placeholderSize = 96.dp,
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = appColors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = stringResource(bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            AppButton(
                text = if (permanentlyDenied) {
                    stringResource(R.string.location_permission_open_settings)
                } else {
                    stringResource(allowRes)
                },
                onClick = onAllowOrOpenSettings,
                fillMaxWidth = true,
                leadingIcon = if (permanentlyDenied) {
                    null
                } else {
                    Icons.AutoMirrored.Filled.Send
                },
                leadingIconContentDescription = if (permanentlyDenied) {
                    null
                } else {
                    stringResource(R.string.cd_location_permission_allow_icon)
                },
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = spacing.touchTarget),
            ) {
                Text(
                    text = stringResource(R.string.location_permission_not_now),
                    color = ColorTokens.Primary500,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPermissionRationalePreview() {
    AppTheme {
        // Preview body only (sheet host needs Activity); show content column.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LocalAppColors.current.outlineDefault),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.location_permission_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.location_permission_body),
                textAlign = TextAlign.Center,
            )
        }
    }
}
