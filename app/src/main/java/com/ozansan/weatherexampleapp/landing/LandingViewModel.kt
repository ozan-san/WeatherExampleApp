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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LandingViewModel(private val app: Application) : AndroidViewModel(app) {

    private val locationClient = LocationClient(app)
    private val geocodingRepository = GeocodingRepository(app)
    private val weatherRepository = WeatherRepository()

    private val _locationAddress = MutableStateFlow("Awaiting location permission...")
    val locationAddress: StateFlow<String> = _locationAddress.asStateFlow()

    private var lastFetchedAddress: String? = null

    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(checkPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

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

    private fun startLocationUpdates() {
        if (!_hasLocationPermission.value) {
            _locationAddress.value = "Location permission not granted."
            return
        }

        _locationAddress.value = "Fetching location..."

        // The 'onEach' block will be called every time a new location is emitted.
        locationClient.getLocationUpdates()
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

                            val now = Calendar.getInstance()
                            val currentHour = now.get(Calendar.HOUR_OF_DAY)
                            val currentDay = now.get(Calendar.DAY_OF_YEAR)

                            var weatherIndex = -1
                            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                            for ((index, timeString) in weatherData.hourlyData.time.withIndex()) {
                                val date = format.parse(timeString)
                                if (date != null) {
                                    val cal = Calendar.getInstance()
                                    cal.time = date
                                    if (cal.get(Calendar.HOUR_OF_DAY) == currentHour && cal.get(
                                            Calendar.DAY_OF_YEAR
                                        ) == currentDay
                                    ) {
                                        weatherIndex = index
                                        break
                                    }
                                }
                            }

                            if (weatherIndex != -1) {
                                val weatherCode = weatherData.hourlyData.weatherCode[weatherIndex]
                                val sunrise = weatherData.dailyData.sunrise[0]
                                val sunset = weatherData.dailyData.sunset[0]
                                val isDay = WeatherCodeMapper.isDay(sunrise, sunset)
                                _weatherInfo.value = WeatherInfo(
                                    temperature = weatherData.hourlyData.temperature[weatherIndex],
                                    weatherDescription = WeatherCodeMapper.toText(weatherCode),
                                    precipitationProbability = weatherData.hourlyData.precipitationProbability[weatherIndex],
                                    weatherIcon = WeatherCodeMapper.toIcon(weatherCode, isDay)
                                )
                            }

                        } catch (e: Exception) {
                            Log.e("LandingViewModel", "Error fetching weather data", e)
                        }
                    }
                }
            }
            .catch { e ->
                _locationAddress.value = "Error fetching location: ${e.message}"
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
