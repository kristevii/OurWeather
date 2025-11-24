package com.kristevi.ourweather

import com.kristevi.ourweather.toSimpleWeatherResponse
import java.util.Locale

class WeatherRepository(private val apiKey: String) {

    private val weatherAPI = WeatherAPI.create()

    private fun getLanguageCode(): String {
        val locale = Locale.getDefault()
        return when (locale.language) {
            "en" -> "en" // English
            "id" -> "id" // Indonesia
            else -> "id" // Default ke Indonesia untuk bahasa lain
        }
    }

    // Current Weather methods
    suspend fun getCurrentWeather(city: String): CurrentWeatherResponse {
        return weatherAPI.getCurrentWeather(city, apiKey, "metric", getLanguageCode())
    }

    suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): CurrentWeatherResponse {
        return weatherAPI.getCurrentWeatherByCoords(lat, lon, apiKey, "metric", getLanguageCode())
    }

    suspend fun getCurrentWeatherById(cityId: Int): CurrentWeatherResponse {
        return weatherAPI.getCurrentWeatherById(cityId, apiKey, "metric", getLanguageCode())
    }

    // Forecast methods
    suspend fun getWeatherForecast(city: String): WeatherForecastResponse {
        return weatherAPI.getWeatherForecast(city, apiKey, "metric", getLanguageCode())
    }

    suspend fun getWeatherForecastByCoords(lat: Double, lon: Double): WeatherForecastResponse {
        return weatherAPI.getWeatherForecastByCoords(lat, lon, apiKey, "metric", getLanguageCode())
    }

    // UV Index methods (tetap tanpa bahasa karena UV index biasanya tidak butuh terjemahan)
    suspend fun getUVIndex(lat: Double, lon: Double): UVIndexResponse {
        return weatherAPI.getUVIndex(lat, lon, apiKey)
    }

    suspend fun getUVIndexForecast(lat: Double, lon: Double): List<UVIndexResponse> {
        return weatherAPI.getUVIndexForecast(lat, lon, apiKey)
    }

    // Convert to simple format for backward compatibility
    suspend fun getSimpleWeather(city: String): SimpleWeatherResponse {
        val response = getCurrentWeather(city)
        return response.toSimpleWeatherResponse()
    }
}