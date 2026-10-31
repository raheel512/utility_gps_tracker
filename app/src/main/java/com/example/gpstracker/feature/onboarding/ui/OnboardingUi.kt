package com.example.gpstracker.feature.onboarding.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.gpstracker.R

data class OnboardingSlide(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
    @param:DrawableRes val imageRes: Int,
)

data class OnboardingUiState(
    val slides: List<OnboardingSlide> = defaultOnboardingSlides,
    val currentPage: Int = 0,
    val isCompleting: Boolean = false,
) {
    val isLastPage: Boolean get() = currentPage >= slides.lastIndex
    val pageCount: Int get() = slides.size
}

sealed interface OnboardingEvent {
    data class PageChanged(val index: Int) : OnboardingEvent
    data object Next : OnboardingEvent
    data object Skip : OnboardingEvent
    data object Back : OnboardingEvent
}

sealed interface OnboardingEffect {
    data object Finished : OnboardingEffect
    data object NavigateBack : OnboardingEffect
}

val defaultOnboardingSlides: List<OnboardingSlide> = listOf(
    OnboardingSlide(
        titleRes = R.string.onboarding_slide1_title,
        bodyRes = R.string.onboarding_slide1_body,
        imageRes = R.drawable.find_places,
    ),
    OnboardingSlide(
        titleRes = R.string.onboarding_slide2_title,
        bodyRes = R.string.onboarding_slide2_body,
        imageRes = R.drawable.live_street,
    ),
    OnboardingSlide(
        titleRes = R.string.onboarding_slide3_title,
        bodyRes = R.string.onboarding_slide3_body,
        imageRes = R.drawable.beat_traffic,
    ),
    OnboardingSlide(
        titleRes = R.string.onboarding_slide4_title,
        bodyRes = R.string.onboarding_slide4_body,
        imageRes = R.drawable.know_your_world,
    ),
)
