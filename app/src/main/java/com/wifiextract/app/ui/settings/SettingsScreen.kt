package com.wifiextract.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.R
import com.wifiextract.app.data.SettingsRepository
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.GithubProfileCard
import com.wifiextract.app.ui.components.GithubContributors
import com.wifiextract.app.ui.components.InfoRow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SectionLabel
import com.wifiextract.app.ui.components.SegmentedControl
import com.wifiextract.app.ui.components.SettingsRow
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors
import com.wifiextract.app.ui.theme.MnaShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: SettingsState,
    versionLabel: String,
    onCheckUpdates: (Boolean) -> Unit,
    onResolveHostnames: (Boolean) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onTimeout: (Int) -> Unit,
    onTheme: (String) -> Unit,
    onLanguage: (String) -> Unit,
    onOpenUpdates: () -> Unit,
    modifier: Modifier = Modifier,
    updateAvailable: Boolean = false
) {
    var changingLanguage by remember { mutableStateOf(false) }
    val languageScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.nav_settings))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GithubProfileCard(
                username = "K3NYEL",
                modifier = Modifier.fillMaxWidth()
            )

            MnaCardRow {
                SettingsRow(
                    title = stringResource(R.string.settings_updates_title),
                    subtitle = stringResource(R.string.settings_updates_desc),
                    onClick = onOpenUpdates,
                    trailing = { if (updateAvailable) StatusChip(stringResource(R.string.chip_new), MnaColors.AccentText) }
                )
            }

            SectionLabel(stringResource(R.string.section_appearance))
            SegmentedControl(
                options = listOf(stringResource(R.string.theme_system), stringResource(R.string.theme_dark), stringResource(R.string.theme_light)),
                selected = when (state.theme) {
                    "system" -> 0
                    "dark" -> 1
                    else -> 2
                },
                onSelect = {
                    val themeStr = when (it) {
                        0 -> "system"
                        1 -> "dark"
                        else -> "light"
                    }
                    onTheme(themeStr)
                }
            )

            SectionLabel(stringResource(R.string.settings_language_title))
            SegmentedControl(
                options = listOf(stringResource(R.string.lang_system), stringResource(R.string.lang_spanish), stringResource(R.string.lang_english)),
                selected = when (state.language) {
                    "system" -> 0
                    "es" -> 1
                    else -> 2
                },
                onSelect = {
                    val langStr = when (it) {
                        0 -> "system"
                        1 -> "es"
                        else -> "en"
                    }
                    if (langStr != state.language && !changingLanguage) {
                        languageScope.launch {
                            changingLanguage = true
                            delay(250)
                            onLanguage(langStr)
                            delay(450)
                            changingLanguage = false
                        }
                    }
                }
            )

            SectionLabel(stringResource(R.string.section_scanning))
            ToggleRow(stringResource(R.string.setting_check_updates_start), state.checkUpdatesOnStart, onCheckUpdates)
            ToggleRow(stringResource(R.string.setting_resolve_hostnames), state.resolveHostnames, onResolveHostnames)
            ToggleRow(stringResource(R.string.setting_keep_screen_on), state.keepScreenOn, onKeepScreenOn)
            MnaCard {
                Eyebrow(stringResource(R.string.eyebrow_timeout_check))
                val options = SettingsRepository.TIMEOUT_OPTIONS
                SegmentedControl(
                    options = options.map { "$it ms" },
                    selected = options.indexOf(state.timeoutMs).coerceAtLeast(0),
                    onSelect = { onTimeout(options[it]) }
                )
                InfoRow(stringResource(R.string.label_scan_range), "Auto /24")
            }

            SectionLabel(stringResource(R.string.section_about))
            MnaCardRow {
                AboutRow(stringResource(R.string.about_contributors)) {
                    GithubContributors(
                        owner = "K3NYEL",
                        repository = "wifi-extract",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AboutRow(stringResource(R.string.about_privacy)) {
                    Note("Los datos de conexión se leen localmente y no se envían a servidores del proyecto. La comprobación de actualizaciones contacta con GitHub (api.github.com), que puede registrar tu dirección IP.")
                    Note("Estado de red, estado Wi-Fi e Internet. «Instalar apps desconocidas» solo se solicita al instalar una actualización verificada. No se piden ubicación ni contactos.")
                }
                AboutRow(stringResource(R.string.about_scope)) {
                    listOf(
                        "Solo redes locales a las que el dispositivo ya está conectado",
                        "Sin captura de paquetes ni modo monitor (requiere root)",
                        "MAC y fabricante limitados desde Android 10+",
                        "Sin acceso a dispositivos ni credenciales de terceros",
                        "Escaneos activos solo con acción explícita del usuario",
                        "Funciones no terminadas se marcan «Próximamente»"
                    ).forEach { Text("·  $it", color = MnaColors.TextSecondary, fontSize = 13.sp, lineHeight = 19.sp) }
                }
                AboutRow(stringResource(R.string.about_licenses)) {
                    Note("Mobile Network Analyzer (MNA) incluye software de código abierto bajo licencias Apache 2.0 y MIT:")
                    listOf(
                        "Android Jetpack & Compose (Apache 2.0) — Google LLC",
                        "Kotlin Coroutines & Standard Library (Apache 2.0) — JetBrains",
                        "Material Components for Android (Apache 2.0) — Google LLC"
                    ).forEach { Text("·  $it", color = MnaColors.TextSecondary, fontSize = 13.sp, lineHeight = 19.sp) }
                }
            }
            InfoRow(stringResource(R.string.lbl_version), versionLabel)
            Spacer(Modifier.height(16.dp))
        }
      }
      if (changingLanguage) {
          Box(
              modifier = Modifier.fillMaxSize().background(MnaColors.Background),
              contentAlignment = Alignment.Center
          ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                  CircularProgressIndicator(color = MnaColors.Accent)
                  Text(stringResource(R.string.language_changing), color = MnaColors.TextPrimary, fontSize = 15.sp)
              }
          }
      }
    }
}

@Composable
private fun MnaCardRow(content: @Composable () -> Unit) {
    Column(Modifier
        .fillMaxWidth()
        .background(MnaColors.Surface, MnaShapes.Card)) { content() }
}

@Composable
private fun AboutRow(title: String, content: @Composable () -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    Column {
        SettingsRow(
            title = title,
            onClick = { open = !open },
            chevron = if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore
        )
        if (open) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .background(MnaColors.Surface, MnaShapes.Card)
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier
            .weight(1f)
            .padding(end = 12.dp), color = MnaColors.TextPrimary, fontSize = 14.sp)
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
