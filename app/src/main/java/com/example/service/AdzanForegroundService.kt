package id.ideahousetech.prayertime_qibla.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import id.ideahousetech.prayertime_qibla.MainActivity
import id.ideahousetech.prayertime_qibla.R

/**
 * Foreground Service untuk memastikan alarm adzan tidak di-kill
 * oleh sistem Android, terutama di MIUI (Xiaomi), ColorOS (OPPO/Realme),
 * dan One UI (Samsung) yang agresif membunuh background process.
 */
class AdzanForegroundService : Service() {

    companion object {
        const val CHANNEL_ID_FG = "adzan_foreground_channel"
        const val NOTIFICATION_ID_FG = 1002
        const val ACTION_START = "id.ideahousetech.prayertime_qibla.START_FOREGROUND"
        const val ACTION_STOP  = "id.ideahousetech.prayertime_qibla.STOP_FOREGROUND"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundWithNotification()
            ACTION_STOP  -> stopSelf()
        }
        return START_STICKY   // Restart otomatis jika di-kill
    }

    private fun startForegroundWithNotification() {
        createForegroundChannel()
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_FG)
            .setContentTitle("Waktu Sholat & Adzan")
            .setContentText("Layanan aktif mendengarkan jadwal sholat...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID_FG,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID_FG, notification)
        }
    }

    private fun createForegroundChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_FG, "Adzan Aktif",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Menjaga alarm adzan tetap aktif" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
