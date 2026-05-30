package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.R

val CinzelFont = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold),
)

val NunitoFont = FontFamily(
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CinzelFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )
)
