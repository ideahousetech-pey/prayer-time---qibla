package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.VolumeOff
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
import id.ideahousetech.prayertime_qibla.model.IslamicHoliday
import id.ideahousetech.prayertime_qibla.ui.components.PrayerCarousel
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.runtime.DisposableEffect
import java.io.File
import java.io.FileOutputStream
import id.ideahousetech.prayertime_qibla.ui.theme.*

/**
 * Screen utama aplikasi Waktu Sholat & Kiblat.
 * Menampilkan tanggal ganda (Gregorian & Hijriah) di header, nama lokasi otomatis dari GPS,
 * kartu informasi waktu sholat berikutnya lengkap dng ticking countdown mundur,
 * dan carousel horizontal jadwal sholat harian 5 waktu.
 */
fun calculatePrayerProgress(
    todaySchedule: id.ideahousetech.prayertime_qibla.model.PrayerTime?,
    nextPrayerName: String
): Float {
    if (todaySchedule == null) return 0.5f
    try {
        val now = java.util.Calendar.getInstance()
        val nowMillis = now.timeInMillis
        
        fun parseTime(timeStr: String, isTomorrow: Boolean = false): java.util.Calendar {
            val parts = timeStr.split(":")
            if (parts.size < 2) return java.util.Calendar.getInstance()
            val hour = parts[0].toIntOrNull() ?: 0
            val min = parts[1].toIntOrNull() ?: 0
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, min)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            if (isTomorrow) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return cal
        }

        val subuhToday = parseTime(todaySchedule.fajr)
        val dzuhurToday = parseTime(todaySchedule.dhuhr)
        val asharToday = parseTime(todaySchedule.asr)
        val maghribToday = parseTime(todaySchedule.maghrib)
        val isyaToday = parseTime(todaySchedule.isha)
        val subuhTomorrow = parseTime(todaySchedule.fajr, isTomorrow = true)

        val pairs = listOf(
            "SUBUH" to (parseTime(todaySchedule.isha).apply { add(java.util.Calendar.DAY_OF_YEAR, -1) } to subuhToday),
            "DZUHUR" to (subuhToday to dzuhurToday),
            "ASHAR" to (dzuhurToday to asharToday),
            "MAGHRIB" to (asharToday to maghribToday),
            "ISYA" to (maghribToday to isyaToday),
            "SUBUH (ESOK)" to (isyaToday to subuhTomorrow)
        )

        val normalizedNextName = nextPrayerName.uppercase()
        val matchKey = if (normalizedNextName.contains("BESOK") || normalizedNextName.contains("ESOK")) {
            "SUBUH (ESOK)"
        } else if (normalizedNextName.contains("SUBUH") || normalizedNextName.contains("FAJR")) {
            "SUBUH"
        } else if (normalizedNextName.contains("DZUHUR") || normalizedNextName.contains("DHUHR")) {
            "DZUHUR"
        } else if (normalizedNextName.contains("ASHAR") || normalizedNextName.contains("ASR")) {
            "ASHAR"
        } else if (normalizedNextName.contains("MAGHRIB")) {
            "MAGHRIB"
        } else if (normalizedNextName.contains("ISYA") || normalizedNextName.contains("ISHA")) {
            "ISYA"
        } else {
            ""
        }

        val matchingPair = pairs.find { it.first == matchKey }?.second ?: (subuhToday to dzuhurToday)
        val prevMillis = matchingPair.first.timeInMillis
        val nextMillis = matchingPair.second.timeInMillis

        if (nextMillis <= prevMillis) return 0.5f
        val fraction = (nowMillis - prevMillis).toFloat() / (nextMillis - prevMillis).toFloat()
        return fraction.coerceIn(0f, 1f)
    } catch (e: Exception) {
        return 0.5f
    }
}

@Composable
fun HomeScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    onNavigateToScreen: (id.ideahousetech.prayertime_qibla.AppScreen) -> Unit,
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

    val userLocation by locationViewModel.userLocation.collectAsState()
    val isLoadingLoc by locationViewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val adzanPrefs = remember { SecurePrefs.get(context) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var enableDailyReminder by remember { mutableStateOf(adzanPrefs.getBoolean("enable_daily_reminder", true)) }

    // Animasi fade-in saat pertama tampil
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(DeepNight, MidnightLayer))
            )
    ) {
        // Ornamen geometri Islam di latar belakang (sangat subtle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 80.dp.toPx()
            var y = 0f
            while (y < size.height) {
                var x = 0f
                while (x < size.width) {
                    drawCircle(
                        color  = Color(0x06D4AF37),
                        radius = 30f,
                        center = Offset(x, y),
                        style  = Stroke(width = 0.5f)
                    )
                    x += step
                }
                y += step
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 10 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                Spacer(Modifier.height(52.dp))

                // 1. Header
                HomeHeader(
                    gregorianDate = todayGregorian,
                    hijriDate     = todayHijri,
                    locationName  = locationName,
                    isLoading     = isLoadingLoc,
                    onRefresh     = {
                        locationViewModel.refreshLocation()
                        userLocation?.let {
                            prayerViewModel.loadPrayerTimesForLocation(it.latitude, it.longitude)
                        }
                    },
                    onSettingsClick = { showSettingsDialog = true }
                )

                Spacer(Modifier.height(20.dp))

                // 2. Hero Card sholat berikutnya
                NextPrayerHeroCard(
                    nextPrayerName = nextPrayerName,
                    countdown      = countdown,
                    todaySchedule  = todaySchedule
                )

                Spacer(Modifier.height(20.dp))

                // 3. 5 waktu sholat hari ini
                TodayPrayerTimesRow(
                    todaySchedule  = todaySchedule,
                    nextPrayerName = nextPrayerName
                )

                Spacer(Modifier.height(28.dp))

                // 4. Grid menu
                GridMenuSection(onNavigateToScreen = onNavigateToScreen)

                if (enableDailyReminder) {
                    Spacer(Modifier.height(16.dp))
                    ReminderNoteCard()
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // PopUp Dialog Hari Besar Islam
        if (currentHolidayPopUp != null) {
            HolidayDialog(
                holiday = currentHolidayPopUp!!,
                onDismiss = { prayerViewModel.dismissHolidayPopUp() }
            )
        }

        // Dialog Pengaturan Aplikasi kustom
        if (showSettingsDialog) {
            SettingsDialog(
                locationViewModel = locationViewModel,
                onDismiss = {
                    showSettingsDialog = false
                    userLocation?.let {
                        prayerViewModel.loadPrayerTimesForLocation(it.latitude, it.longitude)
                    }
                },
                onReminderToggle = { enabled ->
                    enableDailyReminder = enabled
                }
            )
        }
    }
}





@Composable
fun HomeHeader(
    gregorianDate : String,
    hijriDate     : String,
    locationName  : String,
    isLoading     : Boolean,
    onRefresh     : () -> Unit,
    onSettingsClick : () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = locationName.ifEmpty { "Mencari lokasi..." },
                    fontSize   = 12.sp,
                    fontFamily = NunitoFont,
                    color      = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                if (isLoading) {
                    Spacer(Modifier.width(6.dp))
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        color = TealAccent,
                        strokeWidth = 1.5.dp
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text       = hijriDate,
                fontSize   = 20.sp,
                fontFamily = CinzelFont,
                color      = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text       = gregorianDate,
                fontSize   = 12.sp,
                fontFamily = NunitoFont,
                color      = TextMuted
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tombol refresh GPS
            IconButton(
                onClick  = onRefresh,
                modifier = Modifier
                    .size(40.dp)
                    .background(CardElevated, CircleShape)
                    .border(1.dp, Brush.linearGradient(listOf(GoldDim, Color.Transparent)), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Tombol Settings
            IconButton(
                onClick  = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(CardElevated, CircleShape)
                    .border(1.dp, Brush.linearGradient(listOf(GoldDim, Color.Transparent)), CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NextPrayerHeroCard(
    nextPrayerName : String,
    countdown      : String,
    todaySchedule  : id.ideahousetech.prayertime_qibla.model.PrayerTime?
) {
    val nextTime = when (nextPrayerName.uppercase()) {
        "SUBUH"   -> todaySchedule?.fajr    ?: "--:--"
        "DZUHUR"  -> todaySchedule?.dhuhr   ?: "--:--"
        "ASHAR"   -> todaySchedule?.asr     ?: "--:--"
        "MAGHRIB" -> todaySchedule?.maghrib ?: "--:--"
        "ISYA"    -> todaySchedule?.isha    ?: "--:--"
        else      -> "--:--"
    }

    val arabicName = when (nextPrayerName.uppercase()) {
        "SUBUH"   -> "الفجر"
        "DZUHUR"  -> "الظهر"
        "ASHAR"   -> "العصر"
        "MAGHRIB" -> "المغرب"
        "ISYA"    -> "العشاء"
        else      -> ""
    }

    // Efek glow berdenyut
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.55f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow"
    )

    val progressVal = calculatePrayerProgress(todaySchedule, nextPrayerName)
    val progressPct = (progressVal * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(
                Brush.linearGradient(listOf(CardSurface, CardElevated)),
                RoundedCornerShape(28.dp)
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(GoldPrimary.copy(0.5f), Color.Transparent, GoldDim.copy(0.3f))),
                RoundedCornerShape(28.dp)
            )
    ) {
        // Glow lingkaran di belakang jam
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(listOf(GoldGlow.copy(alpha = glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SHOLAT BERIKUTNYA",
                fontSize = 10.sp,
                fontFamily = NunitoFont,
                color = TextMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = arabicName,
                fontSize = 26.sp,
                fontFamily = CinzelFont,
                color = GoldLight,
                textAlign = TextAlign.Center
            )
            Text(
                text = nextPrayerName.uppercase(),
                fontSize = 18.sp,
                fontFamily = CinzelFont,
                color     = GoldPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Jam besar dengan font Cinzel
            Text(
                text      = nextTime,
                fontSize = 58.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color     = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            // Progress bar waktu antara sholat sebelumnya dan berikutnya
            LinearProgressIndicator(
                progress     = { progressVal },
                modifier     = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color        = GoldPrimary,
                trackColor   = DividerLine,
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Menuju $nextPrayerName",
                    fontSize = 10.sp,
                    fontFamily = NunitoFont,
                    color = TextMuted
                )
                Text(
                    text = "$progressPct% berlalu",
                    fontSize = 10.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Countdown dengan animasi digit flip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                AnimatedContent(
                    targetState   = countdown,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { it } + fadeOut())
                    },
                    label = "countdown"
                ) { text ->
                    Text(
                        text       = text,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GoldPrimary,
                        fontFamily = CinzelFont
                    )
                }
            }
        }
    }
}

@Composable
fun TodayPrayerTimesRow(
    todaySchedule  : id.ideahousetech.prayertime_qibla.model.PrayerTime?,
    nextPrayerName : String
) {
    val prayers = listOf(
        "Sub" to (todaySchedule?.fajr    ?: "--:--"),
        "Dzu" to (todaySchedule?.dhuhr   ?: "--:--"),
        "Asr" to (todaySchedule?.asr     ?: "--:--"),
        "Mgr" to (todaySchedule?.maghrib ?: "--:--"),
        "Isy" to (todaySchedule?.isha    ?: "--:--")
    )
    val fullNames = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "HARI INI",
                fontSize = 10.sp,
                fontFamily = NunitoFont,
                color = TextMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(10.dp))
            Divider(modifier = Modifier.weight(1f), color = DividerLine)
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            prayers.forEachIndexed { i, (short, time) ->
                val isNext = fullNames[i].equals(nextPrayerName, ignoreCase = true)
                Column(
                    modifier = Modifier
                        .background(
                            if (isNext) GoldGlow else CardSurface,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            if (isNext) 1.dp else 0.5.dp,
                            if (isNext) GoldPrimary else DividerLine,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text          = short.uppercase(),
                        fontSize      = 9.sp,
                        fontFamily    = NunitoFont,
                        fontWeight    = FontWeight.Bold,
                        color         = if (isNext) GoldPrimary else TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = time,
                        fontSize   = 13.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color      = if (isNext) GoldLight else TextPrimary
                    )
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
                color = GoldPrimary.copy(alpha = 0.20f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
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
                tint = GoldPrimary, // Gold Icon
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Amalan Sholat Tepat Waktu",
                    fontSize = 14.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary // Elegant white header
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"Sesungguhnya sholat itu bagi orang-orang yang beriman adalah kewajiban yang ditentukan waktunya.\"\n— QS. An-Nisa': 103",
                    fontSize = 12.sp,
                    fontFamily = NunitoFont,
                    lineHeight = 16.sp,
                    color = TextSecondary // Soft light teal text
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
fun SettingsDialog(
    locationViewModel: id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel,
    onDismiss: () -> Unit,
    onReminderToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs.get(context) }

    var isAlarmEnabled by remember { mutableStateOf(prefs.getBoolean("enable_adzan_alarm", true)) }
    var isDailyReminderEnabled by remember { mutableStateOf(prefs.getBoolean("enable_daily_reminder", true)) }
    var prayerOffset by remember { mutableStateOf(prefs.getInt("prayer_time_offset", 0)) }

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

            val targetFileName = if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
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
        val targetFileName = if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00332C))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pengaturan Aplikasi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 1: Aktivasi Alarm Adzan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Suara Alarm Adzan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Putar adzan saat waktu sholat tiba",
                            fontSize = 11.sp,
                            color = Color(0xFFB2DFDB)
                        )
                    }
                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = { checked ->
                            prefs.edit().putBoolean("enable_adzan_alarm", checked).apply()
                            isAlarmEnabled = checked
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00332C),
                            checkedTrackColor = Color(0xFFD4AF37),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 2: Aktivasi Kutipan Harian
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kutipan Amalan Harian",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tampilkan hikmah sholat di layar utama",
                            fontSize = 11.sp,
                            color = Color(0xFFB2DFDB)
                        )
                    }
                    Switch(
                        checked = isDailyReminderEnabled,
                        onCheckedChange = { checked ->
                            prefs.edit().putBoolean("enable_daily_reminder", checked).apply()
                            isDailyReminderEnabled = checked
                            onReminderToggle(checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00332C),
                            checkedTrackColor = Color(0xFFD4AF37),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Koreksi Waktu Sholat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Koreksi Waktu Sholat (Menit)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Sesuaikan jadwal sholat agar cocok dengan masjid setempat",
                        fontSize = 11.sp,
                        color = Color(0xFFB2DFDB),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (prayerOffset > -15) {
                                    prayerOffset -= 1
                                    prefs.edit().putInt("prayer_time_offset", prayerOffset).apply()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.12f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("-1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        Text(
                            text = if (prayerOffset == 0) "Sesuai Standar" else if (prayerOffset > 0) "+$prayerOffset Menit (Maju)" else "$prayerOffset Menit (Mundur)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (prayerOffset < 15) {
                                    prayerOffset += 1
                                    prefs.edit().putInt("prayer_time_offset", prayerOffset).apply()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.12f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Lokasi Manual vs GPS Otomatis
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val isManualLoc by locationViewModel.isManualLocation.collectAsState()
                    val currentAddress by locationViewModel.locationName.collectAsState()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lokasi & Koordinat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isManualLoc) "Kota Manual: $currentAddress" else "GPS Otomatis Aktif",
                                fontSize = 11.sp,
                                color = Color(0xFFB2DFDB)
                            )
                        }
                        
                        Button(
                            onClick = {
                                if (isManualLoc) {
                                    locationViewModel.setAutoLocation()
                                    Toast.makeText(context, "Harap sinkron GPS otomatis", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Set default manual ke Jakarta
                                    locationViewModel.setManualLocation("Jakarta", -6.2088, 106.8456)
                                    Toast.makeText(context, "Beralih ke Kota Manual Jakarta", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isManualLoc) Color(0xFF7E1C1C) else Color(0xFF004D40)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (isManualLoc) "Gunakan GPS" else "Set Manual",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (isManualLoc) {
                        val indonesianCities = mapOf(
                            "Jakarta" to Pair(-6.2088, 106.8456),
                            "Surabaya" to Pair(-7.2575, 112.7521),
                            "Bandung" to Pair(-6.9175, 107.6191),
                            "Medan" to Pair(3.5952, 98.6722),
                            "Bekasi" to Pair(-6.2349, 106.9896),
                            "Depok" to Pair(-6.4025, 106.7942),
                            "Tangerang" to Pair(-6.1702, 106.6400),
                            "Semarang" to Pair(-6.9932, 110.4203),
                            "Palembang" to Pair(-2.9761, 104.7754),
                            "Makassar" to Pair(-5.1477, 119.4327),
                            "Yogyakarta" to Pair(-7.7971, 110.3688),
                            "Bogor" to Pair(-6.5971, 106.8060),
                            "Batam" to Pair(1.0457, 104.0305),
                            "Pekanbaru" to Pair(0.5071, 101.4478),
                            "Banjarmasin" to Pair(-3.3194, 114.5908),
                            "Pontianak" to Pair(-0.0263, 109.3425),
                            "Samarinda" to Pair(-0.5021, 117.1536),
                            "Manado" to Pair(1.4748, 124.8421),
                            "Denpasar" to Pair(-8.6705, 115.2126),
                            "Aceh" to Pair(5.5483, 95.3238)
                        )

                        var searchQuery by remember { mutableStateOf("") }
                        val filteredCities = remember(searchQuery) {
                            indonesianCities.keys.filter { it.contains(searchQuery, ignoreCase = true) }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari kota Indonesia...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD4AF37),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 130.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredCities) { city ->
                                    val coords = indonesianCities[city]!!
                                    val isSelected = currentAddress.contains(city, ignoreCase = true)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                locationViewModel.setManualLocation(city, coords.first, coords.second)
                                                Toast.makeText(context, "$city dipilih.", Toast.LENGTH_SHORT).show()
                                            }
                                            .background(
                                                if (isSelected) Color(0xFFD4AF37).copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = city,
                                            color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "${coords.first}, ${coords.second}",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Label Section Suara
                Text(
                    text = "PILIHAN SUARA ADZAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

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

                Spacer(modifier = Modifier.height(4.dp))

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
                        IconButton(
                            onClick = {
                                if (activePreview == "fajr") {
                                    stopPreview()
                                } else {
                                    playPreview("adzan_fajr.mp3", "fajr")
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

                        IconButton(
                            onClick = { launcherFajr.launch("audio/*") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Unggah",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

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

                Spacer(modifier = Modifier.height(20.dp))

                // Section Info Aplikasi
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "INFO APLIKASI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Waktu Sholat & Kiblat v1.0.2\nMari Tegakkan Sholat Tepat Waktu.\n© ferry_pey",
                            fontSize = 11.sp,
                            color = Color(0xFFB2DFDB),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color(0xFF00332C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Simpan & Kembali",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GridMenuSection(
    onNavigateToScreen: (id.ideahousetech.prayertime_qibla.AppScreen) -> Unit
) {
    val context = LocalContext.current
    
    val items = listOf(
        Triple("Jadwal", Icons.Default.Schedule, id.ideahousetech.prayertime_qibla.AppScreen.JADWAL_HARIAN),
        Triple("Kiblat", Icons.Default.CompassCalibration, id.ideahousetech.prayertime_qibla.AppScreen.KIBLAT),
        Triple("Kalender", Icons.Default.CalendarMonth, id.ideahousetech.prayertime_qibla.AppScreen.KALENDER),
        Triple("Bulanan", Icons.Default.TableChart, id.ideahousetech.prayertime_qibla.AppScreen.JADWAL),
        Triple("Doa-Doa", Icons.Default.MenuBook, id.ideahousetech.prayertime_qibla.AppScreen.DOA),
        Triple("Al-Qur'an", Icons.Default.MenuBook, id.ideahousetech.prayertime_qibla.AppScreen.QURAN),
        Triple("Tasbih", Icons.Default.Cached, id.ideahousetech.prayertime_qibla.AppScreen.TASBIH),
        Triple("Masjid", Icons.Default.Place, null),
        Triple("Sholawat", Icons.Default.Audiotrack, null)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = GoldPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MENU UTAMA",
                fontSize = 11.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary, // Gold text
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 3x3 Grid
            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        if (index < items.size) {
                            val item = items[index]
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(CardElevated)
                                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                                        .clickable {
                                            if (item.third != null) {
                                                onNavigateToScreen(item.third!!)
                                            } else {
                                                val toastMsg = when (item.first) {
                                                    "Al-Qur'an" -> "Fitur Al-Qur'an Digital segera hadir!"
                                                    "Dzikir" -> "Koleksi Dzikir Pagi & Petang segera hadir!"
                                                    "Masjid" -> "Peta Pencarian Masjid Terdekat segera hadir!"
                                                    else -> "Kumpulan Sholawat pilihan segera hadir!"
                                                }
                                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.second,
                                        contentDescription = item.first,
                                        tint = GoldPrimary, // Luxurious Gold
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = item.first,
                                    fontSize = 11.sp,
                                    fontFamily = NunitoFont,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
