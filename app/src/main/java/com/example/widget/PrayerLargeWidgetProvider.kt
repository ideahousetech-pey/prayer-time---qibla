package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * BroadcastReceiver Provider untuk widget Waktu Sholat ukuran Large (4x4).
 */
class PrayerLargeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        PrayerWidgetHelper.updateLargeWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val term = android.content.ComponentName(context, PrayerLargeWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(term)
        if (appWidgetIds.isNotEmpty()) {
            PrayerWidgetHelper.updateLargeWidgets(context, appWidgetManager, appWidgetIds)
        }
    }
}
