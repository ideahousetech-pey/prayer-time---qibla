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
 * HIGH-PRECISION DESIGN TOKENS: TYPOGRAPHY
 * ==========================================
 *
 * Font 1: Cinzel - Classic Roman Serif for Display/Titles (Islamic Luxury Royalty)
 * Font 2: Nunito - Modern Rounded Sans-Serif for body readability
 * Font 3: Amiri - Classic Naskh Quranic typeface for Arabic Scriptures
 */

val CinzelFont = FontFamily.Serif

val NunitoFont = FontFamily.SansSerif

val AmiriQuranFont = FontFamily.Default

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
    
    headlineLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
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
