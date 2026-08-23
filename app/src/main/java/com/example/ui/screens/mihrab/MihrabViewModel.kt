package com.example.ui.screens.mihrab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MihrabRepository
import com.example.data.repository.MihrabRepositoryImpl
import com.example.data.repository.PrayerTimeRepository
import com.example.data.repository.PrayerTimeRepositoryImpl
import com.example.domain.model.Ayah
import com.example.domain.model.DailyProgress
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.PrayerTimeConfig
import com.example.domain.model.Zikr
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MihrabUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val azkar: List<Zikr> = emptyList(),
    val favoriteAzkar: List<Zikr> = emptyList(),
    val dailyAyah: Ayah? = null,
    val dailyPrayerTimes: DailyPrayerTimes? = null,
    val dailyProgress: DailyProgress? = null,
    val nextPrayerCountdown: String = "",
    val nextPrayerName: String = "",
    val isFetchingPrayers: Boolean = false,
    val prayerError: String? = null
)

class MihrabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MihrabRepository = MihrabRepositoryImpl(application)
    private val prayerRepository: PrayerTimeRepository = PrayerTimeRepositoryImpl(application)
    
    private val _searchQuery = MutableStateFlow("")
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _prayerConfig = MutableStateFlow(PrayerTimeConfig()) // Could be loaded from dataStore
    private val _isFetchingPrayers = MutableStateFlow(false)
    private val _prayerError = MutableStateFlow<String?>(null)
    private val _countdownStr = MutableStateFlow("")
    private val _nextPrayerNameStr = MutableStateFlow("")
    
    private var countdownJob: Job? = null

    private val _uiState = MutableStateFlow(MihrabUiState())
    val uiState: StateFlow<MihrabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val flow1 = combine(
                repository.getAzkar(""),
                repository.getFavorites(),
                repository.getDailyAyah(),
                repository.getDailyProgress(),
                prayerRepository.getCachedPrayerTimes()
            ) { azkar, favs, ayah, prog, prayers ->
                MihrabUiState(
                    azkar = azkar,
                    favoriteAzkar = favs,
                    dailyAyah = ayah,
                    dailyProgress = prog,
                    dailyPrayerTimes = prayers
                )
            }
            
            val flow2 = combine(
                _searchQuery,
                _showFavoritesOnly,
                _isFetchingPrayers,
                _prayerError,
                _countdownStr
            ) { query, showFavs, fetching, err, countdown ->
                arrayOf(query, showFavs, fetching, err, countdown)
            }
            
            combine(flow1, flow2, _nextPrayerNameStr) { state1, args2, nextP ->
                val query = args2[0] as String
                val showFavs = args2[1] as Boolean
                val fetching = args2[2] as Boolean
                val err = args2[3] as String?
                val countdown = args2[4] as String
                
                var filteredAzkar = if (query.isBlank()) state1.azkar else state1.azkar.filter {
                    it.textAr.contains(query, ignoreCase = true) || it.textEn.contains(query, ignoreCase = true)
                }
                if (showFavs) filteredAzkar = filteredAzkar.filter { it.isFavorite }

                state1.copy(
                    isLoading = false,
                    searchQuery = query,
                    showFavoritesOnly = showFavs,
                    azkar = filteredAzkar,
                    isFetchingPrayers = fetching,
                    prayerError = err,
                    nextPrayerCountdown = countdown,
                    nextPrayerName = nextP
                )
            }.collect { newState ->
                _uiState.value = newState
                newState.dailyPrayerTimes?.let { calculateCountdown(it) }
            }
        }
        
        refreshPrayerTimes()
    }
    
    fun refreshPrayerTimes(lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            _isFetchingPrayers.value = true
            _prayerError.value = null
            val config = _prayerConfig.value
            val finalLat = lat ?: config.manualLatitude
            val finalLng = lng ?: config.manualLongitude
            
            val result = prayerRepository.fetchPrayerTimes(finalLat, finalLng, config)
            if (result.isFailure) {
                _prayerError.value = "Failed to update prayer times. Showing cached data."
            }
            _isFetchingPrayers.value = false
        }
    }

    private fun calculateCountdown(prayers: DailyPrayerTimes) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val sdf = SimpleDateFormat("HH:mm", Locale.US)
                val nowStr = sdf.format(Date())
                val nowParts = nowStr.split(":").map { it.toInt() }
                val nowMins = nowParts[0] * 60 + nowParts[1]

                val times = listOf(
                    prayers.fajr, prayers.sunrise, prayers.dhuhr,
                    prayers.asr, prayers.maghrib, prayers.isha
                )

                var nextTime: com.example.domain.model.PrayerTime? = null
                var minDiff = Int.MAX_VALUE

                for (t in times) {
                    val pParts = t.timeStr.split(" ")[0].split(":").map { it.toInt() }
                    val pMins = pParts[0] * 60 + pParts[1]
                    val diff = if (pMins > nowMins) pMins - nowMins else (24 * 60 - nowMins + pMins)
                    if (diff in 1 until minDiff) {
                        minDiff = diff
                        nextTime = t
                    }
                }
                
                if (nextTime != null) {
                    val hours = minDiff / 60
                    val mins = minDiff % 60
                    _countdownStr.value = "-${String.format(Locale.US, "%02d:%02d", hours, mins)}"
                    _nextPrayerNameStr.value = nextTime.nameAr
                }

                delay(60000)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.update { !it }
    }

    fun toggleFavorite(zikrId: String) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(zikrId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun completeTask() {
        viewModelScope.launch {
            val currentProgress = uiState.value.dailyProgress ?: return@launch
            val newCompleted = (currentProgress.completedTasks + 1).coerceAtMost(currentProgress.totalTasks)
            repository.updateDailyProgress(newCompleted, currentProgress.totalTasks)
        }
    }
}
