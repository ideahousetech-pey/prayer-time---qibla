package id.ideahousetech.prayertime_qibla.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.ideahousetech.prayertime_qibla.model.IslamicHoliday
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun HolidayDialog(
    holiday: IslamicHoliday,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
            tonalElevation = 12.dp,
            border = BorderStroke(2.dp, GoldPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
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
                            tint = TextSecondary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(GoldGlow, CircleShape)
                            .border(1.dp, GoldPrimary, CircleShape),
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
                        color = TealAccent,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = holiday.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. Amalan Utama
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, DividerLine, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ AMALAN UTAMA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = holiday.description,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // 2. Sejarah Peristiwa
                    if (holiday.history.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, DividerLine, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📖 SEJARAH PERISTIWA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealAccent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holiday.history,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // 3. Keutamaan & Dalil
                    if (holiday.quranHadith.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = GoldGlow.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📜 KEUTAMAAN & DALIL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holiday.quranHadith,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DeepNight
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

data class OnlineAdzan(val displayName: String, val url: String, val desc: String)

@Composable
fun SettingsDialog(
    locationViewModel: LocationViewModel,
    onDismiss: () -> Unit,
    onReminderToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs.get(context) }
    val scope = rememberCoroutineScope()

    val onlineAdzans = remember {
        listOf(
            OnlineAdzan("Adzan Makkah", "https://www.islamcan.com/audio/adhans/adhan1.mp3", "Adzan syahdu nan agung dari Masjidil Haram, Makkah."),
            OnlineAdzan("Adzan Madinah", "https://www.islamcan.com/audio/adhans/adhan10.mp3", "Adzan merdu menenangkan dari Masjid Nabawi, Madinah."),
            OnlineAdzan("Adzan Mesir", "https://www.islamcan.com/audio/adhans/adhan13.mp3", "Adzan bernada indah khas gaya legendaris Mesir."),
            OnlineAdzan("Adzan Al-Aqsa", "https://www.islamcan.com/audio/adhans/adhan14.mp3", "Adzan khidmat menyentuh kalbu dari Masjidil Aqsa."),
            OnlineAdzan("Adzan Makkah Subuh", "https://www.islamcan.com/audio/adhans/adhan2.mp3", "Adzan khusus Subuh (dengan tambahan Ash-Shalaatu Khairum Minan-Naum).")
        )
    }

    var isAlarmEnabled by remember { mutableStateOf(prefs.getBoolean("enable_adzan_alarm", true)) }
    var isDailyReminderEnabled by remember { mutableStateOf(prefs.getBoolean("enable_daily_reminder", true)) }
    var prayerOffset by remember { mutableStateOf(prefs.getInt("prayer_time_offset", 0)) }

    var customAdzanName by remember { mutableStateOf(prefs.getString("custom_adzan_name", null)) }
    var customAdzanFajrName by remember { mutableStateOf(prefs.getString("custom_adzan_fajr_name", null)) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadingName by remember { mutableStateOf("") }

    fun downloadAdzanOnline(adzan: OnlineAdzan, isSubuh: Boolean) {
        if (isDownloading) return
        isDownloading = true
        downloadProgress = 0f
        downloadingName = adzan.displayName + (if (isSubuh) " (Subuh)" else " (Umum)")
        scope.launch(Dispatchers.IO) {
            val targetFileName = if (isSubuh) "adzan_fajr.mp3" else "adzan.mp3"
            val targetFile = File(context.filesDir, targetFileName)
            
            var success = false
            var errorMsg = ""
            
            // Build resilient backup array
            val urlsToTry = mutableListOf<String>()
            
            // 1. Primary URL (HTTPS)
            urlsToTry.add(adzan.url)
            
            // 2. Cleartext HTTP attempt if primary is HTTPS (bypasses SSL cert issues)
            if (adzan.url.startsWith("https://")) {
                urlsToTry.add(adzan.url.replace("https://", "http://"))
            }
            
            // 3. Fallback Archive.org links (known stable backups)
            val fallbackUrl = when (adzan.displayName) {
                "Adzan Makkah" -> "https://archive.org/download/adhan_202206/adhan.mp3"
                "Adzan Madinah" -> "https://archive.org/download/AzanMadinah_201712/azan_madinah.mp3"
                "Adzan Makkah Subuh" -> "https://archive.org/download/AzanMadinah_201712/azan_madinah.mp3"
                else -> if (isSubuh) "https://archive.org/download/AzanMadinah_201712/azan_madinah.mp3" else "https://archive.org/download/adhan_202206/adhan.mp3"
            }
            urlsToTry.add(fallbackUrl)
            urlsToTry.add(fallbackUrl.replace("https://", "http://"))
            
            // 4. Hard fallback to stable raw GitHub Pages / UsercontentCDN if other sites are geo-blocked
            // Standard raw file of Sidandv (Highly accessible)
            urlsToTry.add("https://raw.githubusercontent.com/sidandv/My-Azan/master/Azan.mp3")
            
            // Set up a Trust-All SSL Context specifically to avoid handshake failures on old or custom OS
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : javax.net.ssl.X509TrustManager {
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<java.security.cert.X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(certs: Array<java.security.cert.X509Certificate>?, authType: String?) {}
            })
            val sc = javax.net.ssl.SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            
            for ((idx, attemptUrl) in urlsToTry.withIndex()) {
                if (idx > 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Mencoba server cadangan (${idx + 1}/${urlsToTry.size})...", Toast.LENGTH_SHORT).show()
                    }
                }
                try {
                    var currentUrlStr = attemptUrl
                    var connection: java.net.HttpURLConnection? = null
                    var redirectCount = 0
                    val maxRedirects = 6
                    
                    while (redirectCount < maxRedirects) {
                        val url = java.net.URL(currentUrlStr)
                        connection = url.openConnection() as java.net.HttpURLConnection
                        
                        // Disable built-in redirect to manage HTTPS <-> HTTP transitions perfectly
                        connection.instanceFollowRedirects = false
                        
                        // Set modern browser User Agent to satisfy safety headers
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        
                        // Inject our permissive SSL Socket Factory directly on this connection
                        if (connection is javax.net.ssl.HttpsURLConnection) {
                            connection.sslSocketFactory = sc.socketFactory
                            connection.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
                        }
                        
                        val status = connection.responseCode
                        if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP || 
                            status == java.net.HttpURLConnection.HTTP_MOVED_PERM || 
                            status == 303 || status == 307 || status == 308) {
                            
                            val newUrl = connection.getHeaderField("Location")
                            if (newUrl != null) {
                                // Resolve relative URLs seamlessly against current URL base
                                val parentUrl = java.net.URL(currentUrlStr)
                                currentUrlStr = java.net.URL(parentUrl, newUrl).toString()
                                redirectCount++
                                connection.disconnect()
                                continue
                            }
                        }
                        break
                    }
                    
                    if (connection == null) {
                        throw Exception("Gagal membuat koneksi.")
                    }
                    
                    val status = connection.responseCode
                    if (status !in 200..299) {
                        throw Exception("HTTP $status")
                    }
                    
                    val fileLength = connection.contentLength
                    val input = java.io.BufferedInputStream(connection.inputStream, 8192)
                    val tmpFile = File(context.filesDir, "${targetFileName}.tmp")
                    val output = FileOutputStream(tmpFile)
                    val data = ByteArray(8192)
                    var total = 0L
                    var count: Int
                    
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            downloadProgress = total.toFloat() / fileLength.toFloat()
                        } else {
                            // If length is unknown, mock slow progression limit under 95%
                            downloadProgress = (downloadProgress + 0.05f).coerceAtMost(0.95f)
                        }
                        output.write(data, 0, count)
                    }
                    
                    output.flush()
                    output.close()
                    input.close()
                    connection.disconnect()
                    
                    // Validate file completeness (Adzan is usually at least 100KB)
                    if (tmpFile.exists() && tmpFile.length() > 50000) {
                        if (targetFile.exists()) {
                            targetFile.delete()
                        }
                        if (tmpFile.renameTo(targetFile)) {
                            // success!
                        } else {
                            tmpFile.copyTo(targetFile, overwrite = true)
                            tmpFile.delete()
                        }
                        success = true
                        break
                    } else {
                        tmpFile.delete()
                        throw Exception("File kosong atau terpotong.")
                    }
                } catch (e: java.lang.Exception) {
                    errorMsg = e.localizedMessage ?: e.message ?: "Koneksi terputus."
                }
            }
            
            if (success) {
                withContext(Dispatchers.Main) {
                    val savedName = "${adzan.displayName} (Internet)"
                    if (isSubuh) {
                        prefs.edit().putString("custom_adzan_fajr_name", savedName).apply()
                        customAdzanFajrName = savedName
                    } else {
                        prefs.edit().putString("custom_adzan_name", savedName).apply()
                        customAdzanName = savedName
                    }
                    isDownloading = false
                    Toast.makeText(context, "Selesai mengunduh & menerapkan: $savedName", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    Toast.makeText(context, "Gagal mengunduh adzan: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
            if (file.exists() && file.length() > 100) {
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
                .border(2.dp, GoldPrimary, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
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
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pengaturan Aplikasi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Tema: Mode Tampilan
                var appThemeMode by remember { mutableStateOf(prefs.getString("app_theme_mode", "dark") ?: "dark") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Tema & Mode Tampilan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pilih tema gelap, terang, atau ikuti pengaturan sistem Anda",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    // Segmented Button Custom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSurface, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val modes = listOf(
                            "dark" to "Gelap 🌙",
                            "light" to "Terang ☀️",
                            "system" to "Sistem 🔄"
                        )
                        modes.forEach { (modeKey, modeName) ->
                            val isSelected = appThemeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(
                                        color = if (isSelected) GoldPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        prefs.edit().putString("app_theme_mode", modeKey).apply()
                                        appThemeMode = modeKey
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DeepNight else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Aktivasi Alarm Adzan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Suara Alarm Adzan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Putar adzan saat waktu sholat tiba",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = { checked ->
                            prefs.edit().putBoolean("enable_adzan_alarm", checked).apply()
                            isAlarmEnabled = checked
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepNight,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DividerLine
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 2: Aktivasi Kutipan Harian
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kutipan Amalan Harian",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tampilkan hikmah sholat di layar utama",
                            fontSize = 11.sp,
                            color = TextSecondary
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
                            checkedThumbColor = DeepNight,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DividerLine
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Koreksi Waktu Sholat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Koreksi Waktu Sholat (Menit)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sesuaikan jadwal sholat agar cocok dengan masjid setempat",
                        fontSize = 11.sp,
                        color = TextSecondary,
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
                                containerColor = DividerLine,
                                contentColor = TextPrimary
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
                            color = GoldPrimary,
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
                                containerColor = DividerLine,
                                contentColor = TextPrimary
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

                // Section 4: Lokasi Manual vs GPS Otomatis (ditambah tombol Refresh GPS)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
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
                                color = TextPrimary
                            )
                            Text(
                                text = if (isManualLoc) "Kota Manual: $currentAddress" else "GPS Otomatis Aktif",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!isManualLoc) {
                                Button(
                                    onClick = {
                                        locationViewModel.refreshLocation()
                                        Toast.makeText(context, "Sinkronisasi koordinat GPS...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealAccent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Refresh GPS",
                                        tint = DeepNight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sinkron",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNight
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isManualLoc) {
                                        locationViewModel.setAutoLocation()
                                        Toast.makeText(context, "Harap sinkron GPS otomatis", Toast.LENGTH_SHORT).show()
                                    } else {
                                        locationViewModel.setManualLocation("Jakarta", -6.2088, 106.8456)
                                        Toast.makeText(context, "Beralih ke Kota Manual Jakarta", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isManualLoc) ResetRed else DarkTeal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (isManualLoc) "Gunakan GPS" else "Set Manual",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
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
                            placeholder = { Text("Cari kota Indonesia...", color = TextSecondary, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DividerLine
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .background(DeepNight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 130.dp),
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
                                                if (isSelected) GoldGlow else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = city,
                                            color = if (isSelected) GoldPrimary else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "${coords.first}, ${coords.second}",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PILIHAN SUARA ADZAN
                Text(
                    text = "PILIHAN SUARA ADZAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                // Row 1: Adzan Umum
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .border(1.dp, DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adzan Umum (Biasa)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = customAdzanName ?: "Nada Bawaan Aplikasi",
                            fontSize = 11.sp,
                            color = if (customAdzanName != null) GoldPrimary else TextSecondary,
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
                                .background(DividerLine, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (activePreview == "umum") Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Mainkan",
                                tint = if (activePreview == "umum") ErrorRed else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { launcherUmum.launch("audio/*") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldGlow, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Unggah",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (customAdzanName != null) {
                            IconButton(
                                onClick = { deleteCustomAudio(isFajr = false) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ResetRed.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = ErrorRed,
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
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .border(1.dp, DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adzan Khusus Subuh",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = customAdzanFajrName ?: "Nada Bawaan Aplikasi",
                            fontSize = 11.sp,
                            color = if (customAdzanFajrName != null) GoldPrimary else TextSecondary,
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
                                .background(DividerLine, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (activePreview == "fajr") Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Mainkan",
                                tint = if (activePreview == "fajr") ErrorRed else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { launcherFajr.launch("audio/*") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldGlow, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Unggah",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (customAdzanFajrName != null) {
                            IconButton(
                                onClick = { deleteCustomAudio(isFajr = true) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ResetRed.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // ------------------ UNDUH ADZAN DARI INTERNET SINKRONISASI ------------------
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "UNDUH ADZAN DARI INTERNET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DividerLine, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (isDownloading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    progress = downloadProgress,
                                    color = GoldPrimary,
                                    trackColor = DividerLine,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Mengunduh $downloadingName...",
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}% selesai",
                                    fontSize = 11.sp,
                                    color = TealAccent,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Koleksi Adzan Terpopuler Dunia (Unduh langsung):",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                            )

                            onlineAdzans.forEach { adzan ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(DividerLine.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = adzan.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                    Text(
                                        text = adzan.desc,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { downloadAdzanOnline(adzan, isSubuh = false) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GoldPrimary,
                                                contentColor = DeepNight
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pasang Umum", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = { downloadAdzanOnline(adzan, isSubuh = true) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = TealAccent,
                                                contentColor = DeepNight
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pasang Subuh", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2026 ISLAMIC LUXURY DESIGN SHOWCASE TRIGGER
                Spacer(modifier = Modifier.height(16.dp))

                var showDesignSystemShowcase by remember { mutableStateOf(false) }

                if (showDesignSystemShowcase) {
                    Dialog(onDismissRequest = { showDesignSystemShowcase = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 620.dp)
                                .border(1.dp, GoldPrimary, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepNight)
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                id.ideahousetech.prayertime_qibla.ui.components.IslamicLuxuryShowcase(
                                    onDismiss = { showDesignSystemShowcase = false }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { showDesignSystemShowcase = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealAccent.copy(alpha = 0.12f),
                        contentColor = TealAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Design System",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lihat Design System Islamic Luxury 2026",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // INFO APLIKASI
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DividerLine, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "INFO APLIKASI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Waktu Sholat & Kiblat v1.0.2\nMari Tegakkan Sholat Tepat Waktu.\n© ferry_pey",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DeepNight
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
