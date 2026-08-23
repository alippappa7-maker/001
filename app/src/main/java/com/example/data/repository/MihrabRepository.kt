package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.data.local.dataStore
import com.example.domain.model.Ayah
import com.example.domain.model.DailyProgress
import com.example.domain.model.PrayerTime
import com.example.domain.model.Zikr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MihrabRepository {
    fun getAzkar(query: String = ""): Flow<List<Zikr>>
    fun getFavorites(): Flow<List<Zikr>>
    suspend fun toggleFavorite(zikrId: String)
    
    fun getDailyAyah(): Flow<Ayah>
    fun getPrayerTimes(): Flow<List<PrayerTime>>
    
    fun getDailyProgress(): Flow<DailyProgress>
    suspend fun updateDailyProgress(completed: Int, total: Int)
}

class MihrabRepositoryImpl(private val context: Context) : MihrabRepository {
    private val dataStore = context.dataStore

    companion object {
        val FAVORITE_ZIKR_IDS = stringSetPreferencesKey("favorite_zikr_ids")
        val DAILY_COMPLETED_TASKS = intPreferencesKey("daily_completed_tasks")
        val DAILY_TOTAL_TASKS = intPreferencesKey("daily_total_tasks")
    }

    // Static data for now, easily replaceable by DB/API
    private val allAzkar = listOf(
        Zikr(
            id = "z1",
            textAr = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            textEn = "Glory be to Allah and His is the praise",
            sourceAr = "صحيح البخاري: 6405",
            sourceEn = "Sahih Al-Bukhari: 6405",
            count = 100,
            category = "General"
        ),
        Zikr(
            id = "z2",
            textAr = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            textEn = "I seek the forgiveness of Allah and repent to Him",
            sourceAr = "صحيح مسلم: 2702",
            sourceEn = "Sahih Muslim: 2702",
            count = 100,
            category = "General"
        ),
        Zikr(
            id = "z3",
            textAr = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            textEn = "None has the right to be worshipped but Allah alone, Who has no partner. His is the dominion and His is the praise, and He is Able to do all things.",
            sourceAr = "صحيح البخاري: 3293",
            sourceEn = "Sahih Al-Bukhari: 3293",
            count = 10,
            category = "Morning/Evening"
        ),
        Zikr(
            id = "z4",
            textAr = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            textEn = "O Allah, You are my Lord, there is none worthy of worship but You. You created me and I am Your slave. I keep Your covenant, and my pledge to You so far as I am able. I seek refuge in You from the evil of what I have done. I admit to Your blessings upon me, and I admit to my misdeeds. Forgive me, for there is none who may forgive sins but You.",
            sourceAr = "صحيح البخاري: 6306 (سيد الاستغفار)",
            sourceEn = "Sahih Al-Bukhari: 6306 (Sayyid Al-Istighfar)",
            count = 1,
            category = "Morning/Evening"
        )
    )

    override fun getAzkar(query: String): Flow<List<Zikr>> = dataStore.data.map { prefs ->
        val favIds = prefs[FAVORITE_ZIKR_IDS] ?: emptySet()
        allAzkar.filter {
            (query.isBlank() || it.textAr.contains(query, ignoreCase = true) || it.textEn.contains(query, ignoreCase = true))
        }.map { zikr ->
            zikr.copy(isFavorite = favIds.contains(zikr.id))
        }
    }

    override fun getFavorites(): Flow<List<Zikr>> = dataStore.data.map { prefs ->
        val favIds = prefs[FAVORITE_ZIKR_IDS] ?: emptySet()
        allAzkar.filter { favIds.contains(it.id) }.map { zikr ->
            zikr.copy(isFavorite = true)
        }
    }

    override suspend fun toggleFavorite(zikrId: String) {
        dataStore.edit { prefs ->
            val favIds = prefs[FAVORITE_ZIKR_IDS]?.toMutableSet() ?: mutableSetOf()
            if (favIds.contains(zikrId)) {
                favIds.remove(zikrId)
            } else {
                favIds.add(zikrId)
            }
            prefs[FAVORITE_ZIKR_IDS] = favIds
        }
    }

    override fun getDailyAyah(): Flow<Ayah> = dataStore.data.map {
        Ayah(
            id = "a1",
            textAr = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            textEn = "Indeed, with hardship [will be] ease.",
            surahNameAr = "الشرح",
            surahNameEn = "Ash-Sharh",
            ayahNumber = 6
        )
    }

    override fun getPrayerTimes(): Flow<List<PrayerTime>> = dataStore.data.map {
        listOf(
            PrayerTime("pt1", "الفجر", "Fajr", "04:30 AM"),
            PrayerTime("pt2", "الظهر", "Dhuhr", "12:15 PM"),
            PrayerTime("pt3", "العصر", "Asr", "03:45 PM", isNext = true),
            PrayerTime("pt4", "المغرب", "Maghrib", "06:30 PM"),
            PrayerTime("pt5", "العشاء", "Isha", "08:00 PM")
        )
    }

    override fun getDailyProgress(): Flow<DailyProgress> = dataStore.data.map { prefs ->
        val completed = prefs[DAILY_COMPLETED_TASKS] ?: 2
        val total = prefs[DAILY_TOTAL_TASKS] ?: 5
        DailyProgress(completed, total)
    }

    override suspend fun updateDailyProgress(completed: Int, total: Int) {
        dataStore.edit { prefs ->
            prefs[DAILY_COMPLETED_TASKS] = completed
            prefs[DAILY_TOTAL_TASKS] = total
        }
    }
}
