package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.ui.theme.*

/**
 * ExploreScreen mendirikan sentral eksplorasi fitur ibadah sekunder.
 * Memadukan grid modern 2026 dng visualisasi Masjid Terdekat dan Kajian Islami premium.
 */
@Composable
fun ExploreScreen(
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepNight, MidnightLayer)))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp) // Berikan ruang agar tidak tertutup floating bar
        ) {
            // 1. HEADER EXPLORE
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EKSPLORASI RUHANI",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldDim,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Perdalam ibadah dan sempurnakan adab harian Anda",
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. GRID MENU UTAMA
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Kompas Kiblat",
                            description = "Presisi dan bersensor kompas",
                            icon = Icons.Outlined.Explore,
                            onClick = { onNavigateToScreen(AppScreen.KIBLAT) }
                        )
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Dzikir & Tasbih",
                            description = "Hitung ketukan dzikir harian",
                            icon = Icons.Outlined.Cached,
                            onClick = { onNavigateToScreen(AppScreen.TASBIH) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Kalender Hijriah",
                            description = "Penanggalan & jadwal sholat",
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = { onNavigateToScreen(AppScreen.JADWAL_HARIAN) }
                        )
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Doa & Hadits",
                            description = "Riyadhus sholihin & doa harian",
                            icon = Icons.Outlined.MenuBook,
                            onClick = { onNavigateToScreen(AppScreen.DOA) }
                        )
                    }
                }
            }

            // 3. MASJID TERDEKAT (Simulated Locator)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MASJID TERDEKAT JANGKAUAN",
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLine)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MasjidNearbyItem(
                            name = "Masjid Agung Baitul Makmur",
                            distance = "450 m",
                            address = "Jl. Kerajaan Raya No. 12 - GPS verified"
                        )
                        MasjidNearbyItem(
                            name = "Masjid Kahyangan Al-Ikhlas",
                            distance = "1.2 km",
                            address = "Kawasan Hijau Perbukitan Asri"
                        )
                    }
                }
            }

            // 4. KAJIAN ISLAM (Simulated Feed)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JADWAL KAJIAN RUHANI",
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLine)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KajianItem(
                            topic = "Meraih Khusyu' Sempurna dalam Shalat",
                            speaker = "Dr. Ustadz Adi Hidayat, Lc., M.A.",
                            schedule = "Rabu, Ba'da Maghrib - Masjid Agung",
                            type = "Umum & Terbuka"
                        )
                        KajianItem(
                            topic = "Tafsir Ringkas Riyadhus Shalihin",
                            speaker = "Ustadz Dr. Syafiq Riza Basalamah, M.A.",
                            schedule = "Sabtu, 09:00 WIB - Ruang Utama Lantai 2",
                            type = "Kajian Kitab"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreGridItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IslamicGlassCard(
        modifier = modifier
            .height(115.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        cornerRadius = CornerMedium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(CardElevated, RoundedCornerShape(8.dp))
                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 9.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MasjidNearbyItem(
    name: String,
    distance: String,
    address: String
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CardElevated, CircleShape)
                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🕌", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = address,
                    fontSize = 10.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = distance,
                fontSize = 11.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun KajianItem(
    topic: String,
    speaker: String,
    schedule: String,
    type: String
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topic,
                    fontSize = 12.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = type,
                    fontSize = 9.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TealAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TealDim.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = speaker,
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = schedule,
                    fontSize = 10.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary
                )
            }
        }
    }
}
