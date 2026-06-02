package id.ideahousetech.prayertime_qibla.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.calculatePrayerProgress

/**
 * ==========================================
 * EXPERT COMPOSE LUXURY GLASSMORPHIC CARD
 * ==========================================
 * Employs deep velvet-malachite glass overlay with glowing gold hairline borders.
 */
@Composable
fun IslamicGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerMedium,
    elevation: Dp = IslamicLuxuryElevation.normal,
    useGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = if (useGlow) GoldPrimary.copy(alpha = 0.5f) else Color.Black,
                spotColor = if (useGlow) GoldPrimary else Color.Black
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CardSurface.copy(alpha = 0.9f),
                        MidnightLayer.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorder,
                        GlassBorder.copy(alpha = 0.15f),
                        GoldDim.copy(alpha = 0.35f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Specular ambient glare
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GlassGaze.copy(alpha = 0.12f), Color.Transparent),
                        radius = 350f
                    )
                )
        )
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            content()
        }
    }
}

/**
 * ==========================================
 * REDESIGNED HEADER: THE SOVEREIGN CRESCENT BAR
 * ==========================================
 * Features a modern spiritual greeting, twin dates, and high-precision GPS telemetry.
 */
@Composable
fun HomeHeader(
    gregorianDate : String,
    hijriDate     : String,
    locationName  : String,
    isLoading     : Boolean,
    onSettingsClick : () -> Unit
) {
    val context = LocalContext.current
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..10 -> "Subuh Penuh Berkah"
        in 11..14 -> "Zuhur yang Damai"
        in 15..17 -> "Asar yang Teduh"
        in 18..19 -> "Maghrib yang Istijabah"
        else -> "Isya yang Tenang"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // High-Precision Telemetry GPS Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(CornerSmall))
                    .background(CardSurface.copy(alpha = 0.5f))
                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.25f), RoundedCornerShape(CornerSmall))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (locationName.isNotEmpty()) TealAccent else WarningAmber)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = locationName.ifEmpty { "Menghubungkan GPS..." },
                    fontSize = 11.sp,
                    fontFamily = NunitoFont,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isLoading) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        color = TealAccent,
                        strokeWidth = 1.5.dp
                    )
                }
            }
            
            Spacer(Modifier.height(14.dp))
            
            // Spiritual Timely Greeting
            Text(
                text = greeting,
                fontSize = 12.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            
            Spacer(Modifier.height(4.dp))
            
            // Sacred Calligraphy Date
            Text(
                text = hijriDate,
                fontSize = 24.sp,
                fontFamily = CinzelFont,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Spacer(Modifier.height(3.dp))
            
            // Sub Gregorian Date
            Text(
                text = gregorianDate.uppercase(),
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Royal Settings Coin Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .offset(y = 4.dp)
                .size(46.dp)
                .background(CardElevated, CircleShape)
                .border(
                    width = 1.dp,
                    brush = Brush.radialGradient(listOf(GoldLight, GoldDim, Color.Transparent)),
                    shape = CircleShape
                )
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Pengaturan",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * ==========================================
 * ASTROLABE CELESTIAL CLOCK GAUGE
 * ==========================================
 * Visualizes solar movement, countdowns, and progress sweep with high mechanical fidelity.
 */
@Composable
fun AstrolabeProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "clock_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gentle_spin")
    
    // Slow rotational angle for outer celestial gear ring giving the feel of a working astrolabe astronomical tool
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "astrolabe_spin"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue  = 0.15f,
        targetValue   = 0.4f,
        animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutSine), RepeatMode.Reverse),
        label         = "glow"
    )

    Box(
        modifier = modifier.size(136.dp),
        contentAlignment = Alignment.Center
    ) {
        // Deep Spiritual Golden Glow Field
        Box(
            modifier = Modifier
                .size(116.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldGlow.copy(alpha = pulseGlow), Color.Transparent),
                        radius = 180f
                    ),
                    CircleShape
                )
        )

        // Custom Astrolabe Metallic Dial
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.0f - 8.dp.toPx()
            
            if (radius > 0f) {
                // Outer double-layered golden rims
                drawCircle(
                    color = GoldPrimary.copy(alpha = 0.3f),
                    radius = radius,
                    style = Stroke(width = 1.2.dp.toPx())
                )
                drawCircle(
                    color = GoldDim.copy(alpha = 0.15f),
                    radius = radius - 4.dp.toPx(),
                    style = Stroke(width = 0.6.dp.toPx())
                )
                
                // Clock mechanics/tick marks rotated slowly
                rotate(degrees = slowRotation, pivot = center) {
                    val ticksCount = 60
                    for (i in 0 until ticksCount) {
                        val angle = Math.toRadians((i * (360.0 / ticksCount)).toDouble())
                        val isMajor = i % 5 == 0
                        
                        val length = if (isMajor) 6.dp.toPx() else 3.dp.toPx()
                        val alpha = if (isMajor) 0.5f else 0.15f
                        
                        val startX = (center.x + (radius - length) * Math.cos(angle)).toFloat()
                        val startY = (center.y + (radius - length) * Math.sin(angle)).toFloat()
                        val endX = (center.x + radius * Math.cos(angle)).toFloat()
                        val endY = (center.y + radius * Math.sin(angle)).toFloat()
                        
                        drawLine(
                            color = GoldPrimary.copy(alpha = alpha),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.8.dp.toPx()
                        )
                    }
                    
                    // Internal Beautiful 8-pointed Islamic Girih Gilded Compass Star
                    val starSize = radius * 0.45f
                    for (step in 0 until 4) {
                        val deg = step * 45f
                        rotate(degrees = deg, pivot = center) {
                            drawRect(
                                color = GoldPrimary.copy(alpha = 0.08f),
                                topLeft = Offset(center.x - starSize / 2, center.y - starSize / 2),
                                size = androidx.compose.ui.geometry.Size(starSize, starSize),
                                style = Stroke(width = 0.8.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // Active sweep representing the path of the Sun in Malachite-Teal and Champagne Gold
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2.0f - 8.dp.toPx()
            val strokeWidthPx = 3.5.dp.toPx()
            
            if (radius > 0f) {
                // Background Track
                drawCircle(
                    color = MidnightLayer.copy(alpha = 0.5f),
                    radius = radius,
                    style = Stroke(width = strokeWidthPx)
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to TealAccent,
                        0.5f to GoldPrimary,
                        1.0f to GoldLight
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                
                // Draw a beautiful gliding celestial "Sun" marker at the tip of the sweep progress
                val sweepAngleRad = Math.toRadians((animatedProgress * 360f - 90f).toDouble())
                val endPointX = (size.width / 2 + radius * Math.cos(sweepAngleRad)).toFloat()
                val endPointY = (size.height / 2 + radius * Math.sin(sweepAngleRad)).toFloat()
                
                // Radiant core sun marker
                drawCircle(
                    color = GoldLight,
                    radius = 4.dp.toPx(),
                    center = Offset(endPointX, endPointY)
                )
                drawCircle(
                    color = GoldPrimary,
                    radius = 7.dp.toPx(),
                    center = Offset(endPointX, endPointY),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
        }

        // Central Mini Mosque Silhouette Ornament
        IconButton(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .size(42.dp)
                .background(DeepNight.copy(alpha = 0.8f), CircleShape)
                .border(0.8.dp, GoldPrimary.copy(alpha = 0.25f), CircleShape)
        ) {
            Text(
                text = "🕌",
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ==========================================
 * REDESIGNED HERO: CELESTIAL ASTROLABE DIAL CARD
 * ==========================================
 */
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

    val progressVal = calculatePrayerProgress(todaySchedule, nextPrayerName)
    val progressPct = (progressVal * 100).toInt()

    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        useGlow = true
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header System Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(TealAccent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SOLAR NAVIGATION CHRONOGRAPH",
                        fontSize = 10.sp,
                        fontFamily = NunitoFont,
                        color = GoldLight,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "SYS_ACTIVE 2026",
                    fontSize = 9.sp,
                    fontFamily = NunitoFont,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            // Split Core Pane
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Pane: Beautiful Astrolabe Core Dial
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AstrolabeProgressRing(progress = progressVal)
                }

                // Right Pane: Sovereign Info Module
                Column(
                    modifier = Modifier.weight(1.4f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Calligraphy Title Sparked Glow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = arabicName,
                            fontSize = 32.sp,
                            fontFamily = CinzelFont,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "$progressPct%",
                            fontSize = 11.sp,
                            fontFamily = CinzelFont,
                            color = GoldLight.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(CornerSmall))
                                .background(MidnightLayer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "MENUJU $nextPrayerName".uppercase(),
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = nextTime,
                        fontSize = 42.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    // Ticking Hourglass Timer Capsule Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CornerMedium))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(0.8.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(CornerMedium))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = TealAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        AnimatedContent(
                            targetState = countdown,
                            transitionSpec = {
                                (slideInVertically { -it } + fadeIn()) togetherWith
                                (slideOutVertically { it } + fadeOut())
                            },
                            label = "countdown_txt"
                        ) { text ->
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontFamily = CinzelFont,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Subdued detailed precision metadata strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerSmall))
                    .background(CardSurface.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASTRONOMICAL GOLD GAUGE COMPLIANCE",
                    fontSize = 8.sp,
                    fontFamily = NunitoFont,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "HI-PRECISION GPS SECURE",
                    fontSize = 8.sp,
                    fontFamily = NunitoFont,
                    color = TealAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * ==========================================
 * REDESIGNED DAILY SCHEDULE ROW (5 JADWAL HARIAN)
 * ==========================================
 * Displays daily schedule tiles with sovereign glow details highlighting the active prayer.
 */
@Composable
fun TodayPrayerTimesRow(
    todaySchedule  : PrayerTime?,
    nextPrayerName : String
) {
    val prayers = listOf(
        "Subuh" to (todaySchedule?.fajr    ?: "--:--"),
        "Dzuhur" to (todaySchedule?.dhuhr   ?: "--:--"),
        "Ashar" to (todaySchedule?.asr     ?: "--:--"),
        "Maghrib" to (todaySchedule?.maghrib ?: "--:--"),
        "Isya" to (todaySchedule?.isha    ?: "--:--")
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ALIRAN WAKTU SHOLAT HARI INI",
                fontSize = 10.sp,
                fontFamily = NunitoFont,
                color = TextMuted,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLine)
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prayers.forEach { (name, time) ->
                val isNext = name.equals(nextPrayerName, ignoreCase = true)
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (isNext) IslamicLuxuryElevation.normal else 0.dp,
                            shape = RoundedCornerShape(CornerMedium),
                            clip = false,
                            ambientColor = if (isNext) GoldPrimary else Color.Transparent,
                            spotColor = if (isNext) GoldPrimary else Color.Transparent
                        )
                        .clip(RoundedCornerShape(CornerMedium))
                        .background(
                            if (isNext) CardElevated else CardSurface.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isNext) 1.5.dp else 0.5.dp,
                            brush = if (isNext) {
                                Brush.linearGradient(colors = listOf(GoldPrimary, GoldLight))
                            } else {
                                Brush.linearGradient(colors = listOf(DividerLine, Color.Transparent))
                            },
                            shape = RoundedCornerShape(CornerMedium)
                        )
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text          = name.uppercase(),
                        fontSize      = 9.sp,
                        fontFamily    = NunitoFont,
                        fontWeight    = FontWeight.Bold,
                        color         = if (isNext) GoldPrimary else TextSecondary,
                        letterSpacing = 0.5.sp,
                        textAlign     = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text       = time,
                        fontSize   = 14.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color      = if (isNext) GoldLight else TextPrimary,
                        textAlign     = TextAlign.Center
                    )
                    
                    if (isNext) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(TealAccent)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ==========================================
 * DAILY INSIGHT: SACRED SCRIPTS OF WISDOM (AMALAN)
 * ==========================================
 * Redesigned as a luxurious parchment manuscript container.
 */
@Composable
fun ReminderNoteCard() {
    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Elegant illuminated drop-cap ornament
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepNight)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📖",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HIKMAH UTAMA HARI INI",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Brush.linearGradient(listOf(GoldDim.copy(alpha = 0.5f), Color.Transparent)))
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Amalan Sholat Tepat Waktu",
                    fontSize = 14.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "\"Sesungguhnya sholat itu bagi orang-orang yang beriman adalah kewajiban yang ditentukan waktunya.\"\n— QS. An-Nisa': 103",
                    fontSize = 12.sp,
                    fontFamily = NunitoFont,
                    lineHeight = 20.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * ==========================================
 * QUICK ACTIONS MENU: IMPERIAL GRID MATRIX
 * ==========================================
 * Multi-layer modular Glassmorphic tiles with elegant styling and responsive feedback.
 */
@Composable
fun GridMenuSection(
    onNavigateToScreen: (AppScreen) -> Unit
) {
    val context = LocalContext.current
    
    val items = listOf(
        Triple("Jadwal Sholat", Icons.Outlined.Schedule, AppScreen.JADWAL_HARIAN),
        Triple("Kiblat", Icons.Outlined.Explore, AppScreen.KIBLAT),
        Triple("Kalender Hijriah", Icons.Outlined.CalendarMonth, AppScreen.KALENDER),
        Triple("Bulanan", Icons.Outlined.TableChart, AppScreen.JADWAL),
        Triple("Kumpulan Doa", Icons.Outlined.MenuBook, AppScreen.DOA),
        Triple("Al-Qur'an", Icons.Outlined.AutoStories, AppScreen.QURAN),
        Triple("Tasbih Digital", Icons.Outlined.ChangeCircle, AppScreen.TASBIH),
        Triple("Peta Masjid", Icons.Outlined.Place, null),
        Triple("Sholawat Syifa", Icons.Outlined.Audiotrack, null)
    )

    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "MENU UTAMA SPIRITUAL",
                    fontSize = 11.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 2.5.sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Beautiful tactile 3x3 layout matrix
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        if (index < items.size) {
                            val item = items[index]
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(CornerMedium))
                                    .background(MidnightLayer.copy(alpha = 0.4f))
                                    .border(
                                        width = 0.8.dp,
                                        brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.15f), Color.Transparent)),
                                        shape = RoundedCornerShape(CornerMedium)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(),
                                        onClick = {
                                            if (item.third != null) {
                                                onNavigateToScreen(item.third!!)
                                            } else {
                                                val toastMsg = when (item.first) {
                                                    "Peta Masjid" -> "Peta Pencarian Masjid Terdekat segera hadir!"
                                                    else -> "Kumpulan Sholawat Pilihan segera hadir!"
                                                }
                                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Large Tactile Icon Ring Button
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(CardElevated)
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.radialGradient(listOf(GoldLight.copy(alpha = 0.4f), Color.Transparent)),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.second,
                                        contentDescription = item.first,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = item.first,
                                    fontSize = 11.sp,
                                    fontFamily = NunitoFont,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
