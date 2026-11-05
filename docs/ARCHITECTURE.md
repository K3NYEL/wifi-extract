# Architecture

## Layers

- **UI:** Jetpack Compose screens and reusable components.
- **Presentation:** screen state and view models (introduced with feature modules).
- **Domain:** network data models and use cases.
- **Data:** Android platform adapters such as ConnectivityManager, WifiManager, and NsdManager.

## Initial data flow

`MainActivity -> NetworkInfoRepository -> ConnectivityManager / LinkProperties -> NetworkInfo -> Compose UI`

The initial implementation is read-only. It displays the active transport, local IPv4 addresses, default gateway, DNS servers, and interface name when available.

## Planned modules

1. **Device discovery:** use supported Android local service discovery APIs; distinguish advertised services from inferred devices.
2. **Diagnostics:** user-initiated connectivity checks against explicitly entered, authorized targets.
3. **History:** persist completed scan summaries locally.
4. **Reports:** export user-selected diagnostic results.

## Platform constraints

Android versions and OEM behavior affect which network details are visible. Wi-Fi identifiers and local network discovery can require runtime permissions or be restricted by platform policy. Do not treat unavailable data as proof that a device or service does not exist.
