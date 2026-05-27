package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerTime
import com.example.utils.HijriDateUtils
import com.example.viewmodel.LocationViewModel
import com.example.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * DailyScheduleScreen: Screen Jadwal Sholat Harian premium yang merepresentasikan gambar acuan.
 * Menampilkan:
 * 1. Toolbar kustom dengan tombol back (kembali), realtime alamat lokasi (Kecamatan Senen), share, dan calendar.
 * 2. Diagram lintasan matahari (Sun Path Arc) putus-putus dengan node kustom dan ornamen bulan sabit.
 * 3. Horizontal strip hari (weekly timeline strip) melingkar merah pada tanggal terpilih, dapat diklik secara interaktif.
 * 4. Tanggal ganda Gregorian & Hijriah hasil konversi selected date.
 * 5. Daftar 6 waktu sholat (termasuk Terbit matahari) dng tombol toggle speaker alarm individual untuk masing-masing waktu.
 */
@Composable
fun DailyScheduleScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    onBackClick: () -> Unit,
    onNavigateToMonthly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationName by locationViewModel.locationName.collectAsState()
    val monthlySchedule by prayerViewModel.monthlySchedule.collectAsState()

    // 1. Tanggal aktif harian terpilih (default: hari ini)
    val todayCalendar = remember { Calendar.getInstance() }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    // Hitung 7 hari timeline yang melingkupi hari ini (H-3 s.d H+3)
    val timelineDays = remember {
        (0..6).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset - 3)
            cal
        }
    }

    // Mengambil data jadwal sholat untuk tanggal terpilih
    val activeSchedule = remember(selectedCalendar, monthlySchedule) {
        val targetDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        if (monthlySchedule.isNotEmpty()) {
            val idx = (targetDay - 1).coerceIn(0, monthlySchedule.size - 1)
            monthlySchedule[idx]
        } else {
            null
        }
    }

    // Format tanggal gregorian untuk selected date
    val formattedSelectedGregorian = remember(selectedCalendar) {
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale("id", "ID"))
        sdf.format(selectedCalendar.time)
    }

    // Format tanggal hijriah untuk selected date
    val formattedSelectedHijri = remember(selectedCalendar) {
        val hijri = HijriDateUtils.convertToHijri(selectedCalendar)
        hijri.formatted
    }

    // SharedPreferences untuk status ON/OFF adzan individual
    val adzanPrefs = remember { context.getSharedPreferences("adzan_individual_prefs", Context.MODE_PRIVATE) }
    
    // State lokal untuk trigger komposisi ulang saat speaker ditekan
    var refreshSpeakerState by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. TOOLBAR KUSTOM PREMIUM ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00382F).copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Beranda",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Menampilkan nama lokasi realtime AKTIF (e.g. Kecamatan Senen)
                Column {
                    Text(
                        text = if (locationName.isEmpty()) "Mencari Lokasi..." else locationName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Membagikan jadwal sholat hari ini...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onNavigateToMonthly,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Jadwal Bulanan",
                        tint = Color.White
                    )
                }
            }
        }

        // --- 2. DIAGRAM LINTASAN MATAHARI (SUN PATH ARC) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00382F).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
                val w = size.width
                val h = size.height

                // Gambar kurva setengah lingkaran putus-putus (Dashed Semi-Circular Arc)
                val path = Path().apply {
                    // Mulai dari kiri bawah ke kanan bawah membentuk busur kubah
                    moveTo(0f, h)
                    cubicTo(w * 0.25f, h * -0.3f, w * 0.75f, h * -0.3f, w, h)
                }

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.45f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )

                // Gambar bulatan-bulatan kecil pengisi node di sepanjang lintasan (Subuh, Terbit, Dzuhur, Ashar, Maghrib, Isya)
                val nodes = listOf(
                    Offset(0f, h), // Subuh
                    Offset(w * 0.15f, h * 0.65f), // Terbit
                    Offset(w * 0.5f, h * 0.13f), // Dzuhur
                    Offset(w * 0.73f, h * 0.35f), // Ashar
                    Offset(w * 0.9f, h * 0.77f), // Maghrib
                    Offset(w, h) // Isya
                )

                nodes.forEach { node ->
                    // Gambar lingkaran luar putih
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = node
                    )
                    // Centered checkmark dot inside
                    drawCircle(
                        color = Color(0xFF004D40),
                        radius = 3.dp.toPx(),
                        center = node
                    )
                }
            }

            // Ornamen Bulan Sabit di sisi kanan atas (Representing the lunar crescent from image mockup)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 24.dp)
                    .size(38.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        Divider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

        // --- 3. HORIZONTAL TIMELINE STRIP HARI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timelineDays.forEach { cal ->
                val isSelected = cal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
                val dayLabel = when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "M"
                    Calendar.MONDAY -> "S"
                    Calendar.TUESDAY -> "S"
                    Calendar.WEDNESDAY -> "R"
                    Calendar.THURSDAY -> "K"
                    Calendar.FRIDAY -> "J"
                    Calendar.SATURDAY -> "S"
                    else -> "M"
                }
                val dateLabel = cal.get(Calendar.DAY_OF_MONTH).toString()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedCalendar = cal }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dayLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color(0xFFFF5252) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFD32F2F), CircleShape), // Solid Red circle matching mockup
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Divider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

        // --- 4. TANGGAL GANDA AKTIF SELEKSI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$formattedSelectedGregorian  /  $formattedSelectedHijri",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Divider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

        // --- 5. DAFTAR 6 WAKTU SHOLAT DNG INDIVIDUAL SPEAKER TOGGLE ---
        if (activeSchedule != null) {
            val terbitTime = remember(activeSchedule.fajr) { 
                calculateTerbitFromFajr(activeSchedule.fajr) 
            }

            val pList = listOf(
                PrayerRowItem("Subuh (Fajr)", activeSchedule.fajr, "subuh"),
                PrayerRowItem("Terbit", terbitTime, "terbit", isTerbit = true),
                PrayerRowItem("Dzuhur", activeSchedule.dhuhr, "dzuhur"),
                PrayerRowItem("Ashar", activeSchedule.asr, "ashar"),
                PrayerRowItem("Maghrib", activeSchedule.maghrib, "maghrib"),
                PrayerRowItem("Isya", activeSchedule.isha, "isya")
            )

            // Memaksa komposisi ulang saat speaker ditekan
            val stateTrigger = refreshSpeakerState

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                pList.forEach { p ->
                    val isAlarmOn = adzanPrefs.getBoolean("enable_adzan_${p.prefKey}", true)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Nama Sholat
                        Text(
                            text = p.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Area Jam & Action Speaker
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.timeValue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(end = 16.dp)
                            )

                            if (p.isTerbit) {
                                // Ikon larangan untuk terbit matahari sesuai gambar
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Matahari Terbit (Dilarang Sholat)",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                // Tombol speaker interaktif untuk menyalakan/mematikan suara adzan
                                IconButton(
                                    onClick = {
                                        val currentVal = adzanPrefs.getBoolean("enable_adzan_${p.prefKey}", true)
                                        adzanPrefs.edit().putBoolean("enable_adzan_${p.prefKey}", !currentVal).apply()
                                        refreshSpeakerState++
                                        Toast.makeText(
                                            context,
                                            if (!currentVal) "Suara Adzan ${p.name} Diaktifkan" else "Suara Adzan ${p.name} Dimatikan",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAlarmOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                        contentDescription = "Atur Adzan ${p.name}",
                                        tint = if (isAlarmOn) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)
                }
            }
        } else {
            // Animasi / Loading state saat sinkronisasi lokasi GPS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * Model data internal pembangun row list sholat harian
 */
private data class PrayerRowItem(
    val name: String,
    val timeValue: String,
    val prefKey: String,
    val isTerbit: Boolean = false
)

/**
 * Fungsi pembantu dinamis menghitung waktu matahari Terbit dari waktu Subuh.
 * Rata-rata selisih fajar/subuh ke terbit fisis di Indonesia berkisar 75 menit (1 jam 15 menit).
 */
private fun calculateTerbitFromFajr(fajr: String): String {
    return try {
        val parts = fajr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val totalMinutes = hour * 60 + minute + 75
        val targetHour = (totalMinutes / 60) % 24
        val targetMinute = totalMinutes % 60
        "%02d.%02d".format(targetHour, targetMinute)
    } catch (e: Exception) {
        "05.50"
    }
}
