package com.wifiextract.app.ui.network

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
        AnimatedContent(
            targetState = tab,
            modifier = Modifier.weight(1f),
            label = "network-tab-transition",
            transitionSpec = {
                (fadeIn(tween(180)) + slideInHorizontally(tween(220)) { width -> if (targetState == 0) -width / 14 else width / 14 }) togetherWith
                    (fadeOut(tween(120)) + slideOutHorizontally(tween(160)) { width -> if (targetState == 0) width / 14 else -width / 14 })
            }
        ) { selectedTab ->
            if (selectedTab == 0) devices(Modifier.fillMaxSize()) else wifi(Modifier.fillMaxSize())
        }
    }
}
