package com.example.ui.screens.mihrab

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NetworkMonitor
import com.example.data.local.SettingsRepository
import com.example.data.notification.PrayerNotificationScheduler
import com.example.data.repository.MihrabRepository
import com.example.data.repository.MihrabRepositoryImpl
import com.example.data.repository.PrayerTimeRepository
import com.example.data.repository.PrayerTimeRepositoryImpl
import com.example.domain.model.Ayah
import com.example.domain.model.CityModel
import com.example.domain.model.DEFAULT_CITY_ABU_JURAYN
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.DailyProgress
import com.example.domain.model.NotificationSettings
import com.example.domain.model.PrayerTime
import com.example.domain.model.PrayerTimeConfig
import com.example.domain.model.Zikr
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
    val nextPrayerNameAr: String = "",
    val nextPrayerNameEn: String = "",
    val nextPrayerId: String = "",
    val isFetchingPrayers: Boolean = false,
    val prayerError: String? = null,
    val prayerConfig: PrayerTimeConfig = PrayerTimeConfig(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val isConnected: Boolean = true,
    val isDataStale: Boolean = false
)

class MihrabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MihrabRepository = MihrabRepositoryImpl(application)
    private val prayerRepository: PrayerTimeRepository = PrayerTimeRepositoryImpl(application)
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
    private val networkMonitor = NetworkMonitor(application)
    private val notificationScheduler = PrayerNotificationScheduler(application)
    
    private val _searchQuery = MutableStateFlow("")
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _prayerConfig = MutableStateFlow(PrayerTimeConfig())
    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    private val _isFetchingPrayers = MutableStateFlow(false)
    private val _prayerError = MutableStateFlow<String?>(null)
    private val _countdownStr = MutableStateFlow("")
    private val _nextPrayerNameAr = MutableStateFlow("")
    private val _nextPrayerNameEn = MutableStateFlow("")
    private val _nextPrayerId = MutableStateFlow("")
    private val _isConnected = MutableStateFlow(true)
    
    private var countdownJob: Job? = null

    private val _uiState = MutableStateFlow(MihrabUiState())
    val uiState: StateFlow<MihrabUiState> = _uiState.asStateFlow()

    init {
        // 1. Observe settings config
        viewModelScope.launch {
            settingsRepository.prayerConfigFlow.collectLatest { config ->
                _prayerConfig.value = config
                refreshPrayerTimes()
            }
        }

        // 2. Observe notification settings
        viewModelScope.launch {
            settingsRepository.notificationSettingsFlow.collectLatest { notifSettings ->
                _notificationSettings.value = notifSettings
                val cached = prayerRepository.getCachedPrayerTimes().first()
                notificationScheduler.scheduleAll(cached, notifSettings, _prayerConfig.value)
            }
        }

        // 3. Observe network connectivity for silent background sync
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                _isConnected.value = online
                if (online) {
                    val cached = prayerRepository.getCachedPrayerTimes().first()
                    if (cached == null || cached.isStale || _prayerError.value != null) {
                        refreshPrayerTimes(isBackground = true)
                    }
                }
            }
        }

        // 4. Combine all local data streams
        viewModelScope.launch {
            val flow1 = combine(
                repository.getAzkar(""),
                repository.getFavorites(),
                repository.getDailyAyah(),
                repository.getDailyProgress(),
                prayerRepository.getCachedPrayerTimes()
            ) { azkar, favs, ayah, prog, cachedPrayers ->
                MihrabUiState(
                    azkar = azkar,
                    favoriteAzkar = favs,
                    dailyAyah = ayah,
                    dailyProgress = prog,
                    dailyPrayerTimes = cachedPrayers,
                    isDataStale = cachedPrayers?.isStale ?: false
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

            val flow3 = combine(
                combine(_nextPrayerNameAr, _nextPrayerNameEn, _nextPrayerId) { ar, en, id -> Triple(ar, en, id) },
                _prayerConfig,
                _notificationSettings,
                _isConnected
            ) { nameTriple, config, notifSettings, online ->
                arrayOf(nameTriple.first, nameTriple.second, nameTriple.third, config, notifSettings, online)
            }
            
            combine(flow1, flow2, flow3) { state1, args2, args3 ->
                val query = args2[0] as String
                val showFavs = args2[1] as Boolean
                val fetching = args2[2] as Boolean
                val err = args2[3] as String?
                val countdown = args2[4] as String
                
                val nextAr = args3[0] as String
                val nextEn = args3[1] as String
                val nextId = args3[2] as String
                val config = args3[3] as PrayerTimeConfig
                val notifSettings = args3[4] as NotificationSettings
                val online = args3[5] as Boolean
                
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
                    nextPrayerNameAr = nextAr,
                    nextPrayerNameEn = nextEn,
                    nextPrayerId = nextId,
                    prayerConfig = config,
                    notificationSettings = notifSettings,
                    isConnected = online
                )
            }.collect { newState ->
                _uiState.value = newState
                newState.dailyPrayerTimes?.let { startCountdownLoop(it) }
            }
        }
    }
    
    fun refreshPrayerTimes(
        overrideLat: Double? = null,
        overrideLng: Double? = null,
        locationLabelAr: String? = null,
        locationLabelEn: String? = null,
        isBackground: Boolean = false
    ) {
        viewModelScope.launch {
            if (!isBackground) {
                _isFetchingPrayers.value = true
            }
            _prayerError.value = null
            val config = _prayerConfig.value
            val finalLat = overrideLat ?: config.effectiveLatitude
            val finalLng = overrideLng ?: config.effectiveLongitude
            
            val result = prayerRepository.fetchPrayerTimes(
                lat = finalLat,
                lng = finalLng,
                config = config,
                locationNameAr = locationLabelAr ?: config.selectedCity.fullDisplayNameAr,
                locationNameEn = locationLabelEn ?: config.selectedCity.fullDisplayNameEn
            )

            if (result.isFailure) {
                if (!isBackground) {
                    _prayerError.value = "تعذر تحديث المواقيت عبر الإنترنت. يتم عرض البيانات المحفوظة."
                }
            } else {
                result.getOrNull()?.let { newTimes ->
                    startCountdownLoop(newTimes)
                    notificationScheduler.scheduleAll(newTimes, _notificationSettings.value, config)
                }
            }
            if (!isBackground) {
                _isFetchingPrayers.value = false
            }
        }
    }

    private fun startCountdownLoop(prayers: DailyPrayerTimes) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val cal = Calendar.getInstance()
                val nowSeconds = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)

                val times = prayers.toList()

                var nextTime: PrayerTime? = null
                var diffSeconds = 0

                for (t in times) {
                    val pSec = t.rawMinutesOfDay * 60
                    if (pSec > nowSeconds) {
                        nextTime = t
                        diffSeconds = pSec - nowSeconds
                        break
                    }
                }

                if (nextTime == null) {
                    // Next prayer is tomorrow's Fajr
                    nextTime = prayers.fajr
                    val fajrSec = prayers.fajr.rawMinutesOfDay * 60
                    diffSeconds = (24 * 3600 - nowSeconds) + fajrSec
                }

                val hours = diffSeconds / 3600
                val minutes = (diffSeconds % 3600) / 60
                val seconds = diffSeconds % 60
                
                _countdownStr.value = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                _nextPrayerNameAr.value = nextTime.nameAr
                _nextPrayerNameEn.value = nextTime.nameEn
                _nextPrayerId.value = nextTime.id

                delay(1000)
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
                Log.e("MihrabVM", "Failed to toggle favorite", e)
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

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
