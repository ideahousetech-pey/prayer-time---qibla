package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.service.LocationService
import id.ideahousetech.prayertime_qibla.utils.getDouble
import id.ideahousetech.prayertime_qibla.utils.putDouble
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.AppConfig
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
        _isManualLocation.value = prefs.getBoolean(PrefsKeys.IS_MANUAL_LOCATION, false)
        // Memuat lokasi cadangan terakhir dari shared_preference agar UI langsung terisi tanpa menunggu GPS lambat
        loadCachedLocation()
    }

    private var refreshJob: kotlinx.coroutines.Job? = null
    private var lastRefreshTime = 0L

    /**
     * Memperoleh lokasi perangkat terkini secara realtime menggunakan FusedLocationProviderClient.
     * Setelah koordinat didapat, memicu reverse-geocoding untuk memperbarui nama lokasi,
     * lalu menyimpan properti baru tersebut di SharedPreferences.
     * Menggunakan cooldown 30 detik dan pembatalan job lama jika ada request baru berturut-turut.
     */
    fun refreshLocation() {
        if (_isManualLocation.value) {
            return
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < 30_000) {
            // Cooldown aktif, batalkan request demi efisiensi baterai & menghindari spamming GPS
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val loc = locationService.getCurrentLocation()
                if (loc != null) {
                    _userLocation.value = loc
                    val address = locationService.getAddressFromLocation(loc.latitude, loc.longitude)
                    _locationName.value = address
                    saveLocationToCache(loc.latitude, loc.longitude, address)
                    _updateWidgetIfNeeded()
                    lastRefreshTime = System.currentTimeMillis()
                } else {
                    // Gunakan cache jika pembacaan GPS gagal
                    loadCachedLocation()
                }
            } finally {
                _isLoading.value = false
            }
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
            putBoolean(PrefsKeys.IS_MANUAL_LOCATION, true)
            putDouble(PrefsKeys.CACHED_LAT, lat)
            putDouble(PrefsKeys.CACHED_LON, lon)
            putString(PrefsKeys.CACHED_ADDRESS, cityName)
            apply()
        }
        // Pastikan seluruh widget sinkron dengan data koordinat kustom yang baru diset manual
        _updateWidgetIfNeeded()
    }

    /**
     * Kembali ke mode GPS Otomatis
     */
    fun setAutoLocation() {
        _isManualLocation.value = false
        prefs.edit().putBoolean(PrefsKeys.IS_MANUAL_LOCATION, false).apply()
        refreshLocation()
    }

    /**
     * Menyimpan koordinat terakhir dan alamat teks ke Shared Preferences.
     */
    private fun saveLocationToCache(lat: Double, lon: Double, address: String) {
        prefs.edit().apply {
            putDouble(PrefsKeys.CACHED_LAT, lat)
            putDouble(PrefsKeys.CACHED_LON, lon)
            putString(PrefsKeys.CACHED_ADDRESS, address)
            apply()
        }
    }

    /**
     * Memperbarui seluruh widget dari satu titik operasi (Single Point Update)
     */
    private fun _updateWidgetIfNeeded() {
        id.ideahousetech.prayertime_qibla.widget.PrayerWidgetHelper.updateAllWidgets(appContext)
    }

    /**
     * Memuat koordinat cadangan dari Shared Preferences.
     * Default fallback ke Koordinat Jakarta Pusat jika cache kosong.
     */
    private fun loadCachedLocation() {
        val lat = prefs.getDouble(PrefsKeys.CACHED_LAT, -6.175115) // Monas Jakarta
        val lon = prefs.getDouble(PrefsKeys.CACHED_LON, 106.827157)
        val address = prefs.getString(PrefsKeys.CACHED_ADDRESS, "Menteng, Jakarta Pusat") ?: "Menteng, Jakarta Pusat"

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
