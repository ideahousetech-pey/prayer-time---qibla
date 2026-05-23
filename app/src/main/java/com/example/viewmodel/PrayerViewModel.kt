package com.example.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.IslamicHoliday
import com.example.model.PrayerTime
import com.example.service.NotificationService
import com.example.service.PrayerService
import com.example.utils.HijriDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ViewModel utama untuk mengatur jadwal sholat harian, bulanan,
 * perhitungan hitung mundur hitungan detik presisi luring, penentuan urutan sholat selanjutnya,
 * serta notifikasi popup hari besar Islam yang relevan.
 */
class PrayerViewModel(private val context: Context) : ViewModel() {

    private val prayerService = PrayerService(context)
    private val notificationService = NotificationService(context)

    // State list bulanan 30 hari kedepan
    private val _monthlySchedule = MutableStateFlow<List<PrayerTime>>(emptyList())
    val monthlySchedule: StateFlow<List<PrayerTime>> = _monthlySchedule.asStateFlow()

    // State jadwal sholat hari ini
    private val _todaySchedule = MutableStateFlow<PrayerTime?>(null)
    val todaySchedule: StateFlow<PrayerTime?> = _todaySchedule.asStateFlow()

    // State tanggal realtime Gregorian
    private val _todayGregorian = MutableStateFlow("")
    val todayGregorian: StateFlow<String> = _todayGregorian.asStateFlow()

    // State tanggal realtime Hijriah
    private val _todayHijri = MutableStateFlow("")
    val todayHijri: StateFlow<String> = _todayHijri.asStateFlow()

    // State Nama Sholat berikutnya (contoh: "Subuh", "Dzuhur", dst)
    private val _nextPrayerName = MutableStateFlow("")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    // State Waktu target jam sholat berikutnya (contoh: "12:05")
    private val _nextPrayerTimeValue = MutableStateFlow("00:00")
    val nextPrayerTimeValue: StateFlow<String> = _nextPrayerTimeValue.asStateFlow()

    // State Label display di layars (misal: "Subuh (Fajr) (Besok)" atau "Dzuhur")
    private val _nextPrayerLabelLabel = MutableStateFlow("Memuat...")
    val nextPrayerLabelLabel: StateFlow<String> = _nextPrayerLabelLabel.asStateFlow()

    // State teks countdown realtime format HH:mm:ss
    private val _countdownString = MutableStateFlow("00:00:00")
    val countdownString: StateFlow<String> = _countdownString.asStateFlow()

    // State popup hari besar Islam jika ada hari penting hari ini
    private val _currentHolidayPopUp = MutableStateFlow<IslamicHoliday?>(null)
    val currentHolidayPopUp: StateFlow<IslamicHoliday?> = _currentHolidayPopUp.asStateFlow()

    private var countdownJob: Job? = null

    init {
        updateCurrentDateDisplays()
        startCountdownTimer()
    }

    /**
     * Memperbarui UI tanggal realtime, baik penanggalan Masehi maupun penanggalan Hijriah.
     */
    fun updateCurrentDateDisplays() {
        val cal = Calendar.getInstance()
        val formatG = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        _todayGregorian.value = formatG.format(cal.time)

        val hijri = HijriDateUtils.convertToHijri(cal)
        _todayHijri.value = hijri.formatted

        // Periksa hari besar penting untuk pop-up di layar utama
        val holiday = HijriDateUtils.checkHoliday(hijri.day, hijri.month)
        _currentHolidayPopUp.value = holiday
    }

    /**
     * Menutup jendela pop-up hari raya besar
     */
    fun dismissHolidayPopUp() {
        _currentHolidayPopUp.value = null
    }

    /**
     * Menarik ulang jadwal sholat bulanan dari API atau lokal astronomi kalkulator berdasarkan koordinat posisi GPS baru.
     */
    fun loadPrayerTimesForLocation(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH) + 1
            val currentYear = cal.get(Calendar.YEAR)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            // Tarik jadwal bulanan
            val schedule = prayerService.getMonthlyPrayerTimes(lat, lon, currentMonth, currentYear)
            if (schedule.isNotEmpty()) {
                _monthlySchedule.value = schedule
                
                // Cari jadwal untuk hari ini (biasanya berindeks ke day-1)
                val dayIndex = (currentDay - 1).coerceIn(0, schedule.size - 1)
                val todayData = schedule[dayIndex]
                _todaySchedule.value = todayData

                // Mengaktifkan alarm adzan harian lewat NotificationService
                launch(Dispatchers.Main) {
                    notificationService.scheduleDailyAlarms(todayData)
                }
            } else {
                // Total fallback dari perhitungan astronomis lokal langsung
                val localSchedule = prayerService.calculateOfflineMonthlyPrayerTimes(lat, lon, currentMonth, currentYear)
                _monthlySchedule.value = localSchedule
                val dayIndex = (currentDay - 1).coerceIn(0, localSchedule.size - 1)
                val todayData = localSchedule[dayIndex]
                _todaySchedule.value = todayData
                
                launch(Dispatchers.Main) {
                    notificationService.scheduleDailyAlarms(todayData)
                }
            }
        }
    }

    /**
     * Memulai loop coroutine berulang setiap 1 detik.
     * Berfungsi memutakhirkan countdown penanda waktu mundur mundur ke Sholat berikutnya.
     */
    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                // Cek jadwal hari ini
                val today = _todaySchedule.value
                if (today != null) {
                    calculateNextPrayerCountdown(today)
                }
                delay(1000)
            }
        }
    }

    /**
     * Algoritma penentu gerbang waktu sholat berikutnya paling mendekati waktu saat ini.
     * Mengkalkulasi selisih jam, menit, dan detik lalu menampilkannya sebagai countdown di layar.
     */
    private fun calculateNextPrayerCountdown(times: PrayerTime) {
        val now = Calendar.getInstance()
        val currentMillis = now.timeInMillis

        // Daftar waktu sholat hari ini
        val prayerTimesList = listOf(
            Triple("Subuh (Fajr)", times.fajr, false),
            Triple("Dzuhur", times.dhuhr, false),
            Triple("Ashar", times.asr, false),
            Triple("Maghrib", times.maghrib, false),
            Triple("Isya", times.isha, false)
        )

        var foundNext = false
        for (p in prayerTimesList) {
            val name = p.first
            val valStr = p.second
            
            val pCal = parseTimeStringToCalendar(valStr, false)
            if (pCal.timeInMillis > currentMillis) {
                // Waktu sholat ini adalah sholat berikutnya hari ini!
                _nextPrayerName.value = name
                _nextPrayerTimeValue.value = valStr
                _nextPrayerLabelLabel.value = name
                
                val diff = pCal.timeInMillis - currentMillis
                _countdownString.value = formatMillisToCountdown(diff)
                foundNext = true
                break
            }
        }

        // Jika semua sholat hari ini sudah lewat (contoh: sudah jam 21:00 sesudah Isya).
        // Maka sholat berikutnya adalah Subuh BESOK HARI.
        if (!foundNext) {
            _nextPrayerName.value = "Subuh"
            _nextPrayerTimeValue.value = times.fajr
            _nextPrayerLabelLabel.value = "Subuh (Fajr) (Besok)"
            
            val tomorrowSubuh = parseTimeStringToCalendar(times.fajr, true)
            val diff = tomorrowSubuh.timeInMillis - currentMillis
            _countdownString.value = formatMillisToCountdown(diff)
        }
    }

    private fun parseTimeStringToCalendar(timeStr: String, isTomorrow: Boolean): Calendar {
        val parts = timeStr.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (isTomorrow) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun formatMillisToCountdown(millis: Long): String {
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return "%02d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
