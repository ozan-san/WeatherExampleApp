package com.ozansan.weatherexampleapp.landing

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.LocationClient
import com.ozansan.weatherexampleapp.network.WeatherCodeMapper
import com.ozansan.weatherexampleapp.network.WeatherInfo
import com.ozansan.weatherexampleapp.network.WeatherRepository
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

    var locationAddress by mutableStateOf("Awaiting location permission...")
        private set

    private var lastFetchedAddress: String? = null

    var weatherInfo by mutableStateOf<WeatherInfo?>(null)
        private set

    var hasLocationPermission by mutableStateOf(checkPermission())
        private set

    init {
        // If permission is already granted when the ViewModel is created, start listening.
        if (hasLocationPermission) {
            startLocationUpdates()
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        hasLocationPermission = isGranted
        if (isGranted) {
            startLocationUpdates()
        } else {
            locationAddress = "Location permission denied."
        }
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission) {
            locationAddress = "Location permission not granted."
            return
        }

        locationAddress = "Fetching location..."

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

                locationAddress = newLocationAddress

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
                                weatherInfo = WeatherInfo(
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
                locationAddress = "Error fetching location: ${e.message}"
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
