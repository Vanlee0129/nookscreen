package app.orchard.tv.plugins

import android.content.Context
import app.orchard.tv.settings.HomeThemes
import app.orchard.tv.settings.ThemePack
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ThemePluginsTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test fun legacyThemeReferenceSurvivesInstallDisableRemovalAndReinstall() {
        context.getSharedPreferences("home_themes", Context.MODE_PRIVATE).edit()
            .putString("look", "sanrio:family-theme")
            .putString("saved", """[{"id":"family-theme","name":"Family","wallpaper":"photo:family","pack":"sanrio"}]""")
            .commit()
        val store = ThemePlugins.of(context)
        assertEquals(ThemePack.Classic, HomeThemes.activeSelection(context).pack)
        assertEquals("family-theme", HomeThemes.activeSelection(context).customId)
        assertEquals(PluginResult.Installed, artifact().inputStream().use { store.install(it) })
        assertEquals(ThemePack.of("sanrio"), HomeThemes.activeSelection(context).pack)
        assertTrue(store.setEnabled("sanrio", false))
        assertFalse(ThemePlugins(context).installed.value.getValue("sanrio").enabled)
        assertEquals(ThemePack.Classic, HomeThemes.activeSelection(context).pack)
        assertTrue(store.setEnabled("sanrio", true))
        assertTrue(store.remove("sanrio"))
        assertNull(ThemePlugins(context).active("sanrio"))
        assertEquals("photo:family", HomeThemes(context).saved.single().look.wallpaper)
        assertEquals(PluginResult.Installed, artifact().inputStream().use { store.install(it) })
        assertEquals("family-theme", HomeThemes.activeSelection(context).customId)
        assertEquals(ThemePack.of("sanrio"), HomeThemes.activeSelection(context).pack)
    }

    @Test fun independentUpdatePreservesEnablementAndRejectsIncompatibleReplacement() {
        val store = ThemePlugins.of(context)
        assertEquals(PluginResult.Installed, artifact().inputStream().use { store.install(it, "sanrio") })
        assertTrue(store.setEnabled("sanrio", false))
        assertEquals(PluginResult.Incompatible, revised(3, 2).inputStream().use { store.install(it) })
        assertEquals(PluginResult.Invalid, revised(2).inputStream().use { store.install(it, "marvel") })
        assertEquals(1, store.installed.value.getValue("sanrio").manifest.version)
        assertEquals(PluginResult.Installed, revised(2).inputStream().use { store.install(it) })
        val reopened = ThemePlugins(context).installed.value.getValue("sanrio")
        assertEquals(2, reopened.manifest.version)
        assertFalse(reopened.enabled)
        assertEquals(PluginResult.Unchanged, artifact().inputStream().use { store.install(it) })
    }

    private fun artifact() = File(System.getProperty("nook.themePacks"), "nookscreen-theme-sanrio.zip")

    private fun revised(version: Int, minApi: Int = 1): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { output ->
            ZipFile(artifact()).use { source ->
                source.entries().asSequence().forEach { entry ->
                    output.putNextEntry(ZipEntry(entry.name))
                    source.getInputStream(entry).use { input ->
                        if (entry.name == "manifest.json") {
                            val manifest = JSONObject(input.bufferedReader().readText())
                                .put("version", version).put("minApi", minApi).put("maxApi", minApi)
                            output.write(manifest.toString().toByteArray())
                        } else input.copyTo(output)
                    }
                    output.closeEntry()
                }
            }
        }
        return bytes.toByteArray()
    }
}
