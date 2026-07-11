package com.dragonic.guard.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class GuardDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "DRAGONIC Guard: Device Admin aktif ✓", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "DRAGONIC Guard: Device Admin dinonaktifkan", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Menonaktifkan DRAGONIC Guard akan menghapus perlindungan perangkat anak. Masukkan PIN orang tua untuk melanjutkan."
    }
}
