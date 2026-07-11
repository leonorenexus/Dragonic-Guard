package com.dragonic.guard.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── App Usage ───────────────────────────────────────────────────────────────

@Entity(tableName = "app_usage")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val usageMinutes: Long,
    val date: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis()
)

// ── App Rule ────────────────────────────────────────────────────────────────

@Entity(tableName = "app_rules")
data class AppRule(
    @PrimaryKey val packageName: String,
    val appName: String,
    val appIcon: String = "",        // base64 or empty
    val isBlocked: Boolean = false,
    val dailyLimitMinutes: Int = 0,  // 0 = no limit
    val allowedStartHour: Int = 0,   // 0–23
    val allowedEndHour: Int = 23,    // 0–23
    val updatedAt: Long = System.currentTimeMillis()
)

// ── Device Info ─────────────────────────────────────────────────────────────

data class DeviceStatus(
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val isScreenOn: Boolean = false,
    val lastSeen: Long = 0L,
    val currentApp: String = "",
    val todayScreenTimeMinutes: Long = 0L
)

// ── Remote Command ───────────────────────────────────────────────────────────

data class RemoteCommand(
    val type: CommandType = CommandType.PING,
    val payload: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class CommandType {
    PING,
    LOCK_DEVICE,
    UNLOCK_DEVICE,
    BLOCK_APP,
    UNBLOCK_APP,
    SYNC_RULES,
    SCREENSHOT_REQUEST
}

// ── Screen Time Summary ─────────────────────────────────────────────────────

data class ScreenTimeSummary(
    val totalMinutes: Long,
    val topApps: List<AppUsageSummary>,
    val date: String
)

data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val minutes: Long,
    val percentage: Float
)
