package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedQabasColors(
    val gold: Color = QabasGold,
    val goldLight: Color = QabasGoldLight,
    val goldGlow: Color = QabasGoldGlow,
    val goldDark: Color = QabasGoldDark,
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceTranslucent: Color,
    val surfaceElevated: Color,
    val surfaceBorder: Color,
    val surfaceBorderGlow: Color,
    val studio: Color = StudioBlue,
    val studioGlow: Color = StudioBlueGlow,
    val companion: Color = CompanionPurple,
    val companionGlow: Color = CompanionPurpleGlow,
    val mihrab: Color = MihrabGreen,
    val mihrabGlow: Color = MihrabGreenGlow,
    val knowledge: Color = KnowledgeTurquoise,
    val knowledgeGlow: Color = KnowledgeTurquoiseGlow,
    val impact: Color = ImpactOliveGold,
    val impactGlow: Color = ImpactOliveGoldGlow,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val LocalExtendedQabasColors = staticCompositionLocalOf<ExtendedQabasColors> {
    error("No QabasColors provided")
}

private val DarkExtendedColors = ExtendedQabasColors(
    background = QabasDarkBackground,
    backgroundSecondary = QabasDarkBackgroundSecondary,
    surface = QabasSurfaceDark,
    surfaceTranslucent = QabasSurfaceDarkTranslucent,
    surfaceElevated = QabasSurfaceDarkElevated,
    surfaceBorder = QabasSurfaceDarkBorder,
    surfaceBorderGlow = QabasSurfaceDarkBorderGlow,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    isDark = true
)

private val LightExtendedColors = ExtendedQabasColors(
    gold = QabasGoldLightMode,
    goldLight = Color(0xFFC9A227),
    goldGlow = Color(0xFFDFB83D),
    goldDark = Color(0xFF8F6E0E),
    background = QabasLightBackground,
    backgroundSecondary = QabasLightBackgroundSecondary,
    surface = QabasSurfaceLight,
    surfaceTranslucent = QabasSurfaceLightTranslucent,
    surfaceElevated = QabasSurfaceLightElevated,
    surfaceBorder = QabasSurfaceLightBorder,
    surfaceBorderGlow = QabasGoldLightMode.copy(alpha = 0.35f),
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight,
    isDark = false
)

private val DarkColorScheme = darkColorScheme(
    primary = QabasGold,
    onPrimary = Color(0xFF1E1700),
    primaryContainer = Color(0xFF433400),
    onPrimaryContainer = QabasGoldLight,
    secondary = StudioBlueLight,
    onSecondary = Color(0xFF002D6C),
    tertiary = MihrabGreenLight,
    onTertiary = Color(0xFF00391E),
    background = QabasDarkBackground,
    onBackground = TextPrimaryDark,
    surface = QabasSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = QabasSurfaceDarkElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = QabasSurfaceDarkBorder,
    outlineVariant = Color(0xFF151E2D)
)

private val LightColorScheme = lightColorScheme(
    primary = QabasGoldLightMode,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDF7D),
    onPrimaryContainer = Color(0xFF3B2F00),
    secondary = StudioBlue,
    onSecondary = Color.White,
    tertiary = MihrabGreen,
    onTertiary = Color.White,
    background = QabasLightBackground,
    onBackground = TextPrimaryLight,
    surface = QabasSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = QabasSurfaceLightElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = QabasSurfaceLightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

object QabasThemeTokens {
    val colors: ExtendedQabasColors
        @Composable
        get() = LocalExtendedQabasColors.current

    val dimens = QabasDimens
}

@Composable
fun QabasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val view = LocalView.current

    if (!view.isInEditMode && view.context is Activity) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalExtendedQabasColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    QabasTheme(darkTheme = darkTheme, content = content)
}
