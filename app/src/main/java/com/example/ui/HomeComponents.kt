package id.ideahousetech.prayertime_qibla.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.calculatePrayerProgress

@Composable
fun HomeHeader(
    gregorianDate : String,
    hijriDate     : String,
    locationName  : String,
    isLoading     : Boolean,
    onSettingsClick : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
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
                    CircularProgressIndicator(
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

        // Tombol Settings Tunggal di Header (Berdasarkan Redesign Perbaikan 4)
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
                contentDescription = "Pengaturan",
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun NextPrayerHeroCard(
    nextPrayerName : String,
    countdown      : String,
    todaySchedule  : PrayerTime?
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
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 24.dp),
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

            Text(
                text      = nextTime,
                fontSize = 58.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color     = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            LinearProgressIndicator(
                progress     = { progressVal },
                modifier     = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
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
    todaySchedule  : PrayerTime?,
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
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLine)
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
                tint = GoldPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Amalan Sholat Tepat Waktu",
                    fontSize = 14.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"Sesungguhnya sholat itu bagi orang-orang yang beriman adalah kewajiban yang ditentukan waktunya.\"\n— QS. An-Nisa': 103",
                    fontSize = 12.sp,
                    fontFamily = NunitoFont,
                    lineHeight = 16.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun GridMenuSection(
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    
    // Grid Item Redesign (Icon Outlined Unik, Mengganti Duplicate MenuBook / Default Icons)
    val items = listOf(
        Triple("Jadwal", Icons.Outlined.Schedule, AppScreen.JADWAL_HARIAN),
        Triple("Kiblat", Icons.Outlined.Explore, AppScreen.KIBLAT),
        Triple("Kalender", Icons.Outlined.CalendarMonth, AppScreen.KALENDER),
        Triple("Bulanan", Icons.Outlined.TableChart, AppScreen.JADWAL),
        Triple("Doa-Doa", Icons.Outlined.MenuBook, AppScreen.DOA),
        Triple("Al-Qur'an", Icons.Outlined.AutoStories, AppScreen.QURAN),
        Triple("Tasbih", Icons.Outlined.ChangeCircle, AppScreen.TASBIH),
        Triple("Masjid", Icons.Outlined.Place, null),
        Triple("Sholawat", Icons.Outlined.Audiotrack, null)
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
                color = GoldPrimary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 3x3 Grid
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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
                                        tint = GoldPrimary,
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
