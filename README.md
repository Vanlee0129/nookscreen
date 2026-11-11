<div align="center">
  <img src="docs/brand/icon.png" width="96" height="96" alt="栖屏图标">
  <h1>栖屏 NookScreen</h1>
  <p><strong>给你的电视，换个首页。</strong></p>
  <p>栖屏是开源 Android TV 桌面。用遥控器把常用应用、HDMI 和壁纸放到一起，给日常、观影和游戏各留一套布局。</p>
  <p>
    <a href="https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk"><strong>下载日用 APK</strong></a> |
    <a href="#安装">安装指南</a> |
    <a href="https://github.com/Vanlee0129/nookscreen/releases/latest">版本记录</a> |
    <a href="README.en.md">English</a>
  </p>
  <img src="docs/screenshots/night.jpg" width="960" alt="小米电视上的栖屏首页，夜色壁纸前有 HDMI 2、天气和两排应用磁贴">
</div>

## 电视首页能做什么

- **把常用应用放在手边。** 用方向键浏览磁贴，长按 **确定** 可移动、隐藏应用或整理成文件夹。支持的 HDMI 和其他信号源也可以固定到首页。
- **给不同用途留一套场景。** 在「控制中心 → 桌面场景」复制当前场景，给观影、游戏或家人各留一套。应用顺序、文件夹、隐藏项、常用信号源和外观分别保存，重启后回到上次使用的场景。
- **用手机换壁纸和图标。** 手机和电视连同一局域网，在「控制中心 → 主题 → 壁纸与个性化」打开设置二维码。扫码后可上传照片、换应用图标或整理首页。
- **让电视空闲时显示照片或时钟。** 在个性化里选择时钟、照片或航拍等屏保，也可添加[自己的屏保视频](docs/SCREENSAVER_VIDEOS.md)。

| 日常首页 | 场景设置 |
| --- | --- |
| ![黎明壁纸下的 HDMI 1 和应用文件夹](docs/screenshots/morning.jpg) | ![场景设置中的壁纸、常用信号源与重命名](docs/screenshots/scenes.jpg) |

## 安装

日用版支持 **Android 5.0 及以上**，包名为 `app.nookscreen.tv`。安装无需 root。使用 Android 4.x 的设备可参考独立的 [Aerial Lite](docs/LITE.md)。

1. [下载 `nookscreen-daily.apk`](https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk)，拷到 U 盘，用电视的文件管理器打开安装。如果系统提示，按电视的设置允许该安装来源。
2. 从电视的应用列表打开栖屏。在首页按 **↑** 聚焦右上角状态按钮，再按 **确定** 打开控制中心。
3. 在系统设置中选栖屏作为默认桌面，按遥控器的 **主页键** 验证。系统没有默认桌面入口，或主页键仍返回厂商桌面时，打开「控制中心 → 设为主屏幕」，按电视提示开启桌面守护。

### U 盘装不了时

电脑和电视连同一网络，按[各品牌开启 ADB 的说明](docs/devices/adb-by-brand.md)操作，在电脑上运行以下命令。把示例 IP 和端口换成电视实际显示的值。只有无线调试配对入口的设备要先按说明配对。

```sh
adb connect 192.168.1.20:5555
adb install -r nookscreen-daily.apk
```

安装后从应用列表打开栖屏。若仍无法设为默认桌面，可从应用列表启动使用，或查阅对应的[机型适配记录](docs/devices/)。

### 兼容性与恢复

| 遇到的情况 | 从哪里查 |
| --- | --- |
| 小米 / Redmi 的主页键或桌面守护行为与预期不同 | 按系统版本和产品型号查[小米 Android 6](docs/devices/xiaomi-android6.md)、[小米 MFTP0](docs/devices/xiaomi-mitv-mftp0.md)或[小米 MFTR0](docs/devices/xiaomi-mitv-mftr0.md)记录。 |
| TCL / 雷鸟没有默认桌面设置，或找不到 HDMI | 对照 [TCL MT5879](docs/devices/tcl-mt5879-cn.md)、[MT9655](docs/devices/tcl-mt9655-cn.md)和[其他机型记录](docs/devices/)。同品牌不同固件的能力可能不同。 |
| 想恢复电视原桌面 | 在系统设置中改回默认桌面。若开启过「桌面守护」，在无障碍设置中关闭栖屏的守护服务，再按主页键确认。 |

## 主题与插件

Core 提供首页、遥控器导航、场景和更新。桌面之外的功能和内容通过插件交付。**主题是插件的一种**，可以独立安装、更新、停用和移除。在控制中心，「主题」是一级入口，「插件」用于管理功能插件。

在电视上打开 **控制中心 → 主题**，选中主题开始下载。安装完成后，再选中它并按「应用」。手机设置页的入口是「壁纸与屏保 → 主题」。

| 三丽鸥 · 草莓野餐 | 漫威 · 漫画宇宙 |
| --- | --- |
| ![三丽鸥主题的草莓野餐壁纸与粉彩磁贴](docs/screenshots/sanrio-theme.jpg) | ![漫威主题的漫画城市壁纸与红金磁贴](docs/screenshots/marvel-theme.jpg) |

两套主题各有 25 张专属磁贴，包括小爱同学、日历、应用中心、无线投屏和米家。其他应用沿用原图案并搭配主题边框，你上传或指定的图标优先。[磁贴覆盖清单和素材说明](docs/themes.md)

想保存自己的外观，可以在主题页选择「保存当前外观为主题」，再用手机设置页换图标。同一套自定义主题可用于多个场景。

## 更新与排查

| 要做什么 | 操作 |
| --- | --- |
| 更新栖屏 | 在「控制中心 → 栖屏 NookScreen」选择「检查更新」。同包名、同签名的覆盖升级会保留场景、照片和自定义图标。Android 12 及以上可在授权后于空闲时自动更新。 |
| 更新主题 | 在主题的操作菜单选择「检查更新」。下载失败时，确认电视能访问 GitHub Releases 后重试。 |
| 从旧版本恢复主题 | 从 v0.0.2 升级后，重新安装原主题即可恢复保留在场景里的引用。v0.0.3 的主题下载入口是「控制中心 → 主题 → 插件」。 |

## 开发与贡献

需要 **JDK 17** 和 **Android SDK 36**。在仓库根目录运行：

```sh
./gradlew :core:testDailyDebugUnitTest :core:assemblePreviewRelease
./gradlew :core:assembleDailyRelease
./gradlew :plugins:themes:assemble
```

| 目录 | 内容 |
| --- | --- |
| [`core/`](core/) | 桌面、场景、控制中心和手机设置页 |
| [`plugin-api/`](plugin-api/) | 插件兼容版本和 Android 扩展入口 |
| [`plugins/`](plugins/) | 主题资源、独立打包任务和时钟功能插件示例 |
| [`website/`](website/) | GitHub Pages 的中英文官网源码 |

Preview 包名为 `app.nookscreen.tv.preview`，可与日用版并装验收。日用 APK 在 `core/build/outputs/apk/daily/release/`，主题包在 `plugins/themes/build/distributions/`。更多细节见[架构](docs/ARCHITECTURE.md)、[插件开发](docs/PLUGINS.md)和[签名与发布](docs/RELEASE.md)。

欢迎[报告问题或补充机型记录](https://github.com/Vanlee0129/nookscreen/issues/new/choose)。请附上电视型号、Android 和栖屏版本及复现步骤。与主题或插件相关时，也请注明名称和版本。

栖屏基于 [Aerial](https://github.com/pengchujin/aerial-tv)，使用 [MIT 许可证](LICENSE)。
