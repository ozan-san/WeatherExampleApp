package com.ozansan.weatherexampleapp.landing.state

data class WeatherInfo(
    val temperature: Double,
    val weatherDescription: String,
    val precipitationProbability: Int,
    val weatherIcon: Int
)