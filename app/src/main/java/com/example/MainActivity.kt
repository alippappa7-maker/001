package com.example

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.example.ui.SettingsViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.navigation.Routes
import com.example.ui.theme.QabasTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkModePref by viewModel.isDarkMode.collectAsState()
            val languagePref by viewModel.language.collectAsState()
            val hasSeenWelcome by viewModel.hasSeenWelcome.collectAsState()

            // Default dark theme as specified in product rules
            val isDarkMode = isDarkModePref ?: true
            val language = languagePref ?: "ar"

            // Configure Locale and LayoutDirection reactively
            val targetLocale = remember(language) { Locale(language) }
            val isRtl = language == "ar"
            val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            val baseContext = LocalContext.current
            val updatedContext = remember(targetLocale, baseContext) {
                val config = Configuration(baseContext.resources.configuration)
                config.setLocales(LocaleList(targetLocale))
                config.setLayoutDirection(targetLocale)
                baseContext.createConfigurationContext(config)
            }

            val updatedConfiguration = remember(targetLocale) {
                Configuration().apply {
                    setLocales(LocaleList(targetLocale))
                    setLayoutDirection(targetLocale)
                }
            }

            CompositionLocalProvider(
                LocalContext provides updatedContext,
                LocalConfiguration provides updatedConfiguration,
                LocalLayoutDirection provides layoutDirection
            ) {
                QabasTheme(darkTheme = isDarkMode) {
                    val navController = rememberNavController()
                    
                    if (hasSeenWelcome != null) {
                        val startDestination = if (hasSeenWelcome == true) Routes.HOME else Routes.WELCOME
                        AppNavigation(navController = navController, startDestination = startDestination)
                    }
                }
            }
        }
    }
}
