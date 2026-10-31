package com.example.gpstracker.feature.onboarding.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.common.ObserveAsEvents
import com.example.gpstracker.core.locale.AppLocale

@Composable
fun LanguageRoute(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    fromSettings: Boolean = false,
    onNavigateBack: () -> Unit = {},
    viewModel: LanguageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            LanguageEffect.NavigateNext -> applyLanguage(context, uiState.selectedCode, onContinue)
            LanguageEffect.NavigateBack -> applyLanguage(context, uiState.selectedCode, onNavigateBack)
        }
    }

    LanguageScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        fromSettings = fromSettings,
        modifier = modifier,
    )
}

/**
 * Persist the locale, navigate first, then recreate when the language or
 * layout direction (RTL ↔ LTR) does not match. Recreate is posted so
 * navigation (and back-stack save) finishes before the activity is torn down.
 */
private fun applyLanguage(context: Context, code: String, navigate: () -> Unit) {
    AppLocale.persist(context, code)
    navigate()
    val activity = context as? Activity ?: return
    val directionChanged =
        activity.resources.configuration.layoutDirection != AppLocale.layoutDirectionFor(code)
    if (AppLocale.matches(activity, code) && !directionChanged) return
    activity.window.decorView.post {
        if (!activity.isFinishing && !activity.isDestroyed) {
            activity.recreate()
        }
    }
}
