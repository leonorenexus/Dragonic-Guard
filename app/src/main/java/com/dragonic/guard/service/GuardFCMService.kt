package com.dragonic.guard.service

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.dragonic.guard.receiver.GuardDeviceAdminReceiver
import com.dragonic.guard.repository.GuardRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GuardFCMService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        when (data["command"]) {
            "LOCK_DEVICE" -> lockDevice()
            "SYNC_RULES" -> syncRules()
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val repo = GuardRepository(applicationContext)
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("devices")
            .document(repo.deviceId)
            .update("fcmToken", token)
    }

    private fun lockDevice() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, GuardDeviceAdminReceiver::class.java)
        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        }
    }

    private fun syncRules() {
        val repo = GuardRepository(applicationContext)
        scope.launch {
            repo.syncRulesFromFirebase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}
