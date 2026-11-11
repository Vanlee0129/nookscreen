# NookScreen release guide

The daily package is `app.nookscreen.tv`. Updates use the stable release channel at
[Vanlee0129/nookscreen](https://github.com/Vanlee0129/nookscreen/releases).

1. Increase the app's display version and Android version code in `app/build.gradle.kts`.
2. Build with JDK 17 and SDK 36. Run `./gradlew :app:testDailyDebugUnitTest :app:assembleDailyRelease`.
3. Sign with the NookScreen release key. Keep `local/signing.properties` and the key outside Git. The properties are `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`. The file path is relative to `local/`.
4. Publish a stable tag in the form `v0.0.1` with the signed APK named `nookscreen-daily.apk`.

The updater checks the package, release version, Android version code, and signing identity.
The APK contains dex metadata for the install session. For ADB installation on Android 12
and later, extract `assets/aerial-dm/31.dm` from the APK beside it with the same base name
and install both using `adb install-multiple -r`.
