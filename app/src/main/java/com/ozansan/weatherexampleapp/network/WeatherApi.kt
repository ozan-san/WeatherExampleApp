package com.ozansan.weatherexampleapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast?hourly=temperature_2m,weather_code,precipitation_probability&daily=sunrise,sunset,weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&current=temperature_2m,weather_code,rain")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): WeatherData
}
