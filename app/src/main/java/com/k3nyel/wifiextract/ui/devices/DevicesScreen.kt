package com.k3nyel.wifiextract.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3nyel.wifiextract.data.Device
import com.k3nyel.wifiextract.ui.components.MnaCard
import com.k3nyel.wifiextract.ui.components.Eyebrow
import com.k3nyel.wifiextract.ui.components.Note
import com.k3nyel.wifiextract.ui.components.PrimaryButton
import com.k3nyel.wifiextract.ui.components.ScreenHeader
import com.k3nyel.wifiextract.ui.components.SecondaryButton
import com.k3nyel.wifiextract.ui.components.SkeletonListRow
import com.k3nyel.wifiextract.ui.theme.MnaColors

@Composable
fun DevicesScreen(
    state: DevicesState,
    onScan: () -> Unit,
    onStop: () -> Unit,
    onPing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val keepOn = state.scanning && state.keepScreenOn
    DisposableEffect(keepOn) {
        view.keepScreenOn = keepOn
        onDispose { view.keepScreenOn = false }
    }

    Column(modifier.fillMaxSize()) {
        ScreenHeader("Device Discovery")
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton("Escanear red", onScan, Modifier.weight(1f), enabled = !state.scanning)
                    SecondaryButton("Detener", onStop, Modifier.weight(1f), enabled = state.scanning)
                }
            }
            if (state.hasScanned && state.total > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Eyebrow(state.subnetLabel)
                            Text(
                                "${state.devices.size} encontrados · ${state.scanned}/${state.total}",
                                color = MnaColors.TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (state.scanning) {
                            LinearProgressIndicator(
                                progress = { state.scanned.toFloat() / state.total },
                                modifier = Modifier.fillMaxWidth(),
                                color = MnaColors.Accent,
                                trackColor = MnaColors.SurfaceRaised
                            )
                        }
                    }
                }
            }
            if (state.ownIp.isNotBlank()) {
                item { DeviceRow(state.ownIp, "Este dispositivo", null) }
            }
            items(state.devices, key = { it.ip }) { device ->
                DeviceRow(device.ip, detail(device, state.gateway), { onPing(device.ip) })
            }
            if (state.scanning && state.devices.isEmpty()) {
                items(3) { SkeletonListRow() }
            }
            if (!state.hasScanned) {
                item {
                    MnaCard {
                        Eyebrow("SIN RESULTADOS")
                        Note("Todavía no hay resultados. Pulsa «Escanear red» para buscar dispositivos en tu red local.")
                    }
                }
            }
            state.message?.let { message -> item { MnaCard { Note(message) } } }
            item {
                Note("Por restricciones de Android 10+, la dirección MAC y el fabricante pueden no estar disponibles. Un dispositivo que no responde no implica que no exista. Usa el escaneo solo en redes propias o con permiso.")
            }
        }
    }
}

private fun detail(device: Device, gateway: String): String = listOfNotNull(
    device.hostName,
    if (device.ip == gateway) "Puerta de enlace" else null,
    "${device.latencyMs} ms"
).joinToString(" · ")

@Composable
private fun DeviceRow(ip: String, detail: String, onPing: (() -> Unit)?) {
    MnaCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(ip, color = MnaColors.TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                Note(detail)
            }
            if (onPing != null) SecondaryButton("Ping", onPing, Modifier.width(76.dp))
        }
    }
}
