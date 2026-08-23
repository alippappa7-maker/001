package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.api.RetrofitClient
import com.example.data.api.model.AlAdhanResponse
import com.example.data.local.dataStore
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.PrayerTime
import com.example.domain.model.PrayerTimeConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface PrayerTimeRepository {
    suspend fun fetchPrayerTimes(lat: Double, lng: Double, config: PrayerTimeConfig): Result<DailyPrayerTimes>
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
    }

    override suspend fun fetchPrayerTimes(lat: Double, lng: Double, config: PrayerTimeConfig): Result<DailyPrayerTimes> {
        return try {
            val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
            
            val response = api.getTimings(
                date = dateStr,
                latitude = lat,
                longitude = lng,
                method = config.calculationMethod,
                school = config.asrMadhab
            )
            
            // Cache successful response
            dataStore.edit { prefs ->
                prefs[CACHED_PRAYER_RESPONSE] = responseAdapter.toJson(response)
                prefs[CACHED_TIMESTAMP] = System.currentTimeMillis().toString()
            }
            
            Result.success(mapResponseToDomain(response, System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.e("PrayerTimeRepo", "Failed to fetch times", e)
            Result.failure(e)
        }
    }

    override fun getCachedPrayerTimes(): Flow<DailyPrayerTimes?> {
        return dataStore.data.map { prefs ->
            val json = prefs[CACHED_PRAYER_RESPONSE]
            val timestampStr = prefs[CACHED_TIMESTAMP]
            
            if (json != null && timestampStr != null) {
                try {
                    val response = responseAdapter.fromJson(json)
                    response?.let {
                        val ts = timestampStr.toLongOrNull() ?: 0L
                        mapResponseToDomain(it, ts)
                    }
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
    
    private fun mapResponseToDomain(response: AlAdhanResponse, lastUpdated: Long): DailyPrayerTimes {
        val timings = response.data.timings
        val date = response.data.date
        
        // Checking if the cached data is from today based on gregorian date string
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        val isStale = date.gregorian.date != todayStr

        return DailyPrayerTimes(
            fajr = PrayerTime("fajr", "الفجر", "Fajr", timings.fajr),
            sunrise = PrayerTime("sunrise", "الشروق", "Sunrise", timings.sunrise),
            dhuhr = PrayerTime("dhuhr", "الظهر", "Dhuhr", timings.dhuhr),
            asr = PrayerTime("asr", "العصر", "Asr", timings.asr),
            maghrib = PrayerTime("maghrib", "المغرب", "Maghrib", timings.maghrib),
            isha = PrayerTime("isha", "العشاء", "Isha", timings.isha),
            hijriDateStr = "${date.hijri.day} ${date.hijri.month.ar} ${date.hijri.year}",
            gregorianDateStr = date.readable,
            lastUpdated = lastUpdated,
            isStale = isStale
        )
    }
}
