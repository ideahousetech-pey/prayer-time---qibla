package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IslamicHoliday
import com.example.utils.HijriDateUtils
import com.example.utils.HijriDayGridItem
import java.util.Calendar

import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Screen Kalender Hijriah.
 * Menampilkan grid bulanan penanggalan Hijriah lengkap dengan tanggal masehi di bawahnya.
 * Menyediakan tombol navigasi bulan/tahun Hijriah yang interaktif.
 * Menandai tanggal Hari Besar Islam dengan warna hangat dan meluncurkan popup deskripsi spiritual saat diklik.
 */
@Composable
fun CalendarScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Ambil penanggalan Hijriah saat ini sebagai posisi awal de-facto
    val currentCalendar = remember { Calendar.getInstance() }
    val currentHijri = remember { HijriDateUtils.convertToHijri(currentCalendar) }

    // State bulan dan tahun Hijriah aktif yang bisa dinavigasi user
    var activeMonth by remember { mutableStateOf(currentHijri.month) }
    var activeYear by remember { mutableStateOf(currentHijri.year) }

    // State untuk menampung liburan terpilih yang diklik untuk popup penjelasan
    var selectedHoliday by remember { mutableStateOf<IslamicHoliday?>(null) }

    val monthsList = listOf(
        "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir", 
        "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban", 
        "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
    )

    val gridItems = remember(activeMonth, activeYear) {
        HijriDateUtils.getHijriMonthGrid(activeMonth, activeYear)
    }

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
            text = "KALENDER ISLAMI",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB2DFDB), // Soft light-teal theme text
            letterSpacing = 2.sp
        )
        Text(
            text = "Kalender Hijriah",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary, // Gold
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Navigasi Bulan / Tahun Panel
        CalendarNavigationHeader(
            monthName = monthsList[activeMonth - 1],
            yearStr = "$activeYear H",
            onPreviousClick = {
                if (activeMonth == 1) {
                    activeMonth = 12
                    activeYear -= 1
                } else {
                    activeMonth -= 1
                }
            },
            onNextClick = {
                if (activeMonth == 12) {
                    activeMonth = 1
                    activeYear += 1
                } else {
                    activeMonth += 1
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Weekday Labels (Minggu s/d Sabtu)
        WeekdayLabelsRow()

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Kalender Grid: Kotak-Kotak Tanggal Hijriah + Masehi
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gridItems) { item ->
                CalendarGridCell(
                    item = item,
                    isToday = item.hDay == currentHijri.day && item.hMonth == currentHijri.month && item.hYear == currentHijri.year,
                    onClick = {
                        if (item.holidayName != null) {
                            val fullHoliday = HijriDateUtils.checkHoliday(item.hDay, item.hMonth)
                            selectedHoliday = fullHoliday ?: IslamicHoliday(
                                hijriDate = "%02d-%02d".format(item.hDay, item.hMonth),
                                name = item.holidayName,
                                description = item.holidayDescription ?: ""
                            )
                        }
                    }
                )
            }
        }

        // 4b. List Hari Besar Bulan Ini di bawah kalender tanggal
        val monthlyHolidays = remember(gridItems) {
            gridItems.filter { !it.isPadding && it.holidayName != null }
        }

        if (monthlyHolidays.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "Hari Besar di Bulan Ini",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                    )
                    monthlyHolidays.forEach { hItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val fullHoliday = HijriDateUtils.checkHoliday(hItem.hDay, hItem.hMonth)
                                    selectedHoliday = fullHoliday ?: IslamicHoliday(
                                        hijriDate = "%02d-%02d".format(hItem.hDay, hItem.hMonth),
                                        name = hItem.holidayName ?: "",
                                        description = hItem.holidayDescription ?: ""
                                    )
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, Color(0xFFD4AF37), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🕌", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hItem.holidayName ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${hItem.hDay} ${monthsList[activeMonth - 1]} (${hItem.mDayLabel})",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB2DFDB)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Popup Detil Hari Besar jika diklik
        if (selectedHoliday != null) {
            HolidayDialog(
                holiday = selectedHoliday!!,
                onDismiss = { selectedHoliday = null }
            )
        }
    }
}

@Composable
fun CalendarNavigationHeader(
    monthName: String,
    yearStr: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Bulan Sebelumnya",
                    tint = MaterialTheme.colorScheme.primary, // Gold Icon
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Sharp white text
                )
                Text(
                    text = yearStr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary // Gold Accent
                )
            }

            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Bulan Berikutnya",
                    tint = MaterialTheme.colorScheme.primary, // Gold Icon
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun WeekdayLabelsRow() {
    val days = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { dayName ->
            Text(
                text = dayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (dayName == "Min") Color(0xFFFF5252) else Color(0xFFB2DFDB), // red accent & soft light teal
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

@Composable
fun CalendarGridCell(
    item: HijriDayGridItem,
    isToday: Boolean,
    onClick: () -> Unit
) {
    if (item.isPadding) {
        // Kotak kosong penyeimbang selisih tanggal
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    val isHoliday = item.holidayName != null

    val bgModifier = when {
        isToday -> Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        isHoliday -> Modifier.background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
        else -> Modifier.background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    }

    val borderModifier = when {
        isToday -> Modifier.border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
        isHoliday -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        else -> Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
    }

    val mainTextColor = when {
        isToday -> Color(0xFF00382F) // Deep contrast green on gold
        isHoliday -> MaterialTheme.colorScheme.primary
        else -> Color.White
    }

    val subTextColor = when {
        isToday -> Color(0xFF00382F).copy(alpha = 0.75f)
        isHoliday -> Color(0xFFB2DFDB)
        else -> Color(0xFFB2DFDB) // Soft light teal text
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .then(bgModifier)
            .then(borderModifier)
            .clickable(enabled = isHoliday) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = item.hDay.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = mainTextColor
            )
            Text(
                text = item.mDayLabel,
                fontSize = 10.sp,
                color = subTextColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 1.dp)
            )
            if (isHoliday) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
            }
        }
    }
}
