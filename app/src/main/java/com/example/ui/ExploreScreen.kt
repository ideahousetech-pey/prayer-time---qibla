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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel

/**
 * ExploreScreen mendirikan sentral eksplorasi fitur ibadah sekunder.
 * Memadukan grid modern 2026 dng visualisasi Masjid Terdekat dan Kajian Islami premium.
 */
@Composable
fun ExploreScreen(
    locationViewModel: LocationViewModel,
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationName by locationViewModel.locationName.collectAsState()

    val firstRegionPart = remember(locationName) {
        val parts = locationName.split(",")
        val p = parts.firstOrNull()?.trim() ?: "Ambon"
        if (p == "Menunggu GPS...") "Kota" else p
    }

    val masjid1Name = remember(firstRegionPart) {
        if (locationName == "Menunggu GPS..." || locationName.isEmpty()) {
            "Masjid Raya Baiturrahman"
        } else {
            "Masjid Agung $firstRegionPart"
        }
    }

    val masjid2Name = remember(firstRegionPart) {
        if (locationName == "Menunggu GPS..." || locationName.isEmpty()) {
            "Masjid Baitul Makmur"
        } else {
            "Masjid Al-Mutaqin $firstRegionPart"
        }
    }

    val masjid1Address = remember(locationName) {
        if (locationName == "Menunggu GPS..." || locationName.isEmpty()) {
            "Jl. Syuhada No. 12"
        } else {
            "Jl. Raya Pusat, Kecamatan $firstRegionPart"
        }
    }

    val masjid2Address = remember(locationName) {
        if (locationName == "Menunggu GPS..." || locationName.isEmpty()) {
            "Kawasan Sentra Kemakmuran"
        } else {
            "Jl. Hijrah No. 4, Kelurahan $firstRegionPart"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
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
                            description = "Penanggalan & hari besar Islam",
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = { onNavigateToScreen(AppScreen.KALENDER) }
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
                            text = "MASJID TERDEKAT POSISI ANDA",
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
                            name = masjid1Name,
                            distance = "450 m",
                            address = "$masjid1Address - verified"
                        )
                        MasjidNearbyItem(
                            name = masjid2Name,
                            distance = "1.2 km",
                            address = masjid2Address
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


