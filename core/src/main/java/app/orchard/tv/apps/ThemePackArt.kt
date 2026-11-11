package app.orchard.tv.apps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import app.orchard.tv.plugins.InstalledTheme
import app.orchard.tv.plugins.ThemePlugins
import app.orchard.tv.settings.ThemePack
import app.orchard.tv.wallpaper.Images

object ThemePackArt {
    private val frames = LruCache<Pair<String, Long>, Bitmap>(4)

    fun hasTile(context: Context, pack: ThemePack, pkg: String) =
        ThemePlugins.of(context).active(pack.key)?.manifest?.tiles?.containsKey(pkg) == true

    fun tile(context: Context, pack: ThemePack, pkg: String): BitmapDrawable? {
        val theme = ThemePlugins.of(context).active(pack.key) ?: return null
        val file = theme.manifest.tiles[pkg] ?: return null
        return decode(theme, file, TileArtFactory.W * 2, TileArtFactory.W)?.let { BitmapDrawable(context.resources, it) }
    }

    fun wallpaper(context: Context, pack: ThemePack, width: Int, height: Int): Bitmap? {
        val theme = ThemePlugins.of(context).active(pack.key) ?: return null
        val image = if (width <= 384 && height <= 216) theme.manifest.preview else theme.manifest.wallpaper
        val source = decode(theme, image, width, height) ?: return null
        return Images.cover(source, width, height).also { if (it !== source) source.recycle() }
    }

    fun frame(context: Context, pack: ThemePack, art: TileArt): TileArt {
        val theme = ThemePlugins.of(context).active(pack.key) ?: return art
        val file = theme.manifest.frame ?: return art
        val key = pack.key to theme.stamp
        val background = frames.get(key) ?: decode(theme, file, TileArtFactory.W * 2, TileArtFactory.W)
            ?.also { frames.put(key, it) } ?: return art
        return TileArtFactory.withThemeFrame(art, background, theme.manifest.frameTrim)
    }

    private fun decode(theme: InstalledTheme, file: String, width: Int, height: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        theme.read(file) { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth !in 1..4096 || bounds.outHeight !in 1..4096) return null
        val options = BitmapFactory.Options()
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= width && bounds.outHeight / (sample * 2) >= height) sample *= 2
        options.inSampleSize = sample
        return theme.read(file) { BitmapFactory.decodeStream(it, null, options) }
    }
}
