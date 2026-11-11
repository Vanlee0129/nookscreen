# 栖屏主题包

每套插画主题包含一张电视壁纸和 20 张专属磁贴。其余应用会保留原有应用图案并使用主题边框。用户上传和明确选定的图标优先。每个桌面场景记住它应用的主题和壁纸。

| 类型 | 专属磁贴 |
| --- | --- |
| 国内影视 | 云视听极光、云视听小电视、银河奇异果、酷喵、芒果 TV |
| 海外影视 | Netflix、YouTube、Prime Video、Apple TV、Disney+ |
| 音乐和卡拉 OK | Spotify、网易云音乐、全民 K 歌 |
| 本地媒体 | Plex、Kodi、VLC |
| 系统工具 | 天气、相册、设置、电视设置 |

Jellyfin、其他电视厂商的工具和其余已安装应用使用边框适配。应用范围参考 [Android TV 应用展示](https://www.android.com/intl/en_in/tv/)、[当贝电视软件热门榜](https://www.dangbei.com/app/hot/) 以及 [Jellyfin Android TV](https://play.google.com/store/apps/details?id=org.jellyfin.androidtv)。

## 自定义主题

先应用一个底包，然后挑选壁纸。在主题页选择「保存当前外观为主题」并命名，随后从「壁纸与个性化」扫描设置二维码。手机页为应用上传的图片保存到当前自定义主题。切回其他主题后，栖屏继续使用对应主题的图标。

## 素材

壁纸、缩略图、磁贴及边框位于 `app/src/main/assets/themes/`。使用内置 ImageGen 生成插画及图集，将图集裁切成电视磁贴。漫画扩展磁贴复用已有应用标识和新绘制的识别图形，配上同一漫画背景。生成提示词如下。

### 三丽鸥壁纸

```text
Use case illustration-story. Create a finished wide 16:9 wallpaper for a living-room Android TV launcher theme featuring recognizable Sanrio characters Hello Kitty with the red bow, My Melody, Cinnamoroll and Kuromi. A strawberry milk picnic floating on fluffy lavender and blush clouds, hand-painted kawaii sticker illustration with beautiful crisp contour lines and subtle paper grain. Group characters and the strawberry cake in the upper-middle area, substantial clean blush space at the bottom third for the launcher tile row, gentle unobstructed pale corners. Cream, warm pink, lilac, tiny cherry-red details. Actual art only, edge to edge, no words, no labels, no logos, no mockup frame.
```

### 三丽鸥基础磁贴

```text
Use case illustration-story. A perfectly regular 3-column by 3-row sprite atlas of exactly nine edge-to-edge 16:9 app tile illustrations in one landscape canvas, intended to be cropped on exact thirds. NO gutter, no borders, no margins, no text. Each equal cell has an isolated bold, highly readable central app pictogram framed by hand-drawn Sanrio strawberry-picnic details, with the light pastel pink/lilac/cream treatment of a matching character theme. Exact reading order: row 1: multicolor triangular Tencent Video play symbol held by Hello Kitty; Bilibili smiling TV face with Cinnamoroll ears; pink karaoke microphone with My Melody bow. Row 2: red music note with small strawberry blossoms; sunny weather cloud with Cinnamoroll accents; camera/photo landscape pictogram surrounded by strawberries. Row 3: large gray gear with tiny Hello Kitty red bow; two horizontal slider switches with Kuromi details; classic HDMI cable connector with tiny strawberry pin. Strong contrast, large main glyph, use recognizably distinct symbols. Crop cells separately, nothing crosses cell boundaries. Professional illustration, not a screenshot, no typography.
```

### 三丽鸥扩展磁贴

```text
Make an exact four-column by three-row raster sprite sheet of 12 equal edge-to-edge cells, no gutters and no borders, wide canvas. The app logos must be accurate recognizable, LARGE, centered. Left-to-right top to bottom: Netflix red N; YouTube red play rectangle; Prime Video blue smile logo with exact text prime video; Apple TV+ Apple logo + tv; Disney+ wordmark; iQIYI white wordmark inside green TV outline; YOUKU pink-blue play symbol; Mango TV white stylized M on orange; Plex yellow chevron with exact text Plex; Kodi blue diamond with white K; VLC orange traffic cone; Spotify green circle with black sound curves. Each is a wholly separate themed TV launcher tile cropped at exact fourths/thirds, clear silhouette, no text except requested brand marks, no arrangement labels or screenshot. Art treatment: Sanrio strawberry picnic, cheerful hand-drawn pastel scene, pale pink and lilac backgrounds, tiny Sanrio character accents, red bow, strawberries, ribbon, pastel details placed around the logos. Crisp high contrast graphic design.
```

### 漫画城市壁纸

```text
Use case illustration-story. Create a finished 16:9 full-bleed smart TV wallpaper for a superhero-comic themed launcher. Original fictional midnight city skyline, with monumental dark skyscraper silhouettes, a bright gold moon, striking scarlet radial lighting over indigo sky, angular graphic novel inkwork, subtle retro halftone grain, art-directed navy red and gold color palette. Stylized architecture, no characters. Compose the center of interest in the upper half and keep the bottom quarter clean and midnight navy for launcher tiles. No text, no logos, no frames, no UI.
```

### 漫画基础磁贴

```text
Use case illustration-story. A perfectly regular 3-column by 3-row sprite atlas of exactly nine edge-to-edge 16:9 app tile illustrations in one landscape canvas, intended to be cropped on exact thirds. NO gutter, no borders, no margins, no text. Each equal cell has an isolated, large, bold readable central app pictogram staged as a distinct superhero comic panel, dark midnight blue, scarlet, gold and steel colors, angular ink lines, editorial halftone and dynamic but restrained rays. Exact reading order: row 1: multicolor triangular Tencent Video play mark with Iron Man armor details; Bilibili smiling TV face with Spider-Man spiderweb accents; karaoke microphone surrounded by red/gold starburst. Row 2: giant red music note with comic energy arcs; sunny weather cloud with gold lightning accents; camera/photo mountain pictogram over stylized city skyline. Row 3: large silver gear with arc-reactor blue center; two unmistakable horizontal slider switches on a red armored control panel; clear HDMI cable connector on a midnight-blue tech panel. Main glyphs bold, preserve distinct app function. Exact thirds, independent backgrounds, no text or publisher logo, no screenshot.
```
