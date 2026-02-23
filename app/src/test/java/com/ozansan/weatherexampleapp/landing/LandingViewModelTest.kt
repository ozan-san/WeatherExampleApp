package com.ozansan.weatherexampleapp.landing

import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.LocationRepository
import com.ozansan.weatherexampleapp.network.WeatherRepository
import com.ozansan.weatherexampleapp.permissions.PermissionChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.internal.runners.JUnit4ClassRunner
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.mockito.Mockito.mock

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

        viewModel = LandingViewModel(
            locationRepository,
            geocodingRepository,
            weatherRepository,
            permissionChecker
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewmodel instantiates correctly`() {
        // Test passes if setUp() completes without throwing an exception
    }
}
