package com.k3nyel.wifiextract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider

private val Background = Color(0xFF0A0A0B)
private val SurfaceBase = Color(0xFF17171A)
private val SurfaceRaised = Color(0xFF202024)
private val Border = Color(0xFF2C2C31)
private val Accent = Color(0xFFE5322D)
private val TextPrimary = Color(0xFFF2F2F3)
private val TextSecondary = Color(0xFF9A9AA2)
private val Success = Color(0xFF67C587)
private val Warning = Color(0xFFE5B45C)

private enum class Destination { Overview, Devices, Diagnostics, Updates, Settings }

class MainActivity : ComponentActivity() {
    private lateinit var networkViewModel: NetworkOverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkViewModel = ViewModelProvider(this)[NetworkOverviewViewModel::class.java]
        setContent {
            val scheme = darkColorScheme(
                primary = Accent,
                onPrimary = Color.White,
                background = Background,
                onBackground = TextPrimary,
                surface = SurfaceBase,
                onSurface = TextPrimary,
                surfaceVariant = SurfaceRaised,
                outline = Border
            )
            MaterialTheme(colorScheme = scheme) {
                MnaApp(
                    state = networkViewModel.state.collectAsState().value,
                    onRefresh = networkViewModel::refresh
                )
            }
        }
    }
}

@Composable
private fun MnaApp(state: NetworkUiState, onRefresh: () -> Unit) {
    var selected by remember { mutableStateOf(Destination.Overview) }
    val destinations = listOf(
        Destination.Overview to (R.string.nav_overview to Icons.Default.Home),
        Destination.Devices to (R.string.nav_devices to Icons.Default.Devices),
        Destination.Diagnostics to (R.string.nav_diagnostics to Icons.Default.Build),
        Destination.Updates to (R.string.nav_updates to Icons.Default.SystemUpdate),
        Destination.Settings to (R.string.nav_settings to Icons.Default.Settings)
    )

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceBase,
                contentColor = TextPrimary,
                tonalElevation = 0.dp
            ) {
                destinations.forEach { (destination, item) ->
                    NavigationBarItem(
                        selected = selected == destination,
                        onClick = { selected = destination },
                        icon = {
                            Icon(
                                imageVector = item.second,
                                contentDescription = stringResource(item.first),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(stringResource(item.first), fontSize = 11.sp) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        when (selected) {
            Destination.Overview -> OverviewScreen(state, onRefresh, Modifier.padding(padding))
            Destination.Devices -> ComingSoonScreen(
                title = "Dispositivos",
                subtitle = "Descubrimiento de equipos de tu red local.",
                detail = "El módulo se está preparando. Cuando esté disponible, mostrará resultados reales obtenidos desde el dispositivo y permitirá cancelar el proceso.",
                modifier = Modifier.padding(padding)
            )
            Destination.Diagnostics -> ComingSoonScreen(
                title = "Diagnóstico",
                subtitle = "Comprobaciones de conectividad y servicios.",
                detail = "Las pruebas estarán limitadas en duración e intensidad. No se ejecutarán ataques ni operaciones intrusivas.",
                modifier = Modifier.padding(padding)
            )
            Destination.Updates -> ComingSoonScreen(
                title = "Actualizaciones",
                subtitle = "Versiones oficiales de Mobile Network Analyzer.",
                detail = "El actualizador estará disponible cuando la verificación de hash SHA-256 y firma del APK esté integrada. No se instalarán paquetes automáticamente.",
                modifier = Modifier.padding(padding)
            )
            Destination.Settings -> SettingsScreen(Modifier.padding(padding))
        }
    }
}

@Composable
private fun OverviewScreen(state: NetworkUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Eyebrow("MNA / NETWORK ANALYZER")
                    Text("Resumen", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Estado de tu conexión local", fontSize = 13.sp, color = TextSecondary)
                }
                IconButton(onClick = onRefresh, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar información de red", tint = Accent)
                }
            }
        }
        item {
            when (state) {
                NetworkUiState.Loading -> StatePanel("LEYENDO CONEXIÓN", "Consultando el estado de red…", Accent) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
                is NetworkUiState.Error -> StatePanel("ERROR DE LECTURA", state.message, Accent) {
                    TextButton(onClick = onRefresh) { Text("Reintentar", color = Accent) }
                }
                is NetworkUiState.Success -> {
                    val info = state.info
                    StatePanel(
                        "ESTADO DE RED",
                        if (info.connected) "Conectado" else "Sin conexión",
                        if (info.connected) Success else Accent
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Estado de conexión", tint = if (info.connected) Success else Accent, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(info.networkType, color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
        item { Eyebrow("DETALLES DE CONEXIÓN") }
        if (state is NetworkUiState.Success) {
            item { TechnicalValue("TRANSPORTE", state.info.transport) }
            item { TechnicalValue("IP LOCAL", state.info.localAddress) }
            item { TechnicalValue("PUERTA DE ENLACE", state.info.gateway) }
            item { TechnicalValue("DNS", state.info.dnsServers) }
            item { TechnicalValue("INTERFAZ", state.info.interfaceName) }
        } else {
            item { TechnicalValue("DATOS DE RED", if (state is NetworkUiState.Loading) "Cargando…" else "No disponibles") }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(SurfaceRaised, RoundedCornerShape(3.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Eyebrow("USO RESPONSABLE")
                Text(
                    stringResource(R.string.authorized_network_notice),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        item { Text("MNA  /  ANDROID  /  0.1.0", color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp) }
    }
}

@Composable
private fun StatePanel(label: String, message: String, stateColor: Color, trailing: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(3.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Eyebrow(label)
                Text(message, color = stateColor, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp)
            }
            trailing()
        }
    }
}

@Composable
private fun TechnicalValue(label: String, value: String) {
    val clipboard = LocalClipboardManager.current
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(3.dp))
            .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Eyebrow(label)
            Text(
                value.ifBlank { stringResource(R.string.not_available) },
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
        TextButton(onClick = { clipboard.setText(AnnotatedString(value)) }) {
            Text("Copiar", color = Accent, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ComingSoonScreen(title: String, subtitle: String, detail: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Eyebrow("MNA / MÓDULO")
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(subtitle, fontSize = 14.sp, color = TextSecondary)
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(SurfaceBase, RoundedCornerShape(3.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("PRÓXIMAMENTE", color = Warning, fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Eyebrow("MNA / PREFERENCIAS")
        Text("Ajustes", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        SettingsPanel("PRIVACIDAD", "Los datos básicos de conexión se leen localmente para mostrar el estado de la red. MNA no los envía a servidores propios.")
        SettingsPanel("PERMISOS", "La versión actual utiliza permisos de estado de red, estado Wi-Fi y acceso a Internet. No solicita ubicación ni contactos.")
        SettingsPanel("LICENCIAS", "El inventario de licencias de terceros estará disponible antes de la primera distribución estable.")
        SettingsPanel("ALCANCE Y LÍMITES", "MNA está diseñada para redes propias o con autorización. No incluye funciones de ataque, suplantación, fuerza bruta ni recuperación de contraseñas.")
    }
}

@Composable
private fun SettingsPanel(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(SurfaceBase, RoundedCornerShape(3.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Eyebrow(title)
        Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun Eyebrow(value: String) {
    Text(
        value,
        color = TextSecondary,
        fontSize = 11.sp,
        letterSpacing = 1.1.sp,
        fontWeight = FontWeight.Medium
    )
}
