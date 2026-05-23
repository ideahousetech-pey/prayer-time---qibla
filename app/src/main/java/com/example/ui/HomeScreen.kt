package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.IslamicHoliday
import com.example.ui.components.PrayerCarousel
import com.example.viewmodel.LocationViewModel
import com.example.viewmodel.PrayerViewModel
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.DisposableEffect
import java.io.File
import java.io.FileOutputStream

/**
 * Screen utama aplikasi Waktu Sholat & Kiblat.
 * Menampilkan tanggal ganda (Gregorian & Hijriah) di header, nama lokasi otomatis dari GPS,
 * kartu informasi waktu sholat berikutnya lengkap dng ticking countdown mundur,
 * dan carousel horizontal jadwal sholat harian 5 waktu.
 */
@Composable
fun HomeScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    val todayGregorian by prayerViewModel.todayGregorian.collectAsState()
    val todayHijri by prayerViewModel.todayHijri.collectAsState()
    val locationName by locationViewModel.locationName.collectAsState()
    val todaySchedule by prayerViewModel.todaySchedule.collectAsState()
    val nextLabel by prayerViewModel.nextPrayerLabelLabel.collectAsState()
    val nextTime by prayerViewModel.nextPrayerTimeValue.collectAsState()
    val countdown by prayerViewModel.countdownString.collectAsState()
    val nextPrayerName by prayerViewModel.nextPrayerName.collectAsState()
    val currentHolidayPopUp by prayerViewModel.currentHolidayPopUp.collectAsState()

    // Memicu loading pertama kali lokasi dan jadwal sholat jika belum dimuat
    val userLocation by locationViewModel.userLocation.collectAsState()
    val isLoadingLoc by locationViewModel.isLoading.collectAsState()

    // Root Box is transparent to reveal the majestic repeating Islamic pattern below
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Berikan jarak aman bagi Bottom Navigation
        ) {
            // 1. Header Area: Tanpa logo / tombol aksi kanan, murni fokus informasi tanggal realtime
            HeaderSection(
                gregorianDate = todayGregorian,
                hijriDate = todayHijri
            )

            // 2. Location Area: Realtime name with GPS Refresh Button
            LocationSection(
                locationName = locationName,
                isLoading = isLoadingLoc,
                onRefreshClick = {
                    locationViewModel.refreshLocation()
                    userLocation?.let {
                        prayerViewModel.loadPrayerTimesForLocation(it.latitude, it.longitude)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Main Board: Next Prayer Card with ticking live countdown in HH:mm:ss
            NextPrayerCard(
                label = nextLabel,
                timeStr = nextTime,
                countdownStr = countdown
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Carousel Section Header Label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Jadwal Harian",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jadwal Sholat Hari Ini",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "Geser",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // 5. Carousel Row (Horizontal PageView equivalent)
            if (todaySchedule != null) {
                PrayerCarousel(
                    times = todaySchedule!!,
                    nextPrayerName = nextPrayerName,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Menghubungkan Ke Aladhan API...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AdzanSoundSettingsCard()
            Spacer(modifier = Modifier.height(8.dp))
            
            // 6. Quick Daily Reminder Note Card
            ReminderNoteCard()
        }

        // 7. PopUp Dialog Hari Besar Islam
        if (currentHolidayPopUp != null) {
            HolidayDialog(
                holiday = currentHolidayPopUp!!,
                onDismiss = { prayerViewModel.dismissHolidayPopUp() }
            )
        }
    }
}

@Composable
fun HeaderSection(gregorianDate: String, hijriDate: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = gregorianDate,
            fontSize = 15.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFFB2DFDB) // Soft light-teal theme text
        )
        Text(
            text = hijriDate,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.primary, // Glorious Gold Accent
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun LocationSection(
    locationName: String,
    isLoading: Boolean,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Posisi GPS",
                tint = MaterialTheme.colorScheme.primary, // Gold
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = locationName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White // High contrast readable white
            )
        }

        IconButton(
            onClick = onRefreshClick,
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .testTag("gps_refresh_button")
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Pindai Lokasi GPS",
                tint = MaterialTheme.colorScheme.primary, // Gold Icon
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun NextPrayerCard(
    label: String,
    timeStr: String,
    countdownStr: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SHOLAT BERIKUTNYA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary // Gold Accent
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alarm Aktif",
                            tint = MaterialTheme.colorScheme.primary, // Gold Accent
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Adzan Bersuara",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        color = Color.White
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "HITUNG MUNDUR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Timer",
                                tint = MaterialTheme.colorScheme.secondary, // Bright Gold
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 2.dp)
                            )
                            Text(
                                text = countdownStr,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary, // Bright Gold
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderNoteCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Kutipan Harian",
                tint = MaterialTheme.colorScheme.primary, // Gold Icon
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Amalan Sholat Tepat Waktu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // Elegant white header
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"Sesungguhnya sholat itu bagi orang-orang yang beriman adalah kewajiban yang ditentukan waktunya.\"\n— QS. An-Nisa': 103",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFB2DFDB) // Soft light teal text
                )
            }
        }
    }
}

/**
 * Dialog/Popup Khusus Hari Raya Islam
 */
@Composable
fun HolidayDialog(
    holiday: IslamicHoliday,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF00332C), // Deep Emerald Theme color matching app
            tonalElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD4AF37)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Close button header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Scrollable content area so it never overflows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false) // Allow scrollable content up to screen bounds
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFD4AF37).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFFD4AF37), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🕌",
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selamat Memperingati Hari Besar",
                        fontSize = 12.sp,
                        color = Color(0xFFB2DFDB), // Soft teal
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = holiday.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD4AF37), // Gold
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. Section: Deskripsi & Amalan Utama
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ AMALAN UTAMA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = holiday.description,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // 2. Section: Sejarah Peristiwa (jika ada)
                    if (holiday.history.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📖 SEJARAH PERISTIWA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB2DFDB),
                                    letterSpacing = 1.sp
                               )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holiday.history,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // 3. Section: Keutamaan & Dalil (jika ada)
                    if (holiday.quranHadith.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD4AF37).copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📜 KEUTAMAAN & DALIL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holiday.quranHadith,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.95f),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37), // Gold Button
                        contentColor = Color(0xFF002B24) // Dark emerald text for contrast
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Saya Mengerti, Alhamdulillah",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdzanSoundSettingsCard() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("adzan_prefs", Context.MODE_PRIVATE) }
    
    var customAdzanName by remember { mutableStateOf(prefs.getString("custom_adzan_name", null)) }
    var customAdzanFajrName by remember { mutableStateOf(prefs.getString("custom_adzan_fajr_name", null)) }
    
    var player: MediaPlayer? by remember { mutableStateOf(null) }
    var activePreview by remember { mutableStateOf<String?>(null) } // "umum" or "fajr" or null

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    fun playPreview(fileName: String, type: String) {
        try {
            player?.stop()
            player?.release()
            player = null
            
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                player = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        activePreview = null
                    }
                }
                activePreview = type
            } else {
                Toast.makeText(context, "File suara belum diatur, memutar nada bawaan...", Toast.LENGTH_SHORT).show()
                val defaultUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                player = MediaPlayer().apply {
                    setDataSource(context, defaultUri)
                    prepare()
                    start()
                    setOnCompletionListener {
                        activePreview = null
                    }
                }
                activePreview = type
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memutar suara: ${e.message}", Toast.LENGTH_SHORT).show()
            activePreview = null
        }
    }

    fun stopPreview() {
        player?.stop()
        player?.release()
        player = null
        activePreview = null
    }

    fun saveAudioFile(uri: Uri, isFajr: Boolean) {
        try {
            val contentResolver = context.contentResolver
            var origName = if (isFajr) "adzan_fajr_kustom.mp3" else "adzan_kustom.mp3"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        origName = cursor.getString(nameIdx)
                    }
                }
            }

            val targetFileName = if (isFajr) "adzan_fajrd.mp3" else "adzan.mp3"
            val targetFile = File(context.filesDir, targetFileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (isFajr) {
                prefs.edit().putString("custom_adzan_fajr_name", origName).apply()
                customAdzanFajrName = origName
            } else {
                prefs.edit().putString("custom_adzan_name", origName).apply()
                customAdzanName = origName
            }

            Toast.makeText(context, "Berhasil mengunggah suara: $origName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memproses file audio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun deleteCustomAudio(isFajr: Boolean) {
        val targetFileName = if (isFajr) "adzan_fajrd.mp3" else "adzan.mp3"
        val targetFile = File(context.filesDir, targetFileName)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        if (isFajr) {
            prefs.edit().remove("custom_adzan_fajr_name").apply()
            customAdzanFajrName = null
        } else {
            prefs.edit().remove("custom_adzan_name").apply()
            customAdzanName = null
        }
        stopPreview()
        Toast.makeText(context, "Suara adzan dikembalikan ke bawaan", Toast.LENGTH_SHORT).show()
    }

    val launcherUmum = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveAudioFile(it, isFajr = false) }
    }

    val launcherFajr = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveAudioFile(it, isFajr = true) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Pengaturan Adzan",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pengaturan Suara Adzan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Unggah file MP3/WAV adzan pilihan Anda",
                        fontSize = 11.sp,
                        color = Color(0xFFB2DFDB)
                    )
                }
            }

            // Row 1: Adzan Umum
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adzan Umum (Biasa)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = customAdzanName ?: "Nada Bawaan Aplikasi",
                        fontSize = 11.sp,
                        color = if (customAdzanName != null) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Play / Stop button
                    IconButton(
                        onClick = {
                            if (activePreview == "umum") {
                                stopPreview()
                            } else {
                                playPreview("adzan.mp3", "umum")
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (activePreview == "umum") Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Mainkan",
                            tint = if (activePreview == "umum") Color.Red else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Upload button
                    IconButton(
                        onClick = { launcherUmum.launch("audio/*") },
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Unggah",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete button
                    if (customAdzanName != null) {
                        IconButton(
                            onClick = { deleteCustomAudio(isFajr = false) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Adzan Subuh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adzan Khusus Subuh",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = customAdzanFajrName ?: "Nada Bawaan Aplikasi",
                        fontSize = 11.sp,
                        color = if (customAdzanFajrName != null) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Play / Stop button
                    IconButton(
                        onClick = {
                            if (activePreview == "fajr") {
                                stopPreview()
                            } else {
                                playPreview("adzan_fajrd.mp3", "fajr")
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (activePreview == "fajr") Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Mainkan",
                            tint = if (activePreview == "fajr") Color.Red else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Upload button
                    IconButton(
                        onClick = { launcherFajr.launch("audio/*") },
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Unggah",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete button
                    if (customAdzanFajrName != null) {
                        IconButton(
                            onClick = { deleteCustomAudio(isFajr = true) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
