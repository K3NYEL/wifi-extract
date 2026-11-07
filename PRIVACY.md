# Privacy Policy — Mobile Network Analyzer

**Last updated:** 2026-11-07 (v0.1.7)

Mobile Network Analyzer (MNA) is designed to inspect the local network connection on the Android device where it is installed.

## Information read by the app

The overview reads connection state, transport type, local IPv4 address, default gateway, DNS server addresses and network interface name when Android makes those values available.

When the user opens the Wi-Fi monitor and starts a scan, the app reads nearby access-point metadata exposed by Android: SSID, BSSID, signal level, frequency and advertised security capabilities. It also reads current Wi-Fi connection metrics such as RSSI, link speed and Wi-Fi standard when Android permits access. Android requires the app to hold the location permission and may require device location services to be enabled before returning scan results. These values are processed locally and are not used to derive or store the user's physical location.

## Where information goes

The app processes connection and Wi-Fi scan information locally on the device. It does not send IP addresses, MAC addresses, SSIDs, BSSIDs, device names or scan results to project-owned servers. There are no identifying analytics or advertising SDKs.

The updater contacts the official project GitHub Releases source (api.github.com and github.com) to retrieve release metadata and installation files; GitHub may log your IP address. The check runs on start only if enabled in Settings, and can be disabled. The app verifies the published SHA-256 checksum and the APK signing certificate before offering the Android installation flow.

## Storage and control

MNA should store only information needed to provide a requested feature. Diagnostic reports remain local unless the user explicitly chooses to share or export them. The app does not install updates silently.

## Permissions

The app declares network-state, Wi-Fi-state, Wi-Fi scan control and Internet permissions, plus `ACCESS_FINE_LOCATION`, requested at runtime only when the user starts Wi-Fi scanning. Android uses this permission to protect access to nearby Wi-Fi scan results; MNA does not collect GPS coordinates or transmit location data. `REQUEST_INSTALL_PACKAGES` is used only to hand a verified update to the system installer.

## Responsible use

Use MNA only on networks you own or are authorized to assess. The app is not intended for intrusive actions or password recovery.

## Changes to this policy

This policy will be updated when the app adds features that change the information it reads, stores or transmits. The policy must be reviewed before each stable release.
