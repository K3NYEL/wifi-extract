package com.wifiextract.app.ui.diagnostics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wifiextract.app.R
import com.wifiextract.app.core.LogLevel
import com.wifiextract.app.core.MnaLogger
import com.wifiextract.app.core.UiText
import com.wifiextract.app.core.Validation
import com.wifiextract.app.data.DiagnosticsRepository
import com.wifiextract.app.data.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

enum class DiagnosticTool { Ping, Dns, Port, Logs }

data class DiagnosticsState(
    val tool: DiagnosticTool = DiagnosticTool.Ping,
    val host: String = "1.1.1.1",
    val port: String = "443",
    val running: Boolean = false,
    val lines: List<String> = emptyList(),
    val error: UiText? = null
)

class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiagnosticsRepository()
    private val settings = SettingsRepository(application)
    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()
    private var job: Job? = null

    val logs = MnaLogger.logs

    fun selectTool(tool: DiagnosticTool) {
        if (_state.value.running) return
        _state.update { it.copy(tool = tool, lines = emptyList(), error = null) }
        MnaLogger.log("Diagnostics", "Selected tool: $tool", LogLevel.DEBUG)
    }

    fun setHost(value: String) = _state.update { it.copy(host = value.take(253), error = null) }
    fun setPort(value: String) = _state.update { it.copy(port = value.filter(Char::isDigit).take(5), error = null) }

    fun clearLogs() {
        MnaLogger.clear()
        MnaLogger.log("Diagnostics", "Console logs cleared by user", LogLevel.INFO)
    }

    /** Used by Device Discovery: pre-fills a host and runs a ping. */
    fun prefill(host: String) {
        job?.cancel()
        _state.update { it.copy(tool = DiagnosticTool.Ping, host = host, lines = emptyList(), error = null, running = false) }
        run()
    }

    fun run() {
        val current = _state.value
        if (current.running || current.tool == DiagnosticTool.Logs) return
        val host = current.host.trim()
        if (!Validation.isValidHost(host)) {
            _state.update { it.copy(error = UiText.Res(R.string.err_invalid_host)) }
            MnaLogger.log("Diagnostics", "Invalid host input: $host", LogLevel.WARN)
            return
        }
        val flow: Flow<String> = when (current.tool) {
            DiagnosticTool.Ping -> {
                MnaLogger.log("Diagnostics", "Starting Ping to $host (timeout: ${settings.timeoutMs}ms)", LogLevel.INFO)
                repository.ping(host, settings.timeoutMs)
            }
            DiagnosticTool.Dns -> {
                MnaLogger.log("Diagnostics", "Starting DNS lookup for $host", LogLevel.INFO)
                repository.dnsLookup(host)
            }
            DiagnosticTool.Port -> {
                val port = Validation.parsePort(current.port)
                if (port == null) {
                    _state.update { it.copy(error = UiText.Res(R.string.err_invalid_port)) }
                    MnaLogger.log("Diagnostics", "Invalid port input: ${current.port}", LogLevel.WARN)
                    return
                }
                MnaLogger.log("Diagnostics", "Starting TCP Port check on $host:$port (timeout: ${settings.timeoutMs}ms)", LogLevel.INFO)
                repository.tcpPort(host, port, settings.timeoutMs)
            }
            DiagnosticTool.Logs -> return
        }
        _state.update { it.copy(running = true, lines = emptyList(), error = null) }
        job = viewModelScope.launch {
            try {
                flow.collect { line -> _state.update { it.copy(lines = it.lines + line) } }
                MnaLogger.log("Diagnostics", "Tool ${current.tool} completed successfully for $host", LogLevel.INFO)
            } catch (e: CancellationException) {
                _state.update { it.copy(lines = it.lines + "Cancelado.") }
                MnaLogger.log("Diagnostics", "Tool ${current.tool} cancelled for $host", LogLevel.WARN)
                throw e
            } catch (e: UnknownHostException) {
                _state.update { it.copy(error = UiText.Res(R.string.err_unknown_host, listOf(host))) }
                MnaLogger.log("Diagnostics", "Unknown host: $host", LogLevel.ERROR)
            } catch (e: Exception) {
                _state.update { it.copy(error = UiText.Res(R.string.error_generic)) }
                MnaLogger.log("Diagnostics", "Tool failed for $host: ${e.localizedMessage}", LogLevel.ERROR)
            } finally {
                _state.update { it.copy(running = false) }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        MnaLogger.log("Diagnostics", "Job cancelled by user", LogLevel.WARN)
    }
}
