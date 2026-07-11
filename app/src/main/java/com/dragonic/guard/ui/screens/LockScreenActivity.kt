package com.dragonic.guard.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragonic.guard.ui.theme.*

class LockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPkg = intent.getStringExtra("blocked_package") ?: "Aplikasi ini"

        // Disable back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* blocked */ }
        })

        setContent {
            DRAGONICGuardTheme {
                LockScreen(
                    blockedApp = blockedPkg,
                    onUnlock = { finish() }
                )
            }
        }
    }
}

@Composable
fun LockScreen(blockedApp: String, onUnlock: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    // Hardcoded PIN for demo — in production read from DataStore
    val correctPin = "1234"

    val infiniteTransition = rememberInfiniteTransition(label = "lock")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "lockRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(GuardNavy, GuardDeepBlue, GuardBlack)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated lock icon
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(120.dp)
                        .rotate(rotation)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    GuardRed.copy(0f), GuardRed.copy(0.8f),
                                    GuardPurple.copy(0.5f), GuardRed.copy(0f)
                                )
                            ),
                            CircleShape
                        )
                )
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GuardRed.copy(alpha = 0.15f))
                        .border(1.dp, GuardRed.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Lock, null,
                        tint = GuardRed,
                        modifier = Modifier.size(36.dp))
                }
            }

            Text(
                "AKSES DIBLOKIR",
                style = MaterialTheme.typography.displayLarge.copy(
                    letterSpacing = 4.sp,
                    fontSize = 22.sp
                ),
                color = GuardRed
            )

            Text(
                "Aplikasi ini diblokir oleh\nDRAGONIC Guard",
                style = MaterialTheme.typography.bodyLarge,
                color = GuardWhiteDim,
                textAlign = TextAlign.Center
            )

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < pin.length) GuardCyan
                                else GuardGlass
                            )
                            .border(
                                1.dp,
                                if (error) GuardRed else GuardGlassBorder,
                                CircleShape
                            )
                    )
                }
            }

            if (error) {
                Text("PIN salah", color = GuardRed,
                    style = MaterialTheme.typography.bodySmall)
            }

            // Numpad
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GuardGlass)
                    .border(1.dp, GuardGlassBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1 2 3", "4 5 6", "7 8 9", "⌫ 0 ✓").forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.split(" ").forEach { key ->
                                OutlinedButton(
                                    onClick = {
                                        error = false
                                        when (key) {
                                            "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            "✓" -> {
                                                if (pin == correctPin) onUnlock()
                                                else { error = true; pin = "" }
                                            }
                                            else -> if (pin.length < 4) pin += key
                                        }
                                    },
                                    modifier = Modifier.size(64.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, GuardGlassBorder
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (key == "✓") GuardCyan else GuardWhite,
                                        containerColor = if (key == "✓") GuardCyan.copy(0.1f) else GuardSurface
                                    )
                                ) {
                                    Text(key, style = MaterialTheme.typography.titleLarge,
                                        fontSize = if (key in listOf("⌫","✓")) 18.sp else 20.sp)
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Masukkan PIN orang tua untuk membuka",
                style = MaterialTheme.typography.bodySmall,
                color = GuardWhiteDim.copy(0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
