package app.orchard.tv.plugins

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import app.nookscreen.plugin.PluginApi
import app.orchard.tv.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.WeakHashMap
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

internal data class ThemeManifest(
    val id: String,
    val name: String,
    val version: Int,
    val minApi: Int,
    val maxApi: Int,
    val wallpaper: String,
    val preview: String,
    val frame: String?,
    val frameTrim: Int,
    val tiles: Map<String, String>,
) {
    val compatible get() = PluginApi.supports(minApi, maxApi)
}

internal data class InstalledTheme(val manifest: ThemeManifest, val file: File, val stamp: Long, val enabled: Boolean) {
    val active get() = enabled && manifest.compatible

    fun <T> read(name: String, decode: (InputStream) -> T): T? = runCatching {
        ZipFile(file).use { zip -> zip.getInputStream(zip.getEntry(name)).use(decode) }
    }.getOrNull()
}

internal enum class PluginResult(val message: Int) {
    Installed(R.string.plugin_installed),
    Unchanged(R.string.plugin_unchanged),
    Invalid(R.string.plugin_invalid),
    Incompatible(R.string.plugin_incompatible),
    Unavailable(R.string.plugin_unavailable),
    Failed(R.string.plugin_failed),
}

internal val pluginId = Regex("[a-z][a-z0-9-]{0,39}")

/** One archive per theme. Validate a temporary file fully before replacing the installed file. */
internal class ThemePlugins(context: Context) {
    private val directory = File(context.filesDir, "theme-plugins").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("theme_plugins", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(scan())
    val installed = mutable.asStateFlow()

    fun active(id: String): InstalledTheme? = installed.value[id]?.takeIf { it.active }

    private fun enabled(id: String) = prefs.getBoolean("enabled.$id", true)

    private fun scan(): Map<String, InstalledTheme> = directory.listFiles { file -> file.extension == "zip" }
        .orEmpty().mapNotNull { file ->
            runCatching {
                val manifest = inspect(file, validateImages = false)
                require(manifest.id == file.nameWithoutExtension)
                manifest.id to InstalledTheme(manifest, file, file.lastModified(), enabled(manifest.id))
            }.onFailure { Log.w("NookPlugins", "Ignoring unreadable theme archive", it) }.getOrNull()
        }.toMap()

    fun install(input: InputStream, expectedId: String? = null, length: Long? = null): PluginResult {
        val temporary = runCatching { File.createTempFile("import-", ".part", directory) }
            .getOrElse { return PluginResult.Failed }
        try {
            val manifest = try {
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (length == null || total < length) {
                        val requested = length?.let { minOf(it - total, buffer.size.toLong()).toInt() } ?: buffer.size
                        val count = input.read(buffer, 0, requested)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_ARCHIVE)
                        output.write(buffer, 0, count)
                    }
                    require(length == null || total == length)
                }
                inspect(temporary, validateImages = true).also { require(expectedId == null || expectedId == it.id) }
            } catch (_: Exception) {
                return PluginResult.Invalid
            }
            if (!manifest.compatible) return PluginResult.Incompatible
            return publish(temporary, manifest)
        } finally {
            temporary.delete()
        }
    }

    @Synchronized
    private fun publish(temporary: File, manifest: ThemeManifest): PluginResult {
        val previous = installed.value[manifest.id]
        if (previous != null && manifest.version <= previous.manifest.version) return PluginResult.Unchanged
        val target = File(directory, "${manifest.id}.zip")
        if (!temporary.renameTo(target)) return PluginResult.Failed
        mutable.value = installed.value + (manifest.id to InstalledTheme(
            manifest, target, target.lastModified(), enabled(manifest.id),
        ))
        return PluginResult.Installed
    }

    @Synchronized
    fun setEnabled(id: String, enabled: Boolean): Boolean {
        val theme = installed.value[id] ?: return false
        if (enabled && !theme.manifest.compatible) return false
        if (!prefs.edit().putBoolean("enabled.$id", enabled).commit()) return false
        mutable.value = installed.value + (id to theme.copy(enabled = enabled))
        return true
    }

    @Synchronized
    fun remove(id: String): Boolean {
        val theme = installed.value[id] ?: return false
        if (!theme.file.delete() && theme.file.exists()) return false
        prefs.edit().remove("enabled.$id").apply()
        mutable.value = installed.value - id
        return true
    }

    companion object {
        const val MAX_ARCHIVE = 32L * 1024 * 1024
        private const val MAX_IMAGE = 8L * 1024 * 1024
        private const val MAX_MANIFEST = 64L * 1024
        private const val MAX_ENTRIES = 256
        private val imageName = Regex("[a-zA-Z0-9_-]{1,80}\\.(jpg|jpeg|png|webp)")
        private val appName = Regex("[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+")
        private val imageTypes = setOf("image/jpeg", "image/png", "image/webp")
        private val stores = WeakHashMap<Context, ThemePlugins>()

        @Synchronized
        fun of(context: Context): ThemePlugins = stores.getOrPut(context.applicationContext) { ThemePlugins(context.applicationContext) }

        private fun readEntry(zip: ZipFile, entry: ZipEntry): ByteArray = zip.getInputStream(entry).use { input ->
            val bytes = ByteArray(entry.size.toInt())
            var total = 0
            while (total < bytes.size) {
                val count = input.read(bytes, total, bytes.size - total)
                require(count > 0)
                total += count
            }
            require(input.read() == -1)
            require(CRC32().apply { update(bytes) }.value == entry.crc)
            bytes
        }

        private fun inspect(file: File, validateImages: Boolean): ThemeManifest = ZipFile(file).use { zip ->
            require(zip.size() in 2..MAX_ENTRIES)
            val entries = zip.entries().asSequence().toList()
            val byName = entries.associateBy { it.name }
            require(byName.size == entries.size)
            require(entries.all {
                if (it.name == "manifest.json") it.size in 1..MAX_MANIFEST
                else imageName.matches(it.name) && it.size in 1..MAX_IMAGE
            })
            require(entries.sumOf { it.size } <= MAX_ARCHIVE)
            val json = JSONObject(String(readEntry(zip, requireNotNull(byName["manifest.json"])), Charsets.UTF_8))
            fun positive(key: String) = (json.get(key) as? Int)?.also { require(it > 0) } ?: error(key)
            fun image(name: String): String = name.also { require(imageName.matches(it) && it in byName) }
            require(positive("schema") == 1 && json.getString("kind") == "theme")
            val id = json.getString("id")
            require(pluginId.matches(id) && id != "classic")
            val name = json.getString("name").trim()
            require(name.length in 1..64 && name.none(Char::isISOControl))
            val tileJson = json.optJSONObject("tiles") ?: JSONObject()
            val tiles = tileJson.keys().asSequence().associateWith {
                require(it.length <= 200 && appName.matches(it))
                image(tileJson.getString(it))
            }
            val min = positive("minApi")
            val max = positive("maxApi")
            require(max >= min)
            val wallpaper = image(json.getString("wallpaper"))
            val preview = json.optString("preview").ifEmpty { wallpaper }.let(::image)
            val frame = json.optString("frame").takeIf(String::isNotEmpty)?.let(::image)
            val trim = Color.parseColor(json.optString("frameTrim", "#FFFFFF"))
            if (validateImages) {
                for (entry in entries.filter { it.name != "manifest.json" }) {
                    val data = readEntry(zip, entry)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
                    require(bounds.outMimeType in imageTypes)
                    require(bounds.outWidth in 1..4096 && bounds.outHeight in 1..4096)
                    val sampled = BitmapFactory.Options().apply { inSampleSize = 8 }
                    requireNotNull(BitmapFactory.decodeByteArray(data, 0, data.size, sampled)).recycle()
                }
            }
            ThemeManifest(id, name, positive("version"), min, max, wallpaper, preview, frame, trim, tiles)
        }
    }
}
