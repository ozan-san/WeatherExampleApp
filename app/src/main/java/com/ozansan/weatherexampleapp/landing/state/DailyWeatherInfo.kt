package com.ozansan.weatherexampleapp.landing.state

import java.time.LocalDate

data class DailyWeatherInfo(
    val date: LocalDate,
    val dateDisplayName: String,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val weatherIcon: Int
)