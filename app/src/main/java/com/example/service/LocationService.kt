package id.ideahousetech.prayertime_qibla.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Service untuk menangkap titik lokasi GPS (latitude, longitude) secara realtime.
 * Menggunakan FusedLocationProviderClient dari Play Services Location dengan standard suspend coroutine.
 * Melakukan reverse-geocoding untuk mengubah koordinat menjadi nama Kelurahan/Kecamatan & Kota.
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Mengambil lokasi GPS aktif terakhir atau meminta pembaharuan lokasi secara realtime.
     * Menggunakan suspendCancellableCoroutine agar stabil dan aman dari thread leak.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationService", "Izin lokasi belum diberikan, membatalkan request GPS.")
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val cancellationTokenSource = CancellationTokenSource()
        continuation.invokeOnCancellation {
            try {
                cancellationTokenSource.cancel()
            } catch (e: Exception) {
                Log.e("LocationService", "Gagal membatalkan CancellationTokenSource", e)
            }
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    if (continuation.isActive) continuation.resume(task.result)
                } else {
                    try {
                        fusedLocationClient.lastLocation.addOnCompleteListener { lastTask ->
                            if (continuation.isActive) {
                                if (lastTask.isSuccessful && lastTask.result != null) {
                                    continuation.resume(lastTask.result)
                                } else {
                                    continuation.resume(null)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LocationService", "Gagal mengambil lastLocation: ${e.message}")
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Gagal mengambil lokasi GPS langsung: ${e.message}")
            if (continuation.isActive) continuation.resume(null)
        }
    }

    /**
     * Mengubah koordinat menjadi nama alamat administratif lengkap ("Kecamatan X, Kota Y").
     * Fallback apabila offline atau Geocoder gagal: koordinat GPS saat ini.
     */
    @Suppress("DEPRECATION")
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var addressResult = "Lokasi tidak diketahui"
                val addresses = suspendCancellableCoroutine<List<android.location.Address>> { continuation ->
                    try {
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            if (continuation.isActive) continuation.resume(addresses)
                        }
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resume(emptyList())
                    }
                }
                if (addresses.isNotEmpty()) {
                    val addr = addresses[0]
                    val district = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: ""
                    val city = addr.subAdminArea ?: addr.adminArea ?: ""
                    addressResult = formatAddressString(district, city)
                }
                addressResult
            } else {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val district = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: ""
                    val city = addr.subAdminArea ?: addr.adminArea ?: ""
                    formatAddressString(district, city)
                } else {
                    "Lokasi tidak diketahui"
                }
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Gagal memproses Geocoder: ${e.message}")
            "Lat: %.4f, Lon: %.4f".format(Locale.US, latitude, longitude)
        }
    }

    private fun formatAddressString(district: String, city: String): String {
        val clearDistrict = district.replace("Kecamatan ", "").replace("Kelurahan ", "").trim()
        val clearCity = city.replace("Kota ", "").replace("Kabupaten ", "").trim()
        
        return when {
            clearDistrict.isNotEmpty() && clearCity.isNotEmpty() -> "$clearDistrict, $clearCity"
            clearDistrict.isNotEmpty() -> clearDistrict
            clearCity.isNotEmpty() -> clearCity
            else -> "Lokasi tidak diketahui"
        }
    }
}
