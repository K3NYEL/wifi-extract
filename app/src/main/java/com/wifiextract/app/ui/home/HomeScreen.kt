package com.wifiextract.app.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.NetworkInfo
import com.wifiextract.app.NetworkUiState
import com.wifiextract.app.R
import com.wifiextract.app.ui.components.ActionTile
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.MetricTile
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SectionLabel
import com.wifiextract.app.ui.components.SkeletonCard
import com.wifiextract.app.ui.components.StatusChip
import com.wifiextract.app.ui.theme.MnaColors
import com.wifiextract.app.ui.updates.UpdateUiState

/** Home: merges the former Dashboard and Overview into one screen with a single primary action. */
@Composable
fun HomeScreen(
    networkState: NetworkUiState,
    updateState: UpdateUiState,
    deviceCount: Int,
    onScan: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenWifi: () -> Unit,
    onOpenUpdates: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val info = (networkState as? NetworkUiState.Success)?.info
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.nav_home)) {
            if (info != null) {
                if (info.connected) StatusChip(stringResource(R.string.connected).uppercase(), MnaColors.Connected)
                else StatusChip(stringResource(R.string.disconnected).uppercase(), MnaColors.Disconnected)
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (updateState is UpdateUiState.Available) {
                MnaCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Eyebrow(stringResource(R.string.update_available_eyebrow))
                            Text(updateState.info.tag, color = MnaColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        PrimaryButton(stringResource(R.string.btn_view), onOpenUpdates, Modifier.width(88.dp))
                    }
                }
            }

            when (networkState) {
                NetworkUiState.Loading -> SkeletonCard(rows = 3)
                is NetworkUiState.Error -> MnaCard {
                    Eyebrow(stringResource(R.string.error_generic))
                    Text(networkState.message.asString(), color = MnaColors.TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                    SecondaryButton(stringResource(R.string.btn_retry), onRefresh, Modifier.fillMaxWidth())
                }
                is NetworkUiState.Success -> MnaCard {
                    Text(info!!.transport, color = MnaColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Note(info.networkType)
                    Text(
                        info.localAddress,
                        color = MnaColors.TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(stringResource(R.string.refresh), onRefresh, Modifier.weight(1f))
                        SecondaryButton(
                            stringResource(R.string.btn_share),
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(stringResource(R.string.metric_devices), "$deviceCount", Modifier.weight(1f))
                MetricTile(stringResource(R.string.metric_interface), info?.interfaceName ?: "—", Modifier.weight(1f))
                MetricTile(stringResource(R.string.metric_prefix), info?.takeIf { it.prefixLength > 0 }?.let { "/${it.prefixLength}" } ?: "—", Modifier.weight(1f))
            }

            SectionLabel(stringResource(R.string.section_actions))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(stringResource(R.string.action_scan), Icons.Default.Search, onScan, Modifier.weight(1f), primary = true)
                ActionTile("Ping", Icons.Default.NetworkCheck, onOpenTools, Modifier.weight(1f))
                ActionTile(stringResource(R.string.action_nearby_wifi), Icons.Default.Wifi, onOpenWifi, Modifier.weight(1f))
            }

            if (info != null) {
                SectionLabel(stringResource(R.string.section_connection))
                MnaCard {
                    val copy = { v: String -> clipboard.setText(AnnotatedString(v)) }
                    CopyRow(stringResource(R.string.lbl_local_ipv4), info.localAddress, copy)
                    CopyRow(stringResource(R.string.lbl_subnet_mask), maskLabel(info), copy)
                    CopyRow(stringResource(R.string.lbl_gateway), info.gateway, copy)
                    CopyRow(stringResource(R.string.lbl_dns), info.dnsServers, copy)
                    CopyRow(stringResource(R.string.lbl_interface), info.interfaceName, copy)
                }
            }

            Note(stringResource(R.string.authorized_network_notice))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CopyRow(label: String, value: String, onCopy: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(role = Role.Button, onClickLabel = "$label: $value") { onCopy(value) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, Modifier.width(112.dp), color = MnaColors.TextSecondary, fontSize = 13.sp)
        Text(value, Modifier.weight(1f), color = MnaColors.TextPrimary, fontSize = 13.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MnaColors.TextSecondary, modifier = Modifier.width(18.dp))
    }
}

private fun maskLabel(info: NetworkInfo): String =
    if (info.prefixLength > 0) "${info.subnetMask} (/${info.prefixLength})" else info.subnetMask

private fun report(info: NetworkInfo): String = buildString {
    appendLine("Mobile Network Analyzer — report")
    appendLine("Type: ${info.transport}")
    appendLine("Local IPv4: ${info.localAddress}")
    appendLine("Mask: ${maskLabel(info)}")
    appendLine("Gateway: ${info.gateway}")
    appendLine("DNS: ${info.dnsServers}")
    append("Interface: ${info.interfaceName}")
}
