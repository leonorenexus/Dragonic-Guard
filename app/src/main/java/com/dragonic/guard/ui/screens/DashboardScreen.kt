package com.dragonic.guard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragonic.guard.ui.components.*
import com.dragonic.guard.ui.theme.*
import com.dragonic.guard.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    vm: MainViewModel,
    onRequestAdmin: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestUsageStats: () -> Unit
) {
    val rules by vm.allRules.collectAsState()
    val usage by vm.todayUsage.collectAsState()
    val isAdmin by vm.isDeviceAdmin.collectAsState()

    val blockedCount = rules.count { it.isBlocked }
    val totalMinutes = usage.sumOf { it.usageMinutes }

    // Rotating ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "DRAGONIC",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = GuardCyan
                )
                Text(
                    "Guard",
                    style = MaterialTheme.typography.displayLarge,
                    color = GuardWhite
                )
            }
            StatusBadge(label = if (isAdmin) "Protected" else "Setup", active = isAdmin)
        }

        // ── 3D Glass Hero ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GuardNavy,
                            GuardDeepBlue,
                            GuardBlack
                        )
                    )
                )
                .border(1.dp, GuardGlassBorder, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Decorative rotating ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .rotate(rotation)
                    .border(
                        2.dp,
                        Brush.sweepGradient(
                            listOf(
                                GuardCyan.copy(alpha = 0f),
                                GuardCyan.copy(alpha = 0.8f),
                                GuardPurple.copy(alpha = 0.6f),
                                GuardCyan.copy(alpha = 0f)
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .rotate(-rotation * 0.6f)
                    .border(
                        1.dp,
                        Brush.sweepGradient(
                            listOf(
                                GuardPurple.copy(alpha = 0f),
                                GuardPurple.copy(alpha = 0.6f),
                                GuardCyan.copy(alpha = 0.3f),
                                GuardPurple.copy(alpha = 0f)
                            )
                        ),
                        CircleShape
                    )
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = GuardCyan,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${totalMinutes}m",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                    color = GuardWhite
                )
                Text(
                    "SCREEN TIME HARI INI",
                    style = MaterialTheme.typography.labelSmall,
                    color = GuardCyan.copy(alpha = 0.7f)
                )
            }
        }

        // ── Stats Row ────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatChip(
                "App Diblokir",
                "$blockedCount",
                GuardRed,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                "Aturan Aktif",
                "${rules.size}",
                GuardAmber,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                "App Dipantau",
                "${vm.installedApps.value.size}",
                GuardGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Permission Cards ─────────────────────────────────────────────────
        SectionHeader(title = "Status Izin", subtitle = "Aktifkan semua untuk perlindungan penuh")

        PermissionCard(
            title = "Device Administrator",
            description = "Cegah uninstall & kunci layar jarak jauh",
            isActive = isAdmin,
            onActivate = onRequestAdmin
        )
        PermissionCard(
            title = "Accessibility Service",
            description = "Blokir aplikasi secara real-time",
            isActive = false, // check at runtime via Settings
            onActivate = onRequestAccessibility
        )
        PermissionCard(
            title = "Usage Access",
            description = "Pantau screen time setiap aplikasi",
            isActive = false,
            onActivate = onRequestUsageStats
        )

        // ── Quick Actions ────────────────────────────────────────────────────
        SectionHeader(title = "Aksi Cepat")

        GlowButton(
            text = "🔒  KUNCI PERANGKAT SEKARANG",
            onClick = { vm.lockDeviceNow() },
            glowColor = GuardRed,
            enabled = isAdmin
        )

        Spacer(Modifier.height(8.dp))

        // Device ID
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("ID Perangkat Anak", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                vm.deviceId,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = GuardCyan
            )
            Text(
                "Gunakan ID ini di Firebase Console untuk mengirim perintah remote",
                style = MaterialTheme.typography.bodySmall,
                color = GuardWhiteDim.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = GuardWhiteDim,
            maxLines = 1
        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isActive: Boolean,
    onActivate: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GuardWhite)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(12.dp))
            if (isActive) {
                StatusBadge("Aktif", true)
            } else {
                OutlinedButton(
                    onClick = onActivate,
                    border = BorderStroke(1.dp, GuardCyan.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Aktifkan", style = MaterialTheme.typography.labelSmall, color = GuardCyan)
                }
            }
        }
    }
}
