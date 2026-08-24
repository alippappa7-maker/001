package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class QabasNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_PRAYER = "qabas_prayer_times_channel"
        const val CHANNEL_ID_DHIKR = "qabas_dhikr_channel"
        
        const val NOTIF_ID_PRAYER_BASE = 1000
        const val NOTIF_ID_DHIKR = 2000
        const val NOTIF_ID_TEST = 3000

        @Volatile
        private var instance: QabasNotificationManager? = null

        fun getInstance(context: Context): QabasNotificationManager {
            return instance ?: synchronized(this) {
                instance ?: QabasNotificationManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Prayer Times Channel
            val prayerChannelName = context.getString(R.string.notif_channel_prayers_name)
            val prayerChannelDesc = context.getString(R.string.notif_channel_prayers_desc)
            val prayerChannel = NotificationChannel(
                CHANNEL_ID_PRAYER,
                prayerChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = prayerChannelDesc
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Dhikr & Reminders Channel
            val dhikrChannelName = context.getString(R.string.notif_channel_dhikr_name)
            val dhikrChannelDesc = context.getString(R.string.notif_channel_dhikr_desc)
            val dhikrChannel = NotificationChannel(
                CHANNEL_ID_DHIKR,
                dhikrChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = dhikrChannelDesc
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(dhikrChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showPrayerNotification(
        prayerId: String,
        prayerNameAr: String,
        prayerNameEn: String,
        timeStr: String,
        soundEnabled: Boolean = false,
        vibrateEnabled: Boolean = true
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "mihrab")
            putExtra("prayer_id", prayerId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notif_prayer_title, prayerNameAr)
        val content = context.getString(R.string.notif_prayer_body, prayerNameAr, timeStr)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PRAYER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(defaultSoundUri)
        } else {
            builder.setSilent(true)
        }

        if (vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        val notifId = NOTIF_ID_PRAYER_BASE + prayerId.hashCode().mod(500)
        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showDhikrReminderNotification(
        soundEnabled: Boolean = false,
        vibrateEnabled: Boolean = true
    ) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "mihrab")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIF_ID_DHIKR,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notif_dhikr_title)
        val content = context.getString(R.string.notif_dhikr_body)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DHIKR)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(defaultSoundUri)
        } else {
            builder.setSilent(true)
        }

        if (vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 250, 150, 250))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_DHIKR, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showTestNotification(soundEnabled: Boolean, vibrateEnabled: Boolean) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIF_ID_TEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notif_test_title)
        val content = context.getString(R.string.notif_test_body)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PRAYER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(defaultSoundUri)
        } else {
            builder.setSilent(true)
        }

        if (vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 200, 100, 200))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, builder.build())
        } catch (_: SecurityException) {}
    }
}
