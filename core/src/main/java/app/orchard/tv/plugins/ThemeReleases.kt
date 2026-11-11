package app.orchard.tv.plugins

import android.content.Context
import android.os.SystemClock
import app.orchard.tv.R
import app.orchard.tv.update.LegacyUpdateTls
import app.orchard.tv.update.Updates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/** The installed manifest provides the authoritative version and compatibility range. */
internal data class ThemeRelease(val id: String, val name: String) {
    fun title(context: Context): String = localizedThemeTitle(context, id) ?: name

    fun install(context: Context): PluginResult = try {
        val url = "${Updates.RELEASES}/download/plugin-$id/nookscreen-theme-$id.zip"
        withConnection(context, url) { connection ->
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) PluginResult.Unavailable
            else connection.inputStream.use { ThemePlugins.of(context).install(it, id) }
        }
    } catch (_: Exception) {
        PluginResult.Failed
    }
}

internal object ThemeReleases {
    private const val PAGE_SIZE = 100
    private const val REFRESH_MS = 10 * 60 * 1000L
    // Keep the original channels available when the GitHub API is slow or rate limited.
    private val original = listOf(ThemeRelease("sanrio", "Sanrio"), ThemeRelease("marvel", "Marvel"))
    private val mutable = MutableStateFlow(original)
    val catalog = mutable.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextRefresh = 0L
    private var refreshing = false

    @Synchronized
    fun refresh(context: Context) {
        if (refreshing || SystemClock.elapsedRealtime() < nextRefresh) return
        refreshing = true
        val app = context.applicationContext
        scope.launch {
            try {
                val releases = readCatalog(app)
                mutable.value = (original + releases).distinctBy { it.id }
            } catch (_: Exception) {
                // Keep the last usable catalog and retry when the user opens it later.
            } finally {
                finishRefresh()
            }
        }
    }

    @Synchronized
    private fun finishRefresh() {
        nextRefresh = SystemClock.elapsedRealtime() + REFRESH_MS
        refreshing = false
    }

    private fun readCatalog(context: Context): List<ThemeRelease> = buildList {
        var page = 1
        do {
            val url = "https://api.github.com/repos/${Updates.REPOSITORY}/releases?per_page=$PAGE_SIZE&page=$page"
            val json = withConnection(context, url) { connection ->
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            }
            addAll(parseThemeReleases(json))
            page++
        } while (json.length() == PAGE_SIZE)
    }
}

internal fun parseThemeReleases(json: JSONArray): List<ThemeRelease> = buildList {
    for (index in 0 until json.length()) {
        val release = json.optJSONObject(index) ?: continue
        if (release.optBoolean("draft") || !release.optBoolean("prerelease")) continue
        val tag = release.optString("tag_name")
        if (!tag.startsWith("plugin-")) continue
        val id = tag.removePrefix("plugin-")
        if (!pluginId.matches(id) || id == "classic") continue
        val assets = release.optJSONArray("assets") ?: continue
        val hasTheme = (0 until assets.length()).any {
            val asset = assets.optJSONObject(it) ?: return@any false
            asset.optString("name") == "nookscreen-theme-$id.zip" && asset.optString("state") == "uploaded"
        }
        if (!hasTheme) continue
        val name = release.optString("name").filterNot(Char::isISOControl).trim().take(64).ifEmpty { id }
        add(ThemeRelease(id, name))
    }
}

private fun <T> withConnection(context: Context, url: String, read: (HttpURLConnection) -> T): T {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        LegacyUpdateTls.configure(context, connection)
        connection.connectTimeout = 8_000
        connection.readTimeout = 12_000
        connection.setRequestProperty("User-Agent", "NookScreen")
        return read(connection)
    } finally {
        connection.disconnect()
    }
}

private fun localizedThemeTitle(context: Context, id: String): String? = when (id) {
    "sanrio" -> context.getString(R.string.theme_sanrio)
    "marvel" -> context.getString(R.string.theme_marvel)
    else -> null
}

internal fun InstalledTheme.title(context: Context): String = localizedThemeTitle(context, manifest.id) ?: manifest.name
