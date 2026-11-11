# NookScreen release guide

Use JDK 17 and Android SDK 36. Build and verify locally before choosing what to publish.

## Core APK

1. Set `versionName` and increment Android `versionCode` in `core/build.gradle.kts`.
2. Run `./gradlew :core:testDailyDebugUnitTest :core:assembleDailyRelease`. To build only the APK, run `./gradlew :core:assembleDailyRelease`.
3. Verify the application ID is `app.nookscreen.tv` and the signer matches the current release using `apksigner verify --print-certs`. The Gradle release signing config reads `local/signing.properties` with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. Keep the properties and keystore under ignored `local/`. The keystore path is relative to `local/`.
4. When releasing, publish a stable tag such as `v0.0.3` on [Vanlee0129/nookscreen](https://github.com/Vanlee0129/nookscreen/releases) with the signed APK from `core/build/outputs/apk/daily/release/` renamed to `nookscreen-daily.apk`. The updater validates package ID, release version, Android version code and signing identity.

For Xiaomi testing, build `./gradlew :core:assemblePreviewRelease`. Preview uses `app.nookscreen.tv.preview`, a separate app data directory and phone setup port, with no HOME or guard registration. Use `adb install-multiple -r preview.apk preview.dm` with matching names. Extract the DM from `assets/aerial-dm/31.dm` on Android 12 or later or `assets/aerial-dm/28.dm` on Android 9–11. Confirm `speed-profile` using `adb shell dumpsys package dexopt`. Use the Preview package for migration rehearsals and seed it with test data.

## Theme ZIPs

1. Update the theme's files and increment its own `version` in `plugins/themes/<id>/manifest.json`. Keep its ID and set the supported Core API range.
2. Run `./gradlew :plugins:themes:packSanrio` or `./gradlew :plugins:themes:packMarvel`. To build both, run `./gradlew :plugins:themes:assemble`. The ZIPs are in `plugins/themes/build/distributions/`.
3. Import on the TV or upload through the phone settings page. Verify a compatible higher version replaces the ZIP while preserving enablement and references. Verify an incompatible version leaves the current version usable.
4. Each official theme has a separate GitHub **prerelease** channel with its own tag and ZIP. Use `plugin-sanrio` and `nookscreen-theme-sanrio.zip`, or `plugin-marvel` and `nookscreen-theme-marvel.zip`. Replace only the named asset on that channel when updating the theme. The ZIP's manifest carries the monotonically increasing plugin version. The Core stable `/releases/latest` endpoint continues to select the Core APK channel.

Publish both theme channels before the first Core release that moves the artwork out of the APK. Users can install the matching theme from the plugin page after upgrading. The [plugin guide](PLUGINS.md) describes the fallback and offline import.

## Feature APKs

Build the Clock example with `./gradlew :plugins:clock:assembleDebug` for local tests or `./gradlew :plugins:clock:assembleRelease` for an unsigned release APK. Outputs are in `plugins/clock/build/outputs/apk/`. Use the plugin's own release signing key and increment its Android version code independently. Feature APKs can use their own GitHub prereleases, such as `plugin-clock-v1.0.0`. Android handles installation and upgrades, and Core discovers and checks compatibility after installation. The example can be installed with ADB on a test device and removed from Android settings. Core's updater only installs Core.
