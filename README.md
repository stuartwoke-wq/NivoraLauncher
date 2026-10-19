# Nivora Launcher

A fresh Kotlin + Jetpack Compose Android launcher foundation for package `org.nivora.launcher`, targeting Android 12 (API 31) and newer.

## Current milestone

Implemented in this foundation build:

- Android HOME and DEFAULT intent registration, plus an ordinary launcher entry.
- Narrow package visibility query for launchable activities (no `QUERY_ALL_PACKAGES`).
- Installed launchable-app discovery on a background dispatcher.
- Local case-insensitive app-name filtering and alphabetical ordering.
- App launch actions through Android's package manager.
- Initial Nivora dark/glass-inspired home UI, search field, dock, and settings entry screen.
- Unit tests for pure app filtering and sorting logic.

Not implemented yet: persistent workspace pages and folders, real widget hosting, configurable gestures, full theme editing, encrypted backup/restore, and complete settings persistence. The settings screen contains introductory placeholders for later milestones; it does not claim those controls are already functional.

## Toolchain

- Android Gradle Plugin: 8.13.2
- Gradle wrapper distribution: 8.13
- Kotlin: 2.2.21
- Compose BOM: 2025.12.00
- compileSdk: 36
- targetSdk: 36
- minSdk: 31
- JDK: 17

Versions were selected as a conservative compatible baseline rather than mixing the newest versions of every component. Revalidate versions before a release build.

## Build

In an Android SDK environment with JDK 17 and a generated Gradle wrapper JAR:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Both debug and release variants use the requested application ID `org.nivora.launcher`. They cannot be installed side-by-side as separate package variants.

## Phone-only cloud workflow

1. Upload this project ZIP to a cloud Android build environment or GitHub repository.
2. Ensure JDK 17 and Android SDK platform 36/build tools are available.
3. Generate/restore the standard Gradle wrapper JAR if the cloud service requires it.
4. Run `./gradlew testDebugUnitTest` and inspect the complete output.
5. Run `./gradlew assembleDebug`.
6. Download `app/build/outputs/apk/debug/app-debug.apk` and install it on a test device.
7. Verify the package ID, default-home selection, app discovery, search, launching, restart behavior and logs on-device.

## Privacy notes

The initial manifest has no INTERNET permission and does not request broad package inventory access. App discovery queries activities matching `ACTION_MAIN` + `CATEGORY_LAUNCHER`. This should be validated on Android 12+ devices and OEM variants.

## Quality status

This source snapshot has not yet been compiled against the Android SDK or run on a physical device in the current environment. Static checks and available unit tests must be run in a compatible Android build environment before treating the APK or milestone as verified.
