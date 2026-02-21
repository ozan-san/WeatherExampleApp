package com.ozansan.weatherexampleapp.network

data class WeatherInfo(
    val temperature: Double,
    val weatherDescription: String,
    val precipitationProbability: Int,
    val weatherIcon: Int
)
