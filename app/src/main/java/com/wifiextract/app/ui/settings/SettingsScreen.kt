package com.wifiextract.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.R
import com.wifiextract.app.data.SettingsRepository
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.InfoRow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.theme.MnaColors
import com.wifiextract.app.ui.theme.MnaShapes

@Composable
fun SettingsScreen(
    state: SettingsState,
    versionLabel: String,
    onCheckUpdates: (Boolean) -> Unit,
    onResolveHostnames: (Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onTimeout: (Int) -> Unit,
    onOpenUpdates: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        ScreenHeader("Application Settings")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MnaCard {
                Eyebrow(stringResource(R.string.settings_updates_title))
                Note(stringResource(R.string.settings_updates_desc))
                PrimaryButton(stringResource(R.string.nav_updates), onOpenUpdates, Modifier.fillMaxWidth())
            }
            ToggleRow("Comprobar actualizaciones al iniciar", state.checkUpdatesOnStart, onCheckUpdates)
            ToggleRow("Resolver nombres de host", state.resolveHostnames, onResolveHostnames)
            ToggleRow("Mantener pantalla activa al escanear", state.keepScreenOn, onKeepScreenOn)
            MnaCard {
                Eyebrow("TIEMPO DE ESPERA POR COMPROBACIÓN")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsRepository.TIMEOUT_OPTIONS.forEach { ms ->
                        val label = "$ms ms"
                        if (ms == state.timeoutMs) PrimaryButton(label, { onTimeout(ms) }, Modifier.weight(1f))
                        else SecondaryButton(label, { onTimeout(ms) }, Modifier.weight(1f))
                    }
                }
                InfoRow("Rango de escaneo", "Auto /24")
            }
            MnaCard {
                Eyebrow("PRIVACIDAD")
                Note("Los datos de conexión se leen localmente y no se envían a servidores del proyecto. La comprobación de actualizaciones contacta con GitHub (api.github.com), que puede registrar tu dirección IP.")
            }
            MnaCard {
                Eyebrow("PERMISOS")
                Note("Estado de red, estado Wi-Fi e Internet. «Instalar apps desconocidas» solo se solicita al instalar una actualización verificada. No se piden ubicación ni contactos.")
            }
            MnaCard {
                Eyebrow("ALCANCE Y LÍMITES")
                listOf(
                    "Solo redes locales a las que el dispositivo ya está conectado",
                    "Sin captura de paquetes 802.11",
                    "MAC y fabricante limitados desde Android 10+",
                    "Sin acceso a dispositivos ni credenciales de terceros",
                    "Escaneos activos solo con acción explícita del usuario",
                    "Funciones no terminadas se marcan «Próximamente»"
                ).forEach { Text("·  $it", color = MnaColors.TextSecondary, fontSize = 13.sp, lineHeight = 19.sp) }
            }
            MnaCard {
                Eyebrow("LICENCIAS")
                Note("El inventario de licencias de terceros estará disponible antes de la primera distribución estable.")
                InfoRow("Versión", versionLabel)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .background(MnaColors.Surface, MnaShapes.Card)
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f).padding(end = 12.dp), color = MnaColors.TextPrimary, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MnaColors.AccentButton,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = MnaColors.TextSecondary,
                uncheckedTrackColor = MnaColors.SurfaceRaised,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
