package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerTime
import com.example.viewmodel.LocationViewModel
import com.example.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.CircleShape

/**
 * Screen Jadwal Sholat Bulanan.
 * Menampilkan tabel periodik gulir ke bawah berisi jadwal sholat lengkap 30 hari ke depan.
 * Data otomatis disesuaikan dengan lintang/bujur GPS realtime koordinat user saat ini.
 * Menyorot baris tanggal hari ini dengan warna Tosca-Emas agar mudah dipindai oleh mata pengguna.
 */
@Composable
fun MonthlyScheduleScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthlySchedule by prayerViewModel.monthlySchedule.collectAsState()
    val locationName by locationViewModel.locationName.collectAsState()

    // Mendapatkan hari aktif saat ini dalam bulan untuk visual highlight
    val currentDayOfMonth = rememberCurrentDayOfMonth()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali ke Menu Utama",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Header Judul Halaman
        Text(
            text = "PERIODIK BULANAN",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB2DFDB), // Soft light-teal theme text
            letterSpacing = 2.sp
        )
        Text(
            text = "Jadwal Sholat 30 Hari",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary, // Gold
            modifier = Modifier.padding(top = 4.dp)
        )

        // Lokasi Info GPS aktif
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Posisi",
                tint = MaterialTheme.colorScheme.primary, // Gold Icon
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = locationName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White // Sharp readable white
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Header Tabel Jadwal
        TableHeadersRow()

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Daftar Gulir Tabel Bulanan Sholat
        if (monthlySchedule.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(monthlySchedule) { index, item ->
                    // Index ke-0 mewakili Tanggal 1, index ke-(currentDay-1) mewakili Hari ini
                    val isToday = (index + 1) == currentDayOfMonth
                    TableRowItemCard(item = item, dayIndex = index + 1, isToday = isToday)
                }
            }
        } else {
            // Loading State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mempersiapkan data tabel jadwal bulanan...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TableHeadersRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tgl Masehi",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1.8f)
        )
        Text(
            text = "Subuh",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Dzuhur",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Ashar",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Magrib",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Isya",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TableRowItemCard(
    item: PrayerTime,
    dayIndex: Int,
    isToday: Boolean
) {
    val containerBg = if (isToday) {
        MaterialTheme.colorScheme.primary // Solid gold highlight!
    } else {
        Color.White.copy(alpha = 0.08f) // Frosted glass row
    }

    val borderModifier = if (isToday) {
        Modifier.border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
    } else {
        Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
    }

    val textWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
    val mainColor = if (isToday) Color(0xFF00382F) else Color.White // Deep green contrast vs pure white

    // Ubah nama hari panjang "Rabu, 13 Mei 2026" jadi format pendek "13 Mei, Rab" agar muat seluler
    val formattedDateStr = simplifyDateString(item.dateGregorian)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDateStr,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) Color(0xFF00382F) else Color(0xFFB2DFDB), // Dark text vs Soft light-teal
                modifier = Modifier.weight(1.8f)
            )

            Text(
                text = item.fajr,
                fontSize = 12.sp,
                fontWeight = textWeight,
                color = mainColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.dhuhr,
                fontSize = 12.sp,
                fontWeight = textWeight,
                color = mainColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.asr,
                fontSize = 12.sp,
                fontWeight = textWeight,
                color = mainColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.maghrib,
                fontSize = 12.sp,
                fontWeight = textWeight,
                color = mainColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.isha,
                fontSize = 12.sp,
                fontWeight = textWeight,
                color = mainColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Menyederhanakan "Rabu, 13 Mei 2026" menjadi "13 Mei (Rab)" agar hemat ruang layout horizontal
 */
private fun simplifyDateString(raw: String): String {
    return try {
        val parts = raw.split(",")
        val dayName = parts[0].substring(0, 3).trim() // Rab
        val dateBody = parts[1].replace(" 2026", "").trim() // 13 Mei
        "$dateBody ($dayName)"
    } catch (e: Exception) {
        raw
    }
}

@Composable
fun rememberCurrentDayOfMonth(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.DAY_OF_MONTH)
}
