package com.ozansan.weatherexampleapp.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val weatherApi: WeatherApi) {

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherData {
        return weatherApi.getWeather(latitude, longitude)
    }
}
