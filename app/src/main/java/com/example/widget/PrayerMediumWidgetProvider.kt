package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

/**
 * BroadcastReceiver Provider untuk widget Waktu Sholat ukuran Medium (4x2).
 */
class PrayerMediumWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        PrayerWidgetHelper.updateMediumWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val term = android.content.ComponentName(context, PrayerMediumWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(term)
        if (appWidgetIds.isNotEmpty()) {
            PrayerWidgetHelper.updateMediumWidgets(context, appWidgetManager, appWidgetIds)
        }
    }
}
