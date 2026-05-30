package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.service.QiblaService
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton

/**
 * Screen Kompas Kiblat.
 * Menghitung sudut Ka'bah bersandar kepada data GPS saat ini.
 * Membaca sensor kompas (magnetometer) realtime luring untuk memutar jarum jam kompas.
 * Menghitung jarak loxodrome kilometer ke kota suci Makkah secara presisi (Haversine formula).
 */
@Composable
fun QiblaScreen(
    locationViewModel: LocationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val qiblaService = remember { QiblaService(context) }
    val azimuth by qiblaService.azimuthFlow.collectAsState()
    val userLocation by locationViewModel.userLocation.collectAsState()

    // Membuka sensor kompas saat screen aktif dan menutupnya otomatis saat screen ditutup (DisposableEffect)
    DisposableEffect(Unit) {
        qiblaService.startListening()
        onDispose {
            qiblaService.stopListening()
        }
    }

    // Koordinat penentu (Gunakan lokasi aktif dari GPS, atau fallback Jakarta)
    val lat = userLocation?.latitude ?: -6.175115
    val lon = userLocation?.longitude ?: 106.827157

    val qiblaBearing = qiblaService.calculateQiblaDirection(lat, lon)
    val distanceToKabah = calculateDistanceToKabah(lat, lon)

    // Arah rotasi murni jarum kompas terhadap utara magnetik
    // Jarum menunjuk: Arah Kiblat relative terhadap hadap perangkat = (Qibla_Bearing - Device_Azimuth)
    val relativeHeading = (qiblaBearing - azimuth).toFloat()

    // Animasi agar jarum berputar dengan luwes dan tidak bergetar gila-gilaan
    val animatedHeading by animateFloatAsState(
        targetValue = relativeHeading,
        animationSpec = tween(durationMillis = 150),
        label = "JarumKompas"
    )

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
            text = "KOMPAS KIBLAT",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB2DFDB), // Soft teal theme text
            letterSpacing = 2.sp
        )
        Text(
            text = "Arah Kiblat (Ka'bah)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary, // Gold
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Visualisasi Kompas Berbentuk Bulat Premium Jetpack Compose (Glassmorphism design)
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Gambar Dial Dial Kompas Statis (Utara, Selatan, Timur, Barat)
            CompassDialFace(azimuthAngle = azimuth)

            // Gambar Jarum Penunjuk Kiblat (Berputar Dinamis)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(animatedHeading),
                contentAlignment = Alignment.Center
            ) {
                CompassNeedlePointer()
            }

            // Lambang Center Bulat Emas Tengah
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape) // Gold Accent
                    .border(1.5.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = "🕋",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Kartu Detail Jarak & Derajat Navigasi (Glass Panel)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Bearing",
                            tint = MaterialTheme.colorScheme.primary, // Gold Icon
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Derajat Kiblat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB2DFDB) // Light teal text
                        )
                    }
                    Text(
                        text = "%.2f° N".format(qiblaBearing),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White // Elegant white value
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Jarak",
                            tint = MaterialTheme.colorScheme.primary, // Gold Icon
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Jarak Ke Ka'bah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB2DFDB) // Light teal text
                        )
                    }
                    Text(
                        text = "%,.0f KM".format(distanceToKabah),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary // Gold Accent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Panduan Sensor",
                        tint = MaterialTheme.colorScheme.primary, // Gold Icon
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pegang perangkat secara mendatar rata air dan jauhi benda logam magnetis untuk mendapatkan akurasi optimal.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFFB2DFDB) // Light teal info text
                    )
                }
            }
        }
    }
}

/**
 * Menggambar piringan derajar kompas di belakang jarum penunjuk
 */
@Composable
fun CompassDialFace(azimuthAngle: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = size.width / 2

        // Menggambar garis-garis skala mutlak sudut kompas (statis)
        for (i in 0 until 360 step 15) {
            val radians = Math.toRadians((i - 90).toDouble())
            val angleCos = cos(radians).toFloat()
            val angleSin = sin(radians).toFloat()
            
            val len = if (i % 90 == 0) 12.dp.toPx() else 6.dp.toPx()
            val thickness = if (i % 90 == 0) 2.dp.toPx() else 1.dp.toPx()
            val color = if (i % 90 == 0) primaryColor else secondaryColor.copy(alpha = 0.5f)

            drawLine(
                color = color,
                start = Offset(cx + (radius - len) * angleCos, cy + (radius - len) * angleSin),
                end = Offset(cx + radius * angleCos, cy + radius * angleSin),
                strokeWidth = thickness
            )
        }
    }

    // Teks Petunjuk Arah Dunia Kompas Statis
    Box(modifier = Modifier.fillMaxSize()) {
        Text("U", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.align(Alignment.TopCenter))
        Text("S", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.BottomCenter))
        Text("T", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterEnd))
        Text("B", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterStart))
    }
}

/**
 * Menggambar jarum kiblat segitiga cantik berwatak ornamen kubah masjid
 */
@Composable
fun CompassNeedlePointer() {
    val context = LocalContext.current
    val colorGold = MaterialTheme.colorScheme.tertiary
    val colorPrimary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        val len = size.width / 2 - 16.dp.toPx()

        // Path Jarum Segitiga Lancip Menunjuk Atas (Kiblat - Utara)
        val pointerPath = Path().apply {
            moveTo(cx, cy - len)
            lineTo(cx - 15.dp.toPx(), cy)
            lineTo(cx + 15.dp.toPx(), cy)
            close()
        }
        drawPath(
            path = pointerPath,
            color = colorGold
        )

        // Path Ekor Jarum Lancip Menunjuk Bawah (Selatan)
        val tailPath = Path().apply {
            moveTo(cx, cy + len)
            lineTo(cx - 10.dp.toPx(), cy)
            lineTo(cx + 10.dp.toPx(), cy)
            close()
        }
        drawPath(
            path = tailPath,
            color = colorPrimary.copy(alpha = 0.6f)
        )
    }
}

/**
 * Menghitung jarak loxodrome berdasarkan koordinat GPS memakai Formula Haversine (Akurasi Tinggi Keliling Bumi).
 */
private fun calculateDistanceToKabah(userLat: Double, userLon: Double): Double {
    val r = 6371.0 // Radius Bumi dalam Kilometer
    val lat1Rad = Math.toRadians(userLat)
    val lon1Rad = Math.toRadians(userLon)
    val lat2Rad = Math.toRadians(QiblaService.KABAH_LATITUDE)
    val lon2Rad = Math.toRadians(QiblaService.KABAH_LONGITUDE)

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}
