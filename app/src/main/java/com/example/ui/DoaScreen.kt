package id.ideahousetech.prayertime_qibla.ui

import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Screen Kumpulan Doa-Doa harian muslim lengkap.
 * Dilengkapi dengan kolom pencarian interaktif, teks Arab asli berpangkat besar mudah dibaca,
 * transkripsi latin pembacaan (transliterasi), arti bahasa Indonesia lengkap,
 * serta tombol salin taktil (Copy to Clipboard) dengan visual feedback yang responsif.
 */
@Composable
fun DoaScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Bank data doa harian autentik tanah air
    val doaLibrary = remember { getAuthenticDoaList() }

    // Memfilter koleksi doa harian berdasarkan input kolom pencarian user
    val filteredDoa = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            doaLibrary
        } else {
            doaLibrary.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.translation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
            text = "AL-MANAQIB",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB2DFDB), // Soft light-teal theme text
            letterSpacing = 2.sp
        )
        Text(
            text = "Kumpulan Doa Pilihan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary, // Gold
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kolom Pencarian / Search Bar (Sleek Glassmorphism Input)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari doa harian...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
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

        // Tampilan list daftar doa dengan LazyColumn
        if (filteredDoa.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDoa) { doa ->
                    DoaItemCard(doa = doa, onCopyClick = {
                        copyDoaToClipboard(context, doa)
                    })
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Doa yang Anda cari tidak ditemukan.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DoaItemCard(
    doa: DoaModel,
    onCopyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f), // Frosted glass borderline
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), // Exquisite glass
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Judul Doa + Tombol Salin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Buku Doa",
                            tint = MaterialTheme.colorScheme.primary, // Gold
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = doa.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White // Polished white heading
                    )
                }

                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin Teks Doa",
                        tint = MaterialTheme.colorScheme.primary, // Gold Icon
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Teks Arab Tradisional (Format Kanan-ke-Kiri / RTL) -> Gold Glow
            Text(
                text = doa.arabic,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif, // Memakai serif bawaan system agar font Arab terlihat proporsional luhur
                lineHeight = 40.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary // Gold Accent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transliterasi Latin
            Text(
                text = doa.latin,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary, // Bright Gold
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Arti Terjemahan Bahasa Indonesia
            Text(
                text = "Artinya: \"${doa.translation}\"",
                fontSize = 12.sp,
                color = Color(0xFFB2DFDB), // Soft light teal text
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun copyDoaToClipboard(context: Context, doa: DoaModel) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textToCopy = "${doa.title}\n\n${doa.arabic}\n\n${doa.latin}\n\nArtinya: ${doa.translation}"
        val clip = ClipData.newPlainText("Doa Islami", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Doa berhasil disalin ke papan klip!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal menyalin doa.", Toast.LENGTH_SHORT).show()
    }
}

data class DoaModel(
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String
)

private fun getAuthenticDoaList(): List<DoaModel> {
    return listOf(
        DoaModel(
            title = "Doa Kedua Orang Tua",
            arabic = "رَبِّ اغْفِرْ لِيْ وَلِوَالِدَيَّ وَارْحَمْهُمَا كَمَا رَبَّيَانِيْ صَغِيْرًا",
            latin = "Rabbighfir lii waliwaalidayya warhamhumaa kamaa rabbayaanii shaghiiraa.",
            translation = "Ya Tuhanku, ampunilah dosaku dan dosa kedua orang tuaku, serta sayangilah mereka sebagaimana mereka mendidikku di waktu kecil."
        ),
        DoaModel(
            title = "Doa Sapu Jagad (Kebaikan Jagad)",
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            latin = "Rabbanaa aatinaa fid-dun-yaa hasanataw wa fil-aakhirati hasanataw wa qinaa 'adzaaban-naar.",
            translation = "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, serta peliharalah kami dari siksaan api neraka."
        ),
        DoaModel(
            title = "Doa Sebelum Makan",
            arabic = "اَللّٰهُمَّ بَارِكْ لَنَا فِيْمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ",
            latin = "Allaahumma baarik lanaa fiimaa razaqtanaa wa qinaa 'adzaaban-naar.",
            translation = "Ya Allah, berkahilah kami atas rezeki yang telah Engkau berikan kepada kami, dan peliharalah kami dari siksa api neraka."
        ),
        DoaModel(
            title = "Doa Sesudah Makan",
            arabic = "اَلْحَمْدُ لِلّٰهِ الَّذِيْ اَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِيْنَ",
            latin = "Alhamdu lillaahil-ladzii ath'amanaa wa saqaanaa wa ja'alanaa muslimiin.",
            translation = "Segala puji bagi Allah yang telah memberi kami makan dan minum, serta menjadikan kami termasuk golongan orang-orang muslim."
        ),
        DoaModel(
            title = "Doa Sebelum Tidur",
            arabic = "بِاسْمِكَ اللّهُمَّ أَحْيَا وَأَمُوتُ",
            latin = "Bismika allaahumma ahyaa wa amuut.",
            translation = "Dengan nama-Mu, ya Allah, aku hidup dan dengan nama-Mu aku mati."
        ),
        DoaModel(
            title = "Doa Bangun Tidur",
            arabic = "اَلْحَمْدُ لِلّٰهِ الَّذِيْ أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُوْرُ",
            latin = "Alhamdu lillaahil-ladzii ahyaanaa ba'da maa amaatanaa wa ilaihin-nusyuur.",
            translation = "Segala puji bagi Allah yang telah menghidupkan kami kembali setelah mematikan kami (tidur), dan hanya kepada-Nya kami dibangkitkan."
        ),
        DoaModel(
            title = "Doa Masuk Masjid",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            latin = "Allaahummaf-tah lii abwaaba rahmatik.",
            translation = "Ya Allah, bukakanlah bagiku pintu-pintu rahmat-Mu."
        ),
        DoaModel(
            title = "Doa Keluar Masjid",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            latin = "Allaahumma innii as-aluka min fadhlik.",
            translation = "Ya Allah, sesungguhnya aku memohon keutamaan dari-Mu."
        ),
        DoaModel(
            title = "Doa Memohon Ilmu yang Bermanfaat",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلًا مُتَقَبَّلًا",
            latin = "Allaahumma innii as-aluka 'ilman naafi'an, wa rizqan thayyiban, wa 'amalan mutaqabbalan.",
            translation = "Ya Allah, sesungguhnya aku memohon kepada-Mu ilmu yang bermanfaat, rezeki yang baik, dan amal yang diterima."
        ),
        DoaModel(
            title = "Doa Keteguhan Iman (Ketetapan Hati)",
            arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            latin = "Ya muqallibal-quluubi tsabbit qalbii 'alaa diinik.",
            translation = "Wahai Dzat yang membolak-balikkan hati, tetapkanlah hatiku di atas agama-Mu."
        ),
        DoaModel(
            title = "Doa Memohon Kemudahan",
            arabic = "اللَّهُمَّ لاَ سَهْلَ إِلاَّ مَا جَعَلْتَهُ سَهْلاً وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلاً",
            latin = "Allaahumma laa sahla illaa maa ja'altahu sahlan, wa anta taj'alul-hazna idzaa syi'ta sahlan.",
            translation = "Ya Allah, tidak ada kemudahan kecuali apa yang Engkau jadikan mudah. Dan Engkau menjadikan kesedihan (kesulitan), jika Engkau menghendaki, menjadi mudah."
        ),
        DoaModel(
            title = "Doa Keluar Rumah",
            arabic = "بِسْمِ اللهِ تَوَكَّلْتُ عَلَى اللهِ، لاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللهِ",
            latin = "Bismillaahi tawakkaltu 'alallaahi, laa hawla wa laa quwwata illaa billaah.",
            translation = "Dengan nama Allah, aku bertawakal kepada Allah. Tiada daya dan kekuatan kecuali dengan pertolongan Allah."
        ),
        DoaModel(
            title = "Doa Naik Kendaraan",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            latin = "Subhaanal-ladzii sakhkhara lanaa haadzaa wa maa kunnaa lahuu muqriniin, wa innaa ilaa rabbinaa lamunqalibuun.",
            translation = "Maha Suci Allah yang telah menundukkan semua ini bagi kami padahal kami sebelumnya tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami."
        )
    )
}
