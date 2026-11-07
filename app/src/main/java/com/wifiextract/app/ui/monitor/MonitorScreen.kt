package com.wifiextract.app.ui.monitor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.InfoRow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors

private data class WifiConnectionSnapshot(
    val ssid: String,
    val rssi: Int,
    val frequency: Int,
    val linkSpeed: Int,
    val standard: String
)

@Composable
fun MonitorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val wifiManager = remember {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
    var scanResults by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var connection by remember { mutableStateOf(readWifiConnection(context)) }
    var status by remember { mutableStateOf("Pulsa «Escanear redes» para iniciar una búsqueda.") }
    var scanning by remember { mutableStateOf(false) }

    fun hasScanPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadScanResults(fresh: Boolean? = null) {
        if (!hasScanPermissions()) {
            status = "Faltan permisos para consultar los resultados del escaneo."
            scanning = false
            return
        }
        runCatching {
            scanResults = wifiManager.scanResults
                .sortedByDescending { it.level }
                .distinctBy { it.BSSID }
            status = when {
                scanResults.isEmpty() -> "Android no devolvió redes. Comprueba que Wi-Fi y la ubicación estén activados."
                fresh == true -> "${scanResults.size} redes obtenidas en el último escaneo."
                fresh == false -> "Android no actualizó el escaneo; se muestran los últimos resultados disponibles (${scanResults.size})."
                else -> "${scanResults.size} resultados almacenados por Android."
            }
        }.onFailure {
            status = "No se pudieron leer los resultados: ${it.message ?: "error del sistema"}"
        }
        scanning = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            connection = readWifiConnection(context)
            status = "Permisos concedidos. Iniciando escaneo…"
            scanning = true
            @Suppress("DEPRECATION")
            val started = runCatching { wifiManager.startScan() }.getOrDefault(false)
            if (!started) {
                status = "Android limitó o rechazó el escaneo. Puedes consultar los últimos resultados disponibles."
                loadScanResults()
            }
        } else {
            status = "Se necesitan los permisos de ubicación y dispositivos Wi-Fi cercanos para leer redes detectadas."
            scanning = false
        }
    }

    fun requestOrScan() {
        connection = readWifiConnection(context)
        if (!hasScanPermissions()) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }
        if (!isLocationEnabled(context)) {
            status = "Activa los servicios de ubicación del dispositivo para permitir el escaneo Wi-Fi."
            scanning = false
            return
        }
        scanning = true
        status = "Solicitando un nuevo escaneo a Android…"
        @Suppress("DEPRECATION")
        val started = runCatching { wifiManager.startScan() }.getOrDefault(false)
        if (!started) {
            status = "Android no inició un escaneo nuevo (puede aplicar limitación de frecuencia). Mostrando los últimos resultados disponibles."
            loadScanResults()
        }
    }

    DisposableEffect(context, wifiManager) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val fresh = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    loadScanResults(fresh)
                }
            }
        }
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    LaunchedEffect(Unit) {
        connection = readWifiConnection(context)
        if (hasScanPermissions()) loadScanResults()
    }

    Column(modifier.fillMaxSize()) {
        ScreenHeader("Monitor Wi-Fi")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MnaCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Eyebrow("CONEXIÓN ACTUAL")
                        StatusChip(
                            if (connection != null) "CONECTADO" else "SIN CONEXIÓN",
                            if (connection != null) MnaColors.Connected else MnaColors.Disconnected
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = MnaColors.Accent)
                        Text(
                            connection?.ssid ?: "No hay conexión Wi-Fi activa",
                            color = MnaColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (connection != null) {
                        InfoRow("Señal", "${connection!!.rssi} dBm")
                        InfoRow("Frecuencia", "${connection!!.frequency} MHz")
                        InfoRow("Velocidad de enlace", "${connection!!.linkSpeed} Mbps")
                        InfoRow("Estándar", connection!!.standard)
                        InfoRow("Canal", frequencyToChannel(connection!!.frequency).toString())
                    } else {
                        Note("Conecta el dispositivo a una red Wi-Fi para consultar sus métricas.")
                    }
                    Button(
                        onClick = { connection = readWifiConnection(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MnaColors.SurfaceRaised,
                            contentColor = MnaColors.TextPrimary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text("Actualizar conexión")
                    }
                }
            }

            item {
                MnaCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Eyebrow("ESCÁNER DE REDES WI-FI")
                        StatusChip(if (scanning) "ESCANEANDO" else "LISTO", if (scanning) MnaColors.Warning else MnaColors.Connected)
                    }
                    Note("Consulta puntos de acceso cercanos mediante el escaneo estándar de Android. Esto no es captura de tramas 802.11 en modo monitor.")
                    Button(
                        onClick = { requestOrScan() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MnaColors.AccentButton,
                            contentColor = MnaColors.TextPrimary
                        )
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Text("Escanear redes")
                    }
                    Text(status, color = MnaColors.TextSecondary, fontSize = 12.sp)
                }
            }

            if (scanResults.isEmpty()) {
                item {
                    MnaCard {
                        Eyebrow("RESULTADOS")
                        Note("Todavía no hay resultados de escaneo disponibles.")
                    }
                }
            } else {
                item {
                    Eyebrow("REDES DETECTADAS · ${scanResults.size}")
                }
                items(scanResults, key = { it.BSSID }) { result ->
                    MnaCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                result.SSID.ifBlank { "(SSID oculto)" },
                                modifier = Modifier.weight(1f),
                                color = MnaColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "${result.level} dBm",
                                color = signalColor(result.level),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        InfoRow("Canal", frequencyToChannel(result.frequency).toString())
                        InfoRow("Frecuencia", "${result.frequency} MHz")
                        InfoRow("Seguridad", securityLabel(result.capabilities))
                        InfoRow("BSSID", result.BSSID)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private fun isLocationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= 28) manager.isLocationEnabled
    else manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

private fun readWifiConnection(context: Context): WifiConnectionSnapshot? {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivity.activeNetwork ?: return null
    val capabilities = connectivity.getNetworkCapabilities(network) ?: return null
    if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

    val info = if (Build.VERSION.SDK_INT >= 29) {
        capabilities.transportInfo as? WifiInfo
    } else {
        @Suppress("DEPRECATION")
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
    } ?: return null

    val ssid = info.ssid
        ?.takeUnless { it == WifiManager.UNKNOWN_SSID || it == "<unknown ssid>" }
        ?.trim('"')
        ?: "SSID no disponible"

    val standard = if (Build.VERSION.SDK_INT >= 30) {
        when (info.wifiStandard) {
            ScanResult.WIFI_STANDARD_LEGACY -> "Legacy"
            ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
            ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
            ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
            ScanResult.WIFI_STANDARD_11AD -> "WiGig"
            ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7"
            else -> "No disponible"
        }
    } else "No disponible"

    return WifiConnectionSnapshot(
        ssid = ssid,
        rssi = info.rssi,
        frequency = info.frequency,
        linkSpeed = info.linkSpeed,
        standard = standard
    )
}

private fun frequencyToChannel(frequency: Int): Int = when (frequency) {
    2484 -> 14
    in 2412..2472 -> (frequency - 2407) / 5
    in 5000..5895 -> (frequency - 5000) / 5
    in 5955..7115 -> (frequency - 5950) / 5
    else -> 0
}

private fun securityLabel(capabilities: String): String = when {
    capabilities.contains("WPA3", ignoreCase = true) -> "WPA3"
    capabilities.contains("WPA2", ignoreCase = true) -> "WPA2"
    capabilities.contains("WPA", ignoreCase = true) -> "WPA/WPA2"
    capabilities.contains("WEP", ignoreCase = true) -> "WEP"
    else -> "Abierta / no identificada"
}

private fun signalColor(level: Int) = when {
    level >= -55 -> MnaColors.Connected
    level >= -70 -> MnaColors.Warning
    else -> MnaColors.Disconnected
}
