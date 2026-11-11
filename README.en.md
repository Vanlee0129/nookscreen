<div align="center">
  <img src="docs/brand/icon.png" width="96" height="96" alt="NookScreen icon">
  <h1>NookScreen</h1>
  <p><strong>A home screen of your own.</strong></p>
  <p>NookScreen is an open-source Android TV launcher. Arrange your apps, HDMI inputs and wallpaper with the remote, and keep a layout for everyday viewing, movies and games.</p>
  <p>
    <a href="https://vanlee0129.github.io/nookscreen/en/">Website</a> |
    <a href="https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk"><strong>Download APK</strong></a> |
    <a href="#install">Installation</a> |
    <a href="#what-you-can-do">Features and controls</a> |
    <a href="#themes-and-plugins">Themes</a> |
    <a href="https://github.com/Vanlee0129/nookscreen/releases/latest">Release notes</a> |
    <a href="README.md">简体中文</a>
  </p>
  <img src="docs/screenshots/sanrio-home.jpg" width="960" alt="NookScreen on a Xiaomi TV with the Sanrio Strawberry Picnic wallpaper and themed tiles">
</div>

## What you can do

| Task | Where to start |
| --- | --- |
| Arrange your apps | Hold **OK** on a tile to move or hide an app, or put it in a folder. Restore hidden apps in personalization settings. |
| Switch between layouts | Duplicate a scene under **Control Center → Desktop scenes**. Keep streaming apps together in one scene and HDMI and games in another. |
| Put HDMI on Home | Choose supported inputs under **Favorite inputs** for the current scene. Highlight a tile and press **OK** to switch inputs. |
| Change the wallpaper, clock or layout | Under **Control Center → Themes → Wallpaper and personalization**, choose a wallpaper, one or two rows, tiles per row, and the clock’s size and position. |
| Set up Home from your phone | Connect the TV and phone to the same local network and scan the code under **Wallpaper and personalization**. Upload photos and videos, change app artwork, drag to reorder tiles or choose a weather city. |
| Set a screensaver | Under **Wallpaper and personalization → Screensaver**, choose a clock, photos, aerial footage or [your own video](docs/SCREENSAVER_VIDEOS.md), then set the idle time. |
| Set up the TV for children or older adults | Open **Kids and Elders** in personalization settings and choose which apps to show. Kids Mode also supports daily time limits and scheduled breaks. |
| Check the picture, remote or network | Open **Control Center → TV self-check**. Inspect the picture with solid colors, grayscale and edge markers, see which buttons reach NookScreen, or scan a code to check the local connection. The phone page lets you copy a diagnostic report under **Report an Issue**. |

Each scene keeps its own app order, folders, hidden apps, favorite inputs and appearance. Switching scenes switches those settings together. NookScreen restores the last active scene after a restart.

### Remote controls

| Button | What it does |
| --- | --- |
| **Up**, then **OK** on Home | Focus the status button in the top right, then open Control Center. |
| Hold **OK** on a tile | Open the tile menu to arrange apps or remove a pinned input. |
| **Back** on the first row | Start the screensaver immediately if the shortcut is enabled. |

### Screenshots

These screens were captured on a Xiaomi TV. Control Center has separate entries for Themes and Plugins.

| Control Center | Themes |
| --- | --- |
| ![Control Center over the Sanrio wallpaper with Themes, Plugins and Desktop scenes entries](docs/screenshots/control-center.jpg) | ![Themes page with Classic, Sanrio, Marvel, and Wallpaper and Personalization](docs/screenshots/theme-menu.jpg) |

| Sanrio scene | Scene settings |
| --- | --- |
| ![HDMI and app tiles in a Sanrio-themed scene](docs/screenshots/sanrio-scene.jpg) | ![Scene settings for wallpaper, favorite inputs and renaming](docs/screenshots/scenes.jpg) |

### A layout for your console

1. Open **Control Center → Desktop scenes**, duplicate the current scene and name the copy “Games.”
2. In the new scene’s **Favorite inputs**, select the HDMI port your console uses. Return to Home and hold **OK** on the tile to move it where you want it.
3. Pick your usual layout under **Desktop scenes** when you’re done playing. The Games scene keeps its HDMI tile and app order.

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

Check that the TV can install Android APKs, then look up its model. Default Home, wake-from-standby and HDMI behavior vary by firmware. The device notes include earlier upstream Aerial tests, so check which firmware and app version each note covers.

| Situation | Where to look |
| --- | --- |
| Home or Home Guard behaves differently on Xiaomi or Redmi | Match the device and firmware to the [Xiaomi Android 6](docs/devices/xiaomi-android6.md), [MFTP0](docs/devices/xiaomi-mitv-mftp0.md) or [MFTR0](docs/devices/xiaomi-mitv-mftr0.md) notes. |
| Default Home or HDMI is missing on TCL or FFALCON | See [TCL MT5879](docs/devices/tcl-mt5879-cn.md), [MT9655](docs/devices/tcl-mt9655-cn.md) or the [other device notes](docs/devices/). Capabilities vary between firmware versions. |
| Restore the original launcher | Change the default Home app in Android settings. If you enabled Home Guard, turn it off in Accessibility settings, then press Home to check. |

For v0.0.6, Sanrio and Marvel theme updates were tested on a Xiaomi MiTV-MFTR0 using the separate Preview build. Look up other models for earlier tests and known limitations. [v0.0.6 release notes](https://github.com/Vanlee0129/nookscreen/releases/tag/v0.0.6) · [All device notes](docs/devices/)

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

### Common questions

- **Why does Home still open the original launcher after installation?** Open NookScreen from the TV’s app list. To change what Home opens, set the default Home app in Android settings or follow **Control Center → Make NookScreen Your Home Screen** to enable Home Guard. The available options depend on your TV firmware.
- **Why doesn’t the setup page open after scanning the code?** Check that the phone and TV are on the same local network and the TV is on. You can also enter the address shown on the TV in a computer browser on that network.
- **Why do some apps keep their original artwork after I switch themes?** A theme supplies artwork for the apps listed in its coverage. Other apps get the theme’s frame around their usual icon. Artwork you choose manually takes priority. [Theme coverage](docs/themes.md)

## Documentation and feedback

| I want to… | Read |
| --- | --- |
| Check default Home and HDMI on my TV | [Device notes](docs/devices/) · [Where to enable ADB by brand](docs/devices/adb-by-brand.md) |
| Check which apps a theme covers or build a theme | [Theme coverage](docs/themes.md) · [Plugin guide](docs/PLUGINS.md) |
| Use my own video as a screensaver | [Video screensavers](docs/SCREENSAVER_VIDEOS.md) |
| Understand the code, test on a TV or publish a release | [Architecture](docs/ARCHITECTURE.md) · [Device checks](docs/HANDOFF.md) · [Signing and release guide](docs/RELEASE.md) |

Include your TV model, Android and NookScreen versions, and steps to reproduce the issue. If a theme or plugin is involved, include its name and version. [Report a bug](https://github.com/Vanlee0129/nookscreen/issues/new?template=bug.yml) · [Ask for device support](https://github.com/Vanlee0129/nookscreen/issues/new?template=device.yml) · [Share test results](https://github.com/Vanlee0129/nookscreen/issues/new?template=works.yml) · [Suggest a feature](https://github.com/Vanlee0129/nookscreen/issues/new?template=feature.yml)

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

NookScreen is based on [Aerial](https://github.com/pengchujin/aerial-tv) and released under the [MIT license](LICENSE).
