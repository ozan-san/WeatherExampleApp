package com.ozansan.weatherexampleapp.util

import com.ozansan.weatherexampleapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WeatherCodeMapper {

    fun toText(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Clear"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67 -> "Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }

    fun toIcon(weatherCode: Int, isDay: Boolean = true): Int {
        return when (weatherCode) {
            0, 1 -> if (isDay) R.drawable.wi_day_sunny else R.drawable.wi_night_clear
            2 -> if (isDay) R.drawable.wi_day_cloudy else R.drawable.wi_night_partly_cloudy
            3 -> if (isDay) R.drawable.wi_day_sunny_overcast else R.drawable.wi_night_alt_partly_cloudy
            45, 48 -> if (isDay) R.drawable.wi_day_fog else R.drawable.wi_night_fog
            51, 53, 55, 56, 57 -> if (isDay) R.drawable.wi_day_rain_mix else R.drawable.wi_night_rain_mix // Assuming same icon for day/night drizzle
            61, 63, 65, 66, 67, 80, 81, 82 -> if (isDay) R.drawable.wi_day_showers else R.drawable.wi_night_showers
            71, 73, 75, 77, 85, 86 -> if (isDay) R.drawable.wi_day_snow else R.drawable.wi_night_snow
            95, 96, 99 -> if (isDay) R.drawable.wi_day_snow_thunderstorm else R.drawable.wi_night_snow_thunderstorm
            else -> R.drawable.wi_na
        }
    }

    fun isDay(sunrise: String, sunset: String): Boolean {
        val now = Calendar.getInstance()
        val sunriseCal = toCalendar(sunrise)
        val sunsetCal = toCalendar(sunset)

        return now.after(sunriseCal) && now.before(sunsetCal)
    }

    private fun toCalendar(dateTimeString: String): Calendar {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val date = format.parse(dateTimeString)
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        return cal
    }
}