package com.jpmc.weatherapp

class WeatherRepository (private val weatherApiService: WeatherApiService) {
    suspend fun getWeather(city: String): WeatherResponse {
        return weatherApiService.getWeather(city, "6be3ab23d49d8901d204cdcb6ef150d7")
    }
}