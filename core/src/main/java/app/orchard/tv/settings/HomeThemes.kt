package app.orchard.tv.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import app.orchard.tv.plugins.ThemePlugins
import app.orchard.tv.plugins.pluginId
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@ConsistentCopyVisibility
data class ThemePack private constructor(val key: String) {
    val wallpaper get() = if (this == Classic) "daily:latest" else "theme:$key"

    companion object {
        val Classic = ThemePack("classic")
        fun of(key: String?) = if (key != null && pluginId.matches(key)) ThemePack(key) else Classic
    }
}

data class ThemeSelection(val pack: ThemePack = ThemePack.Classic, val customId: String? = null, val revision: Long = 0)
data class ThemeLook(val wallpaper: String, val pack: ThemePack)
data class SavedTheme(val id: String, val name: String, val look: ThemeLook)

/** Saved appearances are reusable across scenes. Applying one copies its appearance to the active scene. */
class HomeThemes(private val context: Context) {
    private val prefs = prefs(context)

    var selection by mutableStateOf(requestedSelection(context)); private set
    var saved by mutableStateOf(readThemes()); private set

    fun select(next: ThemeSelection) {
        if (next == selection) return
        selection = next
        copyStyle(DesktopScenes.activeId(context))
    }

    fun copyStyle(scene: String) = prefs.edit { putString(selectionKey(scene), encode(selection)) }

    fun restoreStyle() { selection = requestedSelection(context) }

    fun removeScene(scene: String) {
        require(scene != DesktopScenes.ORIGINAL)
        prefs.edit { remove(selectionKey(scene)) }
    }

    fun create(name: String, look: ThemeLook, id: String = UUID.randomUUID().toString()): SavedTheme? {
        val title = name.trim().take(MAX_NAME)
        if (title.isEmpty()) return null
        return SavedTheme(id, title, look).also { writeThemes(saved + it) }
    }

    fun update(id: String, name: String? = null, look: ThemeLook? = null) {
        writeThemes(saved.map {
            if (it.id == id) it.copy(name = name?.trim()?.take(MAX_NAME)?.ifEmpty { it.name } ?: it.name, look = look ?: it.look)
            else it
        })
    }

    fun delete(id: String) {
        prefs.edit {
            for ((key, value) in prefs.all) {
                if (key != "look" && !key.endsWith(".look")) continue
                val current = decode(value as? String)
                if (current.customId == id) putString(key, encode(current.copy(customId = null)))
            }
        }
        restoreStyle()
        writeThemes(saved.filterNot { it.id == id })
    }

    private fun readThemes(): List<SavedTheme> {
        val array = runCatching { JSONArray(prefs.getString("saved", null) ?: "[]") }.getOrNull() ?: return emptyList()
        return (0 until array.length()).mapNotNull { index ->
            val entry = array.optJSONObject(index) ?: return@mapNotNull null
            val id = entry.optString("id")
            val name = entry.optString("name")
            val wallpaper = entry.optString("wallpaper")
            if (id.isBlank() || name.isBlank() || wallpaper.isBlank()) return@mapNotNull null
            SavedTheme(id, name, ThemeLook(wallpaper, ThemePack.of(entry.optString("pack"))))
        }
    }

    private fun writeThemes(themes: List<SavedTheme>) {
        val array = JSONArray()
        themes.forEach { theme ->
            array.put(JSONObject().put("id", theme.id).put("name", theme.name)
                .put("wallpaper", theme.look.wallpaper).put("pack", theme.look.pack.key))
        }
        prefs.edit { putString("saved", array.toString()) }
        saved = themes
    }

    companion object {
        const val MAX_NAME = 32

        private fun prefs(context: Context) = context.getSharedPreferences("home_themes", Context.MODE_PRIVATE)
        private fun selectionKey(scene: String) = DesktopScenes.key(scene, "look")
        private fun encode(selection: ThemeSelection) = selection.pack.key + ":" + selection.customId.orEmpty()
        private fun decode(value: String?) = ThemeSelection(
            ThemePack.of(value?.substringBefore(':')),
            value?.substringAfter(':', "")?.takeIf { it.isNotEmpty() },
        )

        private fun requestedSelection(context: Context): ThemeSelection =
            decode(prefs(context).getString(selectionKey(DesktopScenes.activeId(context)), null))

        fun activeSelection(context: Context): ThemeSelection {
            val selected = requestedSelection(context)
            if (selected.pack == ThemePack.Classic) return selected
            val installed = ThemePlugins.of(context).active(selected.pack.key)
            return if (installed == null) selected.copy(pack = ThemePack.Classic)
            else selected.copy(revision = installed.stamp)
        }

        fun activeName(context: Context): String? {
            val id = activeSelection(context).customId ?: return null
            val themes = runCatching { JSONArray(prefs(context).getString("saved", null) ?: "[]") }.getOrNull() ?: return null
            return (0 until themes.length()).asSequence().mapNotNull(themes::optJSONObject)
                .firstOrNull { it.optString("id") == id }?.optString("name")
        }
    }
}
