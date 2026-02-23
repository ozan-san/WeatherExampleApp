package com.ozansan.weatherexampleapp.landing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.LocationRepository
import com.ozansan.weatherexampleapp.landing.state.DailyWeatherInfo
import com.ozansan.weatherexampleapp.landing.state.WeatherInfo
import com.ozansan.weatherexampleapp.util.WeatherCodeMapper
import com.ozansan.weatherexampleapp.network.WeatherRepository
import com.ozansan.weatherexampleapp.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository,
    private val weatherRepository: WeatherRepository,
    permissionChecker: PermissionChecker
) : ViewModel() {

    private var locationJob: Job? = null

    private val _locationAddress = MutableStateFlow("Awaiting location permission...")
    val locationAddress: StateFlow<String> = _locationAddress.asStateFlow()

    private var lastFetchedAddress: String? = null

    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo.asStateFlow()

    private val _weeklyWeatherInfo = MutableStateFlow<ImmutableList<DailyWeatherInfo>?>(null)
    val weeklyWeatherInfo: StateFlow<ImmutableList<DailyWeatherInfo>?> =
        _weeklyWeatherInfo.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(permissionChecker.hasFineLocationPermission())
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
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationJob?.cancel() // Cancel any existing location listeners.

        if (!_hasLocationPermission.value) {
            _locationAddress.value = "Location permission not granted."
            _isRefreshing.value = false
            return
        }

        // The 'onEach' block will be called every time a new location is emitted.
        locationJob = locationRepository.getLocationUpdates()
            .onEach { location ->
                val addresses =
                    geocodingRepository.reverseGeocode(location.latitude, location.longitude)

                val newLocationAddress = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Prioritize locality (city), fallback to adminArea (province/state), then subAdminArea.
                    address.subAdminArea ?: address.adminArea ?: address.locality
                    ?: "Unknown Location"
                } else {
                    "Could not find address for location."
                }

                _locationAddress.value = newLocationAddress

                if (newLocationAddress != lastFetchedAddress || _isRefreshing.value) {
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
                                weatherIcon = WeatherCodeMapper.toIcon(
                                    weatherData.current.weatherCode,
                                    isDay
                                )
                            )

                            val minWeekSize = minOf(
                                weatherData.dailyData.time.size,
                                weatherData.dailyData.weatherCode.size,
                                weatherData.dailyData.temperatureMin.size,
                                weatherData.dailyData.temperatureMax.size
                            )

                            val dailyForecasts = (0..<minWeekSize).map { index ->
                                val date = LocalDate.parse(weatherData.dailyData.time[index])
                                val locale = Locale.getDefault()
                                DailyWeatherInfo(
                                    date = date,
                                    dateDisplayName = date.dayOfWeek.getDisplayName(
                                        TextStyle.SHORT,
                                        locale
                                    ),
                                    temperatureMin = weatherData.dailyData.temperatureMin[index],
                                    temperatureMax = weatherData.dailyData.temperatureMax[index],
                                    weatherIcon = WeatherCodeMapper.toIcon(
                                        weatherData.dailyData.weatherCode[index],
                                        true
                                    )
                                )
                            }

                            _weeklyWeatherInfo.value = dailyForecasts.toImmutableList()


                        } catch (e: Exception) {
                            Log.e("LandingViewModel", "Error fetching weather data", e)
                        } finally {
                            _isRefreshing.value = false
                        }
                    }
                }
            }
            .catch { e ->
                _locationAddress.value = "Error fetching location: ${e.message}"
                _isRefreshing.value = false
            }
            .launchIn(viewModelScope)
    }
}