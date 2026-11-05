package com.k3nyel.wifiextract.ui.diagnostics

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
import com.k3nyel.wifiextract.ui.components.ComingSoonTag
import com.k3nyel.wifiextract.ui.components.Eyebrow
import com.k3nyel.wifiextract.ui.components.MnaCard
import com.k3nyel.wifiextract.ui.components.MnaTextField
import com.k3nyel.wifiextract.ui.components.Note
import com.k3nyel.wifiextract.ui.components.PrimaryButton
import com.k3nyel.wifiextract.ui.components.ScreenHeader
import com.k3nyel.wifiextract.ui.components.SecondaryButton
import com.k3nyel.wifiextract.ui.components.SkeletonLines
import com.k3nyel.wifiextract.ui.theme.MnaColors
import com.k3nyel.wifiextract.ui.theme.MnaShapes

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
        ScreenHeader("Network Diagnostics")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolButton("Ping", state.tool == DiagnosticTool.Ping, Modifier.weight(1f)) { onTool(DiagnosticTool.Ping) }
                ToolButton("Consulta DNS", state.tool == DiagnosticTool.Dns, Modifier.weight(1f)) { onTool(DiagnosticTool.Dns) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolButton("Puerto TCP", state.tool == DiagnosticTool.Port, Modifier.weight(1f)) { onTool(DiagnosticTool.Port) }
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MnaShapes.Button,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MnaColors.SurfaceRaised,
                        disabledContentColor = MnaColors.TextSecondary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Traceroute", fontSize = 14.sp)
                        ComingSoonTag()
                    }
                }
            }
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
                MnaCard {
                    Eyebrow("RESULTADO")
                    state.lines.forEach {
                        Text(it, color = MnaColors.TextPrimary, fontSize = 12.sp, lineHeight = 19.sp, fontFamily = FontFamily.Monospace)
                    }
                    if (state.running) SkeletonLines(lines = 2)
                }
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
