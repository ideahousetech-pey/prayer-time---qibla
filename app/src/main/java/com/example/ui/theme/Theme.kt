package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    primary            = GoldPrimary,
    onPrimary          = DeepNight,
    primaryContainer   = GoldGlow,
    secondary          = TealAccent,
    onSecondary        = DeepNight,
    tertiary           = GoldLight,
    background         = DeepNight,
    onBackground       = TextPrimary,
    surface            = CardSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = CardElevated,
    onSurfaceVariant   = TextSecondary,
    outline            = DividerLine,
    error              = WarningAmber,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
