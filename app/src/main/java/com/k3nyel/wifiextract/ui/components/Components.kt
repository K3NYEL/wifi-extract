package com.k3nyel.wifiextract.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3nyel.wifiextract.ui.theme.MnaColors
import com.k3nyel.wifiextract.ui.theme.MnaShapes

private val SkeletonColor = Color(0xFF2A2A30)

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, modifier = Modifier.weight(1f), color = MnaColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        trailing()
    }
}

@Composable
fun MnaCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().background(MnaColors.Surface, MnaShapes.Card).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = MnaColors.TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun Note(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = MnaColors.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.background(color.copy(alpha = 0.14f), MnaShapes.Chip).padding(horizontal = 8.dp, vertical = 3.dp),
        color = color,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun ComingSoonTag(modifier: Modifier = Modifier) {
    Text(
        "PRÓXIMAMENTE",
        modifier = modifier.background(Color(0xFF3B1210), MnaShapes.Chip).padding(horizontal = 6.dp, vertical = 2.dp),
        color = Color(0xFFFF8A85),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier, mono: Boolean = true) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = MnaColors.TextSecondary, fontSize = 13.sp)
        Text(
            value,
            modifier = Modifier.weight(1f),
            color = MnaColors.TextPrimary,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = MnaShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MnaColors.AccentButton,
            contentColor = Color.White,
            disabledContainerColor = MnaColors.SurfaceRaised,
            disabledContentColor = MnaColors.TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = MnaShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MnaColors.SurfaceRaised,
            contentColor = MnaColors.TextPrimary,
            disabledContainerColor = MnaColors.SurfaceRaised,
            disabledContentColor = MnaColors.TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) { Text(text, fontSize = 14.sp, maxLines = 1) }
}

@Composable
fun MnaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Uri,
    onDone: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
        shape = MnaShapes.Button,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MnaColors.SurfaceFocus,
            unfocusedContainerColor = MnaColors.SurfaceRaised,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MnaColors.TextPrimary,
            unfocusedTextColor = MnaColors.TextPrimary,
            focusedLabelColor = MnaColors.TextSecondary,
            unfocusedLabelColor = MnaColors.TextSecondary,
            cursorColor = MnaColors.Accent
        )
    )
}

/** Placeholder block shown while real data loads. Pulses; every block is hidden from screen readers. */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier, height: Dp = 14.dp) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Reverse),
        label = "skeleton-alpha"
    )
    Box(modifier.height(height).background(SkeletonColor.copy(alpha = alpha), MnaShapes.Card))
}

/** A card of skeleton rows (label on the left, value on the right). */
@Composable
fun SkeletonCard(rows: Int, modifier: Modifier = Modifier) {
    MnaCard(modifier.semantics { contentDescription = "Cargando" }) {
        repeat(rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(Modifier.width(96.dp))
                SkeletonBox(Modifier.width(130.dp))
            }
        }
    }
}

/** Skeleton shaped like a device row: two text lines and a button. */
@Composable
fun SkeletonListRow(modifier: Modifier = Modifier) {
    MnaCard(modifier.semantics { contentDescription = "Cargando" }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(Modifier.width(120.dp))
                SkeletonBox(Modifier.width(180.dp), height = 12.dp)
            }
            SkeletonBox(Modifier.width(64.dp), height = 40.dp)
        }
    }
}

/** Skeleton for free text (release notes, results). */
@Composable
fun SkeletonLines(lines: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().semantics { contentDescription = "Cargando" }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(lines) { index ->
            SkeletonBox(Modifier.fillMaxWidth(if (index == lines - 1) 0.6f else 1f), height = 12.dp)
        }
    }
}
