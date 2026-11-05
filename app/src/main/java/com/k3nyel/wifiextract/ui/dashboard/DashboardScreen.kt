package com.k3nyel.wifiextract.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3nyel.wifiextract.NetworkUiState
import com.k3nyel.wifiextract.ui.components.Eyebrow
import com.k3nyel.wifiextract.ui.components.InfoRow
import com.k3nyel.wifiextract.ui.components.MnaCard
import com.k3nyel.wifiextract.ui.components.PrimaryButton
import com.k3nyel.wifiextract.ui.components.ScreenHeader
import com.k3nyel.wifiextract.ui.components.SecondaryButton
import com.k3nyel.wifiextract.ui.components.SkeletonCard
import com.k3nyel.wifiextract.ui.components.StatusChip
import com.k3nyel.wifiextract.ui.theme.MnaColors
import com.k3nyel.wifiextract.ui.theme.MnaShapes
import com.k3nyel.wifiextract.ui.updates.UpdateUiState

@Composable
fun DashboardScreen(
    networkState: NetworkUiState,
    updateState: UpdateUiState,
    deviceCount: Int,
    onOpenDevices: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenUpdates: () -> Unit,
    onRefreshNetwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        ScreenHeader("Panel")
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Update Banner if available
            if (updateState is UpdateUiState.Available) {
                item {
                    MnaCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Eyebrow("ACTUALIZACIÓN DISPONIBLE")
                                Text(updateState.info.tag, color = MnaColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            PrimaryButton("Ver", onOpenUpdates, Modifier.height(40.dp))
                        }
                    }
                }
            }

            // 2. Network Status Card
            item {
                when (networkState) {
                    NetworkUiState.Loading -> SkeletonCard(rows = 3)
                    is NetworkUiState.Error -> MnaCard {
                        Eyebrow("ESTADO DE RED")
                        Text(networkState.message, color = MnaColors.TextPrimary, fontSize = 14.sp)
                        SecondaryButton("Reintentar", onRefreshNetwork, Modifier.fillMaxWidth())
                    }
                    is NetworkUiState.Success -> {
                        val info = networkState.info
                        MnaCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Eyebrow("ESTADO ACTUAL")
                                if (info.connected) StatusChip("CONECTADO", MnaColors.Success)
                                else StatusChip("SIN CONEXIÓN", MnaColors.AccentText)
                            }
                            Text(info.transport, color = MnaColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            InfoRow("IPv4", info.localAddress)
                            InfoRow("Puerta de enlace", info.gateway)
                            InfoRow("DNS", info.dnsServers)
                        }
                    }
                }
            }

            // 3. Quick Actions Grid
            item {
                MnaCard {
                    Eyebrow("ACCESOS DIRECTOS")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardActionButton("Dispositivos", Icons.Default.Devices, Modifier.weight(1f), onOpenDevices)
                        DashboardActionButton("Diagnóstico", Icons.Default.Build, Modifier.weight(1f), onOpenDiagnostics)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardActionButton("Actualizar", Icons.Default.SystemUpdate, Modifier.weight(1f), onOpenUpdates)
                        DashboardActionButton("Refrescar Red", Icons.Default.Refresh, Modifier.weight(1f), onRefreshNetwork)
                    }
                }
            }

            // 4. Summary Stats
            item {
                MnaCard {
                    Eyebrow("RESUMEN DE RED")
                    InfoRow("Dispositivos descubiertos", if (deviceCount > 0) "$deviceCount" else "0 (Ejecutar escaneo)")
                    InfoRow("Interfaz activa", (networkState as? NetworkUiState.Success)?.info?.interfaceName ?: "—")
                }
            }

            // Bottom spacing below navigation bar
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DashboardActionButton(text: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = MnaShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MnaColors.SurfaceRaised,
            contentColor = MnaColors.TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MnaColors.Accent, modifier = Modifier.height(18.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}
