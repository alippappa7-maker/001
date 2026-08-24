package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.CityModel
import com.example.domain.model.DEFAULT_CITY_ABU_JURAYN
import com.example.domain.model.NotificationSettings
import com.example.domain.model.POPULAR_CITIES
import com.example.domain.model.PrayerTimeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
        
        val PRAYER_CALC_METHOD = intPreferencesKey("prayer_calc_method")
        val PRAYER_ASR_MADHAB = intPreferencesKey("prayer_asr_madhab")
        val PRAYER_TIMEZONE = stringPreferencesKey("prayer_timezone")
        val PRAYER_USE_GPS = booleanPreferencesKey("prayer_use_gps")
        val PRAYER_CITY_ID = stringPreferencesKey("prayer_city_id")
        val PRAYER_CITY_NAME_AR = stringPreferencesKey("prayer_city_name_ar")
        val PRAYER_CITY_NAME_EN = stringPreferencesKey("prayer_city_name_en")
        val PRAYER_CITY_COUNTRY_AR = stringPreferencesKey("prayer_city_country_ar")
        val PRAYER_CITY_COUNTRY_EN = stringPreferencesKey("prayer_city_country_en")
        val PRAYER_CITY_LAT = doublePreferencesKey("prayer_city_lat")
        val PRAYER_CITY_LNG = doublePreferencesKey("prayer_city_lng")
        val PRAYER_CITY_TIMEZONE = stringPreferencesKey("prayer_city_timezone")
        
        val PRAYER_ADJ_FAJR = intPreferencesKey("prayer_adj_fajr")
        val PRAYER_ADJ_SUNRISE = intPreferencesKey("prayer_adj_sunrise")
        val PRAYER_ADJ_DHUHR = intPreferencesKey("prayer_adj_dhuhr")
        val PRAYER_ADJ_ASR = intPreferencesKey("prayer_adj_asr")
        val PRAYER_ADJ_MAGHRIB = intPreferencesKey("prayer_adj_maghrib")
        val PRAYER_ADJ_ISHA = intPreferencesKey("prayer_adj_isha")

        // Notification settings keys
        val NOTIF_MASTER = booleanPreferencesKey("notif_master")
        val NOTIF_FAJR = booleanPreferencesKey("notif_fajr")
        val NOTIF_SUNRISE = booleanPreferencesKey("notif_sunrise")
        val NOTIF_DHUHR = booleanPreferencesKey("notif_dhuhr")
        val NOTIF_ASR = booleanPreferencesKey("notif_asr")
        val NOTIF_MAGHRIB = booleanPreferencesKey("notif_maghrib")
        val NOTIF_ISHA = booleanPreferencesKey("notif_isha")
        val NOTIF_DHIKR_ENABLED = booleanPreferencesKey("notif_dhikr_enabled")
        val NOTIF_DHIKR_HOUR = intPreferencesKey("notif_dhikr_hour")
        val NOTIF_DHIKR_MINUTE = intPreferencesKey("notif_dhikr_minute")
        val NOTIF_SOUND_ENABLED = booleanPreferencesKey("notif_sound_enabled")
        val NOTIF_VIBRATE_ENABLED = booleanPreferencesKey("notif_vibrate_enabled")
    }

    val isDarkModeFlow: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE]
    }

    val languageFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LANGUAGE]
    }
    
    val hasSeenWelcomeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_SEEN_WELCOME] ?: false
    }

    val prayerConfigFlow: Flow<PrayerTimeConfig> = dataStore.data.map { prefs ->
        val method = prefs[PRAYER_CALC_METHOD] ?: 4
        val madhab = prefs[PRAYER_ASR_MADHAB] ?: 0
        val timezone = prefs[PRAYER_TIMEZONE] ?: "Asia/Damascus"
        val useGps = prefs[PRAYER_USE_GPS] ?: false
        
        val cityId = prefs[PRAYER_CITY_ID] ?: DEFAULT_CITY_ABU_JURAYN.id
        val city = POPULAR_CITIES.find { it.id == cityId } ?: CityModel(
            id = cityId,
            nameAr = prefs[PRAYER_CITY_NAME_AR] ?: DEFAULT_CITY_ABU_JURAYN.nameAr,
            nameEn = prefs[PRAYER_CITY_NAME_EN] ?: DEFAULT_CITY_ABU_JURAYN.nameEn,
            countryAr = prefs[PRAYER_CITY_COUNTRY_AR] ?: DEFAULT_CITY_ABU_JURAYN.countryAr,
            countryEn = prefs[PRAYER_CITY_COUNTRY_EN] ?: DEFAULT_CITY_ABU_JURAYN.countryEn,
            latitude = prefs[PRAYER_CITY_LAT] ?: DEFAULT_CITY_ABU_JURAYN.latitude,
            longitude = prefs[PRAYER_CITY_LNG] ?: DEFAULT_CITY_ABU_JURAYN.longitude,
            timezone = prefs[PRAYER_CITY_TIMEZONE] ?: DEFAULT_CITY_ABU_JURAYN.timezone
        )

        PrayerTimeConfig(
            calculationMethod = method,
            asrMadhab = madhab,
            timezone = timezone,
            useGps = useGps,
            selectedCity = city,
            fajrAdjustment = prefs[PRAYER_ADJ_FAJR] ?: 0,
            sunriseAdjustment = prefs[PRAYER_ADJ_SUNRISE] ?: 0,
            dhuhrAdjustment = prefs[PRAYER_ADJ_DHUHR] ?: 0,
            asrAdjustment = prefs[PRAYER_ADJ_ASR] ?: 0,
            maghribAdjustment = prefs[PRAYER_ADJ_MAGHRIB] ?: 0,
            ishaAdjustment = prefs[PRAYER_ADJ_ISHA] ?: 0
        )
    }

    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = languageCode
        }
    }
    
    suspend fun setHasSeenWelcome(hasSeen: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_WELCOME] = hasSeen
        }
    }

    suspend fun setCalculationMethod(methodId: Int) {
        dataStore.edit { prefs ->
            prefs[PRAYER_CALC_METHOD] = methodId
        }
    }

    suspend fun setAsrMadhab(madhabId: Int) {
        dataStore.edit { prefs ->
            prefs[PRAYER_ASR_MADHAB] = madhabId
        }
    }

    suspend fun setTimezone(timezone: String) {
        dataStore.edit { prefs ->
            prefs[PRAYER_TIMEZONE] = timezone
        }
    }

    suspend fun setUseGps(useGps: Boolean) {
        dataStore.edit { prefs ->
            prefs[PRAYER_USE_GPS] = useGps
        }
    }

    suspend fun setSelectedCity(city: CityModel) {
        dataStore.edit { prefs ->
            prefs[PRAYER_CITY_ID] = city.id
            prefs[PRAYER_CITY_NAME_AR] = city.nameAr
            prefs[PRAYER_CITY_NAME_EN] = city.nameEn
            prefs[PRAYER_CITY_COUNTRY_AR] = city.countryAr
            prefs[PRAYER_CITY_COUNTRY_EN] = city.countryEn
            prefs[PRAYER_CITY_LAT] = city.latitude
            prefs[PRAYER_CITY_LNG] = city.longitude
            prefs[PRAYER_CITY_TIMEZONE] = city.timezone
            prefs[PRAYER_TIMEZONE] = city.timezone
        }
    }

    suspend fun setPrayerAdjustment(prayerId: String, deltaMinutes: Int) {
        dataStore.edit { prefs ->
            when (prayerId.lowercase()) {
                "fajr" -> prefs[PRAYER_ADJ_FAJR] = deltaMinutes
                "sunrise" -> prefs[PRAYER_ADJ_SUNRISE] = deltaMinutes
                "dhuhr" -> prefs[PRAYER_ADJ_DHUHR] = deltaMinutes
                "asr" -> prefs[PRAYER_ADJ_ASR] = deltaMinutes
                "maghrib" -> prefs[PRAYER_ADJ_MAGHRIB] = deltaMinutes
                "isha" -> prefs[PRAYER_ADJ_ISHA] = deltaMinutes
            }
        }
    }

    suspend fun resetPrayerAdjustments() {
        dataStore.edit { prefs ->
            prefs[PRAYER_ADJ_FAJR] = 0
            prefs[PRAYER_ADJ_SUNRISE] = 0
            prefs[PRAYER_ADJ_DHUHR] = 0
            prefs[PRAYER_ADJ_ASR] = 0
            prefs[PRAYER_ADJ_MAGHRIB] = 0
            prefs[PRAYER_ADJ_ISHA] = 0
        }
    }

    val notificationSettingsFlow: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        NotificationSettings(
            masterEnabled = prefs[NOTIF_MASTER] ?: true,
            fajrEnabled = prefs[NOTIF_FAJR] ?: true,
            sunriseEnabled = prefs[NOTIF_SUNRISE] ?: false,
            dhuhrEnabled = prefs[NOTIF_DHUHR] ?: true,
            asrEnabled = prefs[NOTIF_ASR] ?: true,
            maghribEnabled = prefs[NOTIF_MAGHRIB] ?: true,
            ishaEnabled = prefs[NOTIF_ISHA] ?: true,
            dhikrReminderEnabled = prefs[NOTIF_DHIKR_ENABLED] ?: true,
            dhikrReminderHour = prefs[NOTIF_DHIKR_HOUR] ?: 8,
            dhikrReminderMinute = prefs[NOTIF_DHIKR_MINUTE] ?: 30,
            soundEnabled = prefs[NOTIF_SOUND_ENABLED] ?: false,
            vibrateEnabled = prefs[NOTIF_VIBRATE_ENABLED] ?: true
        )
    }

    suspend fun setNotificationMaster(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIF_MASTER] = enabled
        }
    }

    suspend fun setPrayerNotificationEnabled(prayerId: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            when (prayerId.lowercase()) {
                "fajr" -> prefs[NOTIF_FAJR] = enabled
                "sunrise" -> prefs[NOTIF_SUNRISE] = enabled
                "dhuhr" -> prefs[NOTIF_DHUHR] = enabled
                "asr" -> prefs[NOTIF_ASR] = enabled
                "maghrib" -> prefs[NOTIF_MAGHRIB] = enabled
                "isha" -> prefs[NOTIF_ISHA] = enabled
            }
        }
    }

    suspend fun setDhikrReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIF_DHIKR_ENABLED] = enabled
        }
    }

    suspend fun setDhikrReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[NOTIF_DHIKR_HOUR] = hour
            prefs[NOTIF_DHIKR_MINUTE] = minute
        }
    }

    suspend fun setNotificationSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIF_SOUND_ENABLED] = enabled
        }
    }

    suspend fun setNotificationVibrateEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[NOTIF_VIBRATE_ENABLED] = enabled
        }
    }
}
