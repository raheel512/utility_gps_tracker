package com.example.gpstracker.feature.onboarding.ui

import androidx.annotation.DrawableRes
import com.example.gpstracker.R

data class LanguageOption(
    val code: String,
    @param:DrawableRes val flagRes: Int,
)

data class LanguageUiState(
    val options: List<LanguageOption> = defaultLanguages,
    val selectedCode: String = "en",
    val isSaving: Boolean = false,
)

sealed interface LanguageEvent {
    data class Select(val code: String) : LanguageEvent
    data object Continue : LanguageEvent
    /** Settings mode: save current selection and return. */
    data object NavigateUp : LanguageEvent
}

sealed interface LanguageEffect {
    data object NavigateNext : LanguageEffect
    data object NavigateBack : LanguageEffect
}

/**
 * Supported languages for now (must stay aligned with
 * R.array.language_english_names and R.array.language_codes).
 * Flag drawables live in res/drawable as {code}.xml (e.g. en.xml).
 */
val defaultLanguages: List<LanguageOption> = listOf(
    LanguageOption("en", R.drawable.en),
    LanguageOption("af", R.drawable.af),
    LanguageOption("ar", R.drawable.ar),
    LanguageOption("zh", R.drawable.zh),
    LanguageOption("cs", R.drawable.cs),
    LanguageOption("da", R.drawable.da),
    LanguageOption("nl", R.drawable.nl),
    LanguageOption("de", R.drawable.de),
    LanguageOption("el", R.drawable.el),
    LanguageOption("hi", R.drawable.hi),
    LanguageOption("id", R.drawable.id),
    LanguageOption("ja", R.drawable.ja),
    LanguageOption("ko", R.drawable.ko),
    LanguageOption("ms", R.drawable.ms),
    LanguageOption("no", R.drawable.no),
    LanguageOption("fa", R.drawable.fa),
    LanguageOption("pt", R.drawable.pt),
    LanguageOption("ru", R.drawable.ru),
    LanguageOption("es", R.drawable.es),
    LanguageOption("th", R.drawable.th),
    LanguageOption("tr", R.drawable.tr),
    LanguageOption("ur", R.drawable.ur),
    LanguageOption("vi", R.drawable.vi),
)
