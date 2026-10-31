package com.example.gpstracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gpstracker.core.data.ThemeMode
import com.example.gpstracker.core.designsystem.theme.AppTheme
import com.example.gpstracker.core.locale.AppLocale
import com.example.gpstracker.core.navigation.AppNavHost
import com.example.gpstracker.core.navigation.AppNavViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val navViewModel: AppNavViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Only wait for the start destination. Do not hold the splash for a
        // locale recreate here — that raced the language screen and froze the UI.
        splashScreen.setKeepOnScreenCondition { !navViewModel.isSettingsReady }

        AppLocale.applyLayoutDirection(this)
        enableEdgeToEdge()

        val languageCode = AppLocale.read(this)
        val layoutDirection = if (AppLocale.isRtl(languageCode)) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }

        setContent {
            // Key on language so the whole UI tree is rebuilt when RTL ↔ LTR flips.
            key(languageCode) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    val themeMode by navViewModel.themeMode.collectAsStateWithLifecycle()
                    val systemDark = isSystemInDarkTheme()
                    val darkTheme = when (themeMode) {
                        ThemeMode.Auto -> systemDark
                        ThemeMode.Light -> false
                        ThemeMode.Dark -> true
                    }
                    AppTheme(darkTheme = darkTheme) {
                        AppNavHost(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = navViewModel,
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Re-assert after the window is attached; decor direction can lag recreate.
        AppLocale.applyLayoutDirection(this)
    }
}
