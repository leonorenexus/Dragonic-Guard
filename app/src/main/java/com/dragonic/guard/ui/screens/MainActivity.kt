 package com.dragonic.guard.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dragonic.guard.model.AppRule
import com.dragonic.guard.receiver.GuardDeviceAdminReceiver
import com.dragonic.guard.service.GuardMonitorService
import com.dragonic.guard.ui.components.*
import com.dragonic.guard.ui.theme.*
import com.dragonic.guard.viewmodel.MainViewModel
import kotlin.math.max

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()
    private val adminLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { vm.refreshAdmin() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForegroundService(Intent(this, GuardMonitorService::class.java))
        setContent { GuardTheme { ChildApp(vm, ::requestAdmin, ::openAccessibility, ::openUsageAccess) } }
    }

    private fun requestAdmin() = adminLauncher.launch(
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this@MainActivity, GuardDeviceAdminReceiver::class.java))
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Diperlukan untuk perlindungan DRAGONIC Guard.")
        })
    private fun openAccessibility() = startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    private fun openUsageAccess()   = startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
}

data class NavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun ChildApp(vm: MainViewModel, onAdmin: () -> Unit, onAccess: () -> Unit, onUsage: () -> Unit) {
    val nav   = rememberNavController()
    val items = listOf(
        NavItem("dash",     Icons.Filled.Shield,   "Dashboard"),
        NavItem("apps",     Icons.Filled.Apps,     "Aplikasi"),
        NavItem("usage",    Icons.Filled.BarChart, "Pemakaian"),
        NavItem("settings", Icons.Filled.Settings, "Pengaturan"),
    )
    Scaffold(
        containerColor = BgBlack,
        bottomBar = {
            NavigationBar(containerColor = BgDeep.copy(0.97f), tonalElevation = 0.dp) {
                val entry by nav.currentBackStackEntryAsState()
                val cur = entry?.destination?.route
                items.forEach { item ->
                    val sel = cur == item.route
                    NavigationBarItem(
                        selected = sel, onClick = {
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        icon  = { Icon(item.icon, null, tint = if (sel) Cyan else TextDim.copy(0.5f)) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall, color = if (sel) Cyan else TextDim.copy(0.5f)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Cyan.copy(0.15f))
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgBlack, BgDeep, BgBlack))).padding(pad)) {
            NavHost(nav, "dash") {
                composable("dash")     { DashScreen(vm, onAdmin, onAccess, onUsage) }
                composable("apps")     { AppsScreen(vm) }
                composable("usage")    { UsageScreen(vm) }
                composable("settings") { SettingsScreen(vm) }
            }
        }
    }
}

// ── Dashboard ────────────────────────────────────────────────────────────────

@Composable
fun DashScreen(vm: MainViewModel, onAdmin: () -> Unit, onAccess: () -> Unit, onUsage: () -> Unit) {
    val rules   by vm.allRules.collectAsState()
    val usage   by vm.todayUsage.collectAsState()
    val isAdmin by vm.isAdmin.collectAsState()
    val total   = usage.sumOf { it.usageMinutes }
    val inf     = rememberInfiniteTransition(label = "r")
    val rot     by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "rot")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("DRAGONIC", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Cyan)
                Text("Guard", style = MaterialTheme.typography.displayLarge, color = TextWhite)
                Text("v4.0.0", style = MaterialTheme.typography.labelSmall, color = Purple)
            }
            StatusBadge(if (isAdmin) "Protected" else "Setup", isAdmin)
        }

        // Hero
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(28.dp))
            .background(Brush.radialGradient(listOf(BgNavy, BgDeep, BgBlack)))
            .border(1.dp, GlassBorder, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center) {
            Box(Modifier.size(160.dp).rotate(rot).border(2.dp,
                Brush.sweepGradient(listOf(Cyan.copy(0f), Cyan.copy(0.9f), Purple.copy(0.5f), Cyan.copy(0f))), CircleShape))
            Box(Modifier.size(115.dp).rotate(-rot * 0.6f).border(1.dp,
                Brush.sweepGradient(listOf(Purple.copy(0f), Purple.copy(0.7f), Cyan.copy(0.3f), Purple.copy(0f))), CircleShape))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Shield, null, tint = Cyan, modifier = Modifier.size(38.dp))
                Spacer(Modifier.height(6.dp))
                Text("${total}m", style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp), color = TextWhite)
                Text("SCREEN TIME HARI INI", style = MaterialTheme.typography.labelSmall, color = Cyan.copy(0.7f))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallStat("Diblokir", "${rules.count { it.isBlocked }}", Red, Modifier.weight(1f))
            SmallStat("Aturan", "${rules.size}", Amber, Modifier.weight(1f))
            SmallStat("Dipantau", "${vm.installedApps.value.size}", Green, Modifier.weight(1f))
        }

        SectionHeader("Status Izin", "Aktifkan semua untuk perlindungan penuh")
        PermCard("Device Admin",        "Cegah uninstall & kunci jarak jauh", isAdmin, onAdmin)
        PermCard("Accessibility",       "Blokir app secara real-time", false, onAccess)
        PermCard("Usage Access",        "Pantau screen time", false, onUsage)

        SectionHeader("Aksi Cepat")
        GlowButton("🔒  KUNCI PERANGKAT SEKARANG", { vm.lockNow() }, color = Red, enabled = isAdmin)

        GlassCard(Modifier.fillMaxWidth()) {
            Text("ID Perangkat", style = MaterialTheme.typography.labelSmall, color = Cyan)
            Spacer(Modifier.height(4.dp))
            Text(vm.deviceId, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Cyan)
        }
    }
}

@Composable
fun SmallStat(label: String, value: String, color: Color, modifier: Modifier) =
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(color.copy(0.1f))
        .border(1.dp, color.copy(0.3f), RoundedCornerShape(14.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextDim)
    }

@Composable
fun PermCard(title: String, desc: String, active: Boolean, onActivate: () -> Unit) =
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextWhite)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = TextDim)
            }
            Spacer(Modifier.width(8.dp))
            if (active) StatusBadge("Aktif", true)
            else OutlinedButton(onClick = onActivate,
                border = BorderStroke(1.dp, Cyan.copy(0.5f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text("Aktifkan", style = MaterialTheme.typography.labelSmall, color = Cyan)
            }
        }
    }

// ── Apps Screen ──────────────────────────────────────────────────────────────

@Composable
fun AppsScreen(vm: MainViewModel) {
    val apps  by vm.installedApps.collectAsState()
    val rules by vm.allRules.collectAsState()
    val ruleMap = remember(rules) { rules.associateBy { it.packageName } }
    var search by remember { mutableStateOf("") }
    var limitPkg by remember { mutableStateOf<String?>(null) }

    val filtered = remember(apps, search) {
        if (search.isBlank()) apps
        else apps.filter { it.appName.contains(search, true) || it.packageName.contains(search, true) }
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            SectionHeader("Kontrol Aplikasi", "Blokir & limit waktu per app")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(search, { search = it },
                placeholder = { Text("Cari app...", color = TextDim.copy(0.4f)) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Cyan) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan, unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, cursorColor = Cyan,
                    focusedContainerColor = Glass, unfocusedContainerColor = Glass))
        }
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                val rule = ruleMap[app.packageName]
                AppRow(app.appName, rule,
                    onBlock = { vm.setBlocked(app.packageName, app.appName, it) },
                    onLimit = { limitPkg = app.packageName })
            }
        }
    }

    limitPkg?.let { pkg ->
        val app  = vm.installedApps.value.find { it.packageName == pkg }
        val rule = ruleMap[pkg]
        var mins by remember { mutableStateOf((rule?.dailyLimitMinutes ?: 0).toString()) }
        AlertDialog(onDismissRequest = { limitPkg = null }, containerColor = BgDeep,
            title = { Text("Batas Harian", color = TextWhite, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(app?.appName ?: pkg, color = Cyan, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(mins, { mins = it.filter { c -> c.isDigit() } },
                        label = { Text("Menit/hari (0 = bebas)") }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassBorder, focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite, cursorColor = Cyan,
                            focusedLabelColor = Cyan, unfocusedLabelColor = TextDim))
                }
            },
            confirmButton = { TextButton({ vm.setLimit(pkg, app?.appName ?: pkg, mins.toIntOrNull() ?: 0); limitPkg = null }) { Text("Simpan", color = Cyan) } },
            dismissButton = { TextButton({ limitPkg = null }) { Text("Batal", color = TextDim) } })
    }
}

@Composable
fun AppRow(name: String, rule: AppRule?, onBlock: (Boolean) -> Unit, onLimit: () -> Unit) {
    val blocked = rule?.isBlocked == true
    GlassCard(Modifier.fillMaxWidth(), borderColor = if (blocked) Red.copy(0.4f) else GlassBorder) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                .background(Brush.radialGradient(listOf(Cyan.copy(0.2f), Purple.copy(0.1f)))),
                contentAlignment = Alignment.Center) {
                Text(name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleMedium, color = Cyan)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge, color = TextWhite)
                if (blocked) Text("DIBLOKIR", style = MaterialTheme.typography.labelSmall, color = Red)
                else if ((rule?.dailyLimitMinutes ?: 0) > 0)
                    Text("Max ${rule?.dailyLimitMinutes}m/hari", style = MaterialTheme.typography.labelSmall, color = Amber)
            }
            Switch(blocked, onBlock, colors = SwitchDefaults.colors(
                checkedThumbColor = Red, checkedTrackColor = Red.copy(0.3f),
                uncheckedThumbColor = TextDim, uncheckedTrackColor = Glass))
            IconButton(onLimit) {
                Icon(Icons.Filled.Timer, null, tint = if ((rule?.dailyLimitMinutes ?: 0) > 0) Amber else TextDim.copy(0.4f))
            }
        }
    }
}

// ── Usage Screen ─────────────────────────────────────────────────────────────

@Composable
fun UsageScreen(vm: MainViewModel) {
    val usage = remember(vm.todayUsage.collectAsState().value) {
        vm.todayUsage.value.sortedByDescending { it.usageMinutes }
    }
    val total = max(1L, usage.sumOf { it.usageMinutes })

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            SectionHeader("Pemakaian Hari Ini", "Screen time per aplikasi")
            Spacer(Modifier.height(12.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    SmallStat("Total", "${total}m", Cyan, Modifier)
                    SmallStat("Apps", "${usage.size}", Purple, Modifier)
                    SmallStat("Durasi", "${total/60}j ${total%60}m", Amber, Modifier)
                }
            }
        }
        if (usage.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 48.sp)
                    Text("Belum ada data", style = MaterialTheme.typography.titleMedium, color = TextDim)
                    Text("Aktifkan Usage Access terlebih dahulu", style = MaterialTheme.typography.bodySmall, color = TextDim.copy(0.5f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(usage) { idx, rec ->
                    val pct   = rec.usageMinutes.toFloat() / total
                    val color = when(idx) { 0 -> Cyan; 1 -> Purple; 2 -> Amber; else -> TextDim.copy(0.5f) }
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                Box(Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(0.15f)), contentAlignment = Alignment.Center) {
                                    Text("${idx+1}", style = MaterialTheme.typography.labelSmall, color = color)
                                }
                                Column {
                                    Text(rec.appName, style = MaterialTheme.typography.bodyLarge, color = TextWhite)
                                    Text("${(pct*100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextDim.copy(0.5f))
                                }
                            }
                            Text("${rec.usageMinutes}m", style = MaterialTheme.typography.titleMedium, color = color)
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Glass)) {
                            Box(Modifier.fillMaxWidth(pct).height(3.dp).clip(RoundedCornerShape(2.dp))
                                .background(Brush.horizontalGradient(listOf(color, color.copy(0.4f)))))
                        }
                    }
                }
            }
        }
    }
}

// ── Settings Screen ───────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(vm: MainViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("Pengaturan", "Konfigurasi DRAGONIC Guard v4")
        GlassCard(Modifier.fillMaxWidth()) {
            Text("ID Perangkat", style = MaterialTheme.typography.labelSmall, color = Cyan)
            Spacer(Modifier.height(4.dp))
            Text(vm.deviceId, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Cyan)
            Spacer(Modifier.height(8.dp))
            Text("Gunakan ID ini di app orang tua untuk terhubung", style = MaterialTheme.typography.bodySmall, color = TextDim.copy(0.5f))
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("📋 Cara Setup", style = MaterialTheme.typography.titleMedium, color = Cyan)
            Spacer(Modifier.height(8.dp))
            listOf("1. Aktifkan Device Admin di Dashboard", "2. Aktifkan Accessibility Service",
                "3. Aktifkan Usage Access", "4. Salin ID Perangkat ke app orang tua",
                "5. Siap! HP terlindungi DRAGONIC Guard v4").forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextDim)
                Spacer(Modifier.height(4.dp))
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Versi", style = MaterialTheme.typography.bodySmall, color = TextDim)
            Text("DRAGONIC Guard v4.0.0", style = MaterialTheme.typography.titleMedium, color = TextWhite)
        }
    }
}
