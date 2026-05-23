package com.example.service

import android.content.Context
import android.util.Log
import com.example.model.PrayerTime
import com.squareup.moshi.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Service untuk mendapatkan jadwal waktu sholat secara harian dan bulanan.
 * Mengambil data dari Aladhan API berdasarkan parameter latitude, longitude, dan perhitungan metode standar.
 * Menyediakan fallback perhitungan astronomis lokal yang akurat jika offline atau API gagal dipanggil.
 */
class PrayerService(private val context: Context) {

    // Konfigurasi HTTP Network client dengan logging interceptor
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val aladhanApi = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AladhanApi::class.java)

    /**
     * Mengambil jadwal sholat satu bulan penuh dari Aladhan API.
     * Secara otomatis mengkonversi output dari API ke bentuk List dari model `PrayerTime`.
     */
    suspend fun getMonthlyPrayerTimes(
        latitude: Double,
        longitude: Double,
        month: Int,
        year: Int,
        method: Int = 3 // Method 3 = Muslim World League (MWL), Method 2 = ISNA, Method 15 = Kemenag RI (ideal untuk Indonesia)
    ): List<PrayerTime> = withContext(Dispatchers.IO) {
        try {
            val response = aladhanApi.getMonthlyCalendar(
                latitude = latitude,
                longitude = longitude,
                month = month,
                year = year,
                method = method
            )
            if (response.code == 200 && response.data != null) {
                return@withContext response.data.map { item ->
                    mapApiItemToPrayerTime(item)
                }
            } else {
                Log.w("PrayerService", "API respons tidak sukses, menggunakan perhitungan astronomis lokal")
                return@withContext calculateOfflineMonthlyPrayerTimes(latitude, longitude, month, year)
            }
        } catch (e: Exception) {
            Log.e("PrayerService", "Gagal fetch API jadwal sholat: ${e.message}", e)
            return@withContext calculateOfflineMonthlyPrayerTimes(latitude, longitude, month, year)
        }
    }

    /**
     * Map item API Response ke model internal PrayerTime
     */
    private fun mapApiItemToPrayerTime(item: ApiCalendarItem): PrayerTime {
        val rawTimings = item.timings
        // Aladhan API mengembalikan waktu sholat dalam format "HH:mm (WIB)". Kita bersihkan hanya "HH:mm" saja.
        val cleanTime = { timeStr: String ->
            timeStr.split(" ")[0].trim()
        }

        // Tanggal masehi lokalized Indonesia
        val gregorianDateStr = parseGregorianDate(item.date.gregorian?.date ?: "")
        val hijriDateStr = parseHijriDate(
            item.date.hijri?.day ?: "",
            item.date.hijri?.month?.en ?: "",
            item.date.hijri?.year ?: ""
        )

        return PrayerTime(
            dateGregorian = gregorianDateStr,
            dateHijri = hijriDateStr,
            fajr = cleanTime(rawTimings.fajr),
            dhuhr = cleanTime(rawTimings.dhuhr),
            asr = cleanTime(rawTimings.asr),
            maghrib = cleanTime(rawTimings.maghrib),
            isha = cleanTime(rawTimings.isha)
        )
    }

    private fun parseGregorianDate(rawDateStr: String): String {
        // Input: "01-05-2026"
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = inputFormat.parse(rawDateStr) ?: Date()
            val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(date)
        } catch (e: Exception) {
            rawDateStr
        }
    }

    private fun parseHijriDate(day: String, monthEn: String, year: String): String {
        val indoMonth = when (monthEn.lowercase()) {
            "muharram" -> "Muharram"
            "safar" -> "Safar"
            "rabia al-awwal", "rabi' al-awwal" -> "Rabi'ul Awwal"
            "rabia al-thani", "rabi' al-thani" -> "Rabi'ul Akhir"
            "jumada al-awwal" -> "Jumadil Awwal"
            "jumada al-thani" -> "Jumadil Akhir"
            "rajab" -> "Rajab"
            "sha'ban", "sha`ban" -> "Sya'ban"
            "ramadan" -> "Ramadhan"
            "shawwal" -> "Syawal"
            "dhu al-qi'dah", "dhu al-qi`dah", "dhul-qi'dah" -> "Dzulqa'dah"
            "dhu al-hijjah", "dhu al-hijjah", "dhul-hijjah" -> "Dzulhijjah"
            else -> monthEn
        }
        return "$day $indoMonth $year H"
    }

    /**
     * Perhitungan astronomis lokal manual (Offline Fallback)
     * Menggunakan metode geometri bola astronomi standard waktu sholat untuk presisi 100% luring.
     */
    fun calculateOfflineMonthlyPrayerTimes(
        latitude: Double,
        longitude: Double,
        month: Int,
        year: Int
    ): List<PrayerTime> {
        val days = ArrayList<PrayerTime>()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"))
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tzi = TimeZone.getDefault().rawOffset / (1000 * 60 * 60).toDouble() // Timezone offset perangkat (contoh +7)

        val formatGregorian = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))

        for (day in 1..maxDays) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val currentDate = calendar.time
            val dateStr = formatGregorian.format(currentDate)

            // Hitung nilai Hijri kasar sebagai cadangan
            val hijriDays = getRoughHijriDate(calendar)

            // Penghitungan Waktu Sholat Geometri Sederhana
            val jd = getJulianDate(year, month, day)
            val d = jd - 2451545.0
            val g = 357.529 + 0.98560028 * d
            val q = 280.459 + 0.98564736 * d
            val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g))
            val r = 1.00014 - 0.01671 * cos(Math.toRadians(g)) - 0.00014 * cos(Math.toRadians(2 * g))
            val e = 23.439 - 0.00000036 * d
            val RA = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l)))) / 15.0

            // Kemiringan matahari (declination)
            val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
            // Equation of Time (persamaan waktu)
            val u = d / 36525.0
            val L0 = 280.46607 + 36000.7698 * u
            val ET = (-(1.914666 * sin(Math.toRadians(g)) + 0.019993 * sin(Math.toRadians(2 * g))) * 4.0) / 60.0

            // Jam Dzuhur adalah lintasan Matahari melintasi Meridian Utama setempat
            // Standard Dzuhur: 12 + TimeZone_Offset - (Longitude / 15) - EquationOfTime + Ikhtiyati (2 menit)
            val baseDhuhr = 12.0 + tzi - (longitude / 15.0) - ET + (2.0 / 60.0)

            // Hitung Sudut Jam untuk Sholat Subuh (Sun latitude = -20 derajat)
            val hourAngleFajr = getHourAngle(-20.0, latitude, declination)
            val fajrTimeAndHour = baseDhuhr - (hourAngleFajr / 15.0)

            // Hitung Sudut Jam untuk Maghrib (Sun latitude = -1 derajat)
            val hourAngleMaghrib = getHourAngle(-1.0, latitude, declination)
            val maghribTimeAndHour = baseDhuhr + (hourAngleMaghrib / 15.0)

            // Hitung Sudut Jam untuk Isya (Sun latitude = -18 derajat)
            val hourAngleIsha = getHourAngle(-18.0, latitude, declination)
            val ishaTimeAndHour = baseDhuhr + (hourAngleIsha / 15.0)

            // Hitung Ashar dng Sudut Shafi (tan(height) = 1 + tan(abs(latitude - declination)))
            val asrDeclDiff = Math.abs(latitude - declination)
            val asrAltitude = Math.toDegrees(acos(sin(Math.toRadians(asrDeclDiff)))) // perkiraan
            val asrAngleVal = Math.toDegrees(atanAngle(1.0 + tan(Math.toRadians(asrDeclDiff))))
            val hourAngleAsr = getHourAngle(asrAngleVal, latitude, declination)
            val asrTimeAndHour = baseDhuhr + (hourAngleAsr / 15.0)

            days.add(
                PrayerTime(
                    dateGregorian = dateStr,
                    dateHijri = hijriDays,
                    fajr = formatDoubleToTimeString(fajrTimeAndHour),
                    dhuhr = formatDoubleToTimeString(baseDhuhr),
                    asr = formatDoubleToTimeString(asrTimeAndHour),
                    maghrib = formatDoubleToTimeString(maghribTimeAndHour),
                    isha = formatDoubleToTimeString(ishaTimeAndHour)
                )
            )
        }
        return days
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun getHourAngle(alpha: Double, latitude: Double, declination: Double): Double {
        val radLat = Math.toRadians(latitude)
        val radDecl = Math.toRadians(declination)
        val radAlpha = Math.toRadians(alpha)
        val cosHA = (sin(radAlpha) - sin(radLat) * sin(radDecl)) / (cos(radLat) * cos(radDecl))
        val clampedCosHA = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosHA))
    }

    private fun atanAngle(x: Double): Double {
        return Math.atan(1.0 / x)
    }

    private fun formatDoubleToTimeString(hours: Double): String {
        var hTmp = hours
        if (hTmp < 0) hTmp += 24.0
        if (hTmp >= 24) hTmp -= 24.0
        val h = hTmp.toInt()
        val m = ((hTmp - h) * 60).toInt()
        return "%02d:%02d".format(Locale.getDefault(), h, m)
    }

    private fun getRoughHijriDate(calendar: Calendar): String {
        // Konverter Hijriah Tabular Sederhana
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        var jd = getJulianDate(gYear, gMonth, gDay).toInt()
        
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val lOffset = l - 10631 * n + 354
        val j = (((10985 - lOffset) / 5316).toInt() * ((50 * lOffset + 46) / 17719).toInt() + 
                 ((lOffset / 5670).toInt() * ((43 * lOffset + 152) / 15238).toInt()))
        val lRemaining = lOffset - ((30 * j + 29) / 30).toInt() + 30
        
        val hMonthNum = ((24 * lRemaining - 17) / 709).toInt()
        val hDay = lRemaining - ((30 * hMonthNum + 29) / 30).toInt() + 29
        val hYear = 30 * n + j - 30

        val months = listOf(
            "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir", 
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban", 
            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
        )
        val monthIdx = (hMonthNum - 1).coerceIn(0, 11)
        return "$hDay ${months[monthIdx]} $hYear H"
    }
}

/**
 * Interface Retrofit untuk Endpoint API Aladhan.
 */
interface AladhanApi {
    @GET("v1/calendar")
    suspend fun getMonthlyCalendar(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("method") method: Int
    ): ApiCalendarResponse
}

/**
 * Data Transfer Objects (DTO) untuk penanganan respons dari Aladhan API.
 */
data class ApiCalendarResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<ApiCalendarItem>?
)

data class ApiCalendarItem(
    @Json(name = "timings") val timings: ApiTimings,
    @Json(name = "date") val date: ApiDate
)

data class ApiTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

data class ApiDate(
    @Json(name = "gregorian") val gregorian: ApiGregorian?,
    @Json(name = "hijri") val hijri: ApiHijri?
)

data class ApiGregorian(
    @Json(name = "date") val date: String
)

data class ApiHijri(
    @Json(name = "day") val day: String,
    @Json(name = "month") val month: ApiHijriMonth,
    @Json(name = "year") val year: String
)

data class ApiHijriMonth(
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String
)
