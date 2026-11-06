package com.wifiextract.app.ui.monitor

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private data class MonitorCapability(
    val hasWifi: Boolean,
    val androidVersion: String
)

private fun readMonitorCapability(context: Context): MonitorCapability {
    val hasWifi = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)
    return MonitorCapability(
        hasWifi = hasWifi,
        androidVersion = Build.VERSION.RELEASE ?: Build.VERSION.SDK_INT.toString()
    )
}

@Composable
fun MonitorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val capability = readMonitorCapability(context)

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
                    StatusChip("INACTIVO", MnaColors.Warning)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = MnaColors.Accent)
                    Text(
                        "Monitor de tramas Wi-Fi",
                        color = MnaColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "La captura todavía no está disponible en este dispositivo. Android no ofrece una API pública estándar para activar el modo monitor 802.11 ni entregar tramas sin procesar a una aplicación normal.",
                    color = MnaColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            MnaCard {
                Eyebrow("COMPATIBILIDAD DEL DISPOSITIVO")
                InfoLine("Wi-Fi", if (capability.hasWifi) "Hardware Wi-Fi detectado" else "No se detectó hardware Wi-Fi")
                InfoLine("Android", capability.androidVersion)
                InfoLine("Acceso a tramas 802.11", "No disponible mediante API estándar")
                InfoLine("Estado de captura", "No iniciada")
            }

            MnaCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MnaColors.Accent)
                    Text("Qué falta para habilitarlo", color = MnaColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "La compatibilidad depende del chipset Wi-Fi, del controlador del fabricante y de la posibilidad de usar una interfaz de captura compatible. Tener Android 14 o permisos de ubicación no garantiza el modo monitor.",
                    color = MnaColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Text(
                    "La aplicación no intentará activar funciones privilegiadas ni mostrará datos de captura simulados. La siguiente etapa será comprobar las capacidades reales del hardware y definir un proveedor de captura compatible.",
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MnaColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = MnaColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
