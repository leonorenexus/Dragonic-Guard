package com.dragonic.guard.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.dragonic.guard.model.AppRule
import com.dragonic.guard.ui.components.*
import com.dragonic.guard.ui.theme.*
import com.dragonic.guard.viewmodel.MainViewModel

@Composable
fun AppControlScreen(vm: MainViewModel) {
    val installedApps by vm.installedApps.collectAsState()
    val rules by vm.allRules.collectAsState()
    var search by remember { mutableStateOf("") }
    var showLimitDialog by remember { mutableStateOf<String?>(null) }

    val ruleMap = rules.associateBy { it.packageName }

    val filtered = installedApps.filter {
        it.appName.contains(search, ignoreCase = true) ||
        it.packageName.contains(search, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            SectionHeader(title = "Kontrol Aplikasi", subtitle = "Kelola akses per aplikasi")
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Cari aplikasi...", color = GuardWhiteDim.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = GuardCyan) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GuardCyan,
                    unfocusedBorderColor = GuardGlassBorder,
                    focusedTextColor = GuardWhite,
                    unfocusedTextColor = GuardWhite,
                    cursorColor = GuardCyan,
                    focusedContainerColor = GuardGlass,
                    unfocusedContainerColor = GuardGlass
                )
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                val rule = ruleMap[app.packageName]
                AppControlItem(
                    appName = app.appName,
                    packageName = app.packageName,
                    rule = rule,
                    onToggleBlock = { vm.setAppBlocked(app.packageName, app.appName, it) },
                    onSetLimit = { showLimitDialog = app.packageName }
                )
            }
        }
    }

    if (showLimitDialog != null) {
        val pkg = showLimitDialog!!
        val app = installedApps.find { it.packageName == pkg }
        val currentLimit = ruleMap[pkg]?.dailyLimitMinutes ?: 0
        var limitInput by remember { mutableStateOf(currentLimit.toString()) }

        AlertDialog(
            onDismissRequest = { showLimitDialog = null },
            containerColor = GuardDeepBlue,
            title = { Text("Batas Waktu Harian", color = GuardWhite, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text("${app?.appName}", color = GuardCyan, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Menit per hari (0 = tidak terbatas)") },
                        singleLine = true,
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val mins = limitInput.toIntOrNull() ?: 0
                    val appName = app?.appName ?: pkg
                    vm.setDailyLimit(pkg, appName, mins)
                    showLimitDialog = null
                }) { Text("Simpan", color = GuardCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = null }) { Text("Batal", color = GuardWhiteDim) }
            }
        )
    }
}

@Composable
fun AppControlItem(
    appName: String,
    packageName: String,
    rule: AppRule?,
    onToggleBlock: (Boolean) -> Unit,
    onSetLimit: () -> Unit
) {
    val isBlocked = rule?.isBlocked == true
    val hasLimit = (rule?.dailyLimitMinutes ?: 0) > 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderGlow = if (isBlocked) GuardRed.copy(alpha = 0.4f) else GuardGlassBorder
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.radialGradient(listOf(GuardCyan.copy(0.2f), GuardPurple.copy(0.1f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(appName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge, color = GuardCyan)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(appName, style = MaterialTheme.typography.bodyLarge, color = GuardWhite)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isBlocked) StatusBadge("Diblokir", false)
                    if (hasLimit) Text("Max ${rule?.dailyLimitMinutes}m/hari",
                        style = MaterialTheme.typography.labelSmall, color = GuardAmber)
                }
            }
            Switch(
                checked = isBlocked,
                onCheckedChange = onToggleBlock,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GuardRed,
                    checkedTrackColor = GuardRed.copy(alpha = 0.3f),
                    uncheckedThumbColor = GuardWhiteDim,
                    uncheckedTrackColor = GuardGlass
                )
            )
            IconButton(onClick = onSetLimit) {
                Icon(Icons.Filled.Timer, null,
                    tint = if (hasLimit) GuardAmber else GuardWhiteDim.copy(0.4f))
            }
        }
    }
}
