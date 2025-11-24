package com.kristevi.ourweather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {

    // Current Weather by City Name
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String
    ): CurrentWeatherResponse

    // 5-Day Weather Forecast by City Name
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String
    ): WeatherForecastResponse

    // Current Weather by Geographic Coordinates
    @GET("weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String
    ): CurrentWeatherResponse

    // 5-Day Forecast by Geographic Coordinates
    @GET("forecast")
    suspend fun getWeatherForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String
    ): WeatherForecastResponse

    // Current Weather by City ID
    @GET("weather")
    suspend fun getCurrentWeatherById(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") language: String
    ): CurrentWeatherResponse

    // UV Index by Geographic Coordinates
    @GET("uvi")
    suspend fun getUVIndex(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): UVIndexResponse

    // UV Index Forecast by Geographic Coordinates
    @GET("uvi/forecast")
    suspend fun getUVIndexForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("cnt") count: Int = 8
    ): List<UVIndexResponse>

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

        fun create(): WeatherAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(WeatherAPI::class.java)
        }
    }
}