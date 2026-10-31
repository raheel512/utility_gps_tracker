package com.example.gpstracker.feature.country.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gpstracker.R

@Composable
fun CountryFlag(
    iso: String,
    modifier: Modifier = Modifier,
    corner: RoundedCornerShape = RoundedCornerShape(6.dp),
) {
    val resId = flagDrawable(iso)
    Box(
        modifier = modifier
            .clip(corner)
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center,
    ) {
        if (resId != 0) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun flagDrawable(iso: String): Int = when (iso) {
    "AE" -> R.drawable.flag_ae
    "AF" -> R.drawable.flag_af
    "AM" -> R.drawable.flag_am
    "AU" -> R.drawable.flag_au
    "AZ" -> R.drawable.flag_az
    "BD" -> R.drawable.flag_bd
    "BE" -> R.drawable.flag_be
    "BH" -> R.drawable.flag_bh
    "BN" -> R.drawable.flag_bn
    "BR" -> R.drawable.flag_br
    "BT" -> R.drawable.flag_bt
    "CA" -> R.drawable.flag_ca
    "CH" -> R.drawable.flag_ch
    "CN" -> R.drawable.flag_cn
    "DE" -> R.drawable.flag_de
    "DK" -> R.drawable.flag_dk
    "EG" -> R.drawable.flag_eg
    "ES" -> R.drawable.flag_es
    "FR" -> R.drawable.flag_fr
    "GB" -> R.drawable.flag_gb
    "GE" -> R.drawable.flag_ge
    "ID" -> R.drawable.flag_id
    "IE" -> R.drawable.flag_ie
    "IN" -> R.drawable.flag_in
    "IQ" -> R.drawable.flag_iq
    "IR" -> R.drawable.flag_ir
    "IT" -> R.drawable.flag_it
    "JO" -> R.drawable.flag_jo
    "JP" -> R.drawable.flag_jp
    "KR" -> R.drawable.flag_kr
    "KW" -> R.drawable.flag_kw
    "KZ" -> R.drawable.flag_kz
    "MY" -> R.drawable.flag_my
    "NL" -> R.drawable.flag_nl
    "NO" -> R.drawable.flag_no
    "NP" -> R.drawable.flag_np
    "OM" -> R.drawable.flag_om
    "PK" -> R.drawable.flag_pk
    "QA" -> R.drawable.flag_qa
    "RU" -> R.drawable.flag_ru
    "SA" -> R.drawable.flag_sa
    "SE" -> R.drawable.flag_se
    "SG" -> R.drawable.flag_sg
    "TH" -> R.drawable.flag_th
    "TR" -> R.drawable.flag_tr
    "UA" -> R.drawable.flag_ua
    "US" -> R.drawable.flag_us
    "UZ" -> R.drawable.flag_uz
    "VN" -> R.drawable.flag_vn
    else -> 0
}
