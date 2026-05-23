package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerTime

/**
 * Widget Carousel horizontal untuk menampilkan jadwal waktu sholat 5 waktu hari ini.
 * Menandai dan menghighlight sholat berikutnya dengan list border bercahaya warna emas khas Islami.
 */
@Composable
fun PrayerCarousel(
    times: PrayerTime,
    nextPrayerName: String,
    modifier: Modifier = Modifier
) {
    val prayers = listOf(
        PrayerCardItem("Subuh", times.fajr, "Fajr", "04::05"),
        PrayerCardItem("Dzuhur", times.dhuhr, "Dhuhr", "12::00"),
        PrayerCardItem("Ashar", times.asr, "Asr", "15::15"),
        PrayerCardItem("Maghrib", times.maghrib, "Maghrib", "18::00"),
        PrayerCardItem("Isya", times.isha, "Isha", "19::15")
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(prayers) { prayer ->
            val isHighlighted = nextPrayerName.contains(prayer.name, ignoreCase = true)
            PrayerItemCard(prayer = prayer, isHighlighted = isHighlighted)
        }
    }
}

@Composable
fun PrayerItemCard(
    prayer: PrayerCardItem,
    isHighlighted: Boolean
) {
    val baseCardColors = if (isHighlighted) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary, // Solid premium Gold
            contentColor = Color(0xFF00382F) // Dark contrast text
        )
    } else {
        CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f), // Elegant frosted glass base
            contentColor = Color.White
        )
    }

    val borderModifier = if (isHighlighted) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFFFFFF)
                )
            ),
            shape = RoundedCornerShape(16.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f), // Thin semi-transparent glass border
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = Modifier
            .width(105.dp)
            .height(130.dp)
            .then(borderModifier),
        shape = RoundedCornerShape(16.dp),
        colors = baseCardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 6.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayer.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) Color(0xFF00382F) else Color.White
            )
            
            Text(
                text = "(${prayer.arabicName})",
                fontSize = 11.sp,
                color = if (isHighlighted) Color(0xFF00382F).copy(alpha = 0.75f) else Color(0xFFB2DFDB), // soft light teal
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .background(
                        color = if (isHighlighted) Color.White.copy(alpha = 0.35f) 
                                else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = prayer.time,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = if (isHighlighted) Color(0xFF00382F) else MaterialTheme.colorScheme.primary // Gold text on normal glass
                )
            }
        }
    }
}

data class PrayerCardItem(
    val name: String,
    val time: String,
    val arabicName: String,
    val id: String
)
