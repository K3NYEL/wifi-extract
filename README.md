# Mobile Network Analyzer

Native Android network information and diagnostics application built with Kotlin and Jetpack Compose.

The six-screen HTML mockup supplied for this project is the product's visual and information-architecture reference. The Android implementation uses four persistent bottom-navigation destinations: **Inicio**, **Red** (Devices + nearby Wi-Fi), **Herramientas** (Diagnostics) and **Ajustes** (including Updates and **Scope and limits**).

## Product structure

| Destination | Purpose | Current status |
|---|---|---|
| Overview | Connection state, transport, IPv4, gateway, DNS and interface | Implemented as read-only overview |
| Devices | Discover devices visible on the connected local network | Implemented: bounded /24 reachability sweep |
| Diagnostics | Bounded, cancellable connectivity checks | Ping, DNS and TCP port implemented; traceroute coming soon |
| Updates | Official GitHub Releases, APK signature and SHA-256 verification | Implemented; requires a published release with APK + `.sha256` |
| Monitor Wi-Fi | Current connection metrics and nearby access-point scan results | Implemented with Android public Wi-Fi APIs; raw 802.11 monitor-mode capture is not exposed by standard Android APIs |
| Settings | Preferences, privacy, permissions, licenses and scope/limits | Implemented (licenses inventory pending) |

Unavailable capabilities are explicitly marked as coming soon. The app must not display simulated scan results as real data.

## Design system

- Background: `#0A0A0B`
- Surfaces: `#17171A` and `#202024`
- No gray borders: surfaces separate content
- Accent: `#E5322D`
- Inter for interface text and JetBrains Mono for technical values (font packaging and license review are pending).
- 16 dp cards, 12 dp buttons, pill chips, segmented controls for option groups, 4/8 dp spacing grid and restrained motion (skeleton pulse only).
- Material 3, accessible touch targets and text/icon state indicators.

## Technology

- Kotlin, Android Gradle Plugin and Jetpack Compose + Material 3
- Android Connectivity APIs and WifiManager scan APIs
- JDK 17, Gradle 9.8.2, compileSdk 37, minSdk 26, targetSdk 37

## Build

Open the repository root in Android Studio, sync Gradle and run the `app` configuration on an Android device or emulator. The Wi-Fi scan requires the runtime location permission and enabled location services; Android may throttle scan requests.

For a debug APK:

```bash
gradle :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Project standards

The mandatory rules are documented in [docs/PROJECT_STANDARDS.md](docs/PROJECT_STANDARDS.md). Security reporting guidance is in [SECURITY.md](SECURITY.md), and changes are tracked in [CHANGELOG.md](CHANGELOG.md).

## Branch model

- `main`: stable, reviewed releases.
- `develop`: integration branch.
- `feature/*`: isolated feature work.
- `fix/*`: targeted fixes.
- `release/*`: release stabilization.

Feature branches merge through reviewed pull requests. A release must pass CI and manual validation before it is promoted to `main`.

## Safety and privacy

MNA is intended only for networks the operator owns or is authorized to assess. Network information is processed locally and is not sent to project-owned servers. See [PRIVACY.md](PRIVACY.md).

The project license is intentionally not assumed; the repository owner must choose and approve MIT, Apache-2.0 or GPL before a stable public distribution.
