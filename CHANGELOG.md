# Changelog

All notable changes to Mobile Network Analyzer are documented here.

## [0.2.0] - Unreleased
- Implements the MNA mockup in Compose: design tokens, rounded 12 dp buttons, borderless surfaces and shared components.
- Adds skeleton loading states (overview, device list, diagnostics results, update check and release notes).
- Device Discovery: bounded (32 concurrent), cancellable /24 reachability sweep started only by the user; per-device Ping hands off to Diagnostics.
- Network Diagnostics: ping, DNS lookup and single-port TCP check with input validation, timeouts and cancellation. Traceroute remains marked "Próximamente".
- Update Manager: checks official GitHub Releases, downloads the APK to app cache, verifies the published SHA-256 and the signing certificate against the installed app, then hands off to the system installer with explicit user confirmation. Never installs silently.
- Settings: persisted preferences (update check on start, hostname resolution, keep screen on, timeout) plus scope and limits.
- Declares `REQUEST_INSTALL_PACKAGES` and a cache-only `FileProvider` for the verified update flow.
- Adds unit tests for validation, version comparison and subnet calculation.

## [0.1.2] - In preparation
- Updates the Android package version to 0.1.2 (versionCode 3).
- Adds a custom red-and-graphite launcher icon.
- Carries forward the redesigned five-destination navigation and network overview.
- Keeps device discovery, diagnostics and the verified updater explicitly unavailable until their implementations pass testing.
- Release signing, APK verification and publication remain pending keystore setup and CI validation.

## [0.1.0] - Initial foundation
- Initial Android foundation.
- Read-only overview of active network connection details.
