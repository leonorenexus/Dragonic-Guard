package com.dragonic.guard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.dragonic.guard.repository.GuardRepository
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createChannels()
        val repo = GuardRepository(this)
        CoroutineScope(Dispatchers.IO).launch { repo.initialSync() }
        repo.startRealtimeSync()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CH_MONITOR, "Guard Monitor", NotificationManager.IMPORTANCE_LOW)
            )
            nm.createNotificationChannel(
                NotificationChannel(CH_ALERT, "Guard Alert", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    companion object {
        const val CH_MONITOR = "guard_monitor"
        const val CH_ALERT   = "guard_alert"
    }
}
