package app.orchard.tv.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import app.orchard.tv.R
import org.json.JSONObject
import java.util.UUID

data class DesktopScene(val id: String, val name: String)

/** Only the catalog and active pointer live here. The original scene keeps its existing preference keys. */
class DesktopScenes(private val context: Context) {
    private val prefs = prefs(context)
    private val names = runCatching { JSONObject(prefs.getString("names", "{}") ?: "{}") }
        .getOrElse { JSONObject() }

    var scenes by mutableStateOf(readScenes()); private set
    var active by mutableStateOf(activeId(context)); private set

    private fun readScenes(): List<DesktopScene> =
        listOf(DesktopScene(ORIGINAL, names.optString(ORIGINAL).ifEmpty { context.getString(R.string.scene_original) })) +
            names.keys().asSequence().filter { it != ORIGINAL }.map { DesktopScene(it, names.getString(it)) }.toList()

    /** Writes a complete copy before publishing the new scene as active. */
    fun duplicate(copy: (String) -> Unit): String {
        val id = UUID.randomUUID().toString()
        val name = generateSequence(1) { it + 1 }
            .map { context.getString(R.string.scene_numbered, it) }
            .first { candidate -> scenes.none { it.name == candidate } }
        copy(id)
        names.put(id, name)
        publish(id)
        return id
    }

    fun activate(id: String): Boolean {
        if (id == active || scenes.none { it.id == id }) return false
        publish(id)
        return true
    }

    fun rename(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        names.put(active, trimmed)
        publish(active)
    }

    fun removeActive(): String? {
        val removed = active.takeUnless { it == ORIGINAL } ?: return null
        names.remove(removed)
        publish(ORIGINAL)
        return removed
    }

    private fun publish(id: String) {
        prefs.edit {
            putString("names", names.toString())
            putString("active", id)
        }
        scenes = readScenes()
        active = id
    }

    companion object {
        const val ORIGINAL = "original"

        private fun prefs(context: Context) = context.getSharedPreferences("desktop_scenes", Context.MODE_PRIVATE)
        fun activeId(context: Context): String = prefs(context).getString("active", ORIGINAL) ?: ORIGINAL
        fun key(id: String, key: String): String = if (id == ORIGINAL) key else "scene.$id.$key"
    }
}
