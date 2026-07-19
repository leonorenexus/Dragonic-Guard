package com.dragonic.guard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dragonic.guard.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val inf = rememberInfiniteTransition(label = "g")
    val a by inf.animateFloat(0.3f, 0.7f,
        infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "a")
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(
                listOf(Color(0x1A4FC3F7), Color(0x0D7C4DFF), Color(0x0A050810)),
                Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ))
            .border(1.dp,
                Brush.linearGradient(listOf(borderColor.copy(a), Purple.copy(a * 0.5f), borderColor.copy(a * 0.3f))),
                RoundedCornerShape(20.dp))
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
fun GlowButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Cyan, enabled: Boolean = true
) {
    val inf = rememberInfiniteTransition(label = "b")
    val s by inf.animateFloat(0.7f, 1f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse), label = "s")
    Box(modifier
        .clip(RoundedCornerShape(14.dp))
        .background(Brush.horizontalGradient(listOf(color.copy(if (enabled) 0.25f else 0.08f), Purple.copy(if (enabled) 0.25f else 0.08f))))
        .border(1.5.dp, Brush.horizontalGradient(listOf(color.copy(s), Purple.copy(s * 0.7f))), RoundedCornerShape(14.dp))
    ) {
        Button(onClick = onClick, enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = color,
                disabledContainerColor = Color.Transparent, disabledContentColor = TextDim.copy(0.3f)
            ), modifier = Modifier.fillMaxWidth()
        ) { Text(text, letterSpacing = 1.5.sp, style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp)) }
    }
}

@Composable
fun StatusBadge(label: String, active: Boolean, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "p")
    val a by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pa")
    val c = if (active) Green else Red
    Row(modifier
        .clip(RoundedCornerShape(50))
        .background(c.copy(0.15f))
        .border(1.dp, c.copy(if (active) a else 0.4f), RoundedCornerShape(50))
        .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(c.copy(if (active) a else 0.4f)))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = c)
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextWhite)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Cyan.copy(0.7f)) }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth(0.3f).height(1.dp)
            .background(Brush.horizontalGradient(listOf(Cyan, Purple, Color.Transparent))))
    }
}
