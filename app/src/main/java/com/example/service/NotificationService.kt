package id.ideahousetech.prayertime_qibla.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Service untuk mengelola pemberitahuan lokal dan alarm adzan.
 * Menggunakan AlarmManager untuk presisi penjadwalan tepat waktu 5 kloter adzan harian.
 * Menghandle pemutaran file Mp3 adzan dengan fallback melodi sistem jika file tidak ada.
 */
class NotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var mediaPlayer: MediaPlayer? = null

    private fun releasePlayer() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: Exception) {
                Log.e("NotificationService", "Gagal menghentikan player: ${e.message}")
            }
            release()
        }
        mediaPlayer = null
    }

    fun playAdzanAudio(isFajr: Boolean) {
        try {
            stopAdzanAudio() // yakinkan dibersihkan terlebih dahulu

            val prefs = SecurePrefs.get(context)
            val isAlarmEnabled = prefs.getBoolean("enable_adzan_alarm", true)
            if (!isAlarmEnabled) {
                Log.d("NotificationService", "Alarm adzan dinonaktifkan di pengaturan.")
                return
            }

            val audioFileName = if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
            val file = File(context.filesDir, audioFileName)

            val prefKey = if (isFajr) "custom_adzan_fajr_name" else "custom_adzan_name"
            val hasCustom = prefs.getString(prefKey, null) != null

            mediaPlayer = MediaPlayer().apply {
                if (file.exists() && (hasCustom || file.length() > 30000)) { // Jika file asli yang valid ada atau kustom ada
                    setDataSource(file.absolutePath)
                } else {
                    // Fallback ke ringtone default Alarm perangkat jika dummy file
                    val notificationUri: Uri = Uri.parse("android.resource://" + context.packageName + "/raw/" + (if (isFajr) "adzan_fajr" else "adzan"))
                    try {
                        setDataSource(context, notificationUri)
                    } catch (e: Exception) {
                        // Total fallback
                        val defaultUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                        setDataSource(context, defaultUri)
                    }
                }
                prepare()
                isLooping = false
                setOnCompletionListener {
                    Log.d("NotificationService", "Adzan selesai diputar, melakukan pembersihan player.")
                    releasePlayer()
                }
                start()
            }

            // Hentikan adzan secara otomatis setelah 60 detik agar tidak bersuara terus-terusan
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                stopAdzanAudio()
            }, 60000)

        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal memutar suara adzan: ${e.message}")
        }
    }

    fun stopAdzanAudio() {
        try {
            releasePlayer()
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal melepaskan MediaPlayer: ${e.message}")
        }
    }

    companion object {
        const val CHANNEL_ID = "islamic_prayer_alarms"
        const val CHANNEL_NAME = "Jadwal Waktu Sholat & Adzan"
        const val ACTION_PLAY_ADZAN = "id.ideahousetech.prayertime_qibla.ACTION_PLAY_ADZAN"
        const val ACTION_STOP_ADZAN = "id.ideahousetech.prayertime_qibla.ACTION_STOP_ADZAN"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_IS_FAJR = "is_fajr"

        @Volatile
        private var instance: NotificationService? = null

        fun getInstance(context: Context): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannel()
        copyAssetAudioFilesIfNeeded()
    }

    /**
     * Membuat Notification Channel untuk Android O (API 26) ke atas.
     * Mengatur kepentingan tinggi agar memicu tampilan pop-up / suara di layar kunci.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran untuk menghantar alarm adzan tepat sholat"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Menyalin file audio dummy dari asset ke memori internal jika belum ditempatkan oleh pengguna,
     * untuk mencegah aplikasi crash saat mencoba membaca audio.
     */
    private fun copyAssetAudioFilesIfNeeded() {
        try {
            val filesToCopy = listOf("adzan.mp3", "adzan_fajr.mp3")
            for (fileName in filesToCopy) {
                val destFile = File(context.filesDir, fileName)
                val isDummy = destFile.exists() && destFile.length() < 50000
                if (!destFile.exists() || isDummy) {
                    context.assets.open(fileName).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("NotificationService", "Tersalin dari asset: $fileName (ukuran: ${destFile.length()} bytes)")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Aset adzan asli belum tersedia atau gagal disalin: ${e.message}")
            // Buat file synthetic dummy hanya jika destFile tidak ada sama sekali
            createSyntheticMp3Placeholder("adzan.mp3")
            createSyntheticMp3Placeholder("adzan_fajr.mp3")
        }
    }

    private fun createSyntheticMp3Placeholder(name: String) {
        try {
            val destFile = File(context.filesDir, name)
            if (!destFile.exists()) {
                // WAV 1 detik berisikan melodi tenang
                destFile.writeBytes(getDummyWavBytes())
                Log.d("NotificationService", "Membuat generator dummy audio: $name")
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal menulis file dummy suara: ${e.message}")
        }
    }

    /**
     * Membuat file WAV sinusoidal berjarak 1 detik sebagai audio pengganti
     */
    private fun getDummyWavBytes(): ByteArray {
        val header = ByteArray(44)
        val sampleRate = 8000
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val totalAudioLen = 16000 // 1 detik
        val totalDataLen = totalAudioLen + 36

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // format subchunk size
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // audio format PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val audioData = ByteArray(totalAudioLen)
        for (i in 0 until totalAudioLen / 2) {
            val frequency = 440.0 // A4 note
            val value = (Math.sin(2.0 * Math.PI * frequency * i / sampleRate) * 32767).toInt()
            audioData[2 * i] = (value and 0xff).toByte()
            audioData[2 * i + 1] = ((value shr 8) and 0xff).toByte()
        }
        return header + audioData
    }

    /**
     * Menjadwalkan alarm otomatis untuk 5 waktu sholat pada tanggal aktif.
     * Dipanggil kembali setiap kali lokasi berubah atau tanggal berganti.
     */
    fun scheduleDailyAlarms(times: PrayerTime) {
        cancelAllScheduledAlarms()

        val sdf = SimpleDateFormat("dd-MM-yyyy EEEE", Locale.US)
        val todayStr = sdf.format(Date())

        val prayerMap = mapOf(
            "Subuh" to Pair(times.fajr, true),
            "Dzuhur" to Pair(times.dhuhr, false),
            "Ashar" to Pair(times.asr, false),
            "Maghrib" to Pair(times.maghrib, false),
            "Isya" to Pair(times.isha, false)
        )

        var alarmIndex = 1
        for ((name, data) in prayerMap) {
            val (timeStr, isFajr) = data
            try {
                val parts = timeStr.split(":")
                val hour = parts[0].toInt()
                val min = parts[1].toInt()

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Jika waktu sholat hari ini telah lewat, jadwalkan untuk besok hari
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_PLAY_ADZAN
                    putExtra(EXTRA_PRAYER_NAME, name)
                    putExtra(EXTRA_IS_FAJR, isFajr)
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmIndex,
                    intent,
                    flags
                )

                // Jadwalkan alarm dengan presisi eksekusi latar belakang (Exact Alarm)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }

                Log.d("NotificationService", "Terjadwal alarm $name pukul ${timeStr} pada milidetik: ${calendar.timeInMillis}")
                alarmIndex++
            } catch (e: Exception) {
                Log.e("NotificationService", "Gagal menjadwalkan alarm untuk sholat $name: ${e.message}")
            }
        }
    }

    private fun cancelAllScheduledAlarms() {
        // Membersihkan alarm sebelumnya agar tidak tumpang tindih
        for (i in 1..5) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_PLAY_ADZAN
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }
            val pendingIntent = PendingIntent.getBroadcast(context, i, intent, flags)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}

/**
 * BroadcastReceiver untuk menangkap pemicu AlarmManager waktu sholat.
 * Ketika dipicu, meluncurkan pemberitahuan kepala (heads-up notification)
 * dan memutar suara adzan yang sesuai.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationService.ACTION_PLAY_ADZAN) {
            val prayerName = intent.getStringExtra(NotificationService.EXTRA_PRAYER_NAME) ?: "Sholat"
            val isFajr = intent.getBooleanExtra(NotificationService.EXTRA_IS_FAJR, false)

            Log.d("AlarmReceiver", "Alarm Berbunyi! Waktu Sholat Tiba: $prayerName")

            // 1. Tampilkan Notifikasi dengan Tombol Aksi Hentikan Adzan
            showHeadsUpNotification(context, prayerName, isFajr)

            // 2. Putar Suara Adzan
            val service = NotificationService.getInstance(context)
            service.playAdzanAudio(isFajr)
        } else if (intent.action == NotificationService.ACTION_STOP_ADZAN) {
            Log.d("AlarmReceiver", "Aksi stop adzan ditekan.")
            val service = NotificationService.getInstance(context)
            service.stopAdzanAudio()
            
            // Hapus notifikasi setelah ditekan hentikan
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001)
        }
    }

    private fun showHeadsUpNotification(context: Context, prayerName: String, isFajr: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent untuk langsung membuka aplikasi dari notifikasi
        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Intent untuk menghentikan adzan
        val stopAdzanIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationService.ACTION_STOP_ADZAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            99,
            stopAdzanIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val textTitle = "Waktu Sholat $prayerName Tiba"
        val textBody = "Mari dirikan sholat tepat waktu. Klik untuk membuka aplikasi atau ketuk tombol Hentikan Adzan."

        val builder = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(textTitle)
            .setContentText(textBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "HENTIKAN ADZAN",
                stopPendingIntent
            )
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(1001, builder.build())
    }
}
