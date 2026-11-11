package app.orchard.tv.plugins

import android.content.Context
import app.orchard.tv.R
import app.orchard.tv.update.LegacyUpdateTls
import app.orchard.tv.update.Updates
import java.net.HttpURLConnection
import java.net.URL

/** Independent prerelease channels. The installed manifest provides the authoritative version. */
internal enum class ThemeRelease(val id: String, val label: Int) {
    Sanrio("sanrio", R.string.theme_sanrio),
    Marvel("marvel", R.string.theme_marvel);

    fun install(context: Context): PluginResult = try {
        val url = "${Updates.RELEASES}/download/plugin-$id/nookscreen-theme-$id.zip"
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            LegacyUpdateTls.configure(context, connection)
            connection.connectTimeout = 8_000
            connection.readTimeout = 12_000
            connection.setRequestProperty("User-Agent", "NookScreen")
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) PluginResult.Unavailable
            else connection.inputStream.use { ThemePlugins.of(context).install(it, id) }
        } finally {
            connection.disconnect()
        }
    } catch (_: Exception) {
        PluginResult.Failed
    }
}

internal fun InstalledTheme.title(context: Context): String =
    ThemeRelease.entries.firstOrNull { it.id == manifest.id }?.let { context.getString(it.label) } ?: manifest.name
