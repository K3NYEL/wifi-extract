package com.k3nyel.wifiextract.ui.diagnostics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.k3nyel.wifiextract.core.Validation
import com.k3nyel.wifiextract.data.DiagnosticsRepository
import com.k3nyel.wifiextract.data.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

enum class DiagnosticTool { Ping, Dns, Port }

data class DiagnosticsState(
    val tool: DiagnosticTool = DiagnosticTool.Ping,
    val host: String = "1.1.1.1",
    val port: String = "443",
    val running: Boolean = false,
    val lines: List<String> = emptyList(),
    val error: String? = null
)

class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiagnosticsRepository()
    private val settings = SettingsRepository(application)
    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()
    private var job: Job? = null

    fun selectTool(tool: DiagnosticTool) {
        if (_state.value.running) return
        _state.update { it.copy(tool = tool, lines = emptyList(), error = null) }
    }

    fun setHost(value: String) = _state.update { it.copy(host = value.take(253), error = null) }
    fun setPort(value: String) = _state.update { it.copy(port = value.filter(Char::isDigit).take(5), error = null) }

    /** Used by Device Discovery: pre-fills a host and runs a ping. */
    fun prefill(host: String) {
        job?.cancel()
        _state.update { it.copy(tool = DiagnosticTool.Ping, host = host, lines = emptyList(), error = null, running = false) }
        run()
    }

    fun run() {
        val current = _state.value
        if (current.running) return
        val host = current.host.trim()
        if (!Validation.isValidHost(host)) {
            _state.update { it.copy(error = "Introduce una IPv4 o un nombre de host válido.") }
            return
        }
        val flow: Flow<String> = when (current.tool) {
            DiagnosticTool.Ping -> repository.ping(host, settings.timeoutMs)
            DiagnosticTool.Dns -> repository.dnsLookup(host)
            DiagnosticTool.Port -> {
                val port = Validation.parsePort(current.port)
                if (port == null) {
                    _state.update { it.copy(error = "El puerto debe estar entre 1 y 65535.") }
                    return
                }
                repository.tcpPort(host, port, settings.timeoutMs)
            }
        }
        _state.update { it.copy(running = true, lines = emptyList(), error = null) }
        job = viewModelScope.launch {
            try {
                flow.collect { line -> _state.update { it.copy(lines = it.lines + line) } }
            } catch (e: CancellationException) {
                _state.update { it.copy(lines = it.lines + "Cancelado.") }
                throw e
            } catch (e: UnknownHostException) {
                _state.update { it.copy(error = "No se pudo resolver «$host». Revisa el nombre y tu conexión.") }
            } catch (e: Exception) {
                _state.update { it.copy(error = "La comprobación falló. Inténtalo de nuevo.") }
            } finally {
                _state.update { it.copy(running = false) }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }
}
