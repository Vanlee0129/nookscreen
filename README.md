# 栖屏 NookScreen

**用遥控器布置你的电视首页。** 栖屏是面向 Android TV 的开源桌面。把常用应用、HDMI 信号源和壁纸摆到一个首页，再给日常、观影或游戏各留一套布局。

[下载日用 APK](https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk) · [版本记录](https://github.com/Vanlee0129/nookscreen/releases) · [反馈问题](https://github.com/Vanlee0129/nookscreen/issues) · [English](README.en.md)

![小米电视实机上的栖屏首页，显示 HDMI 2、天气和两排应用磁贴](docs/screenshots/night.jpg)

## 从安装到打开控制中心

1. 下载 `nookscreen-daily.apk`，传到电视并安装。日用版支持 Android 5.0 及以上，包名为 `app.nookscreen.tv`。
2. 在电视上打开栖屏。在系统设置中选它作为默认桌面。电视没有这个选项时，进入「控制中心 → 设为主屏幕」，按提示开启桌面守护。
3. 在首页按 **↑** 聚焦右上角的状态按钮，再按 **确定** 进入控制中心。长按应用磁贴的 **确定** 键可打开操作菜单。

不同厂商固件开放的桌面设置和信号源能力有差异。已记录的行为和设置路径见[电视适配记录](docs/devices/)。

## 布置你的首页

| 想做什么 | 从哪里开始 |
| --- | --- |
| 整理应用 | 长按磁贴可移动、隐藏或放进文件夹。 |
| 给观影和游戏用不同布局 | 打开「控制中心 → 桌面场景」，选「复制当前场景」，再编辑新场景。 |
| 把 HDMI 放到首页 | 在场景编辑中选「常用信号源」。支持的输入会成为磁贴，按 **确定** 切换。 |
| 用手机上传照片或换磁贴图案 | 让手机和电视连同一局域网，再到「控制中心 → 主题 → 壁纸与个性化」扫描设置二维码。 |

每个场景分别保存应用顺序、文件夹、隐藏项、常用信号源和外观。重启后会回到上次使用的场景。

| 场景示例 | 场景编辑 |
| --- | --- |
| ![黎明壁纸、HDMI 1 和应用文件夹](docs/screenshots/morning.jpg) | ![场景编辑菜单包含壁纸、常用信号源及场景名称](docs/screenshots/scenes.jpg) |

## 插件

Core 负责桌面、遥控器导航、场景和更新。主题是插件的一种，今后桌面核心之外的新功能和内容通过插件交付。插件有自己的版本，可以独立安装、更新、停用和移除。新主题发布后会自动出现在主题目录里。控制中心的「主题」和「插件」是并列入口，「插件」用于管理功能插件。

### 安装和切换主题

在电视打开「控制中心 → 主题」。选一套主题开始下载，安装后再选它并按「应用」。同一个菜单里可以检查更新、停用或移除主题。也可以在手机设置页的「壁纸与屏保 → 主题」下安装并管理主题。

v0.0.3 的电视入口是「控制中心 → 主题 → 插件」。从 v0.0.2 升级后，安装原主题插件即可恢复场景里保留的主题引用。

| 三丽鸥 · 草莓野餐 | 漫威 · 漫画宇宙 |
| --- | --- |
| ![草莓野餐壁纸和粉彩应用磁贴](docs/screenshots/sanrio-theme.jpg) | ![漫画城市壁纸和红金应用磁贴](docs/screenshots/marvel-theme.jpg) |

两套主题各有 20 张专属磁贴，其他应用会沿用原图案并搭配主题边框。你上传或指定的图标优先。停用或移除主题时，桌面使用经典磁贴，缺少的主题壁纸会回退到内置极光，用户照片、图标和场景继续保留。[磁贴覆盖清单与素材说明](docs/themes.md)

### 保存自己的外观

先选好底包和壁纸，再到主题页选「保存当前外观为主题」。在这个自定义主题生效时扫描设置二维码，从手机设置页为各个应用上传图标。同一套自定义主题可以应用到多个场景。

## 更新和反馈

- **更新桌面**　打开「控制中心 → 栖屏 NookScreen」，选「检查更新」。Android 12 及以上支持在电视空闲时自动更新，需授权栖屏安装应用。同包名、同签名的覆盖更新会保留桌面数据。
- **更新主题**　从主题的操作菜单检查更新。主题下载失败时，可先确认电视能访问 GitHub Releases，再试一次。
- **反馈问题**　请附上电视型号、Android 版本、栖屏版本和复现步骤。若问题与插件有关，也请写上插件名称和版本。[提交问题](https://github.com/Vanlee0129/nookscreen/issues)

## 本地开发

使用 **JDK 17** 和 **Android SDK 36**。在仓库根目录运行以下命令。

```sh
./gradlew :core:testDailyDebugUnitTest :core:assemblePreviewRelease
./gradlew :core:assembleDailyRelease
./gradlew :plugins:themes:assemble
```

Preview 使用独立包名 `app.nookscreen.tv.preview`，适合和日用版并装做真机测试。日用 APK 在 `core/build/outputs/apk/daily/release/`，主题发布产物在 `plugins/themes/build/distributions/`。[签名和发布步骤](docs/RELEASE.md)

| 目录 | 内容 |
| --- | --- |
| [`core/`](core/) | Android TV 桌面、场景、插件管理和手机设置页 |
| [`plugin-api/`](plugin-api/) | 插件兼容版本和 Android 功能入口 |
| [`plugins/themes/`](plugins/themes/) | 主题清单、图片资源和独立打包任务 |
| [`plugins/clock/`](plugins/clock/) | 独立 Android 功能插件示例 |
| [`lite/`](lite/) | 面向 Android 4.x 的 Aerial Lite |

提交改动前，先看[架构说明](docs/ARCHITECTURE.md)和[插件开发指南](docs/PLUGINS.md)。栖屏基于 [Aerial](https://github.com/pengchujin/aerial-tv)，使用 [MIT 许可证](LICENSE)。
