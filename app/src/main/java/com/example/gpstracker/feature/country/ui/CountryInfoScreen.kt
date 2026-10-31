package com.example.gpstracker.feature.country.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.RobotoFamily
import com.example.gpstracker.feature.country.Country
import com.example.gpstracker.feature.country.CountryCatalog

private val CardShape = RoundedCornerShape(16.dp)
private val FlagShape = RoundedCornerShape(8.dp)
private val TitleInk = Color(0xFF0F172A)
private val PinInk = Color(0xFF94A3B8)
private val CapitalInk = Color(0xFF64748B)

@Composable
fun CountryInfoScreen(
    onNavigateUp: () -> Unit,
    onOpenCountry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val countries = remember(query) { CountryCatalog.filter(query) }
    CountryScaffold(
        title = stringResource(R.string.screen_country_info),
        searching = searching,
        query = query,
        onQueryChange = { query = it },
        onToggleSearch = {
            searching = !searching
            if (!searching) query = ""
        },
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (countries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.country_search_empty),
                        color = CapitalInk,
                        fontFamily = RobotoFamily,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(countries, key = { it.iso }) { country ->
                CountryInfoCard(
                    country = country,
                    onClick = { onOpenCountry(country.iso) },
                )
            }
        }
    }
}

@Composable
private fun CountryInfoCard(
    country: Country,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), CardShape)
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 36.dp)
                    .clip(FlagShape)
                    .border(1.dp, Color(0xFFE2E8F0), FlagShape)
                    .padding(1.dp),
                contentAlignment = Alignment.Center,
            ) {
                CountryFlag(
                    iso = country.iso,
                    modifier = Modifier.fillMaxSize(),
                    corner = FlagShape,
                )
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = country.name,
                    color = TitleInk,
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = PinInk,
                    )
                    Text(
                        text = country.capital,
                        modifier = Modifier.padding(start = 4.dp),
                        color = CapitalInk,
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_country_chevron),
            contentDescription = stringResource(R.string.cd_country_open),
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
        )
    }
}
