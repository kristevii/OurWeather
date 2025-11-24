package com.kristevi.ourweather

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun CurrentWeatherResponse.getWeatherIconCode(): String {
    return this.weather.firstOrNull()?.icon ?: "01d"
}

fun CurrentWeatherResponse.getWeatherIcon(): String {
    return this.weather.firstOrNull()?.icon ?: "01d"
}

fun CurrentWeatherResponse.getWeatherDescription(): String {
    val description = this.weather.firstOrNull()?.description ?: "Unknown"

    // Default tetap menggunakan kapitalisasi untuk semua bahasa
    return description.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

fun CurrentWeatherResponse.getFormattedTemperature(): String {
    return "${this.main.temp.toInt()}°"
}

fun CurrentWeatherResponse.getFeelsLikeTemperature(): String {
    return "Feels like ${this.main.feelsLike.toInt()}°C"
}

fun CurrentWeatherResponse.getFormattedHumidity(): String {
    return "${this.main.humidity}%"
}

fun CurrentWeatherResponse.getFormattedPressure(): String {
    return "${this.main.pressure} hPa"
}

fun CurrentWeatherResponse.getFormattedWindSpeed(): String {
    return "${this.wind?.speed?.toInt() ?: 0} m/s"
}

fun ForecastItem.getWeatherIcon(): String {
    return this.weather.firstOrNull()?.icon ?: "01d"
}

fun ForecastItem.getWeatherDescription(): String {
    val description = this.weather.firstOrNull()?.description ?: "Unknown"

    // Default tetap menggunakan kapitalisasi untuk semua bahasa
    return description.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

fun ForecastItem.getFormattedTemperature(): String {
    return "${this.main.temp.toInt()}°C"
}

fun ForecastItem.getFormattedDate(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val date = inputFormat.parse(this.dtTxt)
    return outputFormat.format(date ?: Date())
}

fun ForecastItem.getPrecipitationProbability(): String {
    return "${(this.pop * 100).toInt()}%"
}

fun WeatherForecastResponse.getDailyForecasts(): List<ForecastItem> {
    val result = mutableListOf<ForecastItem>()
    val takenDates = mutableSetOf<String>()

    this.list.forEach { item ->
        val date = item.dtTxt.substring(0, 10)
        val time = item.dtTxt.substring(11, 16)

        if (!takenDates.contains(date) && time == "12:00") {
            takenDates.add(date)
            result.add(item)
        }
    }

    return result
}

fun CurrentWeatherResponse.toSimpleWeatherResponse(): SimpleWeatherResponse {
    return SimpleWeatherResponse(
        name = this.name,
        main = SimpleMain(
            temp = this.main.temp.toFloat(),
            humidity = this.main.humidity
        ),
        weather = this.weather.map {
            SimpleWeather(description = it.description)
        }
    )
}