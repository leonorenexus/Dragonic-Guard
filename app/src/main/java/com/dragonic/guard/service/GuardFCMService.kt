package com.dragonic.guard.service

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.dragonic.guard.receiver.GuardDeviceAdminReceiver
import com.dragonic.guard.repository.GuardRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GuardFCMService : FirebaseMessagingService() {

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
        // Save token to Firebase for parent to target
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
        kotlinx.coroutines.GlobalScope.launch {
            repo.syncRulesFromFirebase()
        }
    }
}
