package com.wifiextract.app.ui.updates

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.data.InstalledVersion
import com.wifiextract.app.data.ReleaseInfo
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.InfoRow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SkeletonBox
import com.wifiextract.app.ui.components.SkeletonLines
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors

@Composable
fun UpdatesScreen(
    state: UpdateUiState,
    installed: InstalledVersion,
    onCheck: () -> Unit,
    onDownload: (ReleaseInfo) -> Unit,
    onCancel: () -> Unit,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier.fillMaxSize()) {
        ScreenHeader("Update Manager")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MnaCard {
                InfoRow("Versión instalada", "${installed.name} (${installed.code})")
                when (state) {
                    UpdateUiState.Idle -> InfoRow("Última versión", "—")
                    UpdateUiState.Checking -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Última versión", color = MnaColors.TextSecondary, fontSize = 13.sp)
                        SkeletonBox(Modifier.padding(top = 2.dp).fillMaxWidth(0.3f))
                    }
                    is UpdateUiState.UpToDate -> InfoRow("Última versión", state.latest?.tag ?: "—")
                    is UpdateUiState.Available -> InfoRow("Última versión", state.info.tag)
                    is UpdateUiState.Downloading -> InfoRow("Última versión", state.info.tag)
                    is UpdateUiState.Ready -> InfoRow("Última versión", state.info.tag)
                    is UpdateUiState.Failed -> InfoRow("Última versión", "—")
                }
                Note("Fuente: GitHub Releases oficiales del proyecto.")
            }

            when (state) {
                UpdateUiState.Idle -> {
                    Note("Pulsa «Comprobar» para consultar si hay una versión nueva.")
                    SecondaryButton("Comprobar", onCheck, Modifier.fillMaxWidth())
                }
                UpdateUiState.Checking -> {
                    MnaCard {
                        Eyebrow("NOVEDADES")
                        SkeletonLines(lines = 4)
                    }
                    SecondaryButton("Comprobando…", {}, Modifier.fillMaxWidth(), enabled = false)
                }
                is UpdateUiState.UpToDate -> {
                    MnaCard {
                        StatusChip("ACTUALIZADA", MnaColors.Success)
                        Note("Ya tienes la versión más reciente publicada.")
                    }
                    SecondaryButton("Comprobar de nuevo", onCheck, Modifier.fillMaxWidth())
                    state.latest?.pageUrl?.let { url ->
                        SecondaryButton("Ver en GitHub", {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }, Modifier.fillMaxWidth())
                    }
                }
                is UpdateUiState.Available -> {
                    ReleaseNotes(state.info)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryButton("Descargar", { onDownload(state.info) }, Modifier.weight(1f))
                        SecondaryButton("Comprobar", onCheck, Modifier.weight(1f))
                    }
                    SecondaryButton("Ver en GitHub", {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.info.pageUrl)))
                    }, Modifier.fillMaxWidth())
                    Note("Se verificarán el SHA-256 y la firma del APK antes de ofrecer la instalación.")
                }
                is UpdateUiState.Downloading -> {
                    MnaCard {
                        Eyebrow(if (state.percent == 100) "VERIFICANDO INTEGRIDAD" else "DESCARGANDO ${state.info.tag}")
                        if (state.percent != null) {
                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MnaColors.Accent,
                                trackColor = MnaColors.SurfaceRaised
                            )
                            Note("${state.percent} %")
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MnaColors.Accent,
                                trackColor = MnaColors.SurfaceRaised
                            )
                        }
                    }
                    SecondaryButton("Cancelar", onCancel, Modifier.fillMaxWidth())
                }
                is UpdateUiState.Ready -> {
                    MnaCard {
                        StatusChip("VERIFICADA", MnaColors.Success)
                        Note("SHA-256 y firma del APK coinciden con la app instalada. Android te pedirá confirmar la instalación.")
                    }
                    PrimaryButton("Instalar ${state.info.tag}", onInstall, Modifier.fillMaxWidth())
                }
                is UpdateUiState.Failed -> {
                    MnaCard {
                        Eyebrow("ERROR")
                        Text(state.message, color = MnaColors.AccentText, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                    SecondaryButton(
                        "Reintentar",
                        { if (state.retry != null) onDownload(state.retry) else onCheck() },
                        Modifier.fillMaxWidth()
                    )
                }
            }
            Note("La instalación requiere permitir «instalar apps desconocidas» para MNA y confirmar en el sistema. Nada se instala en silencio.")
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ReleaseNotes(info: ReleaseInfo) {
    MnaCard {
        Eyebrow("NOVEDADES · ${info.title.uppercase()}")
        val notes = info.notes.lines()
            .map { it.trim().trimStart('#', '*', '-', ' ') }
            .filter { it.isNotBlank() }
            .take(8)
        if (notes.isEmpty()) Note("Esta versión no incluye notas.")
        else notes.forEach { Text("· $it", color = MnaColors.TextPrimary, fontSize = 13.sp, lineHeight = 19.sp) }
        if (info.apkSize > 0) Note("Tamaño: ${"%.1f".format(info.apkSize / 1_048_576.0)} MB")
    }
}
