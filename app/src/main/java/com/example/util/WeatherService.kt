package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.data.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WeatherInfo(
    val locationLabel: String,
    val temperatureStr: String,
    val tempInt: Int,
    val condition: String,
    val iconEmoji: String,
    val isRaining: Boolean,
    val isCold: Boolean,
    val isHot: Boolean,
    val rainProbability: Int? = null,
    val minTemp: Int? = null,
    val maxTemp: Int? = null,
    val greetingTip: String,
    val studyTip: String
)

object WeatherService {

    // Fast dictionary for famous Brazilian universities and capitals
    private val KNOWN_LOCATIONS = mapOf(
        "usp" to (-23.5615 to -46.7308),
        "unicamp" to (-22.8184 to -47.0647),
        "unesp" to (-23.5505 to -46.6333),
        "ufrj" to (-22.8612 to -43.2325),
        "ufmg" to (-19.8690 to -43.9664),
        "ufrgs" to (-30.0346 to -51.2177),
        "ufsc" to (-27.6010 to -48.5186),
        "ufpr" to (-25.4284 to -49.2733),
        "unb" to (-15.7632 to -47.8703),
        "ufpe" to (-8.0522 to -34.9511),
        "ufc" to (-3.7417 to -38.5364),
        "ufba" to (-12.9714 to -38.5014),
        "ufpa" to (-1.4722 to -48.4556),
        "ufes" to (-20.2764 to -40.3045),
        "ufg" to (-16.6035 to -49.2665),
        "ifsp" to (-23.5284 to -46.6267),
        "uerj" to (-22.9122 to -43.2356),
        "uff" to (-22.9056 to -43.1311),
        "unifesp" to (-23.5975 to -46.6433),
        "ufscar" to (-21.9840 to -47.8814),
        "ufabc" to (-23.6477 to -46.5298),
        "puc-sp" to (-23.5385 to -46.6713),
        "puc sp" to (-23.5385 to -46.6713),
        "puc-rio" to (-22.9791 to -43.2332),
        "puc rio" to (-22.9791 to -43.2332),
        "puc-pr" to (-25.4510 to -49.2514),
        "puc pr" to (-25.4510 to -49.2514),
        "puc-rs" to (-30.0583 to -51.1742),
        "puc rs" to (-30.0583 to -51.1742),
        "puc-mg" to (-19.9242 to -43.9930),
        "puc minas" to (-19.9242 to -43.9930),
        "mackenzie" to (-23.5480 to -46.6520),
        "fgv" to (-23.5638 to -46.6526),
        "insper" to (-23.5986 to -46.6766),
        "ita" to (-23.2120 to -45.8753),
        "sao paulo" to (-23.5505 to -46.6333),
        "são paulo" to (-23.5505 to -46.6333),
        "rio de janeiro" to (-22.9068 to -43.1729),
        "belo horizonte" to (-19.9167 to -43.9345),
        "curitiba" to (-25.4284 to -49.2733),
        "porto alegre" to (-30.0346 to -51.2177),
        "florianopolis" to (-27.5954 to -48.5480),
        "florianópolis" to (-27.5954 to -48.5480),
        "brasilia" to (-15.7975 to -47.8919),
        "brasília" to (-15.7975 to -47.8919),
        "salvador" to (-12.9777 to -38.5016),
        "recife" to (-8.0476 to -34.8770),
        "fortaleza" to (-3.7172 to -38.5433),
        "campinas" to (-22.9056 to -47.0608),
        "sao carlos" to (-22.0175 to -47.8908),
        "são carlos" to (-22.0175 to -47.8908)
    )

    suspend fun fetchWeather(context: Context, preferences: UserPreferences): WeatherInfo? {
        if (!NetworkUtils.isOnline(context)) return null

        return withContext(Dispatchers.IO) {
            try {
                val (coords, locationLabel) = resolveCoordinates(context, preferences)
                val (lat, lon) = coords

                val apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum&timezone=auto"
                val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "GET"
                }

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(stream)
                    val currentWeather = json.optJSONObject("current_weather")
                    val daily = json.optJSONObject("daily")

                    if (currentWeather != null) {
                        val temp = currentWeather.optDouble("temperature", 22.0)
                        val tempInt = temp.toInt()
                        val code = currentWeather.optInt("weathercode", 0)

                        var rainProb: Int? = null
                        var minT: Int? = null
                        var maxT: Int? = null

                        if (daily != null) {
                            val rainProbArray = daily.optJSONArray("precipitation_probability_max")
                            if (rainProbArray != null && rainProbArray.length() > 0) {
                                rainProb = rainProbArray.optInt(0, 0)
                            }
                            val minArray = daily.optJSONArray("temperature_2m_min")
                            if (minArray != null && minArray.length() > 0) {
                                minT = minArray.optDouble(0, 18.0).toInt()
                            }
                            val maxArray = daily.optJSONArray("temperature_2m_max")
                            if (maxArray != null && maxArray.length() > 0) {
                                maxT = maxArray.optDouble(0, 26.0).toInt()
                            }
                        }

                        val isRainCode = code in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)
                        val isRainLikely = isRainCode || (rainProb != null && rainProb >= 40)
                        val isCold = tempInt <= 18
                        val isHot = tempInt >= 28

                        val (conditionText, emoji) = parseConditionAndEmoji(code, isRainLikely)

                        // Greeting contextual tip as requested:
                        // "hoje vai chover, leve seu casaco e seeu guarda chuva"
                        val greetingTip = when {
                            isRainLikely -> "Hoje vai chover, leve seu casaco e seu guarda-chuva!"
                            isCold -> "Dia frio hoje! Não esqueça de levar seu casaco para a aula."
                            isHot -> "Dia de calor! Leve uma garrafa de água para se hidratar no campus."
                            code in 1..3 -> "Tempo parcialmente nublado e agradável para estudar hoje."
                            code == 0 -> "Dia ensolarado e ótimo clima para assistir às aulas!"
                            else -> "Tempo estável e tranquilo para focar nos estudos."
                        }

                        val studyTip = when {
                            isRainLikely -> if (rainProb != null) "Probabilidade de chuva: $rainProb%. Proteja seus cadernos e livros!" else "Leve guarda-chuva e capa para não se molhar no campus."
                            isCold -> "Mínima de ${minT ?: tempInt}°C. Mantenha-se aquecido durante os estudos."
                            isHot -> "Máxima prevista de ${maxT ?: tempInt}°C. Use roupas frescas e confortáveis."
                            else -> "Clima favorável para as aulas e tarefas do dia."
                        }

                        return@withContext WeatherInfo(
                            locationLabel = locationLabel,
                            temperatureStr = "${tempInt}°C",
                            tempInt = tempInt,
                            condition = conditionText,
                            iconEmoji = emoji,
                            isRaining = isRainLikely,
                            isCold = isCold,
                            isHot = isHot,
                            rainProbability = rainProb,
                            minTemp = minT,
                            maxTemp = maxT,
                            greetingTip = greetingTip,
                            studyTip = studyTip
                        )
                    }
                }
                null
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun resolveCoordinates(
        context: Context,
        preferences: UserPreferences
    ): Pair<Pair<Double, Double>, String> {
        val mode = preferences.weatherLocationMode
        val inst = preferences.institution.trim()
        val customCity = preferences.weatherLocationCity.trim()

        // 1. If explicitly set to INSTITUTION
        if (mode == "INSTITUTION" && inst.isNotEmpty()) {
            val coords = findCoordinatesForText(inst)
            if (coords != null) {
                return coords to inst
            }
        }

        // 2. If explicitly set to CUSTOM city
        if (mode == "CUSTOM" && customCity.isNotEmpty()) {
            val coords = findCoordinatesForText(customCity)
            if (coords != null) {
                return coords to customCity
            }
        }

        // 3. Default / AUTO mode: Try current GPS / Location if permission granted
        val gpsLocation = getLastKnownGpsLocation(context)
        if (gpsLocation != null) {
            val label = if (inst.isNotEmpty()) "$inst (Localização Atual)" else "Localização Atual"
            return (gpsLocation.latitude to gpsLocation.longitude) to label
        }

        // Fallback: If institution exists, use institution location
        if (inst.isNotEmpty()) {
            val coords = findCoordinatesForText(inst)
            if (coords != null) {
                return coords to inst
            }
        }

        // Ultimate default fallback: São Paulo
        return (-23.5505 to -46.6333) to (if (inst.isNotEmpty()) inst else "São Paulo (Padrão)")
    }

    private suspend fun findCoordinatesForText(query: String): Pair<Double, Double>? {
        val clean = query.trim().lowercase()
        // Check local dictionary
        for ((key, pair) in KNOWN_LOCATIONS) {
            if (clean.contains(key) || key.contains(clean)) {
                return pair
            }
        }

        // Query Open-Meteo Geocoding REST API (free, open, no key required)
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=pt&format=json")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val results = json.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    val first = results.getJSONObject(0)
                    val lat = first.optDouble("latitude")
                    val lon = first.optDouble("longitude")
                    lat to lon
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownGpsLocation(context: Context): Location? {
        return try {
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasFine = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasCoarse && !hasFine) return null

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            val gpsLoc = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null

            val netLoc = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            gpsLoc ?: netLoc
        } catch (_: Exception) {
            null
        }
    }

    private fun parseConditionAndEmoji(code: Int, isRainLikely: Boolean): Pair<String, String> {
        return when {
            code == 0 -> "Ensolarado" to "☀️"
            code in 1..2 -> "Parcialmente Nublado" to "🌤️"
            code == 3 -> "Nublado" to "☁️"
            code in listOf(45, 48) -> "Nevoeiro" to "🌫️"
            code in listOf(51, 53, 55) -> "Garoa / Chuva Fina" to "🌦️"
            code in listOf(61, 63, 65) -> "Chuva" to "🌧️"
            code in listOf(80, 81, 82) -> "Pancadas de Chuva" to "🌦️"
            code in listOf(95, 96, 99) -> "Trovoada / Tempestade" to "🌩️"
            isRainLikely -> "Chuva Prevista" to "🌧️"
            else -> "Tempo Agradável" to "🌤️"
        }
    }
}
