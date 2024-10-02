package com.jpmc.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.jpmc.weatherapp.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: WeatherViewModel

    private val sharedPreferences by lazy {
        getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle the permissions response
        if (permissions.all { it.value }) {
            // All permissions granted, proceed with location access
            requestLocationUpdates()
        } else {
            // One or more permissions denied
            // Handle the case where permissions are denied
            Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationUpdates() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener = LocationListener { location ->
            // Handle the location data
            val latitude = location.latitude
            val longitude = location.longitude
            getTheWeatherDetails()
        }
        // Implement other location listener methods if needed
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            )
        } else {
            // Permissions already granted, proceed with location access
            requestLocationUpdates()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getTheWeatherDetails() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherApiService = retrofit.create(WeatherApiService::class.java)
        val viewModelFactory = WeatherViewModelFactory(weatherApiService)
        viewModel = ViewModelProvider(this, viewModelFactory)[WeatherViewModel::class.java]

        // Load the last searched city from shared preferences
        val lastSearchedCity = sharedPreferences.getString("last_searched_city", null)
        if (lastSearchedCity != null) {
            viewModel.getWeather(lastSearchedCity)
        }

        viewModel.weather.observe(this) { weather ->
            if (weather != null) {
                binding.tvCity.text = weather.name
                binding.tvTemperature.text = "${weather?.main?.temp}°C"
                // Update other UI elements with weather data

                // Save the last searched city to shared preferences
                with(sharedPreferences.edit()) {
                    putString("last_searched_city", weather.name)
                    apply()
                }
            } else {
                // Handle errors
                Toast.makeText(this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString()
            viewModel.getWeather(city)
        }
    }
}