package com.wifiextract.app.ui.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.ui.components.ComingSoonTag
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.MnaTextField
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SectionLabel
import com.wifiextract.app.ui.components.SegmentedControl
import com.wifiextract.app.ui.components.TerminalBlock
import com.wifiextract.app.ui.components.SkeletonLines
import com.wifiextract.app.ui.theme.MnaColors
import com.wifiextract.app.ui.theme.MnaShapes

@Composable
fun DiagnosticsScreen(
    state: DiagnosticsState,
    onTool: (DiagnosticTool) -> Unit,
    onHost: (String) -> Unit,
    onPort: (String) -> Unit,
    onRun: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    Column(modifier.fillMaxSize()) {
        ScreenHeader("Herramientas")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentedControl(
                options = listOf("Ping", "DNS", "Puerto", "Trace"),
                selected = when (state.tool) {
                    DiagnosticTool.Ping -> 0
                    DiagnosticTool.Dns -> 1
                    DiagnosticTool.Port -> 2
                    else -> 0
                },
                onSelect = {
                    when (it) {
                        0 -> onTool(DiagnosticTool.Ping)
                        1 -> onTool(DiagnosticTool.Dns)
                        2 -> onTool(DiagnosticTool.Port)
                    }
                },
                disabled = setOf(3)
            )
            MnaCard {
                Eyebrow("DESTINO")
                MnaTextField(state.host, onHost, "Host o IPv4", onDone = onRun)
                if (state.tool == DiagnosticTool.Port) {
                    MnaTextField(state.port, onPort, "Puerto (1–65535)", keyboardType = KeyboardType.Number, onDone = onRun)
                }
                state.error?.let { Text(it, color = MnaColors.AccentText, fontSize = 13.sp, lineHeight = 18.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.running) {
                    SecondaryButton("Cancelar", onCancel, Modifier.weight(1f))
                } else {
                    PrimaryButton("Ejecutar", onRun, Modifier.weight(1f))
                }
                SecondaryButton(
                    "Copiar resultado",
                    { clipboard.setText(AnnotatedString(state.lines.joinToString("\n"))) },
                    Modifier.weight(1f),
                    enabled = state.lines.isNotEmpty()
                )
            }
            if (state.lines.isNotEmpty() || state.running) {
                SectionLabel("Resultado")
                TerminalBlock(state.lines) { if (state.running) SkeletonLines(lines = 2) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Traceroute", color = MnaColors.TextSecondary, fontSize = 12.sp)
                ComingSoonTag()
            }
            Note("Cada comprobación va dirigida a un único host, es cancelable y tiene tiempo máximo. Úsalas solo en redes y equipos propios o con autorización.")
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ToolButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = MnaShapes.Button,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MnaColors.AccentButton else MnaColors.SurfaceRaised,
            contentColor = if (selected) Color.White else MnaColors.TextPrimary
        )
    ) { Text(text, fontSize = 14.sp, maxLines = 1) }
}
