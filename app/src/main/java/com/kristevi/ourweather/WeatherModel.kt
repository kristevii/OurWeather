package com.kristevi.ourweather

import com.google.gson.annotations.SerializedName
import androidx.compose.ui.graphics.Color

// Response untuk Current Weather API
data class CurrentWeatherResponse(
    @SerializedName("coord") val coord: Coord? = null,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("base") val base: String? = null,
    @SerializedName("main") val main: Main,
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("wind") val wind: Wind? = null,
    @SerializedName("clouds") val clouds: Clouds? = null,
    @SerializedName("rain") val rain: Rain? = null,
    @SerializedName("snow") val snow: Snow? = null,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("cod") val cod: Int
)

// Response untuk 5-Day Forecast API
data class WeatherForecastResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: City
)

// Forecast Item (setiap 3 jam)
data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainForecast,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("pop") val pop: Float, // Probability of precipitation
    @SerializedName("rain") val rain: Rain? = null,
    @SerializedName("snow") val snow: Snow? = null,
    @SerializedName("sys") val sys: SysForecast,
    @SerializedName("dt_txt") val dtTxt: String
)

// Common Models
data class Coord(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int? = null,
    @SerializedName("grnd_level") val grndLevel: Int? = null
)

data class MainForecast(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("sea_level") val seaLevel: Int? = null,
    @SerializedName("grnd_level") val grndLevel: Int? = null,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("temp_kf") val tempKf: Double
)

data class Wind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Double? = null
)

data class Clouds(
    @SerializedName("all") val all: Int
)

data class Rain(
    @SerializedName("1h") val oneHour: Double? = null,
    @SerializedName("3h") val threeHours: Double? = null
)

data class Snow(
    @SerializedName("1h") val oneHour: Double? = null,
    @SerializedName("3h") val threeHours: Double? = null
)

data class Sys(
    @SerializedName("type") val type: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

data class SysForecast(
    @SerializedName("pod") val pod: String // Part of day (d = day, n = night)
)

data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: Coord,
    @SerializedName("country") val country: String,
    @SerializedName("population") val population: Int? = null,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

// Simplified models untuk kompatibilitas backward
data class SimpleWeatherResponse(
    val name: String,
    val main: SimpleMain,
    val weather: List<SimpleWeather>
)

data class SimpleMain(
    val temp: Float,
    val humidity: Int
)

data class SimpleWeather(
    val description: String
)

// UV Index Response
data class UVIndexResponse(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("date_iso") val dateIso: String? = null,
    @SerializedName("date") val date: Long,
    @SerializedName("value") val value: Double
)

// UV Index dengan deskripsi
data class UVIndexData(
    val value: Double,
    val description: String,
    val riskLevel: String,
    val color: Color
)