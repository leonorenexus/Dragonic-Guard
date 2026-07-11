package com.dragonic.guard.repository

import android.content.Context
import com.dragonic.guard.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class GuardRepository(private val context: Context) {

    private val db = GuardDatabase.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()

    // ── Device ID ────────────────────────────────────────────────────────────
    private val prefs = context.getSharedPreferences("dragonic_guard", Context.MODE_PRIVATE)
    val deviceId: String get() = prefs.getString("device_id", null)
        ?: UUID.randomUUID().toString().also { prefs.edit().putString("device_id", it).apply() }

    // ── App Rules ────────────────────────────────────────────────────────────

    fun getAllRules(): Flow<List<AppRule>> = db.appRuleDao().getAllRules()

    suspend fun getRuleForPackage(pkg: String): AppRule? =
        db.appRuleDao().getRuleForPackage(pkg)

    suspend fun upsertRule(rule: AppRule) {
        db.appRuleDao().upsertRule(rule)
        // Sync to Firebase
        try {
            firestore.collection("devices")
                .document(deviceId)
                .collection("rules")
                .document(rule.packageName)
                .set(rule, SetOptions.merge())
                .await()
        } catch (_: Exception) {}
    }

    suspend fun getBlockedApps(): List<AppRule> = db.appRuleDao().getBlockedApps()

    // ── Usage ────────────────────────────────────────────────────────────────

    fun getUsageForDate(date: String): Flow<List<AppUsageRecord>> =
        db.appUsageDao().getUsageForDate(date)

    suspend fun recordUsage(record: AppUsageRecord) {
        db.appUsageDao().insertUsage(record)
        // Sync daily summary to Firebase
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val total = db.appUsageDao().getTotalMinutesForDate(today) ?: 0L
            firestore.collection("devices")
                .document(deviceId)
                .set(mapOf(
                    "todayScreenTimeMinutes" to total,
                    "lastSeen" to System.currentTimeMillis(),
                    "currentApp" to record.appName
                ), SetOptions.merge())
                .await()
        } catch (_: Exception) {}
    }

    // ── Device Status ────────────────────────────────────────────────────────

    suspend fun pushDeviceStatus(status: DeviceStatus) {
        try {
            firestore.collection("devices")
                .document(deviceId)
                .set(status, SetOptions.merge())
                .await()
        } catch (_: Exception) {}
    }

    // ── Remote Commands ──────────────────────────────────────────────────────

    fun listenForCommands(onCommand: (RemoteCommand) -> Unit) {
        firestore.collection("devices")
            .document(deviceId)
            .collection("commands")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    val cmd = change.document.toObject(RemoteCommand::class.java)
                    onCommand(cmd)
                    // Delete after processing
                    change.document.reference.delete()
                }
            }
    }

    // ── Sync Rules from Firebase ─────────────────────────────────────────────

    suspend fun syncRulesFromFirebase() {
        try {
            val snapshot = firestore.collection("devices")
                .document(deviceId)
                .collection("rules")
                .get()
                .await()
            snapshot.documents.forEach { doc ->
                val rule = doc.toObject(AppRule::class.java) ?: return@forEach
                db.appRuleDao().upsertRule(rule)
            }
        } catch (_: Exception) {}
    }

    fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
