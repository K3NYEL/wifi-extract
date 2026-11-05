package com.k3nyel.wifiextract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Canvas = Color(0xFF090A0C)
private val Panel = Color(0xFF141619)
private val Line = Color(0xFF292D32)
private val Red = Color(0xFFE53935)
private val Secondary = Color(0xFF92979F)

class MainActivity : ComponentActivity() {
    private val networkInfo by lazy { NetworkInfoRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = darkColorScheme(
                primary = Red,
                background = Canvas,
                surface = Panel,
                onBackground = Color(0xFFF1F2F3),
                onSurface = Color(0xFFF1F2F3)
            )
            MaterialTheme(colorScheme = colors) {
                var state by remember { mutableStateOf(networkInfo.read()) }
                Scaffold(
                    containerColor = Canvas,
                    bottomBar = {
                        NavigationBar(containerColor = Panel, contentColor = Color.White) {
                            NavigationBarItem(
                                selected = true, onClick = {},
                                icon = { Icon(Icons.Default.Router, contentDescription = null) },
                                label = { Text("Overview") }
                            )
                            NavigationBarItem(
                                selected = false, onClick = {},
                                icon = { Icon(Icons.Default.Router, contentDescription = null) },
                                label = { Text("Devices") }
                            )
                            NavigationBarItem(
                                selected = false, onClick = {},
                                icon = { Icon(Icons.Default.Router, contentDescription = null) },
                                label = { Text("Diagnostics") }
                            )
                        }
                    }
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("MNA / NETWORK TOOLS", color = Red, style = MaterialTheme.typography.labelSmall)
                                    Text("Overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Local connection diagnostics", color = Secondary, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { state = networkInfo.read() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Red)
                                }
                            }
                        }
                        item { StatusPanel(state) }
                        item { SectionLabel("CONNECTION DETAILS") }
                        item { DetailPanel("Transport", state.transport) }
                        item { DetailPanel("Local IPv4", state.localAddress) }
                        item { DetailPanel("Default gateway", state.gateway) }
                        item { DetailPanel("DNS servers", state.dnsServers) }
                        item { DetailPanel("Interface", state.interfaceName) }
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .background(Panel, RoundedCornerShape(4.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("ANALYSIS MODULES", color = Secondary, style = MaterialTheme.typography.labelSmall)
                                Text("Device discovery", fontWeight = FontWeight.Medium)
                                Text("Service discovery and authorized TCP diagnostics will be added in the next feature increments.", color = Secondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        item { Text("VERSION 0.1.0  /  ANDROID NATIVE", color = Secondary, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(value: String) {
    Text(value, color = Secondary, style = MaterialTheme.typography.labelSmall)
}

@Composable
private fun StatusPanel(info: NetworkInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(4.dp)).padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            SectionLabel("NETWORK STATUS")
            Text(
                if (info.connected) "Connected" else "Disconnected",
                color = if (info.connected) Color(0xFF69C58A) else Red,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(info.networkType, color = Secondary, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.Router, contentDescription = null, tint = Red, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun DetailPanel(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(4.dp)).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label.uppercase(), color = Secondary, style = MaterialTheme.typography.labelSmall)
        Text(value.ifBlank { "Not available" }, style = MaterialTheme.typography.bodyMedium)
    }
}
