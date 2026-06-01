package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel

/**
 * Screen utama aplikasi Waktu Sholat & Kiblat.
 * Menampilkan tanggal ganda (Gregorian & Hijriah) di header, nama lokasi otomatis dari GPS,
 * kartu informasi waktu sholat berikutnya lengkap dng ticking countdown mundur,
 * dan carousel horizontal jadwal sholat harian 5 waktu.
 * (Telah di-refactor dng memecah component & dialog untuk meningkatkan maintainabilitas).
 */
@Composable
fun HomeScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayGregorian by prayerViewModel.todayGregorian.collectAsState()
    val todayHijri by prayerViewModel.todayHijri.collectAsState()
    val locationName by locationViewModel.locationName.collectAsState()
    val todaySchedule by prayerViewModel.todaySchedule.collectAsState()
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

                // 1. Header (Refresh GPS dipindahkan ke dalam dialog pengaturan)
                HomeHeader(
                    gregorianDate = todayGregorian,
                    hijriDate     = todayHijri,
                    locationName  = locationName,
                    isLoading     = isLoadingLoc,
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

                // 4. Grid menu (Dengan Icons.Outlined yang unik)
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
