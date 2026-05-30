package id.ideahousetech.prayertime_qibla.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.ideahousetech.prayertime_qibla.data.AppDatabase
import id.ideahousetech.prayertime_qibla.data.TasbihSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val toneGenerator = remember { 
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            null
        }
    }

    val database = remember { AppDatabase.getInstance(context) }
    val tasbihDao = remember { database.tasbihDao() }
    val historySessions by tasbihDao.getAllSessions().collectAsState(initial = emptyList())

    val presetDzikir = listOf("Subhanallah", "Alhamdulillah", "Allahu Akbar", "Astaghfirullah")
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var currentDzikirName by remember { mutableStateOf(presetDzikir[0]) }
    var isCustomDzikirActive by remember { mutableStateOf(false) }
    var customDzikirInput by remember { mutableStateOf("") }
    var showCustomDzikirDialog by remember { mutableStateOf(false) }

    var counter by remember { mutableStateOf(0) }
    var selectedTargetIndex by remember { mutableStateOf(0) } // 0: 33, 1: 99, 2: 100, 3: Custom
    var customTargetValue by remember { mutableStateOf(33) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf("") }

    val targetLimit = when (selectedTargetIndex) {
        0 -> 33
        1 -> 99
        2 -> 100
        else -> customTargetValue
    }

    // Hitung persentase progres
    val progress = if (targetLimit > 0) counter.toFloat() / targetLimit.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(150),
        label = "TasbihProgress"
    )

    fun playClickTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            // Abaikan jika audio bermasalah
        }
    }

    fun triggerVibration(duration: Long = 50L) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Abaikan jika vibrasi gagal
        }
    }

    fun saveSessionToDb(name: String, count: Int) {
        if (count <= 0) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                tasbihDao.insertSession(
                    TasbihSession(
                        dzikirName = name,
                        count = count,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Gagal menyimpan ke DB
            }
        }
    }

    fun handleClearHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            tasbihDao.clearHistory()
        }
        Toast.makeText(context, "Riwayat dzikir dihapus", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Tasbih Digital",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selector Presets Dzikir
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00382E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Pilih Dzikir",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetDzikir.take(2).forEachIndexed { index, name ->
                                Button(
                                    onClick = {
                                        if (counter > 0) {
                                            saveSessionToDb(currentDzikirName, counter)
                                        }
                                        selectedPresetIndex = index
                                        currentDzikirName = name
                                        isCustomDzikirActive = false
                                        counter = 0
                                        triggerVibration(70)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isCustomDzikirActive && selectedPresetIndex == index) Color(0xFFD4AF37) else Color(0xFF004D40)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        name,
                                        color = if (!isCustomDzikirActive && selectedPresetIndex == index) Color(0xFF002B24) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetDzikir.drop(2).forEachIndexed { i, name ->
                                val index = i + 2
                                Button(
                                    onClick = {
                                        if (counter > 0) {
                                            saveSessionToDb(currentDzikirName, counter)
                                        }
                                        selectedPresetIndex = index
                                        currentDzikirName = name
                                        isCustomDzikirActive = false
                                        counter = 0
                                        triggerVibration(70)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isCustomDzikirActive && selectedPresetIndex == index) Color(0xFFD4AF37) else Color(0xFF004D40)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        name,
                                        color = if (!isCustomDzikirActive && selectedPresetIndex == index) Color(0xFF002B24) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Dzikir Row Button
                        OutlinedButton(
                            onClick = {
                                showCustomDzikirDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFFD700)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (isCustomDzikirActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCustomDzikirActive) "Kustom: $currentDzikirName" else "Buat Dzikir Kustom...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Target Selector Pills
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00382E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Target Dzikir: $targetLimit kali",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("33", "99", "100").forEachIndexed { index, targetVal ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTargetIndex == index) Color(0xFFD4AF37) else Color(0xFF004D40))
                                        .clickable {
                                            selectedTargetIndex = index
                                            triggerVibration(50)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        targetVal,
                                        color = if (selectedTargetIndex == index) Color(0xFF002B24) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Custom Target Box
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTargetIndex == 3) Color(0xFFD4AF37) else Color(0xFF004D40))
                                    .clickable {
                                        selectedTargetIndex = 3
                                        showCustomTargetDialog = true
                                        triggerVibration(50)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedTargetIndex == 3) "Kustom: $customTargetValue" else "Lainnya...",
                                    color = if (selectedTargetIndex == 3) Color(0xFF002B24) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. Mesin Counter Utama (Besar, Lingkaran di Tengah)
            item {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .padding(12.dp)
                        .shadow(16.dp, CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF004D40), Color(0xFF001F1A))
                            ),
                            CircleShape
                        )
                        .border(4.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            triggerVibration(60)
                            if (counter < targetLimit) {
                                counter++
                                if (counter == targetLimit) {
                                    // Target Tercapai!
                                    triggerVibration(500) // Vibrate panjang
                                    playClickTone()
                                    // Simpan ke DB otomatis
                                    saveSessionToDb(currentDzikirName, counter)
                                    Toast.makeText(context, "Alhamdulillah! Target dzikir $currentDzikirName tercapai.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Sudah melewati batas, reset otomatis atau abaikan
                                playClickTone()
                                counter = 1
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Circular progress ring
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxSize(0.92f),
                        color = Color(0xFFFFD700),
                        strokeWidth = 6.dp,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentDzikirName,
                            color = Color(0xFFFFD700),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "$counter",
                            color = Color.White,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "/ $targetLimit",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 4. Tombol Kontrol (Simpan Manual & Reset)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Simpan Manual Sesi
                    Button(
                        onClick = {
                            if (counter > 0) {
                                saveSessionToDb(currentDzikirName, counter)
                                Toast.makeText(context, "$counter Dzikir $currentDzikirName disimpan ke riwayat.", Toast.LENGTH_SHORT).show()
                                counter = 0
                                triggerVibration(100)
                            } else {
                                Toast.makeText(context, "Ketuk tasbih terlebih dahulu untuk mulai.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005b4f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Sesi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Reset Button
                    Button(
                        onClick = {
                            if (counter > 0) {
                                // Tanyakan / Simpan yang belum sempat tersimpan
                                saveSessionToDb(currentDzikirName, counter)
                            }
                            counter = 0
                            triggerVibration(120)
                            Toast.makeText(context, "Penghitung direset.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E1C1C)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Angka", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. Section Header Riwayat
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Sesi Dzikir",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (historySessions.isNotEmpty()) {
                        Text(
                            text = "Hapus Semua",
                            color = Color(0xFFFF6F6F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { handleClearHistory() }
                        )
                    }
                }
            }

            // 6. List Riwayat
            if (historySessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada riwayat berdzikir hari ini.\nYuk raih pahala dengan mengingat Allah.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                items(historySessions) { session ->
                    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
                    val dateFormatted = dateFormat.format(Date(session.timestamp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF003027).copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.dzikirName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormatted,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF004D40))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${session.count} kali",
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            tasbihDao.deleteSession(session.id)
                                        }
                                        triggerVibration(60)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Custom Dzikir
    if (showCustomDzikirDialog) {
        Dialog(onDismissRequest = { showCustomDzikirDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00382E))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Dzikir Kustom Baru",
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customDzikirInput,
                        onValueChange = { customDzikirInput = it },
                        placeholder = { Text("Contoh: Ya Rahman Ya Rahim", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCustomDzikirDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (customDzikirInput.isNotBlank()) {
                                    if (counter > 0) {
                                        saveSessionToDb(currentDzikirName, counter)
                                    }
                                    currentDzikirName = customDzikirInput.trim()
                                    isCustomDzikirActive = true
                                    counter = 0
                                    showCustomDzikirDialog = false
                                } else {
                                    Toast.makeText(context, "Input dzikir tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                        ) {
                            Text("Terapkan", color = Color(0xFF002B24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Dialog Custom Target
    if (showCustomTargetDialog) {
        Dialog(onDismissRequest = { showCustomTargetDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00382E))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Set Target Dzikir Kustom",
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) customTargetInput = input
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Contoh: 150", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCustomTargetDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val value = customTargetInput.toIntOrNull() ?: 0
                                if (value > 0) {
                                    customTargetValue = value
                                    showCustomTargetDialog = false
                                } else {
                                    Toast.makeText(context, "Harap masukkan target di atas 0!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                        ) {
                            Text("Simpan", color = Color(0xFF002B24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
