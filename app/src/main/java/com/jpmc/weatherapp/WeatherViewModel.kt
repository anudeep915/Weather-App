package com.jpmc.weatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _weather: MutableLiveData<WeatherResponse?> = MutableLiveData()
    val weather: LiveData<WeatherResponse?> get() = _weather

    fun getWeather(city: String) {
        viewModelScope.launch {
            try {
                // Fetch weather data from the repository
                val weather = repository.getWeather(city)
                // Update the weather state in the ViewModel, handling null values
                _weather.value = weather
            } catch (e: Exception) {
                // Handle errors gracefully
                _weather.value = null
                Log.e(e.toString(), "Failed to fetch weather data for city $city")
            }
        }
    }
}

class WeatherViewModelFactory(private val weatherApiService: WeatherApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java))
        {
            return WeatherViewModel(WeatherRepository(weatherApiService)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}