package id.ideahousetech.prayertime_qibla.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
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

class NotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val stopHandler = Handler(Looper.getMainLooper())
    private val isAudioPlaying = java.util.concurrent.atomic.AtomicBoolean(false)

    companion object {
        const val CHANNEL_ID       = "islamic_prayer_alarms"
        const val CHANNEL_NAME     = "Jadwal Waktu Sholat & Adzan"
        const val ACTION_PLAY_ADZAN  = "id.ideahousetech.prayertime_qibla.ACTION_PLAY_ADZAN"
        const val ACTION_STOP_ADZAN  = "id.ideahousetech.prayertime_qibla.ACTION_STOP_ADZAN"
        const val ACTION_PRE_REMINDER = "id.ideahousetech.prayertime_qibla.ACTION_PRE_REMINDER"
        const val EXTRA_PRAYER_NAME  = "prayer_name"
        const val EXTRA_IS_FAJR      = "is_fajr"

        // Batas maksimal durasi adzan yang diputar (5 menit)
        private const val MAX_ADZAN_DURATION_MS = 5 * 60 * 1000L

        @Volatile private var instance: NotificationService? = null

        fun getInstance(context: Context): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService(context.applicationContext)
                    .also { instance = it }
            }
        }
    }

    init {
        createNotificationChannel()
        copyAssetAudioFilesIfNeeded()
    }

    // ── Audio Focus ──────────────────────────────────────────────────────────

    /**
     * Meminta audio focus sebelum memutar adzan.
     * Ini menghentikan atau me-mute musik/media lain yang sedang berjalan.
     */
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    // Hentikan adzan jika focus diambil paksa oleh app lain
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopAdzanAudio()
                    }
                }
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /** Melepaskan audio focus setelah adzan selesai */
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        audioFocusRequest = null
    }

    // ── WakeLock ─────────────────────────────────────────────────────────────

    /**
     * Acquire WakeLock agar CPU tidak tidur saat audio diputar.
     * Otomatis release setelah MAX_ADZAN_DURATION_MS + 10 detik buffer.
     */
    private fun acquireWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PrayerTimes:AdzanWakeLock"
            ).apply {
                acquire(MAX_ADZAN_DURATION_MS + 10_000L)
            }
            Log.d("NotificationService", "WakeLock acquired")
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal acquire WakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null
            Log.d("NotificationService", "WakeLock released")
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal release WakeLock: ${e.message}")
        }
    }

    // ── MediaPlayer ──────────────────────────────────────────────────────────

    private fun releasePlayer() {
        if (!isAudioPlaying.compareAndSet(true, false)) {
            // Sudah dirilis atau tidak sedang memutar adzan
            return
        }

        stopHandler.removeCallbacksAndMessages(null)
        
        // 1. Set mediaPlayer ke null terlebih dahulu untuk menghindari race condition
        val playerToRelease = mediaPlayer
        mediaPlayer = null
        
        try {
            playerToRelease?.apply {
                try {
                    if (isPlaying) {
                        stop()
                    }
                } catch (_: Exception) {}
                reset()
                release()
            }
        } finally {
            // 3. Pastikan WakeLock dan AudioFocus SELALU dirilis di dalam finally block
            releaseAudioFocus()
            releaseWakeLock()
        }
    }

    /**
     * Memutar audio adzan dengan sistem berlapis:
     * 1. File MP3 valid di filesDir (tersalin dari assets atau custom)
     * 2. Langsung dari assets (jika ada file valid)
     * 3. Ringtone alarm sistem (fallback terakhir)
     *
     * Menggunakan prepareAsync() agar tidak memblok main thread.
     */
    fun playAdzanAudio(isFajr: Boolean) {
        try {
            releasePlayer()

            val prefs = SecurePrefs.get(context)
            if (!prefs.getBoolean("enable_adzan_alarm", true)) {
                Log.d("NotificationService", "Alarm adzan dinonaktifkan")
                return
            }

            // Minta audio focus dulu — jika ditolak, adzan tetap diputar (alarm harus berbunyi)
            val focusGranted = requestAudioFocus()
            Log.d("NotificationService", "Audio focus: ${if (focusGranted) "granted" else "denied, lanjut tetap putar"}")

            // Acquire WakeLock agar CPU tidak tidur
            acquireWakeLock()

            val audioFileName = if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"

            val player = MediaPlayer()
            
            // Set flag bermain menjadi true dan assign ke global instance
            isAudioPlaying.set(true)
            mediaPlayer = player

            // Atur stream audio ke STREAM_ALARM untuk bypass silent mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                player.setAudioStreamType(AudioManager.STREAM_ALARM)
            }

            // Tentukan sumber audio
            var sourceSet = false

            // 1. Coba file di filesDir (harus > 100KB agar valid)
            val localFile = File(context.filesDir, audioFileName)
            if (localFile.exists() && localFile.length() > 100_000) {
                try {
                    player.setDataSource(localFile.absolutePath)
                    sourceSet = true
                    Log.d("NotificationService", "Sumber: file lokal ($audioFileName, ${localFile.length()} bytes)")
                } catch (e: Exception) {
                    player.reset()
                    Log.e("NotificationService", "Gagal dari file lokal: ${e.message}")
                }
            }

            // 2. Coba dari assets langsung (harus > 100KB)
            if (!sourceSet) {
                try {
                    val afd = context.assets.openFd(audioFileName)
                    if (afd.length > 100_000) {
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        sourceSet = true
                        Log.d("NotificationService", "Sumber: assets ($audioFileName, ${afd.length} bytes)")
                    }
                    afd.close()
                } catch (e: Exception) {
                    try { player.reset() } catch (_: Exception) {}
                    Log.e("NotificationService", "Gagal dari assets: ${e.message}")
                }
            }

            // 3. Fallback ke ringtone alarm sistem
            if (!sourceSet) {
                try {
                    val alarmUri = android.media.RingtoneManager.getDefaultUri(
                        android.media.RingtoneManager.TYPE_ALARM
                    )
                    player.setDataSource(context, alarmUri)
                    sourceSet = true
                    Log.d("NotificationService", "Sumber: system alarm ringtone (fallback)")
                } catch (e: Exception) {
                    try { player.reset() } catch (_: Exception) {}
                    Log.e("NotificationService", "Gagal dari ringtone sistem: ${e.message}")
                }
            }

            if (!sourceSet) {
                Log.e("NotificationService", "Semua sumber audio gagal, adzan tidak bisa diputar")
                releasePlayer()
                return
            }

            // Gunakan prepareAsync() — tidak memblok main thread
            player.isLooping = false

            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    Log.d("NotificationService", "Adzan mulai diputar")

                    // Auto-stop setelah MAX_ADZAN_DURATION_MS
                    stopHandler.postDelayed({
                        Log.d("NotificationService", "Auto-stop adzan setelah ${MAX_ADZAN_DURATION_MS / 1000}s")
                        stopAdzanAudio()
                    }, MAX_ADZAN_DURATION_MS)
                } catch (e: Exception) {
                    Log.e("NotificationService", "Gagal start player setelah prepare: ${e.message}")
                    releasePlayer()
                }
            }

            player.setOnCompletionListener {
                Log.d("NotificationService", "Adzan selesai diputar")
                releasePlayer()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("NotificationService", "MediaPlayer error: what=$what extra=$extra")
                releasePlayer()
                true
            }

            player.prepareAsync()  // NON-BLOCKING — tidak blok main thread

        } catch (e: Exception) {
            Log.e("NotificationService", "Error playAdzanAudio: ${e.message}")
            releasePlayer()
        }
    }

    fun stopAdzanAudio() {
        try {
            releasePlayer()
            Log.d("NotificationService", "Adzan dihentikan")
        } catch (e: Exception) {
            Log.e("NotificationService", "Error stopAdzanAudio: ${e.message}")
        }
    }

    // ── Notification Channel ─────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm adzan tepat waktu sholat"
                enableLights(true)
                enableVibration(true)
                // Penting: set sound null di channel karena MediaPlayer yang handle audio
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // ── Asset Copy ───────────────────────────────────────────────────────────

    /**
     * Menyalin file audio dari assets ke filesDir.
     * Hanya salin jika file assets VALID (> 100KB).
     * Jika assets tidak valid, biarkan filesDir kosong — playAdzanAudio
     * akan langsung fallback ke ringtone sistem.
     */
    private fun copyAssetAudioFilesIfNeeded() {
        val filesToCopy = listOf("adzan.mp3", "adzan_fajr.mp3")
        for (fileName in filesToCopy) {
            val destFile = File(context.filesDir, fileName)

            // Cek apakah file assets valid sebelum disalin
            try {
                val afd = context.assets.openFd(fileName)
                val assetSize = afd.length
                afd.close()

                // Jika file tujuan sudah ada dan berukuran sama persis dengan asset (artinya identik/sudah terupdate), silakan skip
                if (destFile.exists() && destFile.length() == assetSize && assetSize > 100_000) {
                    Log.d("NotificationService", "$fileName sudah ada dan identik dengan assets di filesDir")
                    continue
                }

                if (assetSize > 100_000) {
                    // File assets valid — salin ke filesDir
                    context.assets.open(fileName).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("NotificationService", "Tersalin/Terupdate: $fileName (${destFile.length()} bytes)")
                } else {
                    // File assets tidak valid (terlalu kecil) — jangan salin
                    Log.w("NotificationService",
                        "$fileName di assets tidak valid (${assetSize} bytes), " +
                        "akan menggunakan ringtone sistem sebagai fallback")
                    if (destFile.exists()) destFile.delete() // Hapus file lama yang tidak valid
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Tidak bisa akses/salin assets/$fileName: ${e.message}")
                // Sebagai fallback, jika file sudah ada > 100KB, biarkan saja
                if (destFile.exists() && destFile.length() > 100_000) {
                    Log.d("NotificationService", "Fallback menggunakan file lokal yang sudah ada: $fileName")
                }
            }
        }
    }

    // ── Alarm Scheduling ─────────────────────────────────────────────────────

    fun scheduleDailyAlarms(times: PrayerTime) {
        cancelAllScheduledAlarms()

        val prayerMap = mapOf(
            "Subuh"   to Pair(times.fajr,    true),
            "Dzuhur"  to Pair(times.dhuhr,   false),
            "Ashar"   to Pair(times.asr,     false),
            "Maghrib" to Pair(times.maghrib, false),
            "Isya"    to Pair(times.isha,    false)
        )

        var alarmIndex = 1
        for ((name, data) in prayerMap) {
            val (timeStr, isFajr) = data
            try {
                val parts  = timeStr.split(":")
                val hour   = parts[0].toInt()
                val minute = parts[1].toInt()

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                // Jadwalkan pre-reminder 15 menit sebelum sholat
                val preCalendar = (calendar.clone() as Calendar).apply {
                    add(Calendar.MINUTE, -15)
                }
                if (preCalendar.timeInMillis > System.currentTimeMillis()) {
                    scheduleExactAlarm(
                        requestCode = alarmIndex + 100,
                        triggerAtMs = preCalendar.timeInMillis,
                        intent = Intent(context, AlarmReceiver::class.java).apply {
                            action = ACTION_PRE_REMINDER
                            putExtra(EXTRA_PRAYER_NAME, name)
                        }
                    )
                    Log.d("NotificationService", "Pre-reminder $name dijadwalkan")
                }

                // Jadwalkan alarm utama adzan
                scheduleExactAlarm(
                    requestCode = alarmIndex,
                    triggerAtMs = calendar.timeInMillis,
                    intent = Intent(context, AlarmReceiver::class.java).apply {
                        action = ACTION_PLAY_ADZAN
                        putExtra(EXTRA_PRAYER_NAME, name)
                        putExtra(EXTRA_IS_FAJR, isFajr)
                    }
                )
                Log.d("NotificationService", "Alarm $name dijadwalkan pukul $timeStr")
                alarmIndex++

            } catch (e: Exception) {
                Log.e("NotificationService", "Gagal jadwalkan $name: ${e.message}")
            }
        }
    }

    private fun scheduleExactAlarm(requestCode: Int, triggerAtMs: Long, intent: Intent) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                    )
                } else {
                    // Exact alarm tidak diizinkan — gunakan inexact sebagai fallback
                    // (bisa telat 5-15 menit, tapi lebih baik daripada tidak sama sekali)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                    )
                    Log.w("NotificationService",
                        "PERINGATAN: Exact alarm tidak diizinkan. " +
                        "Alarm mungkin tidak tepat waktu. " +
                        "Minta user buka Pengaturan > Aplikasi > Izin Khusus > Alarm & Pengingat")
                }
            }
            else -> alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
            )
        }
    }

    private fun cancelAllScheduledAlarms() {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_NO_CREATE

        for (i in 1..5) {
            listOf(i, i + 100).forEach { code ->
                val action = if (code <= 5) ACTION_PLAY_ADZAN else ACTION_PRE_REMINDER
                val pi = PendingIntent.getBroadcast(
                    context, code,
                    Intent(context, AlarmReceiver::class.java).apply { this.action = action },
                    flags
                )
                pi?.let { alarmManager.cancel(it); it.cancel() }
            }
        }
    }
}

// ── AlarmReceiver ─────────────────────────────────────────────────────────────

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationService.ACTION_PLAY_ADZAN -> {
                val prayerName = intent.getStringExtra(NotificationService.EXTRA_PRAYER_NAME) ?: "Sholat"
                val isFajr    = intent.getBooleanExtra(NotificationService.EXTRA_IS_FAJR, false)
                Log.d("AlarmReceiver", "Alarm: $prayerName (fajr=$isFajr)")

                // Tampilkan notifikasi heads-up
                showHeadsUpNotification(context, prayerName)

                // Putar adzan (prepareAsync — tidak blok main thread)
                NotificationService.getInstance(context).playAdzanAudio(isFajr)
            }
            NotificationService.ACTION_PRE_REMINDER -> {
                val prayerName = intent.getStringExtra(NotificationService.EXTRA_PRAYER_NAME) ?: "Sholat"
                showPreReminderNotification(context, prayerName)
            }
            NotificationService.ACTION_STOP_ADZAN -> {
                NotificationService.getInstance(context).stopAdzanAudio()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .cancel(1001)
            }
        }
    }

    private fun showHeadsUpNotification(context: Context, prayerName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openPi = PendingIntent.getActivity(
            context, 0,
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val stopPi = PendingIntent.getBroadcast(
            context, 99,
            Intent(context, AlarmReceiver::class.java).apply {
                action = NotificationService.ACTION_STOP_ADZAN
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 Waktu Sholat $prayerName")
            .setContentText("Mari tegakkan sholat tepat waktu.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openPi)
            .addAction(android.R.drawable.ic_media_pause, "Hentikan Adzan", stopPi)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(openPi, true)  // Tampil di lockscreen
            .build()

        nm.notify(1001, notification)
    }

    private fun showPreReminderNotification(context: Context, prayerName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openPi = PendingIntent.getActivity(
            context, 120,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $prayerName dalam 15 menit")
            .setContentText("Segera persiapkan diri untuk sholat $prayerName.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openPi)
            .setAutoCancel(true)
            .build()

        nm.notify(1002, notification)
    }
}
