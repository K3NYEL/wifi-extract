package com.wifiextract.app.ui.overview

import android.content.Intent
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.NetworkInfo
import com.wifiextract.app.NetworkUiState
import com.wifiextract.app.R
import androidx.compose.ui.res.stringResource
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.InfoRow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SkeletonBox
import com.wifiextract.app.ui.components.SkeletonCard
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors

@Composable
fun OverviewScreen(state: NetworkUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Column(modifier.fillMaxSize()) {
        ScreenHeader("Network Overview")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state) {
                NetworkUiState.Loading -> {
                    MnaCard {
                        Eyebrow("LEYENDO CONEXIÓN")
                        SkeletonBox(Modifier.fillMaxWidth(0.5f), height = 26.dp)
                        SkeletonBox(Modifier.fillMaxWidth(0.7f), height = 12.dp)
                    }
                    SkeletonCard(rows = 6)
                }
                is NetworkUiState.Error -> MnaCard {
                    Eyebrow("ERROR DE LECTURA")
                    Text(state.message, color = MnaColors.TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                    SecondaryButton("Reintentar", onRefresh, Modifier.fillMaxWidth())
                }
                is NetworkUiState.Success -> {
                    val info = state.info
                    MnaCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Eyebrow("ESTADO")
                            if (info.connected) StatusChip("CONECTADO", MnaColors.Success)
                            else StatusChip("SIN CONEXIÓN", MnaColors.AccentText)
                        }
                        Text(info.transport, color = MnaColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Note(info.networkType)
                    }
                    MnaCard {
                        InfoRow("IPv4 local", info.localAddress)
                        InfoRow("Máscara", maskLabel(info))
                        InfoRow("Puerta de enlace", info.gateway)
                        InfoRow("DNS", info.dnsServers)
                        InfoRow("Interfaz", info.interfaceName)
                        InfoRow("Tipo", info.transport, mono = false)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryButton("Actualizar", onRefresh, Modifier.weight(1f))
                        SecondaryButton(
                            "Copiar IP",
                            { clipboard.setText(AnnotatedString(info.localAddress)) },
                            Modifier.weight(1f),
                            enabled = info.connected
                        )
                        SecondaryButton(
                            "Compartir",
                            {
                                val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report(info))
                                context.startActivity(Intent.createChooser(send, null))
                            },
                            Modifier.weight(1f),
                            enabled = info.connected
                        )
                    }
                }
            }
            MnaCard {
                Eyebrow("USO RESPONSABLE")
                Note(stringResource(R.string.authorized_network_notice))
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

private fun maskLabel(info: NetworkInfo): String =
    if (info.prefixLength > 0) "${info.subnetMask} (/${info.prefixLength})" else info.subnetMask

private fun report(info: NetworkInfo): String = buildString {
    appendLine("Mobile Network Analyzer — informe de red")
    appendLine("Tipo: ${info.transport}")
    appendLine("IPv4 local: ${info.localAddress}")
    appendLine("Máscara: ${maskLabel(info)}")
    appendLine("Puerta de enlace: ${info.gateway}")
    appendLine("DNS: ${info.dnsServers}")
    append("Interfaz: ${info.interfaceName}")
}
