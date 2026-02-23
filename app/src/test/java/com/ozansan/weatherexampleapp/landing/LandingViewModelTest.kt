package com.ozansan.weatherexampleapp.landing

import android.location.Address
import android.location.Location
import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.LocationRepository
import com.ozansan.weatherexampleapp.network.WeatherRepository
import com.ozansan.weatherexampleapp.network.model.CurrentWeather
import com.ozansan.weatherexampleapp.network.model.DailyData
import com.ozansan.weatherexampleapp.network.model.HourlyData
import com.ozansan.weatherexampleapp.network.model.WeatherData
import com.ozansan.weatherexampleapp.permissions.PermissionChecker
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(BlockJUnit4ClassRunner::class)
class LandingViewModelTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var geocodingRepository: GeocodingRepository
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var permissionChecker: PermissionChecker

    private lateinit var viewModel: LandingViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        locationRepository = mock(LocationRepository::class.java)
        geocodingRepository = mock(GeocodingRepository::class.java)
        weatherRepository = mock(WeatherRepository::class.java)
        permissionChecker = mock(PermissionChecker::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewmodel instantiates correctly`() {
        // Just instantiate the viewmodel
        viewModel = LandingViewModel(
            locationRepository,
            geocodingRepository,
            weatherRepository,
            permissionChecker
        )
    }

    @Test
    fun `happy path - permission granted, location and weather fetched`() = runTest {
        // 1. Arrange
        `when`(permissionChecker.hasFineLocationPermission()).thenReturn(true)

        val mockLocation = mock(Location::class.java)
        `when`(mockLocation.latitude).thenReturn(40.7128)
        `when`(mockLocation.longitude).thenReturn(-74.0060)
        `when`(locationRepository.getLocationUpdates()).thenReturn(flowOf(mockLocation))

        val mockAddress = mock(Address::class.java)
        `when`(mockAddress.subAdminArea).thenReturn("New York")
        `when`(geocodingRepository.reverseGeocode(40.7128, -74.0060))
            .thenReturn(listOf(mockAddress))

        val mockWeatherData = WeatherData(
            latitude = 40.7128,
            longitude = -74.0060,
            current = CurrentWeather(
                time = "2024-01-01T12:00",
                interval = 900,
                temperature = 25.0,
                weatherCode = 0,
                rain = 0.0
            ),
            hourlyData = HourlyData(
                time = listOf("2024-01-01T12:00"),
                temperature = listOf(25.0),
                weatherCode = listOf(0),
                precipitationProbability = listOf(0)
            ),
            dailyData = DailyData(
                time = listOf("2024-01-01"),
                weatherCode = listOf(0),
                temperatureMin = listOf(10.0),
                temperatureMax = listOf(20.0),
                sunrise = listOf("2024-01-01T07:00"),
                sunset = listOf("2024-01-01T19:00")
            )
        )
        `when`(weatherRepository.getWeather(40.7128, -74.0060)).thenReturn(mockWeatherData)

        // 2. Act
        viewModel = LandingViewModel(
            locationRepository,
            geocodingRepository,
            weatherRepository,
            permissionChecker
        )

        // Let coroutines finish
        advanceUntilIdle()

        // 3. Assert
        assertEquals("New York", viewModel.locationAddress.value)
        assertEquals(25.0, viewModel.weatherInfo.value?.temperature)
        assertEquals("Clear", viewModel.weatherInfo.value?.weatherDescription)
        assertEquals(1, viewModel.weeklyWeatherInfo.value?.size)
    }
}