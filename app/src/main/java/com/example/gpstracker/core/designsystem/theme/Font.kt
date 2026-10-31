package com.example.gpstracker.core.designsystem.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * Font families from docs/design.md section 2.
 *
 * TODO(asset): add the TTF files below to app/src/main/res/font/ and switch each
 * family to a real FontFamily(...) so the design fonts render. Until then both
 * families fall back to [FontFamily.Default] so the build stays green.
 *
 * Roboto (UI text + nav labels — NOT Roboto Flex):
 *   roboto_regular.ttf, roboto_medium.ttf, roboto_semibold.ttf,
 *   roboto_bold.ttf, roboto_black.ttf
 * Plus Jakarta Sans (hero card + section headers):
 *   plus_jakarta_sans_medium.ttf, plus_jakarta_sans_semibold.ttf,
 *   plus_jakarta_sans_bold.ttf
 *
 * Example once the files exist:
 *
 * val RobotoFamily = FontFamily(
 *     Font(R.font.roboto_regular, FontWeight.Normal),
 *     Font(R.font.roboto_medium, FontWeight.Medium),
 *     Font(R.font.roboto_semibold, FontWeight.SemiBold),
 *     Font(R.font.roboto_bold, FontWeight.Bold),
 *     Font(R.font.roboto_black, FontWeight.Black),
 * )
 */
val RobotoFamily: FontFamily = FontFamily.Default // TODO(asset): roboto_*.ttf

val PlusJakartaSansFamily: FontFamily = FontFamily.Default // TODO(asset): plus_jakarta_sans_*.ttf
