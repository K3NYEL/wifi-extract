# Changelog

## [0.1.10] - 2026-11-08 (Stable Release)
- Adds animated transitions between the main app sections with subtle fade and horizontal movement.
- Animates the selected bottom-navigation icon with a restrained scale effect.
- Adds animated transitions between device discovery and nearby Wi-Fi network tabs.
- Adds smooth expand and collapse animations to the expandable sections in Settings.
- Fixes missing English localization strings required by the build.
- Refines the launcher icon while preserving the app's red-and-graphite visual identity.
- Preserves the language-change loading state and avoids changing the network scanning and diagnostics logic.

## [0.1.9] - 2026-11-08
- Redesigns the MNA interface and navigation.
- Adds Android Wi-Fi connectivity APIs and nearby network information.

## [0.1.7] - 2026-11-07
- Adds a functional Wi-Fi scan screen using Android WifiManager and scan-result broadcasts.
- Shows current Wi-Fi connection metrics through ConnectivityManager and NetworkCapabilities: SSID when permitted, RSSI, frequency, link speed, Wi-Fi standard and channel.
- Lists nearby access points with BSSID, signal level, frequency, channel and advertised security capabilities.

## [0.1.6] - 2026-11-05
- Adds Dashboard (Panel) home screen and streamlines bottom navigation (Dashboard, Overview, Diagnostics, Settings).
- Implements the MNA mockup in Compose: design tokens, rounded 12 dp buttons, borderless surfaces and shared components.
- Device Discovery & Network Diagnostics fully functional.
- Update Manager with GitHub Releases, SHA-256 and APK signature verification.
- Upgraded project to compileSdk 37 / targetSdk 37 and AGP 9 built-in Kotlin support.

## [0.1.2] - 2026-11-04
- Updates the Android package version to 0.1.2 (versionCode 3).
- Adds a custom red-and-graphite launcher icon.

## [0.1.0] - Initial foundation
- Initial Android foundation.
- Read-only overview of active network connection details.
