package id.ideahousetech.prayertime_qibla.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "prayer_cache", primaryKeys = ["year", "month"])
data class PrayerTimeCache(
    val year: Int,
    val month: Int,
    val latitude: Double,
    val longitude: Double,
    val jsonData: String,       // JSON serialized List<PrayerTime>
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM prayer_cache WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getCache(year: Int, month: Int): PrayerTimeCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: PrayerTimeCache)

    @Query("DELETE FROM prayer_cache WHERE cachedAt < :expiry")
    suspend fun clearExpiredCache(expiry: Long)
}

@Database(entities = [PrayerTimeCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerCacheDao(): PrayerCacheDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prayer_qibla_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
