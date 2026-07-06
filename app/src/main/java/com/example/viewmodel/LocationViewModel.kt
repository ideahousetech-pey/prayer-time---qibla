package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.service.LocationService
import id.ideahousetech.prayertime_qibla.utils.getDouble
import id.ideahousetech.prayertime_qibla.utils.putDouble
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

    private val appContext = context.applicationContext
    private val locationService = LocationService(appContext)
    private val prefs: SharedPreferences = appContext.getSharedPreferences("user_location_cache", Context.MODE_PRIVATE)

    // State lokasi koordinat
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    // State nama wilayah administrative (format: "Menteng, Jakarta Pusat")
    private val _locationName = MutableStateFlow("Menunggu GPS...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    // State loading status lokasi
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk lokasi manual
    private val _isManualLocation = MutableStateFlow(false)
    val isManualLocation: StateFlow<Boolean> = _isManualLocation.asStateFlow()

    init {
        // Ambil status apakah terakhir kali diset mode manual
        _isManualLocation.value = prefs.getBoolean("is_manual_location", false)
        // Memuat lokasi cadangan terakhir dari shared_preference agar UI langsung terisi tanpa menunggu GPS lambat
        loadCachedLocation()
    }

    /**
     * Memperoleh lokasi perangkat terkini secara realtime menggunakan FusedLocationProviderClient.
     * Setelah koordinat didapat, memicu reverse-geocoding untuk memperbarui nama lokasi,
     * lalu menyimpan properti baru tersebut di SharedPreferences.
     */
    fun refreshLocation() {
        if (_isManualLocation.value) {
            return
        }
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
     * Set lokasi kustom secara manual
     */
    fun setManualLocation(cityName: String, lat: Double, lon: Double) {
        _isManualLocation.value = true
        _locationName.value = cityName
        val manualLoc = Location("manual").apply {
            latitude = lat
            longitude = lon
        }
        _userLocation.value = manualLoc
        
        prefs.edit().apply {
            putBoolean("is_manual_location", true)
            putDouble("cached_lat", lat)
            putDouble("cached_lon", lon)
            putString("cached_address", cityName)
            apply()
        }
        // Pastikan seluruh widget sinkron dengan data koordinat kustom yang baru diset manual
        id.ideahousetech.prayertime_qibla.widget.PrayerWidgetHelper.updateAllWidgets(appContext)
    }

    /**
     * Kembali ke mode GPS Otomatis
     */
    fun setAutoLocation() {
        _isManualLocation.value = false
        prefs.edit().putBoolean("is_manual_location", false).apply()
        refreshLocation()
    }

    /**
     * Menyimpan koordinat terakhir dan alamat teks ke Shared Preferences.
     */
    private fun saveLocationToCache(lat: Double, lon: Double, address: String) {
        prefs.edit().apply {
            putDouble("cached_lat", lat)
            putDouble("cached_lon", lon)
            putString("cached_address", address)
            apply()
        }
        // Pastikan seluruh widget diperbarui ketika koordinat terbaru berhasil ter-cache dari pembacaan GPS otomatis
        id.ideahousetech.prayertime_qibla.widget.PrayerWidgetHelper.updateAllWidgets(appContext)
    }

    /**
     * Memuat koordinat cadangan dari Shared Preferences.
     * Default fallback ke Koordinat Jakarta Pusat jika cache kosong.
     */
    private fun loadCachedLocation() {
        val lat = prefs.getDouble("cached_lat", -6.175115) // Monas Jakarta
        val lon = prefs.getDouble("cached_lon", 106.827157)
        val address = prefs.getString("cached_address", "Menteng, Jakarta Pusat") ?: "Menteng, Jakarta Pusat"

        val mockLoc = Location("cached").apply {
            latitude = lat
            longitude = lon
        }
        _userLocation.value = mockLoc
        _locationName.value = address
    }
}

class LocationViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
