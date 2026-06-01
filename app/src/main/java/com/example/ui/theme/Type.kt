package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.R

/**
 * ==========================================
 * DESIGN TOKENS: TYPOGRAPHY PAIRINGS
 * ==========================================
 *
 * Font 1: Cinzel - Classic Roman/Architectural Serifs for Titles (Islamic Royalty aesthetic)
 * Font 2: Nunito - Modern, organic, rounded sans-serif for high continuous legibility
 */

val CinzelFont = FontFamily(
    Font(resId = R.font.cinzel_regular, weight = FontWeight.Normal),
    Font(resId = R.font.cinzel_bold, weight = FontWeight.Bold)
)

val NunitoFont = FontFamily(
    Font(resId = R.font.nunito_light, weight = FontWeight.Light),
    Font(resId = R.font.nunito_regular, weight = FontWeight.Normal),
    Font(resId = R.font.nunito_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.nunito_bold, weight = FontWeight.Bold)
)

/**
 * Material 3 Expressive Typography Tokens
 */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 34.sp,
        lineHeight = 44.sp,
        letterSpacing = 1.25.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 1.0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.75.sp
    ),
    
    titleLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp
    ),
    
    bodyLarge = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Light,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    labelLarge = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    )
)
