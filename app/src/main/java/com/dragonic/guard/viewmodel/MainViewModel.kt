package com.dragonic.guard.viewmodel

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dragonic.guard.model.AppRule
import com.dragonic.guard.model.AppUsageRecord
import com.dragonic.guard.receiver.GuardDeviceAdminReceiver
import com.dragonic.guard.repository.GuardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GuardRepository(app)
    private val dpm = app.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComp = ComponentName(app, GuardDeviceAdminReceiver::class.java)

    val allRules: StateFlow<List<AppRule>> = repo.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _todayUsage = MutableStateFlow<List<AppUsageRecord>>(emptyList())
    val todayUsage: StateFlow<List<AppUsageRecord>> = _todayUsage

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    private val _isDeviceAdmin = MutableStateFlow(false)
    val isDeviceAdmin: StateFlow<Boolean> = _isDeviceAdmin

    val deviceId: String get() = repo.deviceId

    init {
        loadTodayUsage()
        loadInstalledApps()
        refreshAdminStatus()
    }

    fun refreshAdminStatus() {
        _isDeviceAdmin.value = dpm.isAdminActive(adminComp)
    }

    private fun loadTodayUsage() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repo.getUsageForDate(today).collect { _todayUsage.value = it }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // user apps only
                .map { info ->
                    InstalledApp(
                        packageName = info.packageName,
                        appName = pm.getApplicationLabel(info).toString()
                    )
                }
                .sortedBy { it.appName }
            _installedApps.value = apps
        }
    }

    fun setAppBlocked(packageName: String, appName: String, blocked: Boolean) {
        viewModelScope.launch {
            val existing = repo.getRuleForPackage(packageName)
            repo.upsertRule(
                existing?.copy(isBlocked = blocked) ?: AppRule(
                    packageName = packageName,
                    appName = appName,
                    isBlocked = blocked
                )
            )
        }
    }

    fun setDailyLimit(packageName: String, appName: String, minutes: Int) {
        viewModelScope.launch {
            val existing = repo.getRuleForPackage(packageName)
            repo.upsertRule(
                existing?.copy(dailyLimitMinutes = minutes) ?: AppRule(
                    packageName = packageName,
                    appName = appName,
                    dailyLimitMinutes = minutes
                )
            )
        }
    }

    fun lockDeviceNow() {
        if (dpm.isAdminActive(adminComp)) dpm.lockNow()
    }

    data class InstalledApp(val packageName: String, val appName: String)
}
