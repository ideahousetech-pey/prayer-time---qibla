package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * ==========================================
 * IMPERIAL ISLAMIC LUXURY 2026 COMPOSE THEME
 * ==========================================
 *
 * Integrates:
 * - Malachite Emerald & Saffron Gold Color Tokens
 * - Sculpted Imperial Geometric Shapes and Corners
 * - Spacious calligraphic & modern legible typography pairings
 */

private val AppColorScheme = darkColorScheme(
    primary            = StaticGoldPrimary,
    onPrimary          = StaticDeepNight,
    primaryContainer   = StaticGoldGlow,
    onPrimaryContainer = StaticGoldLight,
    secondary          = StaticTealAccent,
    onSecondary        = StaticDeepNight,
    secondaryContainer = StaticTealDim,
    onSecondaryContainer = StaticTextPrimary,
    tertiary           = StaticGoldLight,
    onTertiary         = StaticDeepNight,
    background         = StaticDeepNight,
    onBackground       = StaticTextPrimary,
    surface            = StaticCardSurface,
    onSurface          = StaticTextPrimary,
    surfaceVariant     = StaticCardElevated,
    onSurfaceVariant   = StaticTextSecondary,
    outline            = StaticDividerLine,
    error              = StaticErrorRed,
    onError            = StaticDeepNight
)

private val AppLightColorScheme = lightColorScheme(
    primary            = Color(0xFFC29D38),
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFFAF9F6),
    onPrimaryContainer = Color(0xFF826315),
    secondary          = Color(0xFF008D80),
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4FAF5),
    onSecondaryContainer = Color(0xFF0C241B),
    tertiary           = Color(0xFF6E5311),
    onTertiary         = Color(0xFFFFFFFF),
    background         = Color(0xFFFAF9F6),
    onBackground       = Color(0xFF0C241B),
    surface            = Color(0xFFFFFFFF),
    onSurface          = Color(0xFF0C241B),
    surfaceVariant     = Color(0xFFFAF9F6),
    onSurfaceVariant   = Color(0xFF426354),
    outline            = Color(0xFFEDEAE1),
    error              = Color(0xFFDC2626),
    onError            = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "dark",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    
    // Sync JVM state for global static coordinate stability
    AppThemeState.isDarkTheme = darkTheme

    val colors = if (darkTheme) {
        AppColorScheme
    } else {
        AppLightColorScheme
    }

    // 2026 Material 3 Expressive Engine Activation
    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}

