package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.R

// Gunakan font lokal yang diunduh secara offline hulu-hilir saat membangun aplikasi
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

