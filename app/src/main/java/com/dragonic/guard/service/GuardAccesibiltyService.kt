package com.dragonic.guard.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.dragonic.guard.model.AppRule
import com.dragonic.guard.repository.GuardRepository
import com.dragonic.guard.ui.screens.LockScreenActivity
import kotlinx.coroutines.*

class GuardAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repo: GuardRepository
    private var blockedPackages = setOf<String>()
    private var lastBlockedPkg = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = GuardRepository(applicationContext)
        loadBlockedApps()
    }

    private fun loadBlockedApps() {
        scope.launch {
            // Reload every 30 seconds
            while (isActive) {
                try {
                    val rules = repo.getBlockedApps()
                    blockedPackages = rules.map { it.packageName }.toSet()
                } catch (_: Exception) {}
                delay(30_000)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // ignore our own app

        if (pkg in blockedPackages && pkg != lastBlockedPkg) {
            lastBlockedPkg = pkg
            showLockScreen(pkg)
        } else if (pkg !in blockedPackages) {
            lastBlockedPkg = ""
        }
    }

    private fun showLockScreen(blockedPkg: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("blocked_package", blockedPkg)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        scope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
