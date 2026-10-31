package com.example.gpstracker.feature.country.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpstracker.R
import com.example.gpstracker.core.designsystem.theme.RobotoFamily
import com.example.gpstracker.feature.country.Country
import com.example.gpstracker.feature.country.CountryCatalog

private val NameInk = Color(0xFF1E293B)
private val CountInk = Color(0xFF404040)
private val CodeInk = Color(0xFF1D4ED8)
private val CodeFill = Color(0xFFEFF6FF)
private val CodeBorder = Color(0xCCDBEAFE)
private val Divider = Color(0xFFF1F5F9)

@Composable
fun CountryCodeScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val countries = remember(query) { CountryCatalog.filter(query) }
    CountryScaffold(
        title = stringResource(R.string.screen_country_code),
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
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = NameInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.country_all_countries_label))
                            append(' ')
                        }
                        withStyle(SpanStyle(color = CountInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                            append("(${countries.size})")
                        }
                    },
                    modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
                    fontFamily = RobotoFamily,
                    lineHeight = 20.sp,
                )
            }
            if (countries.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.country_search_empty),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        color = CountInk,
                        fontFamily = RobotoFamily,
                        fontSize = 14.sp,
                    )
                }
            }
            items(countries, key = { it.iso }) { country ->
                CountryCodeRow(country)
            }
        }
    }
}

@Composable
private fun CountryCodeRow(country: Country) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountryFlag(
                    iso = country.iso,
                    modifier = Modifier.size(width = 44.dp, height = 30.dp),
                )
                Text(
                    text = country.name,
                    modifier = Modifier.padding(start = 14.dp),
                    color = NameInk,
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 22.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .border(1.dp, CodeBorder, RoundedCornerShape(8.dp))
                    .background(CodeFill, RoundedCornerShape(8.dp))
                    .padding(horizontal = 11.dp, vertical = 5.dp),
            ) {
                Text(
                    text = country.dial,
                    color = CodeInk,
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .background(Divider),
        )
    }
}
