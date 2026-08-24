package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.SettingsRepository
import com.example.data.repository.PrayerTimeRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepo = SettingsRepository(context)
                val notifSettings = settingsRepo.notificationSettingsFlow.first()
                val prayerConfig = settingsRepo.prayerConfigFlow.first()
                val notifManager = QabasNotificationManager.getInstance(context)
                val scheduler = PrayerNotificationScheduler(context)

                when (action) {
                    PrayerNotificationScheduler.ACTION_PRAYER_ALARM -> {
                        val prayerId = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_ID) ?: return@launch
                        val nameAr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME_AR) ?: ""
                        val nameEn = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_NAME_EN) ?: ""
                        val timeStr = intent.getStringExtra(PrayerNotificationScheduler.EXTRA_PRAYER_TIME) ?: ""

                        if (notifSettings.isPrayerEnabled(prayerId)) {
                            notifManager.showPrayerNotification(
                                prayerId = prayerId,
                                prayerNameAr = nameAr,
                                prayerNameEn = nameEn,
                                timeStr = timeStr,
                                soundEnabled = notifSettings.soundEnabled,
                                vibrateEnabled = notifSettings.vibrateEnabled
                            )

                            // Automatically reschedule for tomorrow
                            val prayerRepo = PrayerTimeRepositoryImpl(context)
                            val cached = prayerRepo.getCachedPrayerTimes().first()
                            val prayerTime = cached?.toList()?.find { it.id.equals(prayerId, ignoreCase = true) }
                            if (prayerTime != null) {
                                scheduler.schedulePrayerAlarm(prayerTime, prayerConfig.timezone)
                            }
                        }
                    }

                    PrayerNotificationScheduler.ACTION_DHIKR_ALARM -> {
                        if (notifSettings.masterEnabled && notifSettings.dhikrReminderEnabled) {
                            notifManager.showDhikrReminderNotification(
                                soundEnabled = notifSettings.soundEnabled,
                                vibrateEnabled = notifSettings.vibrateEnabled
                            )

                            // Automatically reschedule for tomorrow
                            scheduler.scheduleDhikrAlarm(
                                notifSettings.dhikrReminderHour,
                                notifSettings.dhikrReminderMinute,
                                prayerConfig.timezone
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PrayerAlarmReceiver", "Error processing alarm broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
