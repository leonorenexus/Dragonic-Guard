package com.dragonic.guard.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dragonic.guard.GuardApplication.Companion.CHANNEL_MONITOR
import com.dragonic.guard.model.AppUsageRecord
import com.dragonic.guard.repository.GuardRepository
import com.dragonic.guard.ui.screens.MainActivity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class GuardMonitorService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repo: GuardRepository
    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        repo = GuardRepository(applicationContext)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        startForeground(NOTIF_ID, buildNotification())
        startMonitoring()
    }

    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                try {
                    collectAndSaveUsage()
                } catch (_: Exception) {}
                delay(60_000) // every minute
            }
        }
    }

    private suspend fun collectAndSaveUsage() {
        val now = System.currentTimeMillis()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        )

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val pm = packageManager

        stats?.filter { it.totalTimeInForeground > 0 && it.packageName != packageName }
            ?.forEach { stat ->
                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(stat.packageName, 0)).toString()
                } catch (_: Exception) { stat.packageName }

                repo.recordUsage(
                    AppUsageRecord(
                        packageName = stat.packageName,
                        appName = appName,
                        usageMinutes = stat.totalTimeInForeground / 60000,
                        date = today
                    )
                )
            }
    }

    private fun buildNotification(): Notification {
        val intent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_MONITOR)
            .setContentTitle("DRAGONIC Guard Aktif")
            .setContentText("Memantau aktivitas perangkat anak")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(intent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val NOTIF_ID = 1001
    }
}
