# Privacy Policy — Mobile Network Analyzer

**Last updated:** 2026-11-05

Mobile Network Analyzer (MNA) is designed to inspect the local network connection on the Android device where it is installed.

## Information read by the app

The current overview reads connection state, transport type, local IPv4 address, default gateway, DNS server addresses and network interface name when Android makes those values available.

Future discovery and diagnostic features may read additional local network information only when the user starts the relevant feature and grants any required Android permission. The app must explain that access before requesting it.

## Where information goes

The current app processes connection information locally on the device. It does not send IP addresses, MAC addresses, device names or scan results to project-owned servers. There are no identifying analytics or advertising SDKs.

The updater, when implemented, will contact the official project GitHub Releases source to retrieve release metadata and installation files. It must verify the published SHA-256 checksum and APK signing certificate before offering the Android installation flow.

## Storage and control

MNA should store only information needed to provide a requested feature. Diagnostic reports remain local unless the user explicitly chooses to share or export them. The app does not install updates silently.

## Permissions

The current foundation declares Android network-state, Wi-Fi-state and Internet permissions. Any additional permission must be requested only when the related feature is used, with an explanation.

## Responsible use

Use MNA only on networks you own or are authorized to assess. The app is not intended for intrusive actions or password recovery.

## Changes to this policy

This policy will be updated when the app adds features that change the information it reads, stores or transmits. The policy must be reviewed before each stable release.
