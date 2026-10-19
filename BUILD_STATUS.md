# Build and verification status

Snapshot: initial foundation milestone.

## Checks performed in this environment

- Parsed every XML resource and manifest successfully.
- Checked the manifest includes `MAIN` + `HOME` + `DEFAULT` for home-app selection.
- Checked the manifest does not declare `QUERY_ALL_PACKAGES`.
- Compiled the pure app-catalog model and filtering logic with the installed Kotlin compiler using a minimal Android `Bitmap` type stub.
- Executed four assertions covering alphabetical sorting, case-insensitive filtering, trimmed queries and empty results; all passed.

## Checks not performed

- Android Gradle build (`assembleDebug` / `assembleRelease`): not run; Android SDK and Gradle are not installed in the current execution environment.
- Actual JUnit Gradle task: not run for the same reason.
- Emulator or physical-device installation: not performed.
- Android 12 / OEM-specific launcher selection and package discovery: not yet device-verified.

The GitHub Actions workflow in `.github/workflows/android.yml` is configured to install JDK 17, Android SDK platform 36 and Gradle 8.13, run `testDebugUnitTest`, build the debug APK and upload it as an artifact. Its success must be confirmed from the workflow run after pushing the project to GitHub.

Do not treat this snapshot as a production release until the Android build, tests and device acceptance checks have passed.
