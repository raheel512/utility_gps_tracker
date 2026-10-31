package com.example.gpstracker.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing

/**
 * Top app bar from docs/design.md section 5: 1dp bottom border (outlineDefault),
 * bold title in textPrimary.
 *
 * @param containerColor bar fill. Home uses [screenTintStart] (`#F4F5FF`);
 * other screens keep surface white.
 * @param statusBarAware when true, the bar owns status-bar insets so
 * [containerColor] extends behind the status bar (top-level tabs). When false,
 * the root Scaffold already padded for the status bar — use zero insets to
 * avoid a double gap (detail screens).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    statusBarAware: Boolean = false,
    showDivider: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val dividerColor = appColors.outlineDefault
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = appColors.textPrimary,
            )
        },
        // Draw after content so the 1dp separator sits on top of the bar fill
        // (drawBehind would be covered by TopAppBar's opaque background).
        modifier = if (showDivider) {
            modifier.drawWithContent {
                drawContent()
                val stroke = 1.dp.toPx()
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, size.height - stroke / 2),
                    end = Offset(size.width, size.height - stroke / 2),
                    strokeWidth = stroke,
                )
            }
        } else {
            modifier
        },
        windowInsets = if (statusBarAware) {
            WindowInsets.statusBars
        } else {
            WindowInsets(0, 0, 0, 0)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = appColors.textPrimary,
            actionIconContentColor = appColors.textPrimary,
            navigationIconContentColor = appColors.textPrimary,
        ),
        navigationIcon = {
            if (onNavigateUp != null) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.defaultMinSize(
                        minWidth = spacing.touchTarget,
                        minHeight = spacing.touchTarget,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_up),
                    )
                }
            }
        },
        actions = actions,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    AppTheme {
        AppTopBar(title = "GPS Tracker", onNavigateUp = {})
    }
}
