package com.dragonic.guard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dragonic.guard.service.GuardMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            context.startForegroundService(
                Intent(context, GuardMonitorService::class.java)
            )
        }
    }
}
