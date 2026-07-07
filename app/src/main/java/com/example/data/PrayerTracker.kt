package id.ideahousetech.prayertime_qibla.data

import androidx.room.*
import id.ideahousetech.prayertime_qibla.model.PrayerStatus
import kotlinx.coroutines.flow.Flow

/**
 * Entity untuk melacak aktivitas ibadah sholat harian 5 waktu.
 * Dilengkapi dengan pelacakan tipe penegakan sholat demi tercapainya target habit-building spiritual yang presisi.
 */
@Entity(tableName = "prayer_tracker")
data class PrayerTracker(
    @PrimaryKey
    val date: String, // Format: "yyyy-MM-dd"
    val subuhStatus: PrayerStatus = PrayerStatus.NONE,
    val dhuhrStatus: PrayerStatus = PrayerStatus.NONE,
    val asrStatus: PrayerStatus = PrayerStatus.NONE,
    val maghribStatus: PrayerStatus = PrayerStatus.NONE,
    val isyaStatus: PrayerStatus = PrayerStatus.NONE
) {
    /**
     * Menghitung persentase sholat yang terlaksana untuk hari ini (di luar status Halangan).
     */
    fun calculateCompletionPercentage(): Float {
        val activePrayers = listOf(subuhStatus, dhuhrStatus, asrStatus, maghribStatus, isyaStatus)
        val validPrayers = activePrayers.filter { it != PrayerStatus.HALANGAN }
        if (validPrayers.isEmpty()) return 100f // Jiwa suci dari kewajiban sholat terhitung full compliance
        
        val completed = validPrayers.count { it != PrayerStatus.NONE }
        return (completed.toFloat() / validPrayers.size.toFloat()) * 100f
    }

    /**
     * Cek apakah seluruh sholat hari ini sudah tuntas terisi secara sah (termasuk udzur syar'i / halangan).
     */
    fun isFullyCompleted(): Boolean {
        val list = listOf(subuhStatus, dhuhrStatus, asrStatus, maghribStatus, isyaStatus)
        return list.none { it == PrayerStatus.NONE }
    }
}

/**
 * Data Access Object untuk operasi luring lincah dan reaktif data pelacak sholat harian.
 */
@Dao
interface PrayerTrackerDao {
    @Query("SELECT * FROM prayer_tracker WHERE date = :date LIMIT 1")
    suspend fun getTrackerForDate(date: String): PrayerTracker?

    @Query("SELECT * FROM prayer_tracker WHERE date = :date")
    fun getTrackerFlowForDate(date: String): Flow<PrayerTracker?>

    @Query("SELECT * FROM prayer_tracker ORDER BY date DESC")
    fun getAllTrackersFlow(): Flow<List<PrayerTracker>>

    @Query("SELECT * FROM prayer_tracker WHERE date LIKE :monthQuery ORDER BY date ASC")
    fun getTrackersFlowForMonth(monthQuery: String): Flow<List<PrayerTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tracker: PrayerTracker)

    @Query("DELETE FROM prayer_tracker WHERE date = :date")
    suspend fun deleteTracker(date: String)

    @Query("DELETE FROM prayer_tracker")
    suspend fun clearAllTrackers()
}
