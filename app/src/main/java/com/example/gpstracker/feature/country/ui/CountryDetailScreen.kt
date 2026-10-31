package com.example.gpstracker.feature.country.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.component.AppTopBar
import com.example.gpstracker.core.designsystem.theme.LocalAppColors
import com.example.gpstracker.core.designsystem.theme.PlusJakartaSansFamily
import com.example.gpstracker.feature.country.Country
import com.example.gpstracker.feature.country.CountryCatalog

private val CardShape = RoundedCornerShape(16.dp)
private val Ink = Color(0xFF0F172A)
private val Label = Color(0xFF475569)
private val Muted = Color(0xFF64748B)
private val Capital = Color(0xFF4F46E5)
private val PillBorder = Color(0xE6E2E8F0)

@Composable
fun CountryDetailScreen(
    iso: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val country = CountryCatalog.find(iso)
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenTintStart),
    ) {
        AppTopBar(
            title = stringResource(R.string.screen_country_detail),
            onNavigateUp = onNavigateUp,
            containerColor = colors.screenTintStart,
            showDivider = false,
        )
        if (country == null) {
            Text(
                text = stringResource(R.string.country_search_empty),
                modifier = Modifier.padding(24.dp),
                color = colors.textPrimary,
            )
            return@Column
        }
        CountryDetailContent(country)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CountryDetailContent(country: Country) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CountryFlag(
            iso = country.iso,
            modifier = Modifier.size(width = 160.dp, height = 112.dp),
            corner = RoundedCornerShape(16.dp),
        )
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .border(1.dp, PillBorder, RoundedCornerShape(999.dp))
                .padding(horizontal = 17.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF10B981)),
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)) {
                        append(country.name)
                        append(' ')
                    }
                    withStyle(SpanStyle(color = Color(0xFF94A3B8), fontWeight = FontWeight.Normal)) {
                        append(':')
                    }
                    append(' ')
                    withStyle(SpanStyle(color = Capital, fontWeight = FontWeight.SemiBold)) {
                        append(country.capital)
                    }
                },
                modifier = Modifier.padding(start = 6.dp),
                fontFamily = PlusJakartaSansFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.3.sp,
            )
        }
        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(Color.White)
                .border(1.dp, Color(0xCCE2E8F0), CardShape),
        ) {
            MetricRow(R.drawable.ic_country_call, Color(0xFFEFF6FF), R.string.country_calling_code, country.dial, badge = true, divider = false)
            MetricRow(R.drawable.ic_country_people, Color(0xFFF5F3FF), R.string.country_population, country.population)
            MetricRow(R.drawable.ic_country_money, Color(0xFFFFFBEB), R.string.country_currency, country.currency)
            MetricRow(R.drawable.ic_country_globe, Color(0xFFECFDF5), R.string.country_region, country.region)
            MetricRow(R.drawable.ic_country_time, Color(0xFFFFF1F2), R.string.country_time_zone, country.timeZone, badge = true)
        }
        Column(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
                .fillMaxWidth()
                .clip(CardShape)
                .background(Color.White)
                .border(1.dp, Color(0xCCE2E8F0), CardShape)
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Ink)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_country_borders),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Unspecified,
                    )
                    Text(
                        text = stringResource(R.string.country_borders),
                        modifier = Modifier.padding(start = 6.dp),
                        color = Color.White,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                Text(
                    text = pluralStringResource(
                        R.plurals.country_border_count,
                        country.borders.size,
                        country.borders.size,
                    ),
                    color = Muted,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                country.borders.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, PillBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                        color = Color(0xFF334155),
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    iconRes: Int,
    well: Color,
    labelRes: Int,
    value: String,
    badge: Boolean = false,
    divider: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (divider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9)),
            )
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(well),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = stringResource(labelRes),
                modifier = Modifier.padding(start = 12.dp),
                color = Label,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 1,
            )
        }
        val valueModifier = if (badge) {
            Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        } else {
            Modifier.padding(start = 8.dp)
        }
        Text(
            text = value,
            modifier = valueModifier,
            color = Ink,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
    }
}
