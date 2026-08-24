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

class BootAndConfigReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("BootAndConfigReceiver", "Rescheduling notifications on event: $action")
                    val settingsRepo = SettingsRepository(context)
                    val prayerRepo = PrayerTimeRepositoryImpl(context)

                    val notifSettings = settingsRepo.notificationSettingsFlow.first()
                    val prayerConfig = settingsRepo.prayerConfigFlow.first()
                    val cachedPrayers = prayerRepo.getCachedPrayerTimes().first()

                    val scheduler = PrayerNotificationScheduler(context)
                    scheduler.scheduleAll(cachedPrayers, notifSettings, prayerConfig)
                } catch (e: Exception) {
                    Log.e("BootAndConfigReceiver", "Failed to reschedule on event: $action", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
