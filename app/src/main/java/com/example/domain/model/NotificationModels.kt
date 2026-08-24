package com.example.domain.model

data class NotificationSettings(
    val masterEnabled: Boolean = true,
    val fajrEnabled: Boolean = true,
    val sunriseEnabled: Boolean = false,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val dhikrReminderEnabled: Boolean = true,
    val dhikrReminderHour: Int = 8,
    val dhikrReminderMinute: Int = 30,
    val soundEnabled: Boolean = false,
    val vibrateEnabled: Boolean = true
) {
    fun isPrayerEnabled(prayerId: String): Boolean {
        if (!masterEnabled) return false
        return when (prayerId.lowercase()) {
            "fajr" -> fajrEnabled
            "sunrise" -> sunriseEnabled
            "dhuhr" -> dhuhrEnabled
            "asr" -> asrEnabled
            "maghrib" -> maghribEnabled
            "isha" -> ishaEnabled
            else -> false
        }
    }
}
