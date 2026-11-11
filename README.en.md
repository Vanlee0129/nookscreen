<div align="center">
  <img src="docs/brand/icon.png" width="96" height="96" alt="NookScreen icon">
  <h1>NookScreen</h1>
  <p><strong>A home screen of your own.</strong></p>
  <p>NookScreen is an open-source Android TV launcher. Arrange your apps, HDMI inputs and wallpaper with the remote, and keep a layout for everyday viewing, movies and games.</p>
  <p>
    <a href="https://vanlee0129.github.io/nookscreen/en/">Website</a> |
    <a href="https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk"><strong>Download APK</strong></a> |
    <a href="#install">Installation</a> |
    <a href="https://github.com/Vanlee0129/nookscreen/releases/latest">Release notes</a> |
    <a href="README.md">简体中文</a>
  </p>
  <img src="docs/screenshots/sanrio-home.jpg" width="960" alt="NookScreen on a Xiaomi TV with the Sanrio Strawberry Picnic wallpaper and themed tiles">
</div>

## What you can do

- **Keep your favorites close.** Browse with the directional keys. Hold **OK** to move or hide an app, or group apps in a folder. Pin supported HDMI and other inputs alongside apps.
- **Give each activity its own layout.** Duplicate a scene under **Control Center → Desktop scenes**. Each scene saves its app order, folders, hidden apps, favorite inputs and appearance. The last scene returns after a restart.
- **Customize from your phone.** Put your phone and TV on the same local network, then open the setup code under **Control Center → Themes → Wallpaper and Personalization**. Upload photos, change app artwork or arrange your home screen.
- **Use the idle screen.** Set a clock, photo or aerial screensaver under personalization. You can also add [your own screensaver videos](docs/SCREENSAVER_VIDEOS.md).

| Sanrio scene | Scene settings |
| --- | --- |
| ![HDMI and app tiles in a Sanrio-themed scene](docs/screenshots/sanrio-scene.jpg) | ![Scene settings for wallpaper, favorite inputs and renaming](docs/screenshots/scenes.jpg) |

## Install

NookScreen supports **Android 5.0 and later**. The stable release uses package ID `app.nookscreen.tv` and requires no root access. For Android 4.x, see the separate [Aerial Lite](docs/LITE.md).

1. [Download `nookscreen-daily.apk`](https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk), copy it to a USB drive, then open it in the TV’s file manager. Allow installation from that source if your TV asks.
2. Open NookScreen from the TV’s app list. On Home, press **Up** to focus the status button in the top right, then press **OK** to open Control Center.
3. Select NookScreen as the default Home app in Android settings and press **Home** to check. If the setting is missing or Home still opens the manufacturer’s launcher, choose **Control Center → Make NookScreen Your Home Screen** and follow the prompts to enable Home Guard.

### If you cannot install from USB

Connect the TV and computer to the same network. Enable debugging using the [device-specific ADB notes](docs/devices/adb-by-brand.md) and run these commands from your computer. Replace the sample IP address and port with the ones shown on your TV. If your TV only offers wireless debugging, follow the pairing instructions first.

```sh
adb connect 192.168.1.20:5555
adb install -r nookscreen-daily.apk
```

Open NookScreen from the app list. If you still cannot set it as the default, you can launch it from there or consult the [device notes](docs/devices/).

### Compatibility and restoring the original launcher

| Situation | Where to look |
| --- | --- |
| Home or Home Guard behaves differently on Xiaomi or Redmi | Match the device and firmware to the [Xiaomi Android 6](docs/devices/xiaomi-android6.md), [MFTP0](docs/devices/xiaomi-mitv-mftp0.md) or [MFTR0](docs/devices/xiaomi-mitv-mftr0.md) notes. |
| Default Home or HDMI is missing on TCL or FFALCON | See [TCL MT5879](docs/devices/tcl-mt5879-cn.md), [MT9655](docs/devices/tcl-mt9655-cn.md) or the [other device notes](docs/devices/). Capabilities vary between firmware versions. |
| Restore the original launcher | Change the default Home app in Android settings. If you enabled Home Guard, turn it off in Accessibility settings, then press Home to check. |

## Themes and plugins

Core provides the launcher, remote navigation, scenes and updates. Plugins deliver features and content outside the core launcher. **Themes are a type of plugin** with their own installation, update, disable and removal controls. Control Center gives **Themes** its own entry and uses **Plugins** for feature plugins.

On the TV, open **Control Center → Themes** and select a theme to download it. When the download finishes, select it again and choose **Apply**. On the phone setup page, use **Wallpaper & Screensaver → Themes**.

| Sanrio · Strawberry Picnic | Marvel · Comic Universe |
| --- | --- |
| ![Strawberry picnic wallpaper with pastel tiles](docs/screenshots/sanrio-theme.jpg) | ![Comic city wallpaper with red-and-gold tiles](docs/screenshots/marvel-theme.jpg) |

Each pack has 25 dedicated tiles, including XiaoAI, Calendar, App Center, Wireless Cast and Mi Home. Other apps keep their artwork with a matching frame. Artwork you upload or explicitly select takes priority. [Theme coverage and artwork notes](docs/themes.md)

To reuse a look, choose **Save current appearance** on the Themes page. Use the phone setup page to change app artwork while the custom theme is active. You can use that theme in multiple scenes.

## Updates and troubleshooting

| Task | What to do |
| --- | --- |
| Update NookScreen | Go to **Control Center → NookScreen → Check for updates**. An in-place update with the same package ID and signing certificate keeps scenes, photos and custom artwork. On Android 12 and later, NookScreen can update while idle once authorized. |
| Update a theme | Check for updates from the theme’s menu. If the download fails, make sure the TV can reach GitHub Releases and try again. |
| Restore a theme after upgrading | When upgrading from v0.0.2, reinstall the matching theme to restore the references saved in your scenes. On v0.0.3, theme downloads are under **Control Center → Themes → Plugins**. |

## Development and contributions

Use **JDK 17** and **Android SDK 36**. From the repository root:

```sh
./gradlew :core:testDailyDebugUnitTest :core:assemblePreviewRelease
./gradlew :core:assembleDailyRelease
./gradlew :plugins:themes:assemble
```

| Directory | Contents |
| --- | --- |
| [`core/`](core/) | Launcher, scenes, Control Center and phone setup |
| [`plugin-api/`](plugin-api/) | Compatibility version and Android extension entry point |
| [`plugins/`](plugins/) | Theme assets, packaging tasks and a clock feature plugin example |
| [`website/`](website/) | Chinese and English website source for GitHub Pages |

Preview uses package ID `app.nookscreen.tv.preview` for side-by-side testing. The stable APK is under `core/build/outputs/apk/daily/release/` and theme packages are under `plugins/themes/build/distributions/`. See the [architecture notes](docs/ARCHITECTURE.md), [plugin guide](docs/PLUGINS.md) and [signing and release guide](docs/RELEASE.md).

To preview the website, run `bash tools/build-website.sh <output-directory>` and serve that directory over HTTP. GitHub Pages publishes the generated files from the root of the `gh-pages` branch.

[Report a problem or share device notes](https://github.com/Vanlee0129/nookscreen/issues/new/choose). Include your TV model, Android and NookScreen versions, and steps to reproduce. For a theme or plugin issue, include its name and version.

NookScreen is based on [Aerial](https://github.com/pengchujin/aerial-tv) and released under the [MIT license](LICENSE).
