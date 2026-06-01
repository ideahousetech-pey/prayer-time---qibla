package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.R

// File font tersedia di res/font/:
// cinzel_regular.ttf, cinzel_bold.ttf
// nunito_light.ttf, nunito_regular.ttf, nunito_semibold.ttf, nunito_bold.ttf

val CinzelFont = FontFamily.Serif

val NunitoFont = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 18.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        letterSpacing = 0.3.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 11.sp,
        letterSpacing = 1.2.sp
    )
)

