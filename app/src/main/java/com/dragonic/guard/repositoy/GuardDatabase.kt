package com.dragonic.guard.repository

import android.content.Context
import androidx.room.*
import com.dragonic.guard.model.AppRule
import com.dragonic.guard.model.AppUsageRecord
import kotlinx.coroutines.flow.Flow

// ── DAOs ────────────────────────────────────────────────────────────────────

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules ORDER BY appName ASC")
    fun getAllRules(): Flow<List<AppRule>>

    @Query("SELECT * FROM app_rules WHERE packageName = :pkg LIMIT 1")
    suspend fun getRuleForPackage(pkg: String): AppRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AppRule)

    @Delete
    suspend fun deleteRule(rule: AppRule)

    @Query("SELECT * FROM app_rules WHERE isBlocked = 1")
    suspend fun getBlockedApps(): List<AppRule>
}

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageMinutes DESC")
    fun getUsageForDate(date: String): Flow<List<AppUsageRecord>>

    @Query("SELECT SUM(usageMinutes) FROM app_usage WHERE date = :date")
    suspend fun getTotalMinutesForDate(date: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(record: AppUsageRecord)

    @Query("DELETE FROM app_usage WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)
}

// ── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [AppRule::class, AppUsageRecord::class],
    version = 1,
    exportSchema = false
)
abstract class GuardDatabase : RoomDatabase() {
    abstract fun appRuleDao(): AppRuleDao
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile private var INSTANCE: GuardDatabase? = null

        fun getInstance(context: Context): GuardDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GuardDatabase::class.java,
                    "dragonic_guard.db"
                ).build().also { INSTANCE = it }
            }
    }
}
