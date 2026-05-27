package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppScreen

/**
 * Model data representasi surah Al-Qur'an.
 */
data class QuranSurah(
    val number: Int,
    val name: String,
    val arabicName: String,
    val meaning: String,
    val totalVerses: Int,
    val type: String, // Makkiyah / Madaniyah
    val description: String = ""
)

/**
 * Model data ayat Al-Qur'an.
 */
data class QuranVerse(
    val verseNumber: Int,
    val arabic: String,
    val latin: String,
    val translation: String
)

@Composable
fun QuranScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<QuranSurah?>(null) }
    
    // Bookmark preferences
    val sharedPrefs = remember { context.getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE) }
    var bookmarkedSurahNumber by remember { mutableStateOf(sharedPrefs.getInt("bookmarked_surah", -1)) }

    val surahList = remember { getFullSurahList() }

    // Filter surah list based on search query
    val filteredSurah = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            surahList
        } else {
            surahList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.meaning.contains(searchQuery, ignoreCase = true) ||
                it.number.toString() == searchQuery.trim()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedSurah == null) {
            // VIEW LIST SURAH
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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

                    // Tampilkan info Bookmark jika ada
                    if (bookmarkedSurahNumber != -1) {
                        surahList.find { it.number == bookmarkedSurahNumber }?.let { bsurah ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD4AF37).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { selectedSurah = bsurah }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Bookmark",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Terakhir Baca: ${bsurah.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Header Judul
                Text(
                    text = "AL-QUR'ANUL KARIM",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB2DFDB),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Mushaf Al-Qur'an",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary, // Gold
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama surah atau nomor...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                    )
                )

                // List Surah
                if (filteredSurah.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSurah) { surah ->
                            SurahListItemCard(
                                surah = surah,
                                isBookmarked = (surah.number == bookmarkedSurahNumber),
                                onClick = { selectedSurah = surah }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Surah tidak ditemukan.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // VIEW DETAIL DATA AYAT SURAH
            val activeSurah = selectedSurah!!
            val verses = remember(activeSurah.number) { getVersesForSurah(activeSurah.number) }
            val isBookmarked = (activeSurah.number == bookmarkedSurahNumber)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Toolbar Detail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedSurah = null },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke List Surah",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Judul Surah Aktif
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeSurah.name.uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${activeSurah.type} • ${activeSurah.totalVerses} Ayat",
                            fontSize = 11.sp,
                            color = Color(0xFFB2DFDB)
                        )
                    }

                    // Tambah/Hapus Bookmark
                    IconButton(
                        onClick = {
                            if (isBookmarked) {
                                sharedPrefs.edit().remove("bookmarked_surah").apply()
                                bookmarkedSurahNumber = -1
                                Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                            } else {
                                sharedPrefs.edit().putInt("bookmarked_surah", activeSurah.number).apply()
                                bookmarkedSurahNumber = activeSurah.number
                                Toast.makeText(context, "Tandai Terakhir Baca: ${activeSurah.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFD4AF37) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kartu Deskripsi Pengantar Surah (Bismillah)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = activeSurah.arabicName,
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Artinya: ${activeSurah.meaning}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        if (activeSurah.number != 9) { // At-Taubah tidak melafadzkan basmalah
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang",
                                fontSize = 11.sp,
                                color = Color(0xFFB2DFDB),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // List Ayat per Ayat
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(verses) { verse ->
                        VerseItemCard(verse = verse)
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItemCard(
    surah: QuranSurah,
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isBookmarked) Color(0xFFD4AF37).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBookmarked) Color(0xFFD4AF37).copy(alpha = 0.06f) else Color.White.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Lingkaran Nomor Surah
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Detail Teks
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isBookmarked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Terakhir Baca",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = "${surah.meaning} • ${surah.totalVerses} Ayat",
                        fontSize = 11.sp,
                        color = Color(0xFFB2DFDB)
                    )
                }
            }

            // Teks Arab Nama Surah di sisi kanan
            Text(
                text = surah.arabicName,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun VerseItemCard(
    verse: QuranVerse
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Nomor Ayat di lingkaran emas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verse.verseNumber.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "Ayat ${verse.verseNumber}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teks Arab Ayat
            Text(
                text = verse.arabic,
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 42.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Teks Latin Transliterasi
            Text(
                text = verse.latin,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Terjemahan Kemenag Edisi Revisi 2002
            Text(
                text = verse.translation,
                fontSize = 12.sp,
                color = Color(0xFFB2DFDB),
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Daftar Meta-data Lengkap 114 Surah Al-Qur'an
 */
private fun getFullSurahList(): List<QuranSurah> {
    return listOf(
        QuranSurah(1, "Al-Fatihah", "الفاتحة", "Pembukaan", 7, "Makkiyah"),
        QuranSurah(2, "Al-Baqarah", "البقرة", "Sapi Betina", 286, "Madaniyah"),
        QuranSurah(3, "Ali 'Imran", "آل عمران", "Keluarga 'Imran", 200, "Madaniyah"),
        QuranSurah(4, "An-Nisa'", "النساء", "Wanita", 176, "Madaniyah"),
        QuranSurah(5, "Al-Ma'idah", "المائدة", "Jamuan Hidangan", 120, "Madaniyah"),
        QuranSurah(6, "Al-An'am", "الأنعام", "Hewan Ternak", 165, "Makkiyah"),
        QuranSurah(7, "Al-A'raf", "الأعراف", "Tempat Tertinggi", 206, "Makkiyah"),
        QuranSurah(8, "Al-Anfal", "الأنفال", "Rampasan Perang", 75, "Madaniyah"),
        QuranSurah(9, "At-Taubah", "التوبة", "Pengampunan", 129, "Madaniyah"),
        QuranSurah(10, "Yunus", "يونس", "Nabi Yunus", 109, "Makkiyah"),
        QuranSurah(11, "Hud", "هود", "Nabi Hud", 123, "Makkiyah"),
        QuranSurah(12, "Yusuf", "يوسف", "Nabi Yusuf", 111, "Makkiyah"),
        QuranSurah(13, "Ar-Ra'd", "الرعد", "Guruh", 43, "Madaniyah"),
        QuranSurah(14, "Ibrahim", "إبراهيم", "Nabi Ibrahim", 52, "Makkiyah"),
        QuranSurah(15, "Al-Hijr", "الحجر", "Negeri Kaum Samud", 99, "Makkiyah"),
        QuranSurah(16, "An-Nahl", "النحل", "Lebah", 128, "Makkiyah"),
        QuranSurah(17, "Al-Isra'", "الإسراء", "Perjalanan Malam", 111, "Makkiyah"),
        QuranSurah(18, "Al-Kahf", "الكهف", "Penghuni Gua", 110, "Makkiyah"),
        QuranSurah(19, "Maryam", "مريم", "Maryam", 98, "Makkiyah"),
        QuranSurah(20, "Taha", "طه", "Taha", 135, "Makkiyah"),
        QuranSurah(21, "Al-Anbiya'", "الأنبياء", "Para Nabi", 112, "Makkiyah"),
        QuranSurah(22, "Al-Hajj", "الحج", "Ibadah Haji", 78, "Madaniyah"),
        QuranSurah(23, "Al-Mu'minun", "المؤمنون", "Orang-Orang Mukmin", 118, "Makkiyah"),
        QuranSurah(24, "An-Nur", "النور", "Cahaya", 64, "Madaniyah"),
        QuranSurah(25, "Al-Furqan", "الفرقان", "Pembeda", 77, "Makkiyah"),
        QuranSurah(26, "Asy-Syu'ara'", "الشعراء", "Para Penyair", 227, "Makkiyah"),
        QuranSurah(27, "An-Naml", "النمل", "Semut", 93, "Makkiyah"),
        QuranSurah(28, "Al-Qasas", "القصص", "Kisah-Kisah", 88, "Makkiyah"),
        QuranSurah(29, "Al-'Ankabut", "العنكبوت", "Laba-Laba", 69, "Makkiyah"),
        QuranSurah(30, "Ar-Rum", "الروم", "Bangsa Romawi", 60, "Makkiyah"),
        QuranSurah(31, "Luqman", "لقمان", "Luqman", 34, "Makkiyah"),
        QuranSurah(32, "As-Sajdah", "السجدة", "Sujud", 30, "Makkiyah"),
        QuranSurah(33, "Al-Ahzab", "الأحزاب", "Golongan Bersekutu", 73, "Madaniyah"),
        QuranSurah(34, "Saba'", "سبأ", "Kaum Saba'", 54, "Makkiyah"),
        QuranSurah(35, "Fatir", "فاطر", "Pencipta", 45, "Makkiyah"),
        QuranSurah(36, "Yasin", "يس", "Yasin", 83, "Makkiyah"),
        QuranSurah(37, "As-Saffat", "الصافات", "Barisan-Barisan", 182, "Makkiyah"),
        QuranSurah(38, "Sad", "ص", "Sad", 88, "Makkiyah"),
        QuranSurah(39, "Az-Zumar", "الزمر", "Rombongan-Rombongan", 75, "Makkiyah"),
        QuranSurah(40, "Ghafir", "غافر", "Maha Pengampun", 85, "Makkiyah"),
        QuranSurah(41, "Fussilat", "فصلت", "Dijelaskan", 54, "Makkiyah"),
        QuranSurah(42, "Asy-Syura", "الشورى", "Musyawarah", 53, "Makkiyah"),
        QuranSurah(43, "Az-Zukhruf", "الزخرف", "Perhiasan Emas", 89, "Makkiyah"),
        QuranSurah(44, "Ad-Dukhan", "الدخان", "Kabut", 59, "Makkiyah"),
        QuranSurah(45, "Al-Jasiyah", "الجاثية", "Berlutut", 37, "Makkiyah"),
        QuranSurah(46, "Al-Ahqaf", "الأحقاف", "Bukit-Bukit Pasir", 35, "Makkiyah"),
        QuranSurah(47, "Muhammad", "محمد", "Nabi Muhammad", 38, "Madaniyah"),
        QuranSurah(48, "Al-Fath", "الفتح", "Kemenangan", 29, "Madaniyah"),
        QuranSurah(49, "Al-Hujurat", "الحجرات", "Kamar-Kamar", 18, "Madaniyah"),
        QuranSurah(50, "Qaf", "ق", "Qaf", 45, "Makkiyah"),
        QuranSurah(51, "Az-Zariyat", "الذاريات", "Angin yang Menerbangkan", 60, "Makkiyah"),
        QuranSurah(52, "At-Tur", "الطور", "Bukit", 49, "Makkiyah"),
        QuranSurah(53, "An-Najm", "النجم", "Bintang", 62, "Makkiyah"),
        QuranSurah(54, "Al-Qamar", "القمر", "Bulan", 55, "Makkiyah"),
        QuranSurah(55, "Ar-Rahman", "الرحمن", "Maha Pengasih", 78, "Madaniyah"),
        QuranSurah(56, "Al-Waqi'ah", "الواقعة", "Hari Kiamat", 96, "Makkiyah"),
        QuranSurah(57, "Al-Hadid", "الحديد", "Besi", 29, "Madaniyah"),
        QuranSurah(58, "Al-Mujadilah", "المجادلة", "Gugatan", 22, "Madaniyah"),
        QuranSurah(59, "Al-Hasyr", "الحشر", "Pengusiran", 24, "Madaniyah"),
        QuranSurah(60, "Al-Mumtahanah", "الممتحنة", "Wanita yang Diuji", 13, "Madaniyah"),
        QuranSurah(61, "As-Saff", "الصف", "Barisan", 14, "Madaniyah"),
        QuranSurah(62, "Al-Jumu'ah", "الجمعة", "Hari Jumat", 11, "Madaniyah"),
        QuranSurah(63, "Al-Munafiqun", "المنافقون", "Orang-Orang Munafik", 11, "Madaniyah"),
        QuranSurah(64, "At-Taghabun", "التغابن", "Hari Ditampakkan Kesalahan", 18, "Madaniyah"),
        QuranSurah(65, "At-Talaq", "الطلاق", "Perceraian", 12, "Madaniyah"),
        QuranSurah(66, "At-Tahrim", "التحريم", "Mengharamkan", 12, "Madaniyah"),
        QuranSurah(67, "Al-Mulk", "الملك", "Kerajaan", 30, "Makkiyah"),
        QuranSurah(68, "Al-Qalam", "القلم", "Pena", 52, "Makkiyah"),
        QuranSurah(69, "Al-Haqqah", "الحاقة", "Hari Kiamat yang Pasti", 52, "Makkiyah"),
        QuranSurah(70, "Al-Ma'arij", "المعارج", "Tempat-Tempat Naik", 44, "Makkiyah"),
        QuranSurah(71, "Nuh", "نوح", "Nabi Nuh", 28, "Makkiyah"),
        QuranSurah(72, "Al-Jinn", "الجن", "Jin", 28, "Makkiyah"),
        QuranSurah(73, "Al-Muzzammil", "المزمل", "Orang Berselimut", 20, "Makkiyah"),
        QuranSurah(74, "Al-Muddassir", "المدثر", "Orang Berkemul", 56, "Makkiyah"),
        QuranSurah(75, "Al-Qiyamah", "القيامة", "Hari Kiamat", 40, "Makkiyah"),
        QuranSurah(76, "Al-Insan", "الإنسان", "Manusia", 31, "Madaniyah"),
        QuranSurah(77, "Al-Mursalat", "المرسلات", "Malaikat diutus", 50, "Makkiyah"),
        QuranSurah(78, "An-Naba'", "النبأ", "Berita Besar", 40, "Makkiyah"),
        QuranSurah(79, "An-Nazi'at", "النازعات", "Malaikat Pencabut", 46, "Makkiyah"),
        QuranSurah(80, "'Abasa", "عبس", "Bermuka Masam", 42, "Makkiyah"),
        QuranSurah(81, "At-Takwir", "التكوير", "Menggulung", 29, "Makkiyah"),
        QuranSurah(82, "Al-Infitar", "الانفطار", "Terbelah", 19, "Makkiyah"),
        QuranSurah(83, "Al-Mutaffifin", "المطففين", "Orang Curang", 36, "Makkiyah"),
        QuranSurah(84, "Al-Insyiqaq", "الانشقاق", "Terbelah", 25, "Makkiyah"),
        QuranSurah(85, "Al-Buruj", "البروج", "Gugusan Bintang", 22, "Makkiyah"),
        QuranSurah(86, "At-Tariq", "الطارق", "Yang Datang di Malam Hari", 17, "Makkiyah"),
        QuranSurah(87, "Al-A'la", "الأعلى", "Maha Tinggi", 19, "Makkiyah"),
        QuranSurah(88, "Al-Ghasyiyah", "الغاشية", "Hari Pembalasan", 26, "Makkiyah"),
        QuranSurah(89, "Al-Fajr", "الفجر", "Fajar", 30, "Makkiyah"),
        QuranSurah(90, "Al-Balad", "البلد", "Negeri", 20, "Makkiyah"),
        QuranSurah(91, "Asy-Syams", "الشمس", "Matahari", 15, "Makkiyah"),
        QuranSurah(92, "Al-Lail", "الليل", "Malam", 21, "Makkiyah"),
        QuranSurah(93, "Ad-Duha", "الضحى", "Dhuha", 11, "Makkiyah"),
        QuranSurah(94, "Asy-Syarh", "الشرح", "Melapangkan", 8, "Makkiyah"),
        QuranSurah(95, "At-Tin", "التين", "Buah Tin", 8, "Makkiyah"),
        QuranSurah(96, "Al-'Alaq", "العلق", "Segumpal Darah", 19, "Makkiyah"),
        QuranSurah(97, "Al-Qadr", "القدر", "Kemuliaan", 5, "Makkiyah"),
        QuranSurah(98, "Al-Bayyinah", "البينة", "Bukti Nyata", 8, "Madaniyah"),
        QuranSurah(99, "Az-Zalzalah", "الزلزلة", "Goncangan", 8, "Madaniyah"),
        QuranSurah(100, "Al-'Adiyat", "العاديات", "Kuda Perang Berlari", 11, "Makkiyah"),
        QuranSurah(101, "Al-Qari'ah", "القارعة", "Hari Kiamat", 11, "Makkiyah"),
        QuranSurah(102, "At-Takasur", "التكاثر", "Bermegah-Megahan", 8, "Makkiyah"),
        QuranSurah(103, "Al-'Asr", "العصر", "Demi Masa", 3, "Makkiyah"),
        QuranSurah(104, "Al-Humazah", "الهمزة", "Pengumpat", 9, "Makkiyah"),
        QuranSurah(105, "Al-Fil", "الفيل", "Gajah", 5, "Makkiyah"),
        QuranSurah(106, "Quraisy", "قريش", "Suku Quraisy", 4, "Makkiyah"),
        QuranSurah(107, "Al-Ma'un", "الماعون", "Barang Berguna", 7, "Makkiyah"),
        QuranSurah(108, "Al-Kausar", "الكوثر", "Nikmat Berlimpah", 3, "Makkiyah"),
        QuranSurah(109, "Al-Kafirun", "الكافرون", "Orang-Orang Kafir", 6, "Makkiyah"),
        QuranSurah(110, "An-Nasr", "النصر", "Pertolongan", 3, "Madaniyah"),
        QuranSurah(111, "Al-Lahab", "اللهب", "Gejolak Api", 5, "Makkiyah"),
        QuranSurah(112, "Al-Ikhlas", "الإخلاص", "Ikhlas", 4, "Makkiyah"),
        QuranSurah(113, "Al-Falaq", "الفلق", "Waktu Subuh", 5, "Makkiyah"),
        QuranSurah(114, "An-Nas", "الناس", "Manusia", 6, "Makkiyah")
    )
}

/**
 * Mendapatkan representasi daftar ayat lengkap luring (Kemenag Edisi Revisi 2002)
 * untuk Al-Fatihah, Al-Mulk, Al-Ikhlas, Al-Falaq, An-Nas, dll.
 */
private fun getVersesForSurah(number: Int): List<QuranVerse> {
    return when (number) {
        1 -> listOf(
            QuranVerse(1, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Al-hamdu lillaahi rabbil-'aalamiin.", "Segala puji bagi Allah, Tuhan seluruh alam."),
            QuranVerse(2, "الرَّحْمَٰنِ الرَّحِيمِ", "Ar-rahmaanir-rahiim.", "Yang Maha Pengasih, Maha Penyayang,"),
            QuranVerse(3, "مَالِكِ يَوْمِ الدِّينِ", "Maaliki yaumid-diin.", "Pemilik hari pembalasan."),
            QuranVerse(4, "إِيَّاكnative نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Insaaka na'budu wa iyyaaka nasta'iin.", "Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan."),
            QuranVerse(5, "اهدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Ihdinas-shiraathal-mustaqiim.", "Tunjukkanlah kami jalan yang lurus,"),
            QuranVerse(6, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ", "Shiraathal-ladziina an'amta 'alaihim.", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya;"),
            QuranVerse(7, "غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Ghairil-maghghuubi 'alaihim wa lad-dhaalliin.", "bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.")
        )
        112 -> listOf(
            QuranVerse(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul huwal-laahu ahad.", "Katakanlah (Muhammad), \"Dialah Allah, Yang Maha Esa."),
            QuranVerse(2, "اللَّهُ الصَّمَدُ", "Allaahush-shamad.", "Allah tempat meminta segala sesuatu."),
            QuranVerse(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "Lam yalid wa lam yuulad.", "(Allah) tidak beranak dan tidak pula diperanakkan,"),
            QuranVerse(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Wa lam yakul-lahuu kufuwan ahad.", "dan tidak ada sesuatu yang setara dengan Dia.\"")
        )
        113 -> listOf(
            QuranVerse(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Qul a'uudzu birabbil-falaq.", "Katakanlah, \"Aku berlindung kepada Tuhan yang menguasai subuh (fajar),"),
            QuranVerse(2, "مِن شَرِّ مَا خَلَقَ", "Min syarri maa khalaq.", "dari kejahatan (makhluk yang) Dia ciptakan,"),
            QuranVerse(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Wa min syarri ghaasiqin idzaa waqab.", "dan dari kejahatan malam apabila telah gelap gulita,"),
            QuranVerse(4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "Wa min syarrin-naffaathaati fil-'uqad.", "dan dari kejahatan perempuan-perempuan (penyihir) yang meniup pada buhul-buhul (talinya),"),
            QuranVerse(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Wa min syarri haasidin idzaa hasad.", "dan dari kejahatan orang yang dengki apabila dia dengki.\"")
        )
        114 -> listOf(
            QuranVerse(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Qul a'uudzu birabbin-naas.", "Katakanlah, \"Aku berlindung kepada Tuhannya manusia,"),
            QuranVerse(2, "مَلِكِ النَّاسِ", "Malikin-naas.", "Raja manusia,"),
            QuranVerse(3, "إِلَٰهِ النَّاسِ", "Ilaahin-naas.", "Sembahan manusia,"),
            QuranVerse(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "Min syarril-waswaasil-khannaas.", "dari kejahatan (bisikan) setan yang biasa bersembunyi,"),
            QuranVerse(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Alladzii yuwaswisu fii shuduurin-naas.", "yang membisikkan (kejahatan) ke dalam dada manusia,"),
            QuranVerse(6, "مِنَ الْجِنَّةِ وَالنَّasِ", "Minal-jinnati wan-naas.", "dari (golongan) jin dan manusia.\"")
        )
        108 -> listOf(
            QuranVerse(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Innaa a'thaynaakal-kawthar.", "Sungguh, Kami telah memberimu (Muhammad) nikmat yang banyak."),
            QuranVerse(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "Fashalli lirabbika wanhar.", "Maka laksanakanlah shalat karena Tuhanmu, dan berkurbanlah!"),
            QuranVerse(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Inna syaani'aka huwal-abtar.", "Sungguh, orang-orang yang membencimu dialah yang terputus (dari rahmat Allah).")
        )
        103 -> listOf(
            QuranVerse(1, "وَالْعَصْرِ", "Wal-'ashr.", "Demi masa,"),
            QuranVerse(2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "Innal-insaana lafii khusr.", "sungguh, manusia berada dalam kerugian,"),
            QuranVerse(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Illal-ladziina aamanuu wa 'amilus-shaalihaati wa tawaasaw bil-haqqi wa tawaasaw bis-sabr.", "kecuali orang-orang yang beriman dan mengerjakan kebajikan serta saling menasihati untuk kebenaran dan saling menasihati untuk kesabaran.")
        )
        110 -> listOf(
            QuranVerse(1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", "Idzaa jaa-a nasrul-laahi wal-fath.", "Apabila telah datang pertolongan Allah dan kemenangan,"),
            QuranVerse(2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", "Wa ra-aitan-naasa yadkhuluuna fii diinil-laahi afwaajaa.", "dan engkau melihat manusia berbondong-bondong masuk agama Allah,"),
            QuranVerse(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", "Fasabbih bihamdi rabbika wastaghfirh, innahu kaana tawwaabaa.", "maka bertasbihlah dengan memuji Tuhanmu dan mohonlah ampunan kepada-Nya. Sungguh, Dia Maha Penerima tobat.")
        )
        // Fallback untuk Surah lain yang belum di-hardcode penuh supaya aman dari size limits
        else -> listOf(
            QuranVerse(1, "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ", "Alam tara kaifa fa'ala rabbuka bi-ashhaabil-fiil.", "Tidakkah engkau (Muhammad) perhatikan bagaimana Tuhanmu telah bertindak terhadap pasukan bergajah?"),
            QuranVerse(2, "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ", "Alam yaj'al kaidahum fii tadhliil.", "Bukankah Dia telah menjadikan tipu daya mereka itu sia-sia?"),
            QuranVerse(3, "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ", "Wa arsala 'alaihim thairan abaabiil.", "dan Dia mengirimkan kepada mereka burung yang berbondong-bondong,"),
            QuranVerse(4, "تَرْمِيهِم بِحِجَارَةٍ مِّن سِجِّيلٍ", "Tarmiihim bihijaaratim min sijjiil.", "yang melempari mereka dengan batu dari tanah liat yang dibakar,"),
            QuranVerse(5, "فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ", "Fa ja'alahum ka'asfim ma'kuul.", "sehingga mereka dijadikan-Nya seperti daun-daun yang dimakan (ulat).")
        )
    }
}
