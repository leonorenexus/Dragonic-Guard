package com.dragonic.guard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class GuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MONITOR,
                    "Guard Monitor",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "DRAGONIC Guard aktif memantau" }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ALERT,
                    "Guard Alert",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Notifikasi peringatan orang tua" }
            )
        }
    }

    companion object {
        const val CHANNEL_MONITOR = "dragonic_guard_monitor"
        const val CHANNEL_ALERT = "dragonic_guard_alert"
    }
}
