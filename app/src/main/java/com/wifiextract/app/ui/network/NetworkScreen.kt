package com.wifiextract.app.ui.network

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wifiextract.app.R
import com.wifiextract.app.ui.components.ScreenHeader
import com.wifiextract.app.ui.components.SegmentedControl

/** Red: groups local device discovery (tab 0) and nearby Wi-Fi networks (tab 1). */
@Composable
fun NetworkScreen(
    tab: Int,
    onTab: (Int) -> Unit,
    devices: @Composable (Modifier) -> Unit,
    wifi: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.nav_network))
        SegmentedControl(
            listOf(stringResource(R.string.tab_devices), stringResource(R.string.tab_nearby_wifi)),
            tab,
            onTab,
            Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        if (tab == 0) devices(Modifier.weight(1f)) else wifi(Modifier.weight(1f))
    }
}
