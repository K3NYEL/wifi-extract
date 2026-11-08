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
import android.net.NetworkRequest
import android.net.Network
import android.net.MacAddress
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    var rangingResults by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pendingRanging by remember { mutableStateOf<ScanResult?>(null) }
    var networkSsid by remember { mutableStateOf("") }
    var networkBssid by remember { mutableStateOf("") }
    var networkPassword by remember { mutableStateOf("") }
    var networkStatus by remember { mutableStateOf("Solicita una conexión a una red cercana mediante el diálogo del sistema.") }
    var networkRequestPending by remember { mutableStateOf(false) }
    var activeNetworkCallback by remember { mutableStateOf<ConnectivityManager.NetworkCallback?>(null) }
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val rttManager = remember {
        context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as? WifiRttManager
    }

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
        if (granted && !isLocationEnabled(context)) {
            status = "Activa los servicios de ubicación del dispositivo para permitir el escaneo Wi-Fi."
            scanning = false
        } else if (granted) {
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
            status = "Se necesita el permiso de ubicación precisa para leer los resultados del escaneo Wi-Fi."
            scanning = false
        }
    }


    fun performRanging(result: ScanResult) {
        if (Build.VERSION.SDK_INT < 28 ||
            !context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT) ||
            rttManager == null
        ) {
            rangingResults = rangingResults + (result.BSSID to "Este dispositivo no admite Wi-Fi RTT.")
            return
        }
        if (!isLocationEnabled(context)) {
            rangingResults = rangingResults + (result.BSSID to "Activa los servicios de ubicación para medir distancia.")
            return
        }
        if (!rttManager.isAvailable) {
            rangingResults = rangingResults + (result.BSSID to "Wi-Fi RTT no está disponible en este momento.")
            return
        }
        rangingResults = rangingResults + (result.BSSID to "Midiendo…")
        runCatching {
            val request = RangingRequest.Builder()
                .addAccessPoint(result)
                .build()
            val callback = object : RangingResultCallback() {
                override fun onRangingFailure(code: Int) {
                    rangingResults = rangingResults + (result.BSSID to "La medición falló (código $code).")
                }

                override fun onRangingResults(results: List<RangingResult>) {
                    val measurement = results.firstOrNull {
                        it.status == RangingResult.STATUS_SUCCESS
                    }
                    val label = if (measurement != null) {
                        String.format(java.util.Locale.getDefault(), "%.2f m", measurement.distanceMm / 1000.0)
                    } else {
                        "El punto de acceso no respondió a RTT."
                    }
                    rangingResults = rangingResults + (result.BSSID to label)
                }
            }
            rttManager.startRanging(request, callback, Handler(Looper.getMainLooper()))
        }.onFailure {
            rangingResults = rangingResults + (result.BSSID to "No se pudo iniciar RTT: ${it.message ?: "error del sistema"}.")
        }
    }

    val rttPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allowed = if (Build.VERSION.SDK_INT >= 33) {
            grants[Manifest.permission.NEARBY_WIFI_DEVICES] == true
        } else {
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
        val target = pendingRanging
        pendingRanging = null
        if (allowed && target != null) performRanging(target)
        else if (target != null) {
            rangingResults = rangingResults + (target.BSSID to "Permiso cercano denegado; no se inició RTT.")
        }
    }

    val networkPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) networkRequestPending = true
        else networkStatus = "Permiso de dispositivos Wi-Fi cercanos denegado."
    }

    fun requestSpecificNetwork() {
        if (Build.VERSION.SDK_INT < 29) {
            networkStatus = "WifiNetworkSpecifier requiere Android 10 (API 29) o superior."
            return
        }
        val ssid = networkSsid.trim()
        if (ssid.isEmpty()) {
            networkStatus = "Escribe el SSID de la red."
            return
        }
        if (activeNetworkCallback != null) {
            networkStatus = "Ya existe una solicitud de conexión activa. Cancélala antes de iniciar otra."
            return
        }
        runCatching {
            val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
            if (networkBssid.isNotBlank()) {
                specifierBuilder.setBssid(MacAddress.fromString(networkBssid.trim()))
            }
            if (networkPassword.isNotBlank()) {
                specifierBuilder.setWpa2Passphrase(networkPassword)
            }
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifierBuilder.build())
                .build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    networkStatus = "Conexión local disponible para WiFi Extract. Android mantiene el control de la conexión."
                }
                override fun onUnavailable() {
                    networkStatus = "La solicitud no se completó o fue cancelada."
                    activeNetworkCallback = null
                }
                override fun onLost(network: Network) {
                    networkStatus = "La conexión solicitada se perdió."
                    activeNetworkCallback = null
                }
            }
            connectivityManager.requestNetwork(request, callback)
            activeNetworkCallback = callback
            networkStatus = "Solicitud enviada. Confirma la conexión en el diálogo de Android."
        }.onFailure {
            networkStatus = "No se pudo solicitar la conexión: ${it.message ?: "error del sistema"}"
        }
    }

    fun requestRanging(result: ScanResult) {
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = Manifest.permission.NEARBY_WIFI_DEVICES
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                pendingRanging = result
                rttPermissionLauncher.launch(arrayOf(permission))
                return
            }
        } else if (!hasScanPermissions()) {
            pendingRanging = result
            rttPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            return
        }
        performRanging(result)
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

    LaunchedEffect(networkRequestPending) {
        if (networkRequestPending) {
            networkRequestPending = false
            requestSpecificNetwork()
        }
    }

    DisposableEffect(connectivityManager) {
        onDispose {
            activeNetworkCallback?.let { callback ->
                runCatching { connectivityManager.unregisterNetworkCallback(callback) }
            }
        }
    }

    LaunchedEffect(Unit) {
        connection = readWifiConnection(context)
        if (hasScanPermissions()) loadScanResults()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader("Monitor Wi-Fi")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
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
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Text("Escanear redes")
                    }
                    Text(status, color = MnaColors.TextSecondary, fontSize = 12.sp)
                }
            }

            item {
                MnaCard {
                    Eyebrow("CONEXIÓN A RED ESPECÍFICA")
                    Note("Solicita conexión mediante la API oficial de Android. El sistema mostrará su propio diálogo de confirmación.")
                    OutlinedTextField(
                        value = networkSsid,
                        onValueChange = { networkSsid = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("SSID") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = networkBssid,
                        onValueChange = { networkBssid = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("BSSID (opcional)") },
                        singleLine = true,
                        placeholder = { Text("AA:BB:CC:DD:EE:FF") }
                    )
                    OutlinedTextField(
                        value = networkPassword,
                        onValueChange = { networkPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña WPA2 (opcional)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(
                        onClick = {
                            when {
                                Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED ->
                                    networkPermissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
                                Build.VERSION.SDK_INT in 29..32 &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ->
                                    networkPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                else -> networkRequestPending = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MnaColors.AccentButton, contentColor = Color.White)
                    ) {
                        Text("Solicitar conexión")
                    }
                    if (activeNetworkCallback != null) {
                        OutlinedButton(
                            onClick = {
                                activeNetworkCallback?.let { callback ->
                                    runCatching { connectivityManager.unregisterNetworkCallback(callback) }
                                }
                                activeNetworkCallback = null
                                networkStatus = "Solicitud de conexión cancelada."
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cancelar solicitud") }
                    }
                    Text(networkStatus, color = MnaColors.TextSecondary, fontSize = 12.sp)
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
                                getScanResultSsid(result),
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
                        if (Build.VERSION.SDK_INT >= 28 && result.is80211mcResponder &&
                            context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
                        ) {
                            OutlinedButton(
                                onClick = { requestRanging(result) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Medir distancia (Wi-Fi RTT)")
                            }
                            rangingResults[result.BSSID]?.let { measurement ->
                                InfoRow("Distancia RTT", measurement)
                            }
                        } else if (Build.VERSION.SDK_INT >= 28) {
                            InfoRow("Wi-Fi RTT", "Punto de acceso sin soporte FTM o dispositivo no compatible")
                        }
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
        ?.takeUnless { it == "<unknown ssid>" }
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

private fun getScanResultSsid(result: ScanResult): String {
    val ssid = if (Build.VERSION.SDK_INT >= 33) {
        result.wifiSsid?.toString()?.removeSurrounding("\"")
    } else {
        @Suppress("DEPRECATION")
        result.SSID?.removeSurrounding("\"")
    }
    return ssid?.takeIf { it.isNotBlank() && it != "<unknown ssid>" } ?: "<SSID desconocido>"
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

@Composable
private fun signalColor(level: Int) = when {
    level >= -55 -> MnaColors.Connected
    level >= -70 -> MnaColors.Warning
    else -> MnaColors.Disconnected
}
