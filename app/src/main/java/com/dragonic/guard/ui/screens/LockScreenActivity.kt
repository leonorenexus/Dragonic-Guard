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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dragonic.guard.ui.theme.*

private val android.content.Context.ds by preferencesDataStore("guard_settings")

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
        setContent {
            GuardTheme {
                LockScreen(correctPin = "1234", onUnlock = { finish() })
            }
        }
    }
}

@Composable
fun LockScreen(correctPin: String, onUnlock: () -> Unit) {
    var pin   by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "l")
    val rot by inf.animateFloat(0f, 360f,
        infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "lr")

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(BgNavy, BgDeep, BgBlack))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Lock icon with ring
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(110.dp).rotate(rot).border(
                    2.dp, Brush.sweepGradient(listOf(Red.copy(0f), Red.copy(0.9f), Purple.copy(0.5f), Red.copy(0f))),
                    CircleShape))
                Box(Modifier.size(72.dp).clip(CircleShape).background(Red.copy(0.15f))
                    .border(1.dp, Red.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Lock, null, tint = Red, modifier = Modifier.size(34.dp))
                }
            }

            Text("AKSES DIBLOKIR",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 20.sp, letterSpacing = 4.sp),
                color = Red)
            Text("Masukkan PIN orang tua untuk membuka",
                style = MaterialTheme.typography.bodyLarge, color = TextDim, textAlign = TextAlign.Center)

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { i ->
                    Box(Modifier.size(14.dp).clip(CircleShape)
                        .background(if (i < pin.length) Cyan else Glass)
                        .border(1.dp, if (error) Red else GlassBorder, CircleShape))
                }
            }
            if (error) Text("PIN salah", color = Red, style = MaterialTheme.typography.bodySmall)

            // Numpad
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Glass)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)).padding(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1 2 3", "4 5 6", "7 8 9", "⌫ 0 ✓").forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.split(" ").forEach { key ->
                                OutlinedButton(
                                    onClick = {
                                        error = false
                                        when (key) {
                                            "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            "✓" -> if (pin == correctPin) onUnlock() else { error = true; pin = "" }
                                            else -> if (pin.length < 4) pin += key
                                        }
                                    },
                                    modifier = Modifier.size(62.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (key == "✓") Cyan else TextWhite,
                                        containerColor = if (key == "✓") Cyan.copy(0.1f) else Surface1
                                    )
                                ) { Text(key, fontSize = if (key in listOf("⌫","✓")) 16.sp else 18.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}
