package com.kristevi.ourweather

import android.app.Application
import android.location.Location
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKey = "d554f60a31ce49c1b5e76c8d63bb363b"
    private val repository = WeatherRepository(apiKey)
    private val locationService = LocationService(application.applicationContext)

    private val context: Context = application.applicationContext

    private val _currentWeather = MutableStateFlow<CurrentWeatherResponse?>(null)
    val currentWeather: StateFlow<CurrentWeatherResponse?> = _currentWeather.asStateFlow()

    private val _weatherForecast = MutableStateFlow<WeatherForecastResponse?>(null)
    val weatherForecast: StateFlow<WeatherForecastResponse?> = _weatherForecast.asStateFlow()

    private val _uvIndex = MutableStateFlow<UVIndexData?>(null)
    val uvIndex: StateFlow<UVIndexData?> = _uvIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationData?>(null)
    val currentLocation: StateFlow<LocationData?> = _currentLocation.asStateFlow()

    private val _autoRefreshEnabled = MutableStateFlow(true)
    val autoRefreshEnabled: StateFlow<Boolean> = _autoRefreshEnabled.asStateFlow()

    private val _lastUpdateTime = MutableStateFlow<Long?>(null)
    val lastUpdateTime: StateFlow<Long?> = _lastUpdateTime.asStateFlow()

    private var refreshJob: Job? = null

    private val refreshInterval = 10L // 10 menit

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(refreshInterval * 60 * 1000) // Convert menit ke milidetik
                if (_autoRefreshEnabled.value && _currentLocation.value != null) {
                    refreshWeatherData()
                }
            }
        }
    }

    private fun refreshWeatherData() {
        viewModelScope.launch {
            try {
                _currentLocation.value?.let { location ->
                    loadWeatherByCoords(location.latitude, location.longitude)
                    _lastUpdateTime.value = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                println("Auto-refresh failed: ${e.message}")
            }
        }
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (!locationService.hasLocationPermission()) {
                    _error.value = "Izin lokasi diperlukan untuk mendapatkan cuaca di lokasi Anda"
                    _isLoading.value = false
                    return@launch
                }

                if (!locationService.isLocationEnabled()) {
                    _error.value = "GPS tidak aktif. Silakan aktifkan lokasi perangkat Anda"
                    _isLoading.value = false
                    return@launch
                }

                val lastLocation = locationService.getLastKnownLocation()
                if (lastLocation != null) {
                    handleNewLocation(lastLocation)
                    _isLoading.value = false
                }

                locationService.getCurrentLocation()
                    .onEach { location ->
                        handleNewLocation(location)
                        _isLoading.value = false
                    }
                    .catch { exception ->
                        _error.value = "Gagal mendapatkan lokasi: ${exception.message}"
                        _isLoading.value = false
                    }
                    .launchIn(viewModelScope)

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun handleNewLocation(location: Location) {
        val locationData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude
        )
        _currentLocation.value = locationData

        loadWeatherByCoords(location.latitude, location.longitude)
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    fun toggleAutoRefresh() {
        _autoRefreshEnabled.value = !_autoRefreshEnabled.value
        if (_autoRefreshEnabled.value) {
            startAutoRefresh()
        } else {
            refreshJob?.cancel()
        }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            if (_currentLocation.value != null) {
                refreshWeatherData()
            } else {
                getCurrentLocation()
            }
        }
    }

    fun updateLocationPermission(granted: Boolean) {
        if (granted) {
            getCurrentLocation()
        }
    }

    fun loadCurrentWeather(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _currentWeather.value = repository.getCurrentWeather(city)
                _lastUpdateTime.value = System.currentTimeMillis()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWeatherForecast(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _weatherForecast.value = repository.getWeatherForecast(city)
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllWeatherData(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _currentWeather.value = repository.getCurrentWeather(city)
                _weatherForecast.value = repository.getWeatherForecast(city)

                _currentWeather.value?.coord?.let { coord ->
                    loadUVIndex(coord.lat, coord.lon)
                }

                _lastUpdateTime.value = System.currentTimeMillis()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load UV Index
    private suspend fun loadUVIndex(lat: Double, lon: Double) {
        try {
            val uvResponse = repository.getUVIndex(lat, lon)
            _uvIndex.value = createUVIndexData(uvResponse.value)
        } catch (e: Exception) {
            _uvIndex.value = createUVIndexData(0.0)
            println("Failed to load UV index: ${e.message}")
        }
    }

    private fun createUVIndexData(value: Double): UVIndexData {
        return when {
            value < 3.0 -> UVIndexData(
                value = value,
                description = context.getString(R.string.uv_rendah),
                riskLevel = "Risiko rendah",
                color = Color(0xFF4CAF50) // Hijau
            )
            value < 6.0 -> UVIndexData(
                value = value,
                description = context.getString(R.string.uv_sedang),
                riskLevel = "Risiko sedang",
                color = Color(0xFFFFC107) // Kuning
            )
            value < 8.0 -> UVIndexData(
                value = value,
                description = context.getString(R.string.uv_tinggi),
                riskLevel = "Risiko tinggi",
                color = Color(0xFFFF9800) // Orange
            )
            value < 11.0 -> UVIndexData(
                value = value,
                description = context.getString(R.string.uv_sangattinggi),
                riskLevel = "Risiko sangat tinggi",
                color = Color(0xFFF44336) // Merah
            )
            else -> UVIndexData(
                value = value,
                description = context.getString(R.string.uv_ekstrem),
                riskLevel = "Risiko ekstrem",
                color = Color(0xFF9C27B0) // Ungu
            )
        }
    }

    // Load by coordinates
    fun loadWeatherByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _currentWeather.value = repository.getCurrentWeatherByCoords(lat, lon)
                _weatherForecast.value = repository.getWeatherForecastByCoords(lat, lon)
                loadUVIndex(lat, lon) // Load UV Index
                _lastUpdateTime.value = System.currentTimeMillis()
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fungsi untuk load weather by location
    fun loadAllWeatherDataByLocation(lat: Double, lon: Double) {
        loadWeatherByCoords(lat, lon)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearData() {
        _currentWeather.value = null
        _weatherForecast.value = null
        _uvIndex.value = null
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String? = null
)