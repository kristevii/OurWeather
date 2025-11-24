package com.kristevi.ourweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

class SearchCuacaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OurWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchWeatherScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val currentWeather by viewModel.currentWeather.collectAsState()
    val weatherForecast by viewModel.weatherForecast.collectAsState()
    val uvIndex by viewModel.uvIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isLightTheme = MaterialTheme.colorScheme.background.isLight()
    val backgroundColor = if (isLightTheme) {
        Color(0xFFE3F2FD)
    } else {
        Color(0xFF0A1A2F)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.carikota),
                        color = if (isLightTheme) Color(0xFF1976D2) else Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isLightTheme) Color(0xFF1976D2) else Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Search Bar
                SimpleSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        keyboardController?.hide()
                        if (searchQuery.isNotBlank()) {
                            viewModel.loadAllWeatherData(searchQuery)
                        }
                    },
                    isLightTheme = isLightTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                when {
                    isLoading -> {
                        LoadingState(isLightTheme = isLightTheme)
                    }
                    error != null -> {
                        ErrorState(
                            error = error!!,
                            isLightTheme = isLightTheme,
                            onRetry = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.loadAllWeatherData(searchQuery)
                                }
                            }
                        )
                    }
                    currentWeather != null -> {
                        WeatherDetailScreen(
                            currentWeather = currentWeather!!,
                            weatherForecast = weatherForecast,
                            uvIndex = uvIndex,
                            isLightTheme = isLightTheme
                        )
                    }
                    else -> {
                        EmptySearchState(isLightTheme = isLightTheme)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLightTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFF90CAF9)
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(R.string.inputnamakota),
                color = textColor.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = textColor
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        singleLine = true,
        shape = RoundedCornerShape(25.dp)
    )
}

@Composable
fun WeatherDetailScreen(
    currentWeather: CurrentWeatherResponse,
    weatherForecast: WeatherForecastResponse?,
    uvIndex: UVIndexData?,
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
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isLightTheme) {
                    Color.White
                } else {
                    Color(0xFF1E2B3A)
                }
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(35.dp)
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val weatherIconCode = currentWeather.weather.firstOrNull()?.icon ?: "01d"
                    val weatherIconRes = getWeatherIcon(weatherIconCode)

                    Image(
                        painter = painterResource(id = weatherIconRes),
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(170.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${currentWeather.main.temp.toInt()}°",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = currentWeather.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentWeather.getWeatherDescription(),
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(textColor.copy(alpha = 0.2f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherDetailItemWithIcon(
                        iconRes = R.drawable.ic_thermometer,
                        value = "${currentWeather.main.feelsLike.toInt()}°",
                        label = stringResource(R.string.terasaseperti),
                        isLightTheme = isLightTheme
                    )

                    WeatherDetailItemWithIcon(
                        iconRes = R.drawable.ic_wind,
                        value = "${currentWeather.wind?.speed?.toInt() ?: 0} km/h",
                        label = stringResource(R.string.kecepatanangin),
                        isLightTheme = isLightTheme
                    )

                    WeatherDetailItemWithIcon(
                        iconRes = R.drawable.ic_humidity,
                        value = "${currentWeather.main.humidity}%",
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
                        value = "${(currentWeather.visibility ?: 0) / 1000} km",
                        label = stringResource(R.string.visibilitas),
                        isLightTheme = isLightTheme
                    )

                    WeatherDetailItemWithIcon(
                        iconRes = R.drawable.ic_pressure,
                        value = "${currentWeather.main.pressure} hPa",
                        label = stringResource(R.string.tekananudara),
                        isLightTheme = isLightTheme
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        weatherForecast?.let { forecast ->
            FourDayForecastSection(
                forecast = forecast,
                isLightTheme = isLightTheme
            )
        }
    }
}

@Composable
fun FourDayForecastSection(
    forecast: WeatherForecastResponse,
    isLightTheme: Boolean
) {
    val dailyForecasts = forecast.getDailyForecasts().take(5) // Ambil 5 hari (hari ini + 4 hari ke depan)

    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFF90CAF9)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            text = stringResource(R.string.empathari),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        dailyForecasts.forEachIndexed { index, dailyForecast ->
            DailyForecastRowSearch(
                forecast = dailyForecast,
                isLightTheme = isLightTheme
            )
            if (index < dailyForecasts.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DailyForecastRowSearch(
    forecast: ForecastItem,
    isLightTheme: Boolean
) {
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
            text = forecast.getDayNameSearch(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
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
            text = "${forecast.main.temp.toInt()}°",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

fun ForecastItem.getDayNameSearch(): String {
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
fun WeatherDetailItemWithIcon(
    iconRes: Int,
    value: String,
    label: String,
    isLightTheme: Boolean
) {
    val textColor = if (isLightTheme) {
        Color(0xFF1976D2)
    } else {
        Color(0xFFE3F2FD)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun getWeatherIconCode(iconCode: String): Int {
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

@Composable
fun LoadingState(isLightTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.mencaridata),
            fontSize = 16.sp,
            color = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9)
        )
    }
}

@Composable
fun ErrorState(error: String, isLightTheme: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (Locale.getDefault().language == "en") "An Error Occurred" else "Terjadi Kesalahan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onRetry) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Retry",
                tint = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun EmptySearchState(isLightTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            tint = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.carikota),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.descsearch),
            fontSize = 16.sp,
            color = if (isLightTheme) Color(0xFF1976D2) else Color(0xFF90CAF9),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchWeatherScreenPreview() {
    OurWeatherTheme {
        SearchWeatherScreen(onBackClick = {})
    }
}