package com.wifiextract.app.ui.monitor

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors

@Composable
fun MonitorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val backend = remember { AndroidPublicApiMonitorBackend() }
    var capability by remember { mutableStateOf(backend.inspect(context)) }

    Column(modifier.fillMaxSize()) {
        ScreenHeader("Modo monitor")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MnaCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Eyebrow("CAPTURA 802.11")
                    StatusChip("NO DISPONIBLE", MnaColors.Warning)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = MnaColors.Accent)
                    Text(
                        "Monitor de tramas Wi-Fi",
                        color = MnaColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    capability.explanation,
                    color = MnaColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Button(
                    onClick = { capability = backend.inspect(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MnaColors.Accent,
                        contentColor = MnaColors.AccentText
                    )
                ) {
                    Text("Volver a comprobar")
                }
            }

            MnaCard {
                Eyebrow("DIAGNÓSTICO DEL BACKEND")
                InfoLine(
                    "Hardware Wi-Fi",
                    if (capability.wifiHardwareDetected) "Detectado" else "No detectado"
                )
                InfoLine("Versión de Android", capability.androidVersion)
                InfoLine("Proveedor consultado", capability.providerName)
                InfoLine(
                    "Acceso a tramas 802.11",
                    if (capability.rawFrameCaptureAvailable) "Disponible" else "No disponible"
                )
                InfoLine("Captura", "Detenida")
            }

            MnaCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MnaColors.Accent)
                    Text(
                        "Qué significa este resultado",
                        color = MnaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Detectar un adaptador Wi-Fi no demuestra que admita monitor mode. Para habilitar la captura real hace falta un proveedor compatible con el chipset y su controlador; Android estándar no lo proporciona.",
                    color = MnaColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Text(
                    "Esta versión no muestra paquetes de ejemplo ni intenta iniciar una captura inexistente. El backend está separado de la interfaz para poder incorporar un proveedor compatible cuando se confirme que el dispositivo lo admite.",
                    color = MnaColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MnaColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            color = MnaColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
