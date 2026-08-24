package com.example.domain.model

data class PrayerTime(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val timeStr: String,
    val rawMinutesOfDay: Int = 0,
    val isNext: Boolean = false
)

data class DailyPrayerTimes(
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val hijriDateStr: String,
    val gregorianDateStr: String,
    val lastUpdated: Long, // timestamp in ms
    val isStale: Boolean = false, // If the times are not from today due to offline/cached state
    val locationNameAr: String = "أبو جرين، محافظة حلب، سوريا",
    val locationNameEn: String = "Abu Jurayn, Aleppo Governorate, Syria",
    val calculationMethodName: String = "جامعة أم القرى - مكة المكرمة"
) {
    fun toList(): List<PrayerTime> = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
}

data class CityModel(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val countryEn: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String
) {
    val fullDisplayNameAr: String get() = if (countryAr.isNotEmpty()) "$nameAr، $countryAr" else nameAr
    val fullDisplayNameEn: String get() = if (countryEn.isNotEmpty()) "$nameEn, $countryEn" else nameEn
}

data class CalculationMethodInfo(
    val id: Int,
    val nameAr: String,
    val nameEn: String
)

data class PrayerTimeConfig(
    val calculationMethod: Int = 4, // 4 = Umm Al-Qura, 3 = MWL, 5 = Egyptian, 2 = ISNA, 1 = Karachi
    val asrMadhab: Int = 0, // 0 = Standard (Shafii, Maliki, Hanbali), 1 = Hanafi
    val timezone: String = "Asia/Damascus",
    val useGps: Boolean = false,
    val selectedCity: CityModel = DEFAULT_CITY_ABU_JURAYN,
    val fajrAdjustment: Int = 0,
    val sunriseAdjustment: Int = 0,
    val dhuhrAdjustment: Int = 0,
    val asrAdjustment: Int = 0,
    val maghribAdjustment: Int = 0,
    val ishaAdjustment: Int = 0
) {
    val effectiveLatitude: Double get() = selectedCity.latitude
    val effectiveLongitude: Double get() = selectedCity.longitude

    fun toTuneString(): String {
        return "0,$fajrAdjustment,$sunriseAdjustment,$dhuhrAdjustment,$asrAdjustment,0,$maghribAdjustment,$ishaAdjustment,0"
    }
}

val DEFAULT_CITY_ABU_JURAYN = CityModel(
    id = "abu_jurayn",
    nameAr = "أبو جرين",
    nameEn = "Abu Jurayn",
    countryAr = "محافظة حلب، سوريا",
    countryEn = "Aleppo Governorate, Syria",
    latitude = 36.155,
    longitude = 37.644,
    timezone = "Asia/Damascus"
)

val POPULAR_CITIES = listOf(
    DEFAULT_CITY_ABU_JURAYN,
    CityModel("aleppo", "حلب", "Aleppo", "سوريا", "Syria", 36.2021, 37.1343, "Asia/Damascus"),
    CityModel("damascus", "دمشق", "Damascus", "سوريا", "Syria", 33.5138, 36.2765, "Asia/Damascus"),
    CityModel("homs", "حمص", "Homs", "سوريا", "Syria", 34.7324, 36.7137, "Asia/Damascus"),
    CityModel("latakia", "اللاذقية", "Latakia", "سوريا", "Syria", 35.5317, 35.7901, "Asia/Damascus"),
    CityModel("makkah", "مكة المكرمة", "Makkah", "المملكة العربية السعودية", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
    CityModel("madinah", "المدينة المنورة", "Madinah", "المملكة العربية السعودية", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
    CityModel("riyadh", "الرياض", "Riyadh", "المملكة العربية السعودية", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
    CityModel("jerusalem", "القدس الشريف", "Jerusalem", "فلسطين", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem"),
    CityModel("cairo", "القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
    CityModel("amman", "عمّان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
    CityModel("beirut", "بيروت", "Beirut", "لبنان", "Lebanon", 33.8938, 35.5018, "Asia/Beirut"),
    CityModel("baghdad", "بغداد", "Baghdad", "العراق", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),
    CityModel("istanbul", "إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
    CityModel("dubai", "دبي", "Dubai", "الإمارات العربية المتحدة", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
    CityModel("doha", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
    CityModel("kuwait", "الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait"),
    CityModel("rabat", "الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416, "Africa/Casablanca")
)

val AVAILABLE_CALCULATION_METHODS = listOf(
    CalculationMethodInfo(4, "جامعة أم القرى - مكة المكرمة", "Umm Al-Qura University, Makkah"),
    CalculationMethodInfo(3, "رابطة العالم الإسلامي", "Muslim World League"),
    CalculationMethodInfo(5, "الهيئة المصرية العامة للمساحة", "Egyptian General Authority of Survey"),
    CalculationMethodInfo(2, "الاتحاد الإسلامي لأمريكا الشمالية (ISNA)", "Islamic Society of North America (ISNA)"),
    CalculationMethodInfo(1, "جامعة العلوم الإسلامية بكراتشي", "University of Islamic Sciences, Karachi"),
    CalculationMethodInfo(13, "رئاسة الشؤون الدينية التركية (Diyanet)", "Presidency of Religious Affairs, Turkey"),
    CalculationMethodInfo(8, "منطقة الخليج العربي", "Gulf Region")
)

val COMMON_TIMEZONES = listOf(
    "Asia/Damascus",
    "Asia/Riyadh",
    "Asia/Jerusalem",
    "Africa/Cairo",
    "Asia/Amman",
    "Asia/Beirut",
    "Asia/Baghdad",
    "Europe/Istanbul",
    "Asia/Dubai",
    "Asia/Qatar",
    "Asia/Kuwait",
    "Africa/Casablanca",
    "Europe/London",
    "America/New_York",
    "UTC"
)
