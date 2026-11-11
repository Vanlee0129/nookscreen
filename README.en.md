# NookScreen

**A TV home screen you can arrange with your remote.**

NookScreen is an open-source Android TV launcher based on [Aerial](https://github.com/pengchujin/aerial-tv). Keep apps, inputs, and wallpaper together and switch layouts for movies, games, or everyday use.

[Download the daily APK](https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk) · [Releases](https://github.com/Vanlee0129/nookscreen/releases) · [Issues](https://github.com/Vanlee0129/nookscreen/issues) · [中文](README.md)

![NookScreen on a Xiaomi TV with a night wallpaper, HDMI 2 and two rows of app tiles](docs/screenshots/night.jpg)

## Install

1. Download `nookscreen-daily.apk` and install it on your Android TV. The package is `app.nookscreen.tv`.
2. Open NookScreen and choose it as the default Home app in Android settings. If the TV offers no default Home setting, use **Control Center → Make NookScreen Your Home Screen** to enable Home Guard.
3. On Home, press **Up** to focus the status button, then press **OK** to open Control Center. Hold **OK** on an app tile to move it, hide it, or put it in a folder.

## Desktop scenes

Open **Control Center → Desktop scenes** and duplicate the current layout. Set a wallpaper and favorite inputs for the new scene. Each scene saves its own app order, folders and hidden apps, and the active scene returns after a restart. Press **OK** on an input tile to open that input.

| Example scene | Scene settings |
| :-- | :-- |
| ![Dawn wallpaper with HDMI 1 and an app folder](docs/screenshots/morning.jpg) | ![Scene settings for wallpaper, favorite inputs and renaming](docs/screenshots/scenes.jpg) |

## Themes

Open **Control Center → Themes → Plugins** to install Sanrio or Marvel, or import a theme ZIP. Return to Themes to preview and apply it. The phone setup page also supports uploading, applying and removing theme ZIPs under **More → Plugins**. Each scene remembers its appearance.

To make a custom theme, choose a base pack and wallpaper, then select **Save current appearance**. Open Wallpaper and Personalization, scan the setup code, and upload per-app artwork to your theme. Saved themes can be reused across scenes.

Each illustrated pack has 20 dedicated tiles for popular streaming, music, local playback and system apps. Other apps retain their recognizable artwork inside a matching frame. Uploaded and explicitly selected artwork takes priority.

| Sanrio · Strawberry Picnic | Marvel · Comic Universe |
| :-- | :-- |
| ![Strawberry picnic wallpaper with pastel app tiles](docs/screenshots/sanrio-theme.jpg) | ![Midnight comic city wallpaper with red and gold app tiles](docs/screenshots/marvel-theme.jpg) |

[Theme coverage and artwork notes](docs/themes.md)

## Updates

Open **Control Center → NookScreen** to check for releases. On Android 12 and later, enable **Automatic updates** and allow NookScreen to install apps. NookScreen attempts to install a newer signed build while Home is idle. Use the update panel to install when Android asks for confirmation.

## Development and feedback

Use JDK 17 and Android SDK 36, then run `./gradlew :core:assembleDailyRelease`. Build theme ZIPs independently with `./gradlew :plugins:themes:assemble`. The Core, plugin API, themes and Clock example live in `core/`, `plugin-api/`, `plugins/themes/` and `plugins/clock/`. See the [plugin guide](docs/PLUGINS.md) and [release guide](docs/RELEASE.md). When opening an issue, include the TV model, Android version, and steps to reproduce.

Released under the [MIT license](LICENSE).
