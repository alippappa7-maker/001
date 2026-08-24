package com.example.data.api.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class AlAdhanResponse(
    val code: Int = 200,
    val status: String = "OK",
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
    @Json(name = "Sunset") val sunset: String? = null,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String? = null,
    @Json(name = "Midnight") val midnight: String? = null
)

@JsonClass(generateAdapter = true)
data class AlAdhanDate(
    val readable: String = "",
    val timestamp: String = "",
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

@JsonClass(generateAdapter = true)
data class HijriDate(
    val date: String = "",
    val format: String? = null,
    val day: String = "",
    val weekday: Weekday? = null,
    val month: HijriMonth,
    val year: String = ""
)

@JsonClass(generateAdapter = true)
data class GregorianDate(
    val date: String = "",
    val format: String? = null,
    val day: String = "",
    val weekday: Weekday? = null,
    val month: GregorianMonth,
    val year: String = ""
)

@JsonClass(generateAdapter = true)
data class Weekday(
    val en: String = "",
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class HijriMonth(
    val number: Int = 1,
    val en: String = "",
    val ar: String = ""
)

@JsonClass(generateAdapter = true)
data class GregorianMonth(
    val number: Int = 1,
    val en: String = ""
)

@JsonClass(generateAdapter = true)
data class AlAdhanMeta(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "Asia/Damascus",
    val method: CalculationMethod = CalculationMethod(4, "Umm Al-Qura")
)

@JsonClass(generateAdapter = true)
data class CalculationMethod(
    val id: Int = 4,
    val name: String = "Umm Al-Qura"
)

fun AlAdhanResponse.toDailyPrayerTimes(
    locationAr: String = "أبو جرين، محافظة حلب، سوريا",
    locationEn: String = "Abu Jurayn, Aleppo Governorate, Syria",
    isStale: Boolean = false,
    lastUpdated: Long = System.currentTimeMillis()
): com.example.domain.model.DailyPrayerTimes {
    fun clean(t: String): String = t.split(" ")[0].trim()
    fun parseMins(t: String): Int {
        return try {
            val parts = t.split(" ")[0].trim().split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (_: Exception) { 0 }
    }

    val timings = this.data.timings
    val date = this.data.date
    val fajr = clean(timings.fajr)
    val sunrise = clean(timings.sunrise)
    val dhuhr = clean(timings.dhuhr)
    val asr = clean(timings.asr)
    val maghrib = clean(timings.maghrib)
    val isha = clean(timings.isha)

    val hijriDisplay = if (date.hijri.day.isNotBlank()) {
        val monthAr = date.hijri.month.ar.ifBlank { date.hijri.month.en }
        "${date.hijri.day} $monthAr ${date.hijri.year} هـ"
    } else {
        date.hijri.date
    }

    return com.example.domain.model.DailyPrayerTimes(
        fajr = com.example.domain.model.PrayerTime("fajr", "الفجر", "Fajr", fajr, parseMins(fajr)),
        sunrise = com.example.domain.model.PrayerTime("sunrise", "الشروق", "Sunrise", sunrise, parseMins(sunrise)),
        dhuhr = com.example.domain.model.PrayerTime("dhuhr", "الظهر", "Dhuhr", dhuhr, parseMins(dhuhr)),
        asr = com.example.domain.model.PrayerTime("asr", "العصر", "Asr", asr, parseMins(asr)),
        maghrib = com.example.domain.model.PrayerTime("maghrib", "المغرب", "Maghrib", maghrib, parseMins(maghrib)),
        isha = com.example.domain.model.PrayerTime("isha", "العشاء", "Isha", isha, parseMins(isha)),
        hijriDateStr = hijriDisplay,
        gregorianDateStr = date.readable.ifBlank { date.gregorian.date },
        lastUpdated = lastUpdated,
        isStale = isStale,
        locationNameAr = locationAr,
        locationNameEn = locationEn,
        calculationMethodName = this.data.meta.method.name
    )
}

