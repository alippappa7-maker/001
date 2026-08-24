package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsRepository
import com.example.data.notification.PrayerNotificationScheduler
import com.example.data.notification.QabasNotificationManager
import com.example.data.repository.PrayerTimeRepositoryImpl
import com.example.domain.model.CityModel
import com.example.domain.model.DEFAULT_CITY_ABU_JURAYN
import com.example.domain.model.NotificationSettings
import com.example.domain.model.PrayerTimeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val prayerRepository = PrayerTimeRepositoryImpl(application)
    private val notificationManager = QabasNotificationManager.getInstance(application)
    private val notificationScheduler = PrayerNotificationScheduler(application)

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val language: StateFlow<String?> = repository.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
        
    val hasSeenWelcome: StateFlow<Boolean?> = repository.hasSeenWelcomeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val prayerConfig: StateFlow<PrayerTimeConfig> = repository.prayerConfigFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, PrayerTimeConfig())

    val notificationSettings: StateFlow<NotificationSettings> = repository.notificationSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, NotificationSettings())

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(isDark)
        }
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.setLanguage(languageCode)
        }
    }
    
    fun setHasSeenWelcome() {
        viewModelScope.launch {
            repository.setHasSeenWelcome(true)
        }
    }

    fun setCalculationMethod(methodId: Int) {
        viewModelScope.launch {
            repository.setCalculationMethod(methodId)
            rescheduleNotifications()
        }
    }

    fun setAsrMadhab(madhabId: Int) {
        viewModelScope.launch {
            repository.setAsrMadhab(madhabId)
            rescheduleNotifications()
        }
    }

    fun setTimezone(timezone: String) {
        viewModelScope.launch {
            repository.setTimezone(timezone)
            rescheduleNotifications()
        }
    }

    fun setUseGps(useGps: Boolean) {
        viewModelScope.launch {
            repository.setUseGps(useGps)
            rescheduleNotifications()
        }
    }

    fun setSelectedCity(city: CityModel) {
        viewModelScope.launch {
            repository.setSelectedCity(city)
            rescheduleNotifications()
        }
    }

    fun setPrayerAdjustment(prayerId: String, deltaMinutes: Int) {
        viewModelScope.launch {
            repository.setPrayerAdjustment(prayerId, deltaMinutes)
            rescheduleNotifications()
        }
    }

    fun resetPrayerAdjustments() {
        viewModelScope.launch {
            repository.resetPrayerAdjustments()
            rescheduleNotifications()
        }
    }

    // Notification operations
    fun setNotificationMaster(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationMaster(enabled)
            rescheduleNotifications()
        }
    }

    fun setPrayerNotificationEnabled(prayerId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setPrayerNotificationEnabled(prayerId, enabled)
            rescheduleNotifications()
        }
    }

    fun setDhikrReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDhikrReminderEnabled(enabled)
            rescheduleNotifications()
        }
    }

    fun setDhikrReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setDhikrReminderTime(hour, minute)
            rescheduleNotifications()
        }
    }

    fun setNotificationSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationSoundEnabled(enabled)
        }
    }

    fun setNotificationVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationVibrateEnabled(enabled)
        }
    }

    fun sendTestNotification() {
        val current = notificationSettings.value
        notificationManager.showTestNotification(
            soundEnabled = current.soundEnabled,
            vibrateEnabled = current.vibrateEnabled
        )
    }

    fun hasNotificationPermission(): Boolean {
        return notificationManager.hasNotificationPermission()
    }

    private fun rescheduleNotifications() {
        viewModelScope.launch {
            try {
                val notifSettings = repository.notificationSettingsFlow.first()
                val config = repository.prayerConfigFlow.first()
                val cached = prayerRepository.getCachedPrayerTimes().first()
                notificationScheduler.scheduleAll(cached, notifSettings, config)
            } catch (_: Exception) {}
        }
    }
}
