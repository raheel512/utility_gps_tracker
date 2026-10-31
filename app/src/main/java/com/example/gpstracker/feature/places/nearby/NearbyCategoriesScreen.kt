package com.example.gpstracker.feature.places.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.AppShadow
import com.example.gpstracker.core.designsystem.theme.AppTextStyles
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.LocalSpacing
import com.example.gpstracker.core.designsystem.theme.appShadow
import com.example.gpstracker.core.places.NearbyCategory

@Composable
fun NearbyCategoriesScreen(
    uiState: NearbyCategoriesUiState,
    onEvent: (NearbyCategoriesEvent) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = LocalAppColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = stringResource(R.string.screen_nearby_places),
            onNavigateUp = onNavigateUp,
            containerColor = colors.screenTintStart,
            showDivider = false,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.nearby_categories_header),
                style = AppTextStyles.toolCardLabel.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.size(spacing.xs))
            Text(
                text = stringResource(R.string.nearby_location_current),
                style = AppTextStyles.sectionLink.copy(fontWeight = FontWeight.Medium),
                color = colors.primary,
                maxLines = 1,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = spacing.md,
                end = spacing.md,
                top = spacing.md,
                bottom = spacing.xl,
            ),
            horizontalArrangement = Arrangement.spacedBy(spacing.categoryGap),
            verticalArrangement = Arrangement.spacedBy(spacing.categoryGap),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.categories, key = { it.id }) { category ->
                CategoryCell(
                    category = category,
                    onClick = { onEvent(NearbyCategoriesEvent.CategoryClicked(category)) },
                )
            }
        }
    }
}

@Composable
private fun CategoryCell(
    category: NearbyCategory,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val label = stringResource(category.labelRes)
    val shape = RoundedCornerShape(16.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .appShadow(AppShadow.Tile, shape)
            .clip(shape)
            .border(1.dp, colors.cardBorder, shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ColorTokens.BanksBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(category.iconRes()),
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = AppTextStyles.tileLabel.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NearbyCategoriesPreview() {
    AppTheme {
        NearbyCategoriesScreen(
            uiState = NearbyCategoriesUiState(),
            onEvent = {},
            onNavigateUp = {},
        )
    }
}
