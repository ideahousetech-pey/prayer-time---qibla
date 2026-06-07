package id.ideahousetech.prayertime_qibla.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositori mandiri untuk menjembatani Database DAO dengan ViewModel.
 * Menyediakan pemanduan data flow reaktif hulu-hilir, kalkulasi streak secara luring,
 * serta integrasi logging analitis sederhana.
 */
class PrayerTrackerRepository(private val dao: PrayerTrackerDao) {

    /**
     * Aliran flow reaktif jadwal tracker untuk tanggal tertentu ("yyyy-MM-dd").
     */
    fun getTrackerFlowForDate(date: String): Flow<PrayerTracker?> {
        return dao.getTrackerFlowForDate(date)
    }

    /**
     * Memperoleh semua data tracker dalam bentuk Flow.
     */
    fun getAllTrackersFlow(): Flow<List<PrayerTracker>> {
        return dao.getAllTrackersFlow()
    }

    /**
     * Memperoleh tracker untuk bulan tertentu, misalnya "2026-06-%".
     */
    fun getTrackersFlowForMonth(monthQuery: String): Flow<List<PrayerTracker>> {
        return dao.getTrackersFlowForMonth(monthQuery)
    }

    /**
     * Menyimpan atau memperbarui data tracker sholat.
     */
    suspend fun saveOrUpdateTracker(tracker: PrayerTracker) {
        withContext(Dispatchers.IO) {
            dao.insertOrUpdate(tracker)
            // Sistem Logger Analitis Event
            logAnalyticsUpdate(tracker)
        }
    }

    /**
     * Ambil data langsung tanpa flow (sinkron/one-shot).
     */
    suspend fun getTrackerForDateDirect(date: String): PrayerTracker? {
        return withContext(Dispatchers.IO) {
            dao.getTrackerForDate(date)
        }
    }

    /**
     * Menghapus catatan pelacak hari tertentu.
     */
    suspend fun deleteTracker(date: String) {
        withContext(Dispatchers.IO) {
            dao.deleteTracker(date)
        }
    }

    /**
     * Melakukan analisis analitis sederhana (Analytics) untuk melacak kemajuan habit user secara luring.
     */
    private fun logAnalyticsUpdate(tracker: PrayerTracker) {
        val completedPrayers = listOf(
            "Subuh" to tracker.subuhStatus,
            "Dzuhur" to tracker.dhuhrStatus,
            "Ashar" to tracker.asrStatus,
            "Maghrib" to tracker.maghribStatus,
            "Isya" to tracker.isyaStatus
        ).filter { it.second != "None" }
        
        android.util.Log.d(
            "PrayerTrackerAnalytics",
            "EVENT_HABIT_UPDATE: Date=${tracker.date} | CompletedCount=${completedPrayers.size} | Detail=$completedPrayers"
        )
    }
}
