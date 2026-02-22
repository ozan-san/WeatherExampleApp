package com.ozansan.weatherexampleapp.landing

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.LocationClient
import com.ozansan.weatherexampleapp.network.WeatherCodeMapper
import com.ozansan.weatherexampleapp.network.WeatherInfo
import com.ozansan.weatherexampleapp.network.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LandingViewModel(private val app: Application) : AndroidViewModel(app) {

    private val locationClient = LocationClient(app)
    private val geocodingRepository = GeocodingRepository(app)
    private val weatherRepository = WeatherRepository()
    private var locationJob: Job? = null

    private val _locationAddress = MutableStateFlow("Awaiting location permission...")
    val locationAddress: StateFlow<String> = _locationAddress.asStateFlow()

    private var lastFetchedAddress: String? = null

    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(checkPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // If permission is already granted when the ViewModel is created, start listening.
        if (_hasLocationPermission.value) {
            startLocationUpdates()
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _hasLocationPermission.value = isGranted
        if (isGranted) {
            startLocationUpdates()
        } else {
            _locationAddress.value = "Location permission denied."
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        lastFetchedAddress = null // Force a weather refetch
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationJob?.cancel() // Cancel any existing location listeners.

        if (!_hasLocationPermission.value) {
            _locationAddress.value = "Location permission not granted."
            if (_isRefreshing.value) _isRefreshing.value = false
            return
        }

        _locationAddress.value = "Fetching location..."

        // The 'onEach' block will be called every time a new location is emitted.
        locationJob = locationClient.getLocationUpdates()
            .onEach { location ->
                val addresses = geocodingRepository.reverseGeocode(location.latitude, location.longitude)

                val newLocationAddress = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Prioritize locality (city), fallback to adminArea (province/state), then subAdminArea.
                    address.subAdminArea ?: address.adminArea ?: address.locality ?: "Unknown Location"
                } else {
                    "Could not find address for location."
                }

                _locationAddress.value = newLocationAddress

                if (newLocationAddress != lastFetchedAddress) {
                    lastFetchedAddress = newLocationAddress
                    viewModelScope.launch {
                        try {
                            val weatherData =
                                weatherRepository.getWeather(location.latitude, location.longitude)

                            val sunrise = weatherData.dailyData.sunrise[0]
                            val sunset = weatherData.dailyData.sunset[0]
                            val isDay = WeatherCodeMapper.isDay(sunrise, sunset)

                            _weatherInfo.value = WeatherInfo(
                                temperature = weatherData.current.temperature,
                                weatherDescription = WeatherCodeMapper.toText(weatherData.current.weatherCode),
                                precipitationProbability = weatherData.current.rain.toInt(),
                                weatherIcon = WeatherCodeMapper.toIcon(weatherData.current.weatherCode, isDay)
                            )
                        } catch (e: Exception) {
                            Log.e("LandingViewModel", "Error fetching weather data", e)
                        } finally {
                            if (_isRefreshing.value) _isRefreshing.value = false
                        }
                    }
                } else {
                    if (_isRefreshing.value) _isRefreshing.value = false
                }
            }
            .catch { e ->
                _locationAddress.value = "Error fetching location: ${e.message}"
                if (_isRefreshing.value) _isRefreshing.value = false
            }
            .launchIn(viewModelScope)
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            app,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
