# Architecture

## Current implementation

The Android app currently uses a single Gradle `:app` module. The presentation layer is Jetpack Compose. Network overview state is exposed by `NetworkOverviewViewModel` as a `StateFlow`; `NetworkInfoRepository` reads Android's `ConnectivityManager` and `LinkProperties` APIs on an IO coroutine dispatcher.

`MainActivity -> NetworkOverviewViewModel -> NetworkInfoRepository -> Android Connectivity APIs`

The UI consumes presentation state and does not call Android network APIs directly.

## Package layout (v0.2.0, still one `:app` module)

- `core`: validation, version comparison and subnet maths (pure Kotlin, unit tested).
- `data`: repositories for discovery, diagnostics, updates and settings.
- `ui/theme`, `ui/components`: design tokens, buttons, cards and skeletons.
- `ui/overview|devices|diagnostics|updates|settings`: screen + ViewModel per destination.

These packages map one-to-one onto the target modules below, so extracting them later is mechanical.

## Target module boundaries

The intended Gradle module layout is:

- `:app`: application entry point, navigation and dependency composition.
- `:core`: shared domain models, validation, dispatchers and repository contracts.
- `:design-system`: palette, typography, spacing tokens and reusable Compose components.
- `:overview`: connection overview.
- `:discovery`: local network device/service discovery.
- `:diagnostics`: bounded and cancellable diagnostics.
- `:updater`: official release metadata, APK hash and certificate verification.
- `:settings`: preferences, privacy, scope/limits and third-party licenses.

This is the target architecture; the repository has not yet been split into these Gradle modules.

## Product information architecture

The supplied HTML mockup defines six screens. The Android app maps them to five persistent bottom destinations:

1. Overview — current connection state and technical details.
2. Devices — authorized local network discovery.
3. Diagnostics — connectivity and service checks.
4. Updates — official release checking and verified installation flow.
5. Settings — preferences, privacy, licenses, and the mockup's Scope and limits content.

## Architecture rules

- MVVM/UDF: ViewModels expose immutable UI state through StateFlow.
- Repositories isolate Android platform APIs from Compose.
- Network work uses coroutines, cancellation, timeouts and bounded concurrency.
- Use dependency injection (Hilt or Koin) when module composition is introduced.
- Unit-test subnet calculations, parsing and input validation; add Compose UI tests for critical flows.
- Do not treat unavailable Android network information as proof that a host or service does not exist.
- No scan is performed without a clear user action. Operations must be bounded, cancellable and limited to authorized networks.

## Platform constraints

Android version, OEM behavior and runtime permissions affect which network details are visible. Wi-Fi identifiers and local network discovery may be restricted. The app must explain limitations rather than bypass platform protections.
