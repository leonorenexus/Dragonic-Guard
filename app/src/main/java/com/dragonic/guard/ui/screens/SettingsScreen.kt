package com.dragonic.guard.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dragonic.guard.ui.components.*
import com.dragonic.guard.ui.theme.*
import com.dragonic.guard.viewmodel.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel) {
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SectionHeader(title = "Pengaturan", subtitle = "Konfigurasi DRAGONIC Guard")

        // PIN section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("PIN Orang Tua", style = MaterialTheme.typography.titleMedium, color = GuardWhite)
            Text(
                "PIN diperlukan untuk membuka kunci layar dan mengubah pengaturan",
                style = MaterialTheme.typography.bodySmall,
                color = GuardWhiteDim.copy(0.5f)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it.filter { c -> c.isDigit() } },
                label = { Text("PIN Baru (4-6 digit)") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GuardCyan,
                    unfocusedBorderColor = GuardGlassBorder,
                    focusedTextColor = GuardWhite,
                    unfocusedTextColor = GuardWhite,
                    cursorColor = GuardCyan,
                    focusedLabelColor = GuardCyan,
                    unfocusedLabelColor = GuardWhiteDim
                )
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = pinConfirm,
                onValueChange = { if (it.length <= 6) pinConfirm = it.filter { c -> c.isDigit() } },
                label = { Text("Konfirmasi PIN") },
                singleLine = true,
                isError = pinConfirm.isNotEmpty() && pin != pinConfirm,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (pin == pinConfirm) GuardCyan else GuardRed,
                    unfocusedBorderColor = GuardGlassBorder,
                    focusedTextColor = GuardWhite,
                    unfocusedTextColor = GuardWhite,
                    cursorColor = GuardCyan,
                    focusedLabelColor = GuardCyan,
                    unfocusedLabelColor = GuardWhiteDim
                )
            )
            Spacer(Modifier.height(12.dp))
            GlowButton(
                text = if (pinSaved) "✓  PIN TERSIMPAN" else "SIMPAN PIN",
                onClick = {
                    if (pin.length >= 4 && pin == pinConfirm) pinSaved = true
                },
                glowColor = if (pinSaved) GuardGreen else GuardCyan,
                enabled = pin.length >= 4 && pin == pinConfirm
            )
        }

        // Info cards
        SettingInfoItem(
            icon = Icons.Filled.DevicesOther,
            title = "ID Perangkat",
            value = vm.deviceId.take(16) + "…"
        )
        SettingInfoItem(
            icon = Icons.Filled.Cloud,
            title = "Firebase Sync",
            value = "Aktif — data tersinkron ke cloud"
        )
        SettingInfoItem(
            icon = Icons.Filled.Info,
            title = "Versi",
            value = "DRAGONIC Guard v1.0.0"
        )

        // Cara setup Firebase
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("📋  Cara Setup Firebase", style = MaterialTheme.typography.titleMedium, color = GuardCyan)
            Spacer(Modifier.height(8.dp))
            listOf(
                "1. Buka console.firebase.google.com",
                "2. Buat project baru",
                "3. Tambah Android app (package: com.dragonic.guard)",
                "4. Download google-services.json",
                "5. Upload ke repo GitHub lalu push",
                "6. GitHub Actions akan build APK otomatis"
            ).forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = GuardWhiteDim)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun SettingInfoItem(icon: ImageVector, title: String, value: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = GuardCyan, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GuardWhite)
                Text(value, style = MaterialTheme.typography.bodySmall, color = GuardWhiteDim)
            }
        }
    }
}
