package com.kristevi.ourweather

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global.getString
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kristevi.ourweather.ui.theme.OurWeatherTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            OurWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp()
                }
            }
        }
    }
}

fun Color.isLight(): Boolean = this.luminance() > 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp() {
    val viewModel: WeatherViewModel = viewModel()
    val currentWeather by viewModel.currentWeather.collectAsState()
    val weatherForecast by viewModel.weatherForecast.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    var isInitialLoad by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isLightTheme = MaterialTheme.colorScheme.background.isLight()

    LaunchedEffect(Unit) {
        if (isInitialLoad) {
            viewModel.getCurrentLocation()
            isInitialLoad = false
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            viewModel.loadAllWeatherDataByLocation(location.latitude, location.longitude)
        }
    }

    val backgroundColor = if (isLightTheme) {
        Color(0xFFE3F2FD)
    } else {
        Color(0xFF0A1A2F)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TopAppBar(
                title = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isLightTheme) {
                        Color(0xFFE3F2FD)
                    } else {
                        Color(0xFF0A1A2F)
                    }
                ),
                actions = {
                    IconButton(
                        onClick = {
                            currentLocation?.let { location ->
                                viewModel.loadAllWeatherDataByLocation(location.latitude, location.longitude)
                            } ?: viewModel.getCurrentLocation()
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = if (isLightTheme) {
                                    Color(0xFF1976D2)
                                } else {
                                    Color.White
                                },
                                strokeWidth = 2.dp
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_refresh),
                                contentDescription = "Refresh",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(context, SearchCuacaActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isLightTheme) {
                                Color(0xFF1976D2)
                            } else {
                                Color.White
                            }
                        )
                    }
                }
            )

            when {
                currentWeather != null -> {
                    WeatherContent(
                        currentWeather = currentWeather!!,
                        weatherForecast = weatherForecast,
                        isLightTheme = isLightTheme
                    )
                }
                else -> {
                    EmptyState(isLightTheme = isLightTheme)
                }
            }
        }
    }
}

@Composable
fun WeatherContent(
    currentWeather: CurrentWeatherResponse,
    weatherForecast: WeatherForecastResponse?,
    isLightTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CurrentLocationSection(
            locationName = currentWeather.name,
            temperature = currentWeather.getFormattedTemperature(),
            weatherIconCode = currentWeather.getWeatherIconCode(),
            weatherDescription = currentWeather.getWeatherDescription(),
            isLightTheme = isLightTheme
        )

        Spacer(modifier = Modifier.height(16.dp))

        weatherForecast?.let { forecast ->
            HourlyForecastSection(forecast = forecast, isLightTheme = isLightTheme)
            Spacer(modifier = Modifier.height(10.dp))
        }

        weatherForecast?.let { forecast ->
            FiveDayForecastSection(forecast = forecast, isLightTheme = isLightTheme)
            Spacer(modifier = Modifier.height(10.dp))
        }

        WeatherDetailsSection(weather = currentWeather, isLightTheme = isLightTheme)
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun CurrentLocationSection(
    locationName: String,
    temperature: String,
    weatherIconCode: String,
    weatherDescription: String,
    isLightTheme: Boolean
) {
    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFF90CAF9)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row untuk icon lokasi dan nama lokasi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = textColor,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = locationName,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = getCurrentDayAndDate(),
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            contentAlignment = Alignment.Center
        ) {
            val weatherIconRes = getWeatherIcon(weatherIconCode)

            Image(
                painter = painterResource(id = weatherIconRes),
                contentDescription = weatherDescription,
                modifier = Modifier
                    .size(330.dp)
                    .align(Alignment.TopCenter)
            )
            Text(
                text = temperature,
                fontSize = 98.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-50).dp)
            )
            Text(
                text = weatherDescription,
                fontSize = 18.sp,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-40).dp)
            )
        }
    }
}

fun getWeatherIcon(iconCode: String): Int {
    return when (iconCode) {
        "01d" -> R.drawable.day_with_sun
        "02d", "03d" -> R.drawable.day_with_sun
        "04d" -> R.drawable.day_with_sun
        "09d", "10d" -> R.drawable.day_with_rain
        "11d" -> R.drawable.ic_storm
        "13d" -> R.drawable.day_with_snow
        "50d" -> R.drawable.day_with_wind
        "01n" -> R.drawable.night_with_moon
        "02n", "03n" -> R.drawable.night_with_moon
        "04n" -> R.drawable.night_with_moon
        "09n", "10n" -> R.drawable.night_with_rain
        "11n" -> R.drawable.ic_storm
        "13n" -> R.drawable.night_with_snow
        "50n" -> R.drawable.night_with_wind
        else -> R.drawable.day_with_sun
    }
}

fun getCurrentDayAndDate(): String {
    val locale = if (Locale.getDefault().language == "en") {
        Locale.ENGLISH
    } else {
        Locale("id", "ID") // Default Indonesia
    }
    val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", locale)
    return dateFormat.format(Date())
}

@Composable
fun HourlyForecastSection(forecast: WeatherForecastResponse, isLightTheme: Boolean) {
    val hourlyForecasts = forecast.list.take(8)

    Column(
        modifier = Modifier.padding(15.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hourlyForecasts) { hourlyForecast ->
                HourlyForecastItem(forecast = hourlyForecast, isLightTheme = isLightTheme)
            }
        }
    }
}

@Composable
fun HourlyForecastItem(forecast: ForecastItem, isLightTheme: Boolean) {
    val cardColor = if (isLightTheme) {
        Color.White
    } else {
        Color(0xFF1E2B3A)
    }

    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFF90CAF9)
    }

    // Mendapatkan jam dari timestamp
    val hour = forecast.dtTxt.substring(11, 13).toInt()

    // Menentukan apakah siang atau malam dan membuat kode ikon yang sesuai
    val iconCode = if (hour in 6..17) {
        // Siang (06:00 - 17:59) - gunakan ikon 'd'
        forecast.getWeatherIcon().replace("n", "d")
    } else {
        // Malam (18:00 - 05:59) - gunakan ikon 'n'
        forecast.getWeatherIcon().replace("d", "n")
    }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(35.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .width(45.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = forecast.dtTxt.substring(11, 16),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            Image(
                painter = painterResource(id = getWeatherIcon(iconCode)),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = forecast.getFormattedTemperature(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = "${(forecast.pop * 100).toInt()}%",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}
@Composable
fun FiveDayForecastSection(forecast: WeatherForecastResponse, isLightTheme: Boolean) {
    val dailyForecasts = forecast.getDailyForecasts().take(6)

    Column(
        modifier = Modifier.padding(15.dp)
    ) {
        Text(
            text = stringResource(R.string.empathari),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLightTheme) {
                Color(0xFF1976D2)
            } else {
                Color(0xFF90CAF9)
            },
            modifier = Modifier.padding(bottom = 12.dp)
        )
        dailyForecasts.forEachIndexed { index, dailyForecast ->
            DailyForecastRow(
                forecast = dailyForecast,
                isToday = index == 0,
                isLightTheme = isLightTheme
            )
            if (index < dailyForecasts.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DailyForecastRow(forecast: ForecastItem, isToday: Boolean = false, isLightTheme: Boolean) {
    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFF90CAF9)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) forecast.getFormattedDate("Hari Ini") else forecast.getDayName(),
            fontSize = 16.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(2f)
        )

        val dayIconCode = forecast.getWeatherIcon().replace("n", "d")
        Image(
            painter = painterResource(id = getWeatherIcon(dayIconCode)),
            contentDescription = "Weather Icon",
            modifier = Modifier
                .size(32.dp)
                .weight(1.2f)
        )

        Text(
            text = "${(forecast.pop * 100).toInt()}%",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            text = forecast.getFormattedTemperature(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

fun ForecastItem.getFormattedDate(prefix: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val forecastDate = inputFormat.parse(this.dtTxt) ?: Date()

    val locale = if (Locale.getDefault().language == "en") {
        Locale.ENGLISH
    } else {
        Locale("id", "ID") // Default Indonesia
    }

    val dateFormat = SimpleDateFormat("dd/MM", locale)
    val formattedDate = dateFormat.format(forecastDate)

    // Sesuaikan prefix berdasarkan bahasa
    val translatedPrefix = when {
        Locale.getDefault().language == "en" && prefix == "Hari Ini" -> "Today"
        Locale.getDefault().language == "en" && prefix == "Besok" -> "Tomorrow"
        else -> prefix
    }

    return "$formattedDate  $translatedPrefix"
}

fun ForecastItem.getDayName(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val forecastDate = inputFormat.parse(this.dtTxt) ?: Date()

    val calendar = Calendar.getInstance()
    val today = calendar.time

    calendar.time = today
    val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
    val todayYear = calendar.get(Calendar.YEAR)

    calendar.time = forecastDate
    val forecastDay = calendar.get(Calendar.DAY_OF_YEAR)
    val forecastYear = calendar.get(Calendar.YEAR)

    val locale = if (Locale.getDefault().language == "en") {
        Locale.ENGLISH
    } else {
        Locale("id", "ID") // Default Indonesia
    }

    val dateFormat = SimpleDateFormat("dd/MM", locale)
    val formattedDate = dateFormat.format(forecastDate)

    return when {
        todayDay == forecastDay && todayYear == forecastYear -> {
            val prefix = if (Locale.getDefault().language == "en") "Today" else "Hari Ini"
            "$formattedDate  $prefix"
        }
        todayDay + 1 == forecastDay && todayYear == forecastYear -> {
            val prefix = if (Locale.getDefault().language == "en") "Tomorrow" else "Besok"
            "$formattedDate  $prefix"
        }
        else -> {
            val dayFormat = if (Locale.getDefault().language == "en") {
                SimpleDateFormat("dd/MM  EEEE", Locale.ENGLISH)
            } else {
                SimpleDateFormat("dd/MM  EEEE", Locale("id", "ID"))
            }
            dayFormat.format(forecastDate)
        }
    }
}
@Composable
fun WeatherDetailsSection(
    weather: CurrentWeatherResponse,
    isLightTheme: Boolean
) {
    val viewModel: WeatherViewModel = viewModel()
    val uvIndex by viewModel.uvIndex.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(35.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isLightTheme) {
                Color.White
            } else {
                Color(0xFF1E2B3A)
            }
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItemWithIcon(
                    iconRes = R.drawable.ic_thermometer,
                    value = weather.getFormattedTemperature(),
                    label = stringResource(R.string.terasaseperti),
                    isLightTheme = isLightTheme
                )

                WeatherDetailItemWithIcon(
                    iconRes = R.drawable.ic_wind,
                    value = weather.getFormattedWindSpeed(),
                    label = stringResource(R.string.kecepatanangin),
                    isLightTheme = isLightTheme
                )

                WeatherDetailItemWithIcon(
                    iconRes = R.drawable.ic_humidity,
                    value = weather.getFormattedHumidity(),
                    label = stringResource(R.string.kelembaban),
                    isLightTheme = isLightTheme
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItemWithIcon(
                    iconRes = R.drawable.ic_uv,
                    value = uvIndex?.description ?: "-",
                    label = "UV Index",
                    isLightTheme = isLightTheme
                )

                WeatherDetailItemWithIcon(
                    iconRes = R.drawable.ic_visibility,
                    value = "${(weather.visibility ?: 0) / 1000} km",
                    label = stringResource(R.string.visibilitas),
                    isLightTheme = isLightTheme
                )

                WeatherDetailItemWithIcon(
                    iconRes = R.drawable.ic_pressure,
                    value = weather.getFormattedPressure(),
                    label = stringResource(R.string.tekananudara),
                    isLightTheme = isLightTheme
                )
            }
        }
    }
}

@Composable
fun WeatherDetailGridItem(
    iconRes: Int,
    value: String,
    label: String,
    isLightTheme: Boolean,
    textColor: Color? = null
) {
    val defaultTextColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFFE3F2FD)
    }

    val actualTextColor = textColor ?: defaultTextColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = actualTextColor
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = actualTextColor.copy(alpha = 0.7f)
        )
    }
}

fun formatTime(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

@Composable
fun EmptyState(isLightTheme: Boolean) {
    val backgroundColor = if (isLightTheme) {
        Color(0xFFE3F2FD)
    } else {
        Color(0xFF0A1A2F)
    }

    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFF90CAF9)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.day_with_sun),
            contentDescription = "Sunny",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Our Weather",
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.slogan),
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    OurWeatherTheme {
        WeatherApp()
    }
}