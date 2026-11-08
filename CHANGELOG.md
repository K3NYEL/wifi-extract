# Changelog

## [1.0.0] - 2026-11-08 (First Stable Release)
- **First Stable Version (v1.0.0, versionCode 10):** Official stable release of Mobile Network Analyzer (MNA).
- **Internationalization (i18n):** Complete English & Spanish localization with automatic system locale detection (`locales_config.xml`), `UiText` architecture, locale-aware `Formatters`, and in-app language switcher in Settings (System, Spanish, English).
- **System Theme Detection:** Adds automatic system theme detection ("Sistema") alongside Dark and Light modes in Settings (`Apariencia`).
- **In-App Log Console:** Implements `MnaLogger` and a dedicated `Consola` viewer tab in `Herramientas` to inspect real-time log events, network actions, diagnostics, and errors with clear/copy capabilities.
- **Licenses & Attribution:** Adds full open-source third-party license attribution (Apache 2.0 / MIT) under `Acerca de -> Licencias`.
- Adds the Android 13+ `NEARBY_WIFI_DEVICES` permission while retaining precise location permission for Wi-Fi scan results and AP ranging.
- Redesigns navigation into four tabs: Inicio, Red, Herramientas and Ajustes.
- Update Manager with GitHub Releases, SHA-256 and APK signature verification.

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
