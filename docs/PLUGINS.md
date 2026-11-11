# 栖屏插件

DeepSeek Harness Desktop 让桌面壳和 Web 共用插件管理器，将运行时与用户已安装的插件分开保存。主题服务通过独立扩展点发布主题快照，缺少覆盖项时使用默认图标。栖屏沿用集中管理、声明式资源和默认回退的思路。渲染、遥控器焦点、桌面场景、用户文件、系统权限及 OTA 都由 Core 管理。 [Desktop 实现](https://github.com/deepseek-ai/deepseek-harness/tree/master/apps/desktop) · [主题服务](https://github.com/deepseek-ai/deepseek-harness/tree/master/packages/client/ui-theme)

| 目录 | 职责 | 产物 |
| --- | --- | --- |
| `core/` | Android TV 桌面、插件管理、内建经典外观、手机设置页和 Core OTA | 日用 APK、隔离 Preview APK |
| `plugin-api/` | Core 与插件共用的 API 版本和 Android 功能入口约定 | Java 库 |
| `plugins/themes/` | 三丽鸥和漫威主题、各自的清单和打包任务 | 每套主题一个 ZIP |
| `plugins/clock/` | 独立进程运行的时钟功能示例 | Android APK |
| `lite/` | Android 4.x 桌面 | Lite APK |

## 产品边界

Core 负责桌面运行、遥控器导航、桌面场景、用户数据、权限和更新。主题是插件的一种。控制中心将「主题」和「插件」作为并列入口，「插件」页用于管理功能插件。今后核心桌面之外的新功能和内容通过插件交付，插件独立版本化和分发。

## 使用

- 在电视上进入「控制中心 → 主题」，选择主题直接下载。安装后可预览，打开主题菜单可应用、检查更新、启用、停用或移除。
- 手机设置页的「壁纸与屏保 → 主题」提供主题安装和管理。电视负责从栖屏的主题发布渠道取包。
- 主题被停用、移除或与 Core 不兼容时，应用恢复经典磁贴，引用该主题的壁纸改为内置极光。独立选用的照片、用户图标、自定义主题、场景配置均保存在 Core 的原有位置。同 ID 的兼容插件装回并启用后，仍引用它的场景会恢复外观。
- v0.0.2 的 `sanrio`、`marvel` ID、壁纸引用和自定义主题继续可读。首次升级后从主题页安装相应主题。若主动选择其他主题或壁纸，该场景会记住新选择。

## 主题包

发布资产使用扁平 ZIP，由栖屏下载和校验。栖屏会从项目的 GitHub Releases 发现带有对应资产的 `plugin-<id>` 预发布。新主题发布后会出现在目录中，已有主题更新沿用原 ID。根目录包含 UTF-8 `manifest.json` 和 JPEG、PNG 或 WebP 图片。以 [三丽鸥清单](../plugins/themes/sanrio/manifest.json) 为例。

- `schema: 1` 和 `kind: "theme"` 标识文件格式。小写 `id` 是跨发布及场景的稳定标识，`classic` 由 Core 保留。
- `version` 为递增正整数。 `minApi` 和 `maxApi` 为闭区间，Core 当前 API 为 1。插件版本可以增长而不改变 API。
- `name` 是显示名称。 `wallpaper` 必填，`preview` 省略时复用壁纸，`frame` 与 `frameTrim` 定义通用边框，`tiles` 按 Android 包名覆盖磁贴。图标优先顺序仍由 Core 决定。
- 包大小上限 32 MiB，单张图片 8 MiB，单边至多 4096 像素，最多 256 个条目。Core 验证临时包后才原子替换已安装 ZIP。相同或更低版本、错误 ID、不兼容或无效的包均保留已安装版本。用户可以显式移除后再安装旧版。
- 主题读取只涉及归档内的已声明图片。插件无 Android 权限，无法访问 Core 的应用数据或执行代码。

## Android 功能插件

功能插件是普通 Android APK，具有独立的 UID、签名、版本及权限。用户通过 Android 安装或卸载 APK，然后在栖屏「插件」中启用或停用。栖屏通过发现已安装 APK 的入口完成接入。管理菜单提供 Android 应用信息入口，供用户卸载或查看权限。

1. 声明导出的 Activity，添加 `app.nookscreen.plugin.OPEN_FEATURE` 和 `android.intent.category.DEFAULT`。
2. 在 Activity 上提供整数元数据 `app.nookscreen.plugin.MIN_API`、`app.nookscreen.plugin.MAX_API`。可参考 [时钟示例清单](../plugins/clock/src/main/AndroidManifest.xml)。
3. 在插件页启用，再用遥控器选择「打开」。Core 校验 API 以及启用时记录的组件和签名，然后发起只指定组件与操作的 Intent。没有参数、文件 URI 或权限委托。插件停用后 Core 不再打开它。返回键回到栖屏。
4. APK 更新通过 Android 软件包安装机制。保持插件签名和入口，启用状态可随更新保留。缺少兼容元数据、入口失效或签名变化时，插件无法从 Core 打开，桌面仍可使用。开发示例只显示本地时间，无额外 Android 权限。

Android 进程和应用数据权限由系统提供隔离。[Android 应用沙箱与代码加载](https://developer.android.com/privacy-and-security/security-tips)

构建产物、更新地址和本地验收见 [发布说明](RELEASE.md)。
