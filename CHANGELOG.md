# Changelog

## [Unreleased]
- Adds the Android 13+ `NEARBY_WIFI_DEVICES` permission while retaining precise location permission for Wi-Fi scan results and AP ranging.
- Adds Wi-Fi RTT distance measurements for compatible access points, including Android 15 IEEE 802.11az responder detection when the device reports NTB initiator support.
- Adds Wi-Fi Network Request using `WifiNetworkSpecifier`, with SSID/BSSID filters and optional WPA2 credentials through Android's system approval dialog.
- Documents that these APIs do not provide raw 802.11 MAC-frame capture or monitor mode.

All notable changes to Mobile Network Analyzer are documented here.

## [Unreleased]
- Redesigns navigation into four tabs: Inicio, Red, Herramientas and Ajustes.
- Merges Dashboard and Overview into Inicio with a single primary action; Devices and Wi-Fi scan now live under Red; Updates is a sub-screen of Ajustes.
- Adds SegmentedControl, MetricTile, ActionTile, SettingsRow, SectionLabel and TerminalBlock components; cards use 16 dp radius and chips are pills.
- Groups Ajustes into Apariencia, Escaneo and Acerca de.

## [0.1.7] - 2026-11-07
- Adds a functional Wi-Fi scan screen using Android WifiManager and scan-result broadcasts.
- Shows current Wi-Fi connection metrics through ConnectivityManager and NetworkCapabilities: SSID when permitted, RSSI, frequency, link speed, Wi-Fi standard and channel.
- Lists nearby access points with BSSID, signal level, frequency, channel and advertised security capabilities.
- Declares the runtime location permission required by Android Wi-Fi scan results and explains scan restrictions in the interface.
- Distinguishes refreshed scan results from cached results when Android throttles or rejects a new scan.
- Clarifies that standard Wi-Fi scanning is not raw 802.11 monitor-mode capture.

## [0.1.6] - 2026-11-05
- Adds Dashboard (Panel) home screen and streamlines bottom navigation (Dashboard, Overview, Diagnostics, Settings).
- Moves Update Manager option directly into Settings.
- Implements the MNA mockup in Compose: design tokens, rounded 12 dp buttons, borderless surfaces and shared components.
- Adds skeleton loading states across all screens.
- Device Discovery & Network Diagnostics fully functional.
- Update Manager with GitHub Releases, SHA-256 and APK signature verification.
- Upgraded project to compileSdk 37 / targetSdk 37 and AGP 9 built-in Kotlin support.

## [0.1.2] - 2026-11-04
- Updates the Android package version to 0.1.2 (versionCode 3).
- Adds a custom red-and-graphite launcher icon.
- Carries forward the redesigned five-destination navigation and network overview.

## [0.1.0] - Initial foundation
- Initial Android foundation.
- Read-only overview of active network connection details.
