package com.example.data.api.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class AlAdhanResponse(
    val code: Int,
    val status: String,
    val data: AlAdhanData
)

@JsonClass(generateAdapter = true)
data class AlAdhanData(
    val timings: AlAdhanTimings,
    val date: AlAdhanDate,
    val meta: AlAdhanMeta
)

@JsonClass(generateAdapter = true)
data class AlAdhanTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String,
    @Json(name = "Midnight") val midnight: String
)

@JsonClass(generateAdapter = true)
data class AlAdhanDate(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: Weekday,
    val month: HijriMonth,
    val year: String
)

@JsonClass(generateAdapter = true)
data class GregorianDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: Weekday,
    val month: GregorianMonth,
    val year: String
)

@JsonClass(generateAdapter = true)
data class Weekday(
    val en: String,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

@JsonClass(generateAdapter = true)
data class GregorianMonth(
    val number: Int,
    val en: String
)

@JsonClass(generateAdapter = true)
data class AlAdhanMeta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val method: CalculationMethod
)

@JsonClass(generateAdapter = true)
data class CalculationMethod(
    val id: Int,
    val name: String
)
