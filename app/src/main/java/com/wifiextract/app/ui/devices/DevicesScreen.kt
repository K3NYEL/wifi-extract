package com.wifiextract.app.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiextract.app.R
import com.wifiextract.app.data.Device
import com.wifiextract.app.ui.components.Eyebrow
import com.wifiextract.app.ui.components.MnaCard
import com.wifiextract.app.ui.components.Note
import com.wifiextract.app.ui.components.PrimaryButton
import com.wifiextract.app.ui.components.SecondaryButton
import com.wifiextract.app.ui.components.SkeletonListRow
import com.wifiextract.app.ui.theme.MnaColors

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

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(stringResource(R.string.action_scan), onScan, Modifier.weight(1f), enabled = !state.scanning)
                    SecondaryButton(stringResource(R.string.btn_stop), onStop, Modifier.weight(1f), enabled = state.scanning)
                }
            }
            if (state.hasScanned && state.total > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Eyebrow(state.subnetLabel)
                            Text(
                                pluralStringResource(R.plurals.devices_found_count, state.devices.size, state.devices.size, state.scanned, state.total),
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
                item { DeviceRow(state.ownIp, stringResource(R.string.this_device), null) }
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
                        Eyebrow(stringResource(R.string.empty_state).uppercase())
                        Note(stringResource(R.string.devices_scan_prompt))
                    }
                }
            }
            state.message?.let { message -> item { MnaCard { Note(message.asString()) } } }
            item {
                Note(stringResource(R.string.devices_permission_notice))
            }
            item {
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun detail(device: Device, gateway: String): String = listOfNotNull(
    device.hostName,
    if (device.ip == gateway) stringResource(R.string.gateway_label) else null,
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
