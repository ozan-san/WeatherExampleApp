package com.ozansan.weatherexampleapp.network

import com.google.gson.annotations.SerializedName

data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("hourly")
    val hourlyData: HourlyData,
    @SerializedName("daily")
    val dailyData: DailyData
)

data class HourlyData(
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>
)

data class DailyData(
    val time: List<String>,
    val sunrise: List<String>,
    val sunset: List<String>
)
