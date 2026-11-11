package app.orchard.tv.apps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import app.orchard.tv.settings.ThemePack
import app.orchard.tv.wallpaper.Images

object ThemePackArt {
    internal val tiles = mapOf(
        "com.ktcp.video" to "video",
        "com.xiaodianshi.tv.yst" to "bilibili",
        "com.tencent.karaoketv" to "karaoke",
        "com.netease.cloudmusic.tv" to "music",
        "com.xiaomi.tweather" to "weather",
        "com.mitv.gallery" to "gallery",
        "com.android.tv.settings" to "settings",
        "com.xiaomi.mitv.settings" to "tv-settings",
        "com.netflix.ninja" to "netflix",
        "com.google.android.youtube.tv" to "youtube",
        "com.amazon.amazonvideo.livingroom" to "prime",
        "com.apple.atve.androidtv.appletv" to "apple-tv",
        "com.disney.disneyplus" to "disney",
        "com.gitvdemo.video" to "iqiyi",
        "com.cibn.tv" to "youku",
        "com.hunantv.license" to "mango",
        "com.plexapp.android" to "plex",
        "org.xbmc.kodi" to "kodi",
        "org.videolan.vlc" to "vlc",
        "com.spotify.tv.android" to "spotify",
    )
    private val frames = java.util.concurrent.ConcurrentHashMap<ThemePack, Bitmap>()

    fun hasTile(pack: ThemePack, pkg: String) = pack != ThemePack.Classic && pkg in tiles

    fun tile(context: Context, pack: ThemePack, pkg: String): BitmapDrawable? {
        if (!hasTile(pack, pkg)) return null
        return decode(context, pack, "${tiles.getValue(pkg)}.jpg")?.let { BitmapDrawable(context.resources, it) }
    }

    fun wallpaper(context: Context, pack: ThemePack, width: Int, height: Int): Bitmap? {
        val image = if (width <= 384 && height <= 216) "preview.jpg" else "wallpaper.jpg"
        val source = decode(context, pack, image) ?: return null
        return Images.cover(source, width, height).also { if (it !== source) source.recycle() }
    }

    fun frame(context: Context, pack: ThemePack, art: TileArt): TileArt {
        if (pack == ThemePack.Classic) return art
        val background = frames[pack] ?: synchronized(frames) {
            frames[pack] ?: decode(context, pack, "frame.jpg")?.also { frames[pack] = it }
        } ?: return art
        val trim = if (pack == ThemePack.Sanrio) 0xFFFFE8F1 else 0xFFF3BD68
        return TileArtFactory.withThemeFrame(art, background, trim.toInt())
    }

    private fun decode(context: Context, pack: ThemePack, file: String): Bitmap? =
        runCatching { context.assets.open("themes/${pack.key}/$file").use(BitmapFactory::decodeStream) }.getOrNull()
}
