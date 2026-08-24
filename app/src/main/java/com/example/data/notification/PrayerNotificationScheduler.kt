package com.example.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.NotificationSettings
import com.example.domain.model.PrayerTime
import com.example.domain.model.PrayerTimeConfig
import java.util.Calendar
import java.util.TimeZone

class PrayerNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.action.PRAYER_ALARM"
        const val ACTION_DHIKR_ALARM = "com.example.action.DHIKR_ALARM"

        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME_AR = "extra_prayer_name_ar"
        const val EXTRA_PRAYER_NAME_EN = "extra_prayer_name_en"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"

        val REQUEST_CODES = mapOf(
            "fajr" to 101,
            "sunrise" to 102,
            "dhuhr" to 103,
            "asr" to 104,
            "maghrib" to 105,
            "isha" to 106,
            "dhikr" to 201
        )
    }

    fun scheduleAll(
        dailyPrayers: DailyPrayerTimes?,
        settings: NotificationSettings,
        config: PrayerTimeConfig
    ) {
        if (!settings.masterEnabled) {
            cancelAll()
            return
        }

        // Schedule Prayer Times
        if (dailyPrayers != null) {
            val prayerList = dailyPrayers.toList()
            for (prayer in prayerList) {
                val isEnabled = settings.isPrayerEnabled(prayer.id)
                if (isEnabled) {
                    schedulePrayerAlarm(prayer, config.timezone)
                } else {
                    cancelPrayerAlarm(prayer.id)
                }
            }
        }

        // Schedule Daily Dhikr Reminder
        if (settings.dhikrReminderEnabled) {
            scheduleDhikrAlarm(settings.dhikrReminderHour, settings.dhikrReminderMinute, config.timezone)
        } else {
            cancelDhikrAlarm()
        }
    }

    fun schedulePrayerAlarm(prayer: PrayerTime, timezoneId: String) {
        val requestCode = REQUEST_CODES[prayer.id.lowercase()] ?: 100
        val targetMillis = calculateNextTriggerTime(prayer.rawMinutesOfDay, timezoneId)

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_ID, prayer.id)
            putExtra(EXTRA_PRAYER_NAME_AR, prayer.nameAr)
            putExtra(EXTRA_PRAYER_NAME_EN, prayer.nameEn)
            putExtra(EXTRA_PRAYER_TIME, prayer.timeStr)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(targetMillis, pendingIntent)
        Log.d("PrayerScheduler", "Scheduled prayer alarm for ${prayer.id} at $targetMillis")
    }

    fun scheduleDhikrAlarm(hour: Int, minute: Int, timezoneId: String) {
        val requestCode = REQUEST_CODES["dhikr"] ?: 201
        val targetMinutesOfDay = hour * 60 + minute
        val targetMillis = calculateNextTriggerTime(targetMinutesOfDay, timezoneId)

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_DHIKR_ALARM
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(targetMillis, pendingIntent)
        Log.d("PrayerScheduler", "Scheduled dhikr alarm at $hour:$minute (millis: $targetMillis)")
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e("PrayerScheduler", "Failed to set alarm", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (_: Exception) {}
        }
    }

    fun cancelPrayerAlarm(prayerId: String) {
        val requestCode = REQUEST_CODES[prayerId.lowercase()] ?: return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun cancelDhikrAlarm() {
        val requestCode = REQUEST_CODES["dhikr"] ?: return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_DHIKR_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun cancelAll() {
        REQUEST_CODES.values.forEach { code ->
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    fun calculateNextTriggerTime(minutesOfDay: Int, timezoneId: String): Long {
        val tz = try {
            if (timezoneId.isNotBlank() && timezoneId != "UTC") TimeZone.getTimeZone(timezoneId) else TimeZone.getDefault()
        } catch (_: Exception) {
            TimeZone.getDefault()
        }

        val calendar = Calendar.getInstance(tz)
        val nowMillis = calendar.timeInMillis

        val targetHour = minutesOfDay / 60
        val targetMinute = minutesOfDay % 60

        calendar.set(Calendar.HOUR_OF_DAY, targetHour)
        calendar.set(Calendar.MINUTE, targetMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= nowMillis) {
            // If the time has already passed today, schedule for tomorrow
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}
