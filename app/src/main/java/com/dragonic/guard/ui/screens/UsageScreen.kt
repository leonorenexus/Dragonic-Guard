package com.dragonic.guard.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragonic.guard.ui.components.*
import com.dragonic.guard.ui.theme.*
import com.dragonic.guard.viewmodel.MainViewModel
import kotlin.math.max

@Composable
fun UsageScreen(vm: MainViewModel) {
    val usageList by vm.todayUsage.collectAsState()
    val totalMinutes = max(1L, usageList.sumOf { it.usageMinutes })

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            SectionHeader(title = "Pemakaian Hari Ini", subtitle = "Screen time per aplikasi")
            Spacer(Modifier.height(16.dp))

            // Total card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatChip("Total", "${totalMinutes}m", GuardCyan)
                    StatChip("Aplikasi", "${usageList.size}", GuardPurple)
                    val hours = totalMinutes / 60
                    val mins = totalMinutes % 60
                    StatChip("Durasi", "${hours}j ${mins}m", GuardAmber)
                }
            }
        }

        if (usageList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada data pemakaian",
                        style = MaterialTheme.typography.titleMedium,
                        color = GuardWhiteDim)
                    Text("Data akan muncul setelah izin Usage Access diaktifkan",
                        style = MaterialTheme.typography.bodySmall,
                        color = GuardWhiteDim.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(usageList.sortedByDescending { it.usageMinutes }) { index, record ->
                    val percent = record.usageMinutes.toFloat() / totalMinutes
                    val barColor = when (index) {
                        0 -> GuardCyan
                        1 -> GuardPurple
                        2 -> GuardAmber
                        else -> GuardWhiteDim.copy(alpha = 0.5f)
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(barColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = barColor
                                    )
                                }
                                Column {
                                    Text(record.appName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = GuardWhite)
                                    Text("${(percent * 100).toInt()}% dari total",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GuardWhiteDim.copy(0.5f))
                                }
                            }
                            Text(
                                "${record.usageMinutes}m",
                                style = MaterialTheme.typography.titleMedium,
                                color = barColor
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        // Progress bar
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GuardGlass)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(percent)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(barColor, barColor.copy(alpha = 0.4f))
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
