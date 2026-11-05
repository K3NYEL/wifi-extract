package com.k3nyel.wifiextract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.k3nyel.wifiextract.ui.dashboard.DashboardScreen
import com.k3nyel.wifiextract.ui.devices.DevicesScreen
import com.k3nyel.wifiextract.ui.devices.DevicesViewModel
import com.k3nyel.wifiextract.ui.diagnostics.DiagnosticsScreen
import com.k3nyel.wifiextract.ui.diagnostics.DiagnosticsViewModel
import com.k3nyel.wifiextract.ui.overview.OverviewScreen
import com.k3nyel.wifiextract.ui.settings.SettingsScreen
import com.k3nyel.wifiextract.ui.settings.SettingsViewModel
import com.k3nyel.wifiextract.ui.theme.MnaColors
import com.k3nyel.wifiextract.ui.theme.MnaTheme
import com.k3nyel.wifiextract.ui.updates.UpdateUiState
import com.k3nyel.wifiextract.ui.updates.UpdatesScreen
import com.k3nyel.wifiextract.ui.updates.UpdatesViewModel

private enum class Destination(val label: Int, val icon: ImageVector) {
    Dashboard(R.string.nav_dashboard, Icons.Default.Dashboard),
    Overview(R.string.nav_overview, Icons.Default.Home),
    Diagnostics(R.string.nav_diagnostics, Icons.Default.Build),
    Settings(R.string.nav_settings, Icons.Default.Settings)
}

private sealed interface CurrentScreen {
    data class Tab(val destination: Destination) : CurrentScreen
    data object Devices : CurrentScreen
    data object Updates : CurrentScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val provider = ViewModelProvider(this)
        val network = provider[NetworkOverviewViewModel::class.java]
        val devices = provider[DevicesViewModel::class.java]
        val diagnostics = provider[DiagnosticsViewModel::class.java]
        val updates = provider[UpdatesViewModel::class.java]
        val settings = provider[SettingsViewModel::class.java]
        setContent {
            MnaTheme {
                MnaApp(network, devices, diagnostics, updates, settings)
            }
        }
    }
}

@Composable
private fun MnaApp(
    network: NetworkOverviewViewModel,
    devices: DevicesViewModel,
    diagnostics: DiagnosticsViewModel,
    updates: UpdatesViewModel,
    settings: SettingsViewModel
) {
    var currentScreen by rememberSaveable { mutableStateOf<CurrentScreen>(CurrentScreen.Tab(Destination.Dashboard)) }
    val context = LocalContext.current
    val updateState by updates.state.collectAsState()
    val installed = updates.installed

    val selectedTab = (currentScreen as? CurrentScreen.Tab)?.destination

    Scaffold(
        containerColor = MnaColors.Background,
        bottomBar = {
            NavigationBar(containerColor = MnaColors.NavBar, contentColor = MnaColors.TextPrimary, tonalElevation = 0.dp) {
                Destination.entries.forEach { destination ->
                    val label = stringResource(destination.label)
                    val isSelected = selectedTab == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = CurrentScreen.Tab(destination) },
                        icon = {
                            BadgedBox(badge = {
                                if (destination == Destination.Settings && updateState is UpdateUiState.Available) {
                                    Badge(containerColor = MnaColors.Accent)
                                }
                            }) { Icon(destination.icon, contentDescription = label) }
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MnaColors.AccentText,
                            selectedTextColor = MnaColors.AccentText,
                            unselectedIconColor = MnaColors.TextSecondary,
                            unselectedTextColor = MnaColors.TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (val screen = currentScreen) {
            is CurrentScreen.Tab -> {
                when (screen.destination) {
                    Destination.Dashboard -> DashboardScreen(
                        networkState = network.state.collectAsState().value,
                        updateState = updateState,
                        deviceCount = devices.state.collectAsState().value.devices.size,
                        onOpenDevices = { currentScreen = CurrentScreen.Devices },
                        onOpenDiagnostics = { currentScreen = CurrentScreen.Tab(Destination.Diagnostics) },
                        onOpenUpdates = { currentScreen = CurrentScreen.Updates },
                        onRefreshNetwork = network::refresh,
                        modifier = modifier
                    )
                    Destination.Overview -> OverviewScreen(
                        state = network.state.collectAsState().value,
                        onRefresh = network::refresh,
                        modifier = modifier
                    )
                    Destination.Diagnostics -> DiagnosticsScreen(
                        state = diagnostics.state.collectAsState().value,
                        onTool = diagnostics::selectTool,
                        onHost = diagnostics::setHost,
                        onPort = diagnostics::setPort,
                        onRun = diagnostics::run,
                        onCancel = diagnostics::cancel,
                        modifier = modifier
                    )
                    Destination.Settings -> SettingsScreen(
                        state = settings.state.collectAsState().value,
                        versionLabel = "${installed.name} (${installed.code})",
                        onCheckUpdates = settings::setCheckUpdates,
                        onResolveHostnames = settings::setResolveHostnames,
                        onKeepScreenOn = settings::setKeepScreenOn,
                        onTimeout = settings::setTimeout,
                        onOpenUpdates = { currentScreen = CurrentScreen.Updates },
                        modifier = modifier
                    )
                }
            }
            CurrentScreen.Devices -> DevicesScreen(
                state = devices.state.collectAsState().value,
                onScan = devices::startScan,
                onStop = devices::stopScan,
                onPing = { host ->
                    diagnostics.prefill(host)
                    currentScreen = CurrentScreen.Tab(Destination.Diagnostics)
                },
                modifier = modifier
            )
            CurrentScreen.Updates -> UpdatesScreen(
                state = updateState,
                installed = installed,
                onCheck = updates::check,
                onDownload = updates::download,
                onCancel = updates::cancelDownload,
                onInstall = { updates.install(context) },
                modifier = modifier
            )
        }
    }
}
