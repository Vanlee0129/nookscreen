package app.orchard.tv.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HomeThemesTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test fun themeSelectionFollowsTheSceneAndIsCopiedWhenDuplicated() {
        val scenes = DesktopScenes(context)
        val themes = HomeThemes(context)
        assertEquals(ThemeSelection(), themes.selection)
        themes.select(ThemeSelection(ThemePack.Sanrio))
        val copy = scenes.duplicate(themes::copyStyle)
        assertEquals(ThemeSelection(ThemePack.Sanrio), HomeThemes(context).selection)
        themes.select(ThemeSelection(ThemePack.Marvel))
        scenes.activate(DesktopScenes.ORIGINAL)
        themes.restoreStyle()
        assertEquals(ThemeSelection(ThemePack.Sanrio), themes.selection)
        scenes.activate(copy)
        assertEquals(ThemeSelection(ThemePack.Marvel), HomeThemes(context).selection)
    }

    @Test fun aCustomThemeCanBeReopenedRenamedUpdatedAndDeleted() {
        val themes = HomeThemes(context)
        val look = ThemeLook("photo:family", ThemePack.Sanrio)
        themes.create("  Family  ", look)
        val saved = HomeThemes(context).saved.single()
        assertEquals("Family", saved.name)
        assertEquals(look, saved.look)
        themes.update(saved.id, name = "Evening", look = ThemeLook(ThemePack.Marvel.wallpaper, ThemePack.Marvel))
        val reopened = HomeThemes(context).saved.single()
        assertEquals(SavedTheme(saved.id, "Evening", ThemeLook(ThemePack.Marvel.wallpaper, ThemePack.Marvel)), reopened)
        themes.delete(saved.id)
        assertTrue(HomeThemes(context).saved.isEmpty())
    }
}
