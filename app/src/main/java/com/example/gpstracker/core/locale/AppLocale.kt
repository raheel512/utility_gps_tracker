package com.example.gpstracker.core.locale

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.text.TextUtils
import android.view.View
import com.example.gpstracker.core.common.AppConfig
import java.util.Locale

/**
 * Applies the language chosen on the language screen.
 * The code is stored in preferences so it can be read while the activity
 * is starting, including on Android 7, before DataStore is ready.
 */
object AppLocale {
    private const val PREFS = "app_locale"
    private const val KEY_CODE = "code"

    fun wrap(base: Context): Context {
        val locale = localeFor(read(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    /**
     * Forces window + view layout direction to match the saved language.
     * Needed when switching RTL ↔ LTR: [createConfigurationContext] updates
     * strings, but the decor view can keep the previous direction.
     */
    fun applyLayoutDirection(activity: Activity) {
        val direction = layoutDirectionFor(read(activity))
        activity.window.decorView.layoutDirection = direction
        activity.window.decorView.textDirection = if (direction == View.LAYOUT_DIRECTION_RTL) {
            View.TEXT_DIRECTION_RTL
        } else {
            View.TEXT_DIRECTION_LTR
        }
        val content = activity.findViewById<View>(android.R.id.content)
        content?.layoutDirection = direction
    }

    fun persist(context: Context, code: String) {
        val normalized = normalize(code)
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CODE, normalized)
            .commit()
    }

    fun read(context: Context): String =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CODE, AppConfig.DEFAULT_LANGUAGE_CODE)
            ?: AppConfig.DEFAULT_LANGUAGE_CODE

    fun matches(context: Context, code: String): Boolean {
        val current = context.resources.configuration.locales[0].language
        val normalized = normalize(code)
        val languageMatches = when (normalized) {
            "id" -> current == "id" || current == "in"
            "no" -> current == "no" || current == "nb" || current == "nn"
            "zh" -> current == "zh"
            else -> current == normalized
        }
        if (!languageMatches) return false
        return context.resources.configuration.layoutDirection == layoutDirectionFor(normalized)
    }

    fun isRtl(code: String): Boolean =
        layoutDirectionFor(code) == View.LAYOUT_DIRECTION_RTL

    fun layoutDirectionFor(code: String): Int =
        TextUtils.getLayoutDirectionFromLocale(localeFor(code))

    fun localeFor(code: String): Locale = when (normalize(code)) {
        "no" -> Locale.forLanguageTag("nb")
        "zh" -> Locale.forLanguageTag("zh-CN")
        "id" -> Locale.forLanguageTag("id")
        else -> Locale.forLanguageTag(normalize(code))
    }

    private fun normalize(code: String): String = code.trim().lowercase()
}
