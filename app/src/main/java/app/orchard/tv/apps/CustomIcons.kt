package app.orchard.tv.apps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import app.orchard.tv.wallpaper.Images
import java.io.File
import java.io.InputStream

/**
 * Pictures the owner chose for app tiles, sent from the phone page. Each is cropped to the tile's
 * 16:9 and kept by package name in files/icons; a tile with one draws it instead of the app's own
 * artwork (AppCatalog). Only the owner's own uploads live here, and removing one only ever deletes
 * that app's file.
 */
object CustomIcons {
    /** 1280×720: sharp on a 4K UI's 704 px tile, small enough to decode on every app reload. */
    private const val W = 1280
    private const val H = 720

    /** Bumped on every change, so Home reloads its tiles. */
    var version by mutableIntStateOf(0); private set

    /**
     * Told of every change on the main thread, even while Home's Compose window is stopped (an
     * upload from the phone, PR #997). A plain callback: this file is shared with Lite, which has
     * no coroutines.
     */
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<() -> Unit>()

    fun onChange(listener: () -> Unit) { listeners += listener }

    fun removeOnChange(listener: () -> Unit) { listeners -= listener }

    private lateinit var dir: File

    fun init(context: Context) {
        if (::dir.isInitialized) return
        dir = File(context.filesDir, "icons").apply { mkdirs() }
    }

    /** By package name; a pinned input's key holds slashes, so it goes by a hash of the key. */
    private fun directory(theme: String?): File = if (theme == null) dir else File(dir, "themes/$theme")

    private fun fileFor(pkg: String, theme: String?): File =
        File(directory(theme), if (isValidKey(pkg)) "$pkg.png" else "input-${hash(pkg)}.png")

    private fun selectedFile(pkg: String, theme: String?): File =
        theme?.let { fileFor(pkg, it).takeIf(File::isFile) } ?: fileFor(pkg, null)

    private fun hash(key: String): String =
        java.security.MessageDigest.getInstance("SHA-1").digest(key.toByteArray()).joinToString("") { "%02x".format(it) }

    fun has(pkg: String, theme: String? = null): Boolean = ::dir.isInitialized && fileFor(pkg, theme).isFile

    /** When the app's picture last changed; 0 without one. Part of the tile cache's key. */
    fun stamp(pkg: String, theme: String? = null): Long = if (::dir.isInitialized) selectedFile(pkg, theme).lastModified() else 0L

    /** The app's picture at tile size, or null. Off the main thread. */
    fun load(pkg: String, theme: String? = null): Bitmap? =
        if (::dir.isInitialized) selectedFile(pkg, theme).takeIf(File::isFile)?.let { BitmapFactory.decodeFile(it.path) } else null

    /** Stores [input] as the app's picture. Off the main thread; false if it is not an image. */
    fun save(pkg: String, input: InputStream, cacheDir: File, theme: String? = null): Boolean {
        val tmp = File.createTempFile("icon", ".bin", cacheDir)
        return try {
            tmp.outputStream().use { input.copyTo(it) }
            val bmp = Images.decodeCover(tmp, W, H) ?: return false
            val target = fileFor(pkg, theme)
            target.parentFile?.mkdirs()
            val atomic = android.util.AtomicFile(target)
            val output = atomic.startWrite()
            try {
                check(bmp.compress(Bitmap.CompressFormat.PNG, 100, output))
                atomic.finishWrite(output)
            } catch (e: Exception) {
                atomic.failWrite(output)
                throw e
            } finally {
                bmp.recycle()
            }
            true
        } finally {
            tmp.delete()
        }
    }

    fun clear(pkg: String, theme: String? = null) {
        android.util.AtomicFile(fileFor(pkg, theme)).delete()
    }

    fun copyTheme(from: String, to: String) {
        val source = directory(from)
        if (source.isDirectory) check(source.copyRecursively(directory(to)))
    }

    fun deleteTheme(id: String) { directory(id).deleteRecursively() }

    /** Called on the main thread after a save or clear. */
    fun changed() {
        version++
        for (listener in listeners) listener()
    }

    /** A package name, kept as a plain file name. */
    fun isValidKey(pkg: String): Boolean = Regex("^[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+$").matches(pkg)
}
