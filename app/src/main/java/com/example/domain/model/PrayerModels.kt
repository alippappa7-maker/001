package com.example.domain.model

data class DailyPrayerTimes(
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val hijriDateStr: String,
    val gregorianDateStr: String,
    val lastUpdated: Long, // timestamp
    val isStale: Boolean = false // If the times are not from today due to offline
) {
    fun toList(): List<PrayerTime> = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
}

data class PrayerTimeConfig(
    val calculationMethod: Int = 2, // ISNA by default, or 4 for Umm Al Qura
    val asrMadhab: Int = 0, // 0 = Shafii, 1 = Hanafi
    val manualCityMode: Boolean = true,
    val manualLatitude: Double = 36.035, // Abu Jirin, Aleppo
    val manualLongitude: Double = 37.452
)
