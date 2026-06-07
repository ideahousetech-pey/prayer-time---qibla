package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * BroadcastReceiver Provider untuk widget Waktu Sholat ukuran Small (2x2).
 */
class PrayerSmallWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Lakukan pembaharuan visual render widget luring
        PrayerWidgetHelper.updateSmallWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        // Menerima signal update manual jika ada kustom update intent
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val term = android.content.ComponentName(context, PrayerSmallWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(term)
        if (appWidgetIds.isNotEmpty()) {
            PrayerWidgetHelper.updateSmallWidgets(context, appWidgetManager, appWidgetIds)
        }
    }
}
