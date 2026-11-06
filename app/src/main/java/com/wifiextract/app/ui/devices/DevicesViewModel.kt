package com.wifiextract.app.ui.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wifiextract.app.NetworkInfoRepository
import com.wifiextract.app.core.Subnet
import com.wifiextract.app.data.Device
import com.wifiextract.app.data.DiscoveryRepository
import com.wifiextract.app.data.ScanEvent
import com.wifiextract.app.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class DevicesState(
    val scanning: Boolean = false,
    val hasScanned: Boolean = false,
    val scanned: Int = 0,
    val total: Int = 0,
    val subnetLabel: String = "",
    val ownIp: String = "",
    val gateway: String = "",
    val devices: List<Device> = emptyList(),
    val message: String? = null,
    val keepScreenOn: Boolean = false
)

class DevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val network = NetworkInfoRepository(application)
    private val discovery = DiscoveryRepository()
    private val settings = SettingsRepository(application)
    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state.asStateFlow()
    private var job: Job? = null

    fun startScan() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            val info = withContext(Dispatchers.IO) { runCatching { network.read() }.getOrNull() }
            val allowed = info != null && info.connected && info.prefixLength > 0 &&
                (info.transport == "Wi-Fi" || info.transport == "Ethernet")
            if (info == null || !allowed) {
                _state.update {
                    it.copy(hasScanned = true, message = "Conéctate a una red Wi-Fi o Ethernet para buscar dispositivos.")
                }
                return@launch
            }
            val hosts = Subnet.hosts(InetAddress.getByName(info.localAddress).address, info.prefixLength)
            _state.value = DevicesState(
                scanning = true,
                hasScanned = true,
                total = hosts.size,
                subnetLabel = "${hosts.first().substringBeforeLast('.')}.0/24",
                ownIp = info.localAddress,
                gateway = info.gateway,
                keepScreenOn = settings.keepScreenOn
            )
            try {
                discovery.scan(hosts, settings.timeoutMs, settings.resolveHostnames).collect { event ->
                    when (event) {
                        is ScanEvent.Progress -> _state.update { it.copy(scanned = event.scanned) }
                        is ScanEvent.Found -> _state.update {
                            it.copy(devices = (it.devices + event.device).sortedBy { d -> Subnet.ipToLong(d.ip) })
                        }
                    }
                }
                _state.update { it.copy(message = if (it.devices.isEmpty()) "Ningún dispositivo respondió." else null) }
            } finally {
                _state.update { it.copy(scanning = false) }
            }
        }
    }

    fun stopScan() {
        if (job?.isActive != true) return
        _state.update { it.copy(message = "Escaneo detenido. Resultados parciales.") }
        job?.cancel()
    }
}
