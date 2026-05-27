package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import com.example.ui.CalendarScreen
import com.example.ui.DoaScreen
import com.example.ui.HomeScreen
import com.example.ui.MonthlyScheduleScreen
import com.example.ui.QiblaScreen
import com.example.ui.DailyScheduleScreen
import com.example.ui.QuranScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LocationViewModel
import com.example.viewmodel.PrayerViewModel

/**
 * Aktivitas utama (MainActivity) yang bertindak sebagai gerbang masuk aplikasi.
 * Menghandle perizinan modular runtime (GPS + Notifikasi di Android 13+).
 * Memiliki Bottom Navigation Bar custom Material 3 dengan penayangan 5 halaman penting realtime.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            // Inisialisasi ViewModel secara mandiri
            val locationViewModel = remember { LocationViewModel(context.applicationContext) }
            val prayerViewModel = remember { PrayerViewModel(context.applicationContext) }

            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2500)
                showSplash = false
            }

            MyApplicationTheme {
                if (showSplash) {
                    SplashScreen()
                } else {
                    MainLayout(
                        locationViewModel = locationViewModel,
                        prayerViewModel = prayerViewModel
                    )
                }
            }
        }
    }
}

/**
 * Definisikan enum halaman/menu navigasi utama
 */
enum class AppScreen(val title: String, val icon: ImageVector) {
    SHOLAT("Sholat", Icons.Default.Timer),
    KIBLAT("Kiblat", Icons.Default.CompassCalibration),
    KALENDER("Kalender", Icons.Default.CalendarMonth),
    JADWAL("Jadwal", Icons.Default.Schedule),
    DOA("Doa-Doa", Icons.Default.MenuBook),
    JADWAL_HARIAN("Jadwal Harian", Icons.Default.Schedule),
    QURAN("Al-Qur'an", Icons.Default.MenuBook)
}

@Composable
fun MainLayout(
    locationViewModel: LocationViewModel,
    prayerViewModel: PrayerViewModel
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.SHOLAT) }
    val userLocation by locationViewModel.userLocation.collectAsState()

    // 1. Integrasi Izin Lokasi dan Notifikasi di Awal Startup (Android Runtime Permissions)
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Memicu pencarian lokasi langsung jika izin disetujui
            locationViewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        // Cek izin satu per satu secara aman luring
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Minta akses notifikasi untuk Alarm Adzan di Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocation) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Izin sudah ada hulu, panggil pemindai lokasi realtime langsung
            locationViewModel.refreshLocation()
        }
    }

    // 2. Pemutakhiran Jadwal Sholat Otomatis Berdasarkan Koordinat GPS Aktif
    LaunchedEffect(userLocation) {
        userLocation?.let {
            prayerViewModel.loadPrayerTimesForLocation(it.latitude, it.longitude)
        }
    }

    // Penanganan Edge-to-Edge dengan windowInsetsPadding untuk area Notch / Drawing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        Color(0xFF004D40), // Deep emerald
                        Color(0xFF002B24)  // Darker deep emerald
                    )
                )
            )
    ) {
        // Dynamic Repeating Diamond Islamic Pattern Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = 60.dp.toPx() // Repeating every 60dp for elegant density
            val cols = (size.width / sizePx).toInt() + 1
            val rows = (size.height / sizePx).toInt() + 1
            for (col in 0..cols) {
                for (row in 0..rows) {
                    val x = col * sizePx
                    val y = row * sizePx
                    
                    val path = Path().apply {
                        moveTo(x + sizePx / 2, y)
                        lineTo(x + sizePx, y + sizePx / 2)
                        lineTo(x + sizePx / 2, y + sizePx)
                        lineTo(x, y + sizePx / 2)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFFFD700).copy(alpha = 0.04f), // Delicate gold stroke
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.SHOLAT -> HomeScreen(
                        prayerViewModel = prayerViewModel,
                        locationViewModel = locationViewModel,
                        onNavigateToScreen = { screen ->
                            currentScreen = screen
                        }
                    )
                    AppScreen.KIBLAT -> QiblaScreen(
                        locationViewModel = locationViewModel,
                        onBackClick = { currentScreen = AppScreen.SHOLAT }
                    )
                    AppScreen.KALENDER -> CalendarScreen(
                        onBackClick = { currentScreen = AppScreen.SHOLAT }
                    )
                    AppScreen.JADWAL -> MonthlyScheduleScreen(
                        prayerViewModel = prayerViewModel,
                        locationViewModel = locationViewModel,
                        onBackClick = { currentScreen = AppScreen.SHOLAT }
                    )
                    AppScreen.DOA -> DoaScreen(
                        onBackClick = { currentScreen = AppScreen.SHOLAT }
                    )
                    AppScreen.JADWAL_HARIAN -> DailyScheduleScreen(
                        prayerViewModel = prayerViewModel,
                        locationViewModel = locationViewModel,
                        onBackClick = { currentScreen = AppScreen.SHOLAT },
                        onNavigateToMonthly = { currentScreen = AppScreen.JADWAL }
                    )
                    AppScreen.QURAN -> QuranScreen(
                        onBackClick = { currentScreen = AppScreen.SHOLAT }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        Color(0xFF004D40), // Deep emerald
                        Color(0xFF002B24)  // Darker deep emerald
                    )
                )
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Majestic Glowing Image Container
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(32.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_artwork),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "WAKTU SHOLAT & KIBLAT",
                color = Color(0xFFD4AF37), // Majestic Gold
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mari Tegakkan Sholat Tepat Waktu",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
