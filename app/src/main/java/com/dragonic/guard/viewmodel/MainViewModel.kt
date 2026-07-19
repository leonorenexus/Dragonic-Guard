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

    private val repo  = GuardRepository(app)
    private val dpm   = app.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val admin = ComponentName(app, GuardDeviceAdminReceiver::class.java)

    val allRules: StateFlow<List<AppRule>> = repo.allRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _usage = MutableStateFlow<List<AppUsageRecord>>(emptyList())
    val todayUsage: StateFlow<List<AppUsageRecord>> = _usage

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    val deviceId get() = repo.deviceId

    init {
        loadUsage()
        loadInstalledApps()
        refreshAdmin()
    }

    fun refreshAdmin() { _isAdmin.value = dpm.isAdminActive(admin) }

    private fun loadUsage() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repo.usageForDate(today).collect { _usage.value = it }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            _installedApps.value = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .map { InstalledApp(it.packageName, pm.getApplicationLabel(it).toString()) }
                .sortedBy { it.appName }
        }
    }

    fun setBlocked(pkg: String, name: String, blocked: Boolean) {
        viewModelScope.launch {
            val existing = repo.ruleFor(pkg)
            val rule = existing?.copy(isBlocked = blocked)
                ?: AppRule(pkg, name, isBlocked = blocked)
            // Save locally via Firestore (repo syncs back)
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("devices").document(repo.deviceId)
                .collection("rules").document(pkg)
                .set(rule)
        }
    }

    fun setLimit(pkg: String, name: String, minutes: Int) {
        viewModelScope.launch {
            val existing = repo.ruleFor(pkg)
            val rule = existing?.copy(dailyLimitMinutes = minutes)
                ?: AppRule(pkg, name, dailyLimitMinutes = minutes)
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("devices").document(repo.deviceId)
                .collection("rules").document(pkg)
                .set(rule)
        }
    }

    fun lockNow() { if (dpm.isAdminActive(admin)) dpm.lockNow() }

    data class InstalledApp(val packageName: String, val appName: String)
}
