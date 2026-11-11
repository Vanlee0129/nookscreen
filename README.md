# 栖屏 NookScreen

**用遥控器布置你的电视首页。**

栖屏是基于 [Aerial](https://github.com/pengchujin/aerial-tv) 的开源 Android TV 桌面。把常用应用、信号源和壁纸放在同一个首页，按观影、游戏或日常使用切换。

[下载日用版 APK](https://github.com/Vanlee0129/nookscreen/releases/latest/download/nookscreen-daily.apk) · [版本记录](https://github.com/Vanlee0129/nookscreen/releases) · [反馈问题](https://github.com/Vanlee0129/nookscreen/issues) · [English](README.en.md)

![小米电视真机截图。夜色壁纸上显示 HDMI 2、天气及两排应用磁贴](docs/screenshots/night.jpg)

## 安装

1. 下载 `nookscreen-daily.apk`，传到 Android TV 并安装。正式包名为 `app.nookscreen.tv`。
2. 打开栖屏，在电视的系统设置中设为默认桌面。没有默认桌面选项的电视，可在栖屏「控制中心 → 设为主屏幕」按提示开启桌面守护。
3. 回到首页，按 **↑** 聚焦右上角状态按钮，再按 **确定** 打开控制中心。长按应用磁贴可以移动、隐藏或放进文件夹。

## 桌面场景

从「控制中心 → 桌面场景」复制当前桌面，再为新场景挑选壁纸和常用信号源。应用顺序、文件夹和隐藏项随场景保存，重启后恢复上次使用的场景。常用信号源会成为桌面磁贴，按 **确定** 才会打开对应输入。

| 场景示例 | 场景编辑 |
| :-- | :-- |
| ![黎明壁纸、HDMI 1 和应用文件夹](docs/screenshots/morning.jpg) | ![场景编辑菜单，可设置壁纸、常用信号源及名称](docs/screenshots/scenes.jpg) |

## 图标与壁纸

内置常用应用的 16:9 高清磁贴。打开「个性化」并用手机扫描设置二维码，可以上传壁纸、视频和自定义图标。上传的图标优先显示。

![九张高清应用磁贴，包含哔哩哔哩、小米视频、Netflix、YouTube 和 Spotify](docs/screenshots/hd-tiles.png)

## 更新

在「控制中心 → 栖屏 NookScreen」检查更新。Android 12 及以上可开启「自动更新」，授权栖屏安装应用后，桌面空闲时会尝试安装已发布的同签名版本。系统要求确认时，可在更新页面手动安装。

## 开发与反馈

使用 JDK 17 和 Android SDK 36，运行 `./gradlew :app:assembleDailyRelease` 构建日用版。详见[发布说明](docs/RELEASE.md)。反馈时请附上电视型号、系统版本和复现步骤。

项目采用 [MIT 许可证](LICENSE)。
