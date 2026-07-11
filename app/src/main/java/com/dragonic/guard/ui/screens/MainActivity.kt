package com.dragonic.guard.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dragonic.guard.receiver.GuardDeviceAdminReceiver
import com.dragonic.guard.service.GuardMonitorService
import com.dragonic.guard.ui.theme.*
import com.dragonic.guard.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private val adminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { vm.refreshAdminStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start monitor service
        startForegroundService(Intent(this, GuardMonitorService::class.java))

        setContent {
            DRAGONICGuardTheme {
                GuardApp(
                    vm = vm,
                    onRequestAdmin = { requestDeviceAdmin() },
                    onRequestAccessibility = { openAccessibilitySettings() },
                    onRequestUsageStats = { openUsageStatsSettings() }
                )
            }
        }
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(this@MainActivity, GuardDeviceAdminReceiver::class.java))
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "DRAGONIC Guard memerlukan hak Device Admin untuk mencegah uninstall dan mengunci perangkat dari jarak jauh.")
        }
        adminLauncher.launch(intent)
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun openUsageStatsSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}

// ── Navigation ───────────────────────────────────────────────────────────────

@Composable
fun GuardApp(
    vm: MainViewModel,
    onRequestAdmin: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestUsageStats: () -> Unit
) {
    val navController = rememberNavController()

    val navItems = listOf(
        NavItem("dashboard", Icons.Filled.Shield, "Dashboard"),
        NavItem("apps", Icons.Filled.Apps, "Aplikasi"),
        NavItem("usage", Icons.Filled.BarChart, "Pemakaian"),
        NavItem("settings", Icons.Filled.Settings, "Pengaturan"),
    )

    Scaffold(
        containerColor = GuardBlack,
        bottomBar = {
            NavigationBar(
                containerColor = GuardDeepBlue.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination
                navItems.forEach { item ->
                    val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.label,
                                tint = if (selected) GuardCyan else GuardWhiteDim.copy(alpha = 0.5f))
                        },
                        label = {
                            Text(item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) GuardCyan else GuardWhiteDim.copy(alpha = 0.5f))
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GuardCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(GuardBlack, GuardDeepBlue, GuardBlack)
                    )
                )
                .padding(padding)
        ) {
            NavHost(navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(vm, onRequestAdmin, onRequestAccessibility, onRequestUsageStats)
                }
                composable("apps") {
                    AppControlScreen(vm)
                }
                composable("usage") {
                    UsageScreen(vm)
                }
                composable("settings") {
                    SettingsScreen(vm)
                }
            }
        }
    }
}

data class NavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)
