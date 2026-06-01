package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

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
    primary            = GoldPrimary,
    onPrimary          = DeepNight,
    primaryContainer   = GoldGlow,
    onPrimaryContainer = GoldLight,
    secondary          = TealAccent,
    onSecondary        = DeepNight,
    secondaryContainer = TealDim,
    onSecondaryContainer = TextPrimary,
    tertiary           = GoldLight,
    onTertiary         = DeepNight,
    background         = DeepNight,
    onBackground       = TextPrimary,
    surface            = CardSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = CardElevated,
    onSurfaceVariant   = TextSecondary,
    outline            = DividerLine,
    error              = ErrorRed,
    onError            = DeepNight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 2026 Material 3 Expressive Engine Activation
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
