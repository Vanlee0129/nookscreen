# NookScreen

**Arrange your TV home screen with the remote.** NookScreen is an open-source launcher for Android TV. Keep apps, HDMI inputs and wallpaper on one screen, with different layouts for everyday viewing, movies and games.

[Download the daily APK](https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk) · [Release history](https://github.com/Vanlee0129/nookscreen/releases) · [Report an issue](https://github.com/Vanlee0129/nookscreen/issues) · [中文](README.md)

![NookScreen running on a Xiaomi TV, with HDMI 2, weather and two rows of app tiles](docs/screenshots/night.jpg)

## Install and open Control Center

1. Download `nookscreen-daily.apk`, transfer it to your TV and install it. The daily build supports Android 5.0 and later and uses package ID `app.nookscreen.tv`.
2. Open NookScreen and select it as the default Home app in Android settings. If your TV has no default Home setting, open **Control Center → Make NookScreen Your Home Screen** and follow the prompts to enable Home Guard.
3. On Home, press **Up** to focus the status button in the top right, then press **OK** to open Control Center. Hold **OK** on an app tile to open its menu.

Default Home settings and available inputs vary by manufacturer. See the [device notes](docs/devices/) for tested models and settings.

## Arrange your home screen

| Task | Where to start |
| --- | --- |
| Organize apps | Hold **OK** on a tile to move it, hide it or put it in a folder. |
| Give movies and games different layouts | Open **Control Center → Desktop scenes**, duplicate the current scene and edit the copy. |
| Add HDMI to Home | Pick **Favorite inputs** in the scene editor. Supported inputs become tiles you can open with **OK**. |
| Send photos or app artwork from your phone | Connect your phone to the same local network, then scan the setup code under **Control Center → Themes → Wallpaper and Personalization**. |

Each scene saves its app order, folders, hidden apps, favorite inputs and appearance. NookScreen restores the active scene after a restart.

| Example scene | Scene settings |
| --- | --- |
| ![Dawn wallpaper with HDMI 1 and an app folder](docs/screenshots/morning.jpg) | ![Scene settings for wallpaper, favorite inputs and renaming](docs/screenshots/scenes.jpg) |

## Plugins

Core provides the launcher, remote navigation, scenes and updates. Themes are a type of plugin. New features and content outside the core launcher ship as plugins, with their own versions and installation, update, disable and removal controls. Newly published themes appear in the catalog automatically. Control Center has separate **Themes** and **Plugins** entries. The Plugins page manages feature plugins.

### Install and switch themes

On the TV, open **Control Center → Themes**. Select a theme to download it, then select it again and choose **Apply**. The same menu offers updates, disabling and removal. The phone setup page has theme controls under **Wallpaper & Screensaver → Themes**.

On v0.0.3, the TV path is **Control Center → Themes → Plugins**. When upgrading from v0.0.2, install the matching theme plugin to restore the theme references saved in your scenes.

| Sanrio · Strawberry Picnic | Marvel · Comic Universe |
| --- | --- |
| ![Strawberry picnic wallpaper and pastel app tiles](docs/screenshots/sanrio-theme.jpg) | ![Comic city wallpaper and red-and-gold app tiles](docs/screenshots/marvel-theme.jpg) |

Each pack has 20 dedicated tiles. Other apps keep their artwork with a matching frame, and uploaded or explicitly selected icons take priority. Disabling or removing a theme restores classic tiles and uses the built-in Aurora wallpaper where a theme wallpaper is missing. Photos, icons and scenes are retained. [Theme coverage and artwork notes](docs/themes.md)

### Save your own look

Choose a base theme and wallpaper, then select **Save current appearance** on the Themes page. With your custom theme active, scan the setup code to upload per-app artwork from your phone. You can reuse the same custom theme across scenes.

## Updates and support

- **Update the launcher**　Open **Control Center → NookScreen** and select **Check for updates**. Android 12 and later support idle-time updates once you allow NookScreen to install apps. An in-place update with the same package and signing certificate keeps your data.
- **Update a theme**　Check for updates in the theme’s menu. If a theme download fails, check whether the TV can reach GitHub Releases and try again.
- **Report a problem**　Include your TV model, Android and NookScreen versions, and the steps to reproduce it. For plugin issues, include the plugin name and version. [Open an issue](https://github.com/Vanlee0129/nookscreen/issues)

## Local development

Use **JDK 17** and **Android SDK 36**. Run these commands from the repository root.

```sh
./gradlew :core:testDailyDebugUnitTest :core:assemblePreviewRelease
./gradlew :core:assembleDailyRelease
./gradlew :plugins:themes:assemble
```

Preview uses package ID `app.nookscreen.tv.preview` for side-by-side testing on a TV. Find the daily APK under `core/build/outputs/apk/daily/release/` and the theme release assets under `plugins/themes/build/distributions/`. [Signing and release steps](docs/RELEASE.md)

| Directory | Contents |
| --- | --- |
| [`core/`](core/) | Launcher, scenes, plugin management and phone setup |
| [`plugin-api/`](plugin-api/) | Plugin compatibility version and Android entry point |
| [`plugins/themes/`](plugins/themes/) | Theme manifests, artwork and packaging tasks |
| [`plugins/clock/`](plugins/clock/) | Example of a standalone Android feature plugin |
| [`lite/`](lite/) | Aerial Lite for Android 4.x |

See the [architecture notes](docs/ARCHITECTURE.md) and [plugin guide](docs/PLUGINS.md) before contributing. NookScreen is based on [Aerial](https://github.com/pengchujin/aerial-tv) and released under the [MIT license](LICENSE).
