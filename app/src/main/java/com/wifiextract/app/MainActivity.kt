package com.wifiextract.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.wifiextract.app.ui.home.HomeScreen
import com.wifiextract.app.ui.network.NetworkScreen
import com.wifiextract.app.ui.devices.DevicesScreen
import com.wifiextract.app.ui.devices.DevicesViewModel
import com.wifiextract.app.ui.diagnostics.DiagnosticsScreen
import com.wifiextract.app.ui.diagnostics.DiagnosticsViewModel
import com.wifiextract.app.ui.monitor.MonitorScreen
import com.wifiextract.app.ui.settings.SettingsScreen
import com.wifiextract.app.ui.settings.SettingsViewModel
import com.wifiextract.app.ui.theme.MnaColors
import com.wifiextract.app.ui.theme.MnaTheme
import com.wifiextract.app.ui.updates.UpdateUiState
import com.wifiextract.app.ui.updates.UpdatesScreen
import com.wifiextract.app.ui.updates.UpdatesViewModel

enum class Screen(val label: Int? = null, val icon: ImageVector? = null, val isTab: Boolean = true) {
    Home(R.string.nav_home, Icons.Default.Home),
    Network(R.string.nav_network, Icons.Default.Wifi),
    Tools(R.string.nav_tools, Icons.Default.Build),
    Settings(R.string.nav_settings, Icons.Default.Settings),
    Updates(isTab = false)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val provider = ViewModelProvider(this)
        val network = provider[NetworkOverviewViewModel::class.java]
        val devices = provider[DevicesViewModel::class.java]
        val diagnostics = provider[DiagnosticsViewModel::class.java]
        val updates = provider[UpdatesViewModel::class.java]
        val settings = provider[SettingsViewModel::class.java]
        setContent {
            val settingsState by settings.state.collectAsState()
            MnaTheme(darkTheme = settingsState.theme == "dark") {
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
    var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }
    var networkTab by rememberSaveable { mutableStateOf(0) }
    val context = LocalContext.current
    val updateState by updates.state.collectAsState()
    val installed = updates.installed

    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = if (currentScreen == Screen.Updates) Screen.Settings else Screen.Home
    }

    Scaffold(
        containerColor = MnaColors.Background,
        bottomBar = {
            Surface(color = MnaColors.NavBar, modifier = Modifier.fillMaxWidth()) {
                NavigationBar(
                    containerColor = MnaColors.NavBar,
                    contentColor = MnaColors.TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    Screen.entries.filter { it.isTab }.forEach { screen ->
                        val label = stringResource(screen.label!!)
                        val isSelected = currentScreen == screen || (screen == Screen.Settings && currentScreen == Screen.Updates)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                BadgedBox(badge = {
                                    if (screen == Screen.Settings && updateState is UpdateUiState.Available) {
                                        Badge(containerColor = MnaColors.Accent)
                                    }
                                }) { Icon(screen.icon!!, contentDescription = label) }
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
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (currentScreen) {
            Screen.Home -> HomeScreen(
                networkState = network.state.collectAsState().value,
                updateState = updateState,
                deviceCount = devices.state.collectAsState().value.devices.size,
                onScan = {
                    devices.startScan()
                    networkTab = 0
                    currentScreen = Screen.Network
                },
                onOpenTools = { currentScreen = Screen.Tools },
                onOpenWifi = {
                    networkTab = 1
                    currentScreen = Screen.Network
                },
                onOpenUpdates = { currentScreen = Screen.Updates },
                onRefresh = network::refresh,
                modifier = modifier
            )
            Screen.Network -> NetworkScreen(
                tab = networkTab,
                onTab = { networkTab = it },
                devices = { m ->
                    DevicesScreen(
                        state = devices.state.collectAsState().value,
                        onScan = devices::startScan,
                        onStop = devices::stopScan,
                        onPing = { host ->
                            diagnostics.prefill(host)
                            currentScreen = Screen.Tools
                        },
                        modifier = m
                    )
                },
                wifi = { m -> MonitorScreen(modifier = m) },
                modifier = modifier
            )
            Screen.Tools -> DiagnosticsScreen(
                state = diagnostics.state.collectAsState().value,
                onTool = diagnostics::selectTool,
                onHost = diagnostics::setHost,
                onPort = diagnostics::setPort,
                onRun = diagnostics::run,
                onCancel = diagnostics::cancel,
                modifier = modifier
            )
            Screen.Settings -> SettingsScreen(
                state = settings.state.collectAsState().value,
                versionLabel = "${installed.name} (${installed.code})",
                onCheckUpdates = settings::setCheckUpdates,
                onResolveHostnames = settings::setResolveHostnames,
                onKeepScreenOn = settings::setKeepScreenOn,
                onTimeout = settings::setTimeout,
                onTheme = settings::setTheme,
                onOpenUpdates = { currentScreen = Screen.Updates },
                modifier = modifier,
                updateAvailable = updateState is UpdateUiState.Available
            )
            Screen.Updates -> UpdatesScreen(
                state = updateState,
                installed = installed,
                onCheck = updates::check,
                onDownload = updates::download,
                onCancel = updates::cancelDownload,
                onInstall = { updates.install(context) },
                onBack = { currentScreen = Screen.Settings },
                modifier = modifier
            )
        }
    }
}
