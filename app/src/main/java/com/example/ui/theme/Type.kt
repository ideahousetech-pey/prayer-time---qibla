package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.R

// Definisikan Font Provider Google Fonts yang terpercaya lewat Play Services
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Font Cinzel yang dipanggil dinamis
val cinzelFontName = GoogleFont("Cinzel")
val CinzelFont = FontFamily(
    Font(googleFont = cinzelFontName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = cinzelFontName, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Font Nunito yang dipanggil dinamis
val nunitoFontName = GoogleFont("Nunito")
val NunitoFont = FontFamily(
    Font(googleFont = nunitoFontName, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = nunitoFontName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = nunitoFontName, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = nunitoFontName, fontProvider = fontProvider, weight = FontWeight.Bold)
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

