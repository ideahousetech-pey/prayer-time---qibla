package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Utilitas helper untuk preferences berenkripsi (SecurePrefs).
 * Jika inisiasi EncryptedSharedPreferences gagal (misal karena KeyStore corrupt di OS khusus),
 * fungsi ini akan otomatis melakukan aman fallback (fallback ke standard SharedPreferences biasa).
 */
object SecurePrefs {
    private const val PREFS_NAME = "adzan_secure_prefs"

    fun get(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Hapus log error sensitif / Log.e. Gunakan fallback aman secara senyap.
            // Inisiasi fallback SharedPreferences standar agar aplikasi tidak crash.
            context.getSharedPreferences(PREFS_NAME + "_plain_fallback", Context.MODE_PRIVATE)
        }
    }
}
