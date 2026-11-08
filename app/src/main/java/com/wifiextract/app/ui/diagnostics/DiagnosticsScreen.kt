package com.wifiextract.app.ui.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.R
import com.wifiextract.app.core.LogEntry
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.MnaTextField
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SectionLabel
import com.wifiextract.app.ui.components.SegmentedControl
import com.wifiextract.app.ui.components.SkeletonLines
import com.wifiextract.app.ui.components.TerminalBlock
import com.wifiextract.app.ui.theme.MnaColors

@Composable
fun DiagnosticsScreen(
    state: DiagnosticsState,
    logs: List<LogEntry>,
    onTool: (DiagnosticTool) -> Unit,
    onHost: (String) -> Unit,
    onPort: (String) -> Unit,
    onRun: () -> Unit,
    onCancel: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val formattedLogs = logs.map { "[${it.formattedTime}] [${it.level}] ${it.tag}: ${it.message}" }

    Column(modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.nav_tools))
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentedControl(
                options = listOf("Ping", "DNS", stringResource(R.string.tool_port), stringResource(R.string.tool_console)),
                selected = when (state.tool) {
                    DiagnosticTool.Ping -> 0
                    DiagnosticTool.Dns -> 1
                    DiagnosticTool.Port -> 2
                    DiagnosticTool.Logs -> 3
                },
                onSelect = {
                    when (it) {
                        0 -> onTool(DiagnosticTool.Ping)
                        1 -> onTool(DiagnosticTool.Dns)
                        2 -> onTool(DiagnosticTool.Port)
                        3 -> onTool(DiagnosticTool.Logs)
                    }
                }
            )

            if (state.tool == DiagnosticTool.Logs) {
                MnaCard {
                    Eyebrow(stringResource(R.string.console_title_count, logs.size))
                    Note(stringResource(R.string.console_desc))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(stringResource(R.string.btn_clear), onClearLogs, Modifier.weight(1f))
                    SecondaryButton(
                        stringResource(R.string.btn_copy_console),
                        { clipboard.setText(AnnotatedString(formattedLogs.joinToString("\n"))) },
                        Modifier.weight(1f),
                        enabled = formattedLogs.isNotEmpty()
                    )
                }
                SectionLabel(stringResource(R.string.section_system_logs))
                TerminalBlock(
                    lines = if (formattedLogs.isEmpty()) listOf(stringResource(R.string.console_empty)) else formattedLogs
                ) {}
            } else {
                MnaCard {
                    Eyebrow(stringResource(R.string.destination_eyebrow))
                    MnaTextField(state.host, onHost, stringResource(R.string.host_or_ip_label), onDone = onRun)
                    if (state.tool == DiagnosticTool.Port) {
                        MnaTextField(state.port, onPort, stringResource(R.string.port_range_label), keyboardType = KeyboardType.Number, onDone = onRun)
                    }
                    state.error?.let { Text(it.asString(), color = MnaColors.AccentText, fontSize = 13.sp, lineHeight = 18.sp) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.running) {
                        SecondaryButton(stringResource(R.string.btn_cancel), onCancel, Modifier.weight(1f))
                    } else {
                        PrimaryButton(stringResource(R.string.btn_run), onRun, Modifier.weight(1f))
                    }
                    SecondaryButton(
                        stringResource(R.string.btn_copy_result),
                        { clipboard.setText(AnnotatedString(state.lines.joinToString("\n"))) },
                        Modifier.weight(1f),
                        enabled = state.lines.isNotEmpty()
                    )
                }
                if (state.lines.isNotEmpty() || state.running) {
                    SectionLabel(stringResource(R.string.section_result))
                    TerminalBlock(state.lines) { if (state.running) SkeletonLines(lines = 2) }
                }
            }

            Note(stringResource(R.string.diagnostics_notice))
            Spacer(Modifier.height(28.dp))
        }
    }
}
