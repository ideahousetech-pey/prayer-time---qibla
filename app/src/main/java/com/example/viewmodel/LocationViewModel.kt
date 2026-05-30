package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola titik integrasi GPS perangkat.
 * Mengotomatisasi izin baca koordinat, caching SharedPreferences,
 * serta memperbarui state nama lokasi ("Kecamatan X, Kota Y") secara realtime.
 */
class LocationViewModel(context: Context) : ViewModel() {

    private val locationService = LocationService(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("user_location_cache", Context.MODE_PRIVATE)

    // State lokasi koordinat
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    // State nama wilayah administrative (format: "Menteng, Jakarta Pusat")
    private val _locationName = MutableStateFlow("Menunggu GPS...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    // State loading status lokasi
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Memuat lokasi cadangan terakhir dari shared_preference agar UI langsung terisi tanpa menunggu GPS lambat
        loadCachedLocation()
    }

    /**
     * Memperoleh lokasi perangkat terkini secara realtime menggunakan FusedLocationProviderClient.
     * Setelah koordinat didapat, memicu reverse-geocoding untuk memperbarui nama lokasi,
     * lalu menyimpan properti baru tersebut di SharedPreferences.
     */
    fun refreshLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            val loc = locationService.getCurrentLocation()
            if (loc != null) {
                _userLocation.value = loc
                val address = locationService.getAddressFromLocation(loc.latitude, loc.longitude)
                _locationName.value = address
                saveLocationToCache(loc.latitude, loc.longitude, address)
            } else {
                // Gunakan cache jika pembacaan GPS gagal
                loadCachedLocation()
            }
            _isLoading.value = false
        }
    }

    /**
     * Menyimpan koordinat terakhir dan alamat teks ke Shared Preferences.
     */
    private fun saveLocationToCache(lat: Double, lon: Double, address: String) {
        prefs.edit().apply {
            putFloat("cached_lat", lat.toFloat())
            putFloat("cached_lon", lon.toFloat())
            putString("cached_address", address)
            apply()
        }
    }

    /**
     * Memuat koordinat cadangan dari Shared Preferences.
     * Default fallback ke Koordinat Jakarta Pusat jika cache kosong.
     */
    private fun loadCachedLocation() {
        val lat = prefs.getFloat("cached_lat", -6.175115f).toDouble() // Monas Jakarta
        val lon = prefs.getFloat("cached_lon", 106.827157f).toDouble()
        val address = prefs.getString("cached_address", "Menteng, Jakarta Pusat") ?: "Menteng, Jakarta Pusat"

        val mockLoc = Location("cached").apply {
            latitude = lat
            longitude = lon
        }
        _userLocation.value = mockLoc
        _locationName.value = address
    }
}
