package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.api.RetrofitClient
import com.example.data.api.model.AlAdhanResponse
import com.example.data.local.dataStore
import com.example.domain.model.AVAILABLE_CALCULATION_METHODS
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.PrayerTime
import com.example.domain.model.PrayerTimeConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

interface PrayerTimeRepository {
    suspend fun fetchPrayerTimes(lat: Double, lng: Double, config: PrayerTimeConfig, locationNameAr: String? = null, locationNameEn: String? = null): Result<DailyPrayerTimes>
    fun getCachedPrayerTimes(): Flow<DailyPrayerTimes?>
}

class PrayerTimeRepositoryImpl(private val context: Context) : PrayerTimeRepository {

    private val api = RetrofitClient.alAdhanApi
    private val dataStore = context.dataStore
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val responseAdapter = moshi.adapter(AlAdhanResponse::class.java)

    companion object {
        val CACHED_PRAYER_RESPONSE = stringPreferencesKey("cached_prayer_response")
        val CACHED_TIMESTAMP = stringPreferencesKey("cached_prayer_timestamp")
        val CACHED_LOCATION_AR = stringPreferencesKey("cached_prayer_location_ar")
        val CACHED_LOCATION_EN = stringPreferencesKey("cached_prayer_location_en")
    }

    override suspend fun fetchPrayerTimes(
        lat: Double,
        lng: Double,
        config: PrayerTimeConfig,
        locationNameAr: String?,
        locationNameEn: String?
    ): Result<DailyPrayerTimes> {
        return try {
            val dateSdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            if (config.timezone.isNotBlank() && config.timezone != "UTC") {
                try {
                    dateSdf.timeZone = TimeZone.getTimeZone(config.timezone)
                } catch (_: Exception) {}
            }
            val dateStr = dateSdf.format(Date())

            // Build tune string: Imsak,Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha,Midnight
            val tuneStr = "0,${config.fajrAdjustment},${config.sunriseAdjustment},${config.dhuhrAdjustment},${config.asrAdjustment},0,${config.maghribAdjustment},${config.ishaAdjustment},0"
            
            val response = api.getTimings(
                date = dateStr,
                latitude = lat,
                longitude = lng,
                method = config.calculationMethod,
                school = config.asrMadhab,
                tune = tuneStr,
                timezone = config.timezone
            )

            val locAr = locationNameAr ?: config.selectedCity.fullDisplayNameAr
            val locEn = locationNameEn ?: config.selectedCity.fullDisplayNameEn
            val currentTimestamp = System.currentTimeMillis()
            
            // Cache successful response locally
            dataStore.edit { prefs ->
                prefs[CACHED_PRAYER_RESPONSE] = responseAdapter.toJson(response)
                prefs[CACHED_TIMESTAMP] = currentTimestamp.toString()
                prefs[CACHED_LOCATION_AR] = locAr
                prefs[CACHED_LOCATION_EN] = locEn
            }
            
            val methodName = AVAILABLE_CALCULATION_METHODS.find { it.id == config.calculationMethod }?.nameAr
                ?: response.data.meta.method.name

            Result.success(mapResponseToDomain(response, currentTimestamp, locAr, locEn, methodName))
        } catch (e: Exception) {
            Log.e("PrayerTimeRepo", "Failed to fetch times online: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getCachedPrayerTimes(): Flow<DailyPrayerTimes?> {
        return dataStore.data.map { prefs ->
            val json = prefs[CACHED_PRAYER_RESPONSE]
            val timestampStr = prefs[CACHED_TIMESTAMP]
            val locAr = prefs[CACHED_LOCATION_AR] ?: "أبو جرين، محافظة حلب، سوريا"
            val locEn = prefs[CACHED_LOCATION_EN] ?: "Abu Jurayn, Aleppo Governorate, Syria"
            
            if (json != null && timestampStr != null) {
                try {
                    val response = responseAdapter.fromJson(json)
                    response?.let {
                        val ts = timestampStr.toLongOrNull() ?: 0L
                        mapResponseToDomain(it, ts, locAr, locEn, it.data.meta.method.name)
                    }
                } catch (e: Exception) {
                    Log.e("PrayerTimeRepo", "Failed to decode cached prayers", e)
                    null
                }
            } else {
                null
            }
        }
    }
    
    private fun mapResponseToDomain(
        response: AlAdhanResponse,
        lastUpdated: Long,
        locationNameAr: String,
        locationNameEn: String,
        methodName: String
    ): DailyPrayerTimes {
        val timings = response.data.timings
        val date = response.data.date
        
        // Checking if the cached data is from today based on date string comparison
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        val isStale = date.gregorian.date.isNotBlank() && date.gregorian.date != todayStr

        fun cleanTime(raw: String): String = raw.split(" ")[0].trim()
        fun parseMinutes(raw: String): Int {
            return try {
                val clean = cleanTime(raw)
                val parts = clean.split(":")
                parts[0].toInt() * 60 + parts[1].toInt()
            } catch (_: Exception) {
                0
            }
        }

        val fajrClean = cleanTime(timings.fajr)
        val sunriseClean = cleanTime(timings.sunrise)
        val dhuhrClean = cleanTime(timings.dhuhr)
        val asrClean = cleanTime(timings.asr)
        val maghribClean = cleanTime(timings.maghrib)
        val ishaClean = cleanTime(timings.isha)

        val hijriDisplay = if (date.hijri.day.isNotBlank()) {
            val monthAr = date.hijri.month.ar.ifBlank { date.hijri.month.en }
            "${date.hijri.day} $monthAr ${date.hijri.year} هـ"
        } else {
            date.hijri.date
        }

        return DailyPrayerTimes(
            fajr = PrayerTime("fajr", "الفجر", "Fajr", fajrClean, parseMinutes(fajrClean)),
            sunrise = PrayerTime("sunrise", "الشروق", "Sunrise", sunriseClean, parseMinutes(sunriseClean)),
            dhuhr = PrayerTime("dhuhr", "الظهر", "Dhuhr", dhuhrClean, parseMinutes(dhuhrClean)),
            asr = PrayerTime("asr", "العصر", "Asr", asrClean, parseMinutes(asrClean)),
            maghrib = PrayerTime("maghrib", "المغرب", "Maghrib", maghribClean, parseMinutes(maghribClean)),
            isha = PrayerTime("isha", "العشاء", "Isha", ishaClean, parseMinutes(ishaClean)),
            hijriDateStr = hijriDisplay,
            gregorianDateStr = date.readable.ifBlank { date.gregorian.date },
            lastUpdated = lastUpdated,
            isStale = isStale,
            locationNameAr = locationNameAr,
            locationNameEn = locationNameEn,
            calculationMethodName = methodName
        )
    }
}
