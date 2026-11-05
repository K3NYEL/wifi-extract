# Mobile Network Analyzer

Native Android network diagnostics app built with Kotlin and Jetpack Compose.

> Foundation stage: this repository establishes the Android app, visual system, and read-only network overview. Discovery and authorized diagnostics will be added in isolated feature branches.

## Stack
- Kotlin
- Android Gradle Plugin
- Jetpack Compose + Material 3
- Android Connectivity APIs

## Current foundation
- Dark graphite interface with restrained red accents, inspired by the Slim Executor visual language.
- Network connection state and transport type.
- Local IPv4 address, default route gateway, DNS servers, and interface name when Android exposes them.
- No root requirement and no attempt to bypass Android platform restrictions.

## Build
Open the repository root in Android Studio, allow Gradle sync, then run the `app` configuration on an Android device or emulator.

Requirements: JDK 17 and Android SDK 35.

## Branch model
- `main`: stable, reviewed releases.
- `develop`: integration branch.
- `feature/*`: isolated feature work.
- `fix/*`: targeted fixes.
- `release/*`: release stabilization.

Feature branches merge into `develop` through pull requests. Only validated releases move from `develop` to `main`.

## Safety and privacy
Network discovery and diagnostics must be limited to networks and devices the operator owns or is authorized to assess. Android may restrict access to Wi-Fi identifiers, neighboring devices, and low-level network operations. The app reports only data available through supported Android APIs.
