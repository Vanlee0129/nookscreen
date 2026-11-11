package app.orchard.tv.apps

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import android.graphics.Bitmap
import android.graphics.Color
import app.orchard.tv.settings.ThemePack
import app.orchard.tv.plugins.ThemePlugins
import app.orchard.tv.plugins.PluginResult
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BundledIconsTest {
    @Test fun everyMappingDecodesWithinTheTileMemoryBudget() {
        val context = RuntimeEnvironment.getApplication()
        for (pkg in BundledIcons.assets.keys) {
            val bitmap = requireNotNull(BundledIcons.load(context, pkg)) { pkg }.bitmap
            assertTrue(pkg, bitmap.width >= TileArtFactory.W)
            assertTrue(pkg, bitmap.width <= TileArtFactory.W * 2)
            assertTrue(pkg, bitmap.height >= TileArtFactory.H)
            bitmap.recycle()
        }
        assertNull(BundledIcons.load(context, "example.unmapped"))
    }

    @Test fun separateAppsHaveSeparateArtwork() {
        assertEquals(BundledIcons.assets.size, BundledIcons.assets.values.toSet().size)
        assertNotEquals(BundledIcons.assets["com.mitv.gallery"], BundledIcons.assets["com.xiaomi.tv.gallery"])
    }

    @Test fun themePacksHaveArtworkForAllMappedAppsAndAFallbackForUnmappedApps() {
        val context = RuntimeEnvironment.getApplication()
        for (pack in listOf(ThemePack.of("sanrio"), ThemePack.of("marvel"))) {
            val store = ThemePlugins.of(context)
            val fixture = File(System.getProperty("nook.themePacks"), "nookscreen-theme-${pack.key}.zip")
            assertEquals(PluginResult.Installed, fixture.inputStream().use { store.install(it) })
            for (pkg in requireNotNull(store.active(pack.key)).manifest.tiles.keys) {
                val art = requireNotNull(ThemePackArt.tile(context, pack, pkg)) { "${pack.key}/$pkg" }
                assertEquals(16 * art.bitmap.height, 9 * art.bitmap.width)
                art.bitmap.recycle()
            }
            val wallpaper = requireNotNull(ThemePackArt.wallpaper(context, pack, 384, 216))
            assertEquals(384, wallpaper.width)
            wallpaper.recycle()
            assertFalse(ThemePackArt.hasTile(context, pack, "org.jellyfin.androidtv"))
            val original = TileArtFactory.forSystem(SystemTile.Media)
            assertNotSame(original, ThemePackArt.frame(context, pack, original))
        }
    }

    @Test fun reductionAveragesFineDetailInsteadOfDroppingAlternatePixels() {
        val source = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        for (y in 0 until 16) for (x in 0 until 16) {
            source.setPixel(x, y, if ((x + y) % 2 == 0) Color.BLACK else Color.WHITE)
        }
        val reduced = BundledIcons.filteredReduction(source, 4)
        assertTrue(source.isRecycled)
        for (y in 0 until 4) for (x in 0 until 4) {
            assertTrue(Color.red(reduced.getPixel(x, y)) in 120..136)
        }
        reduced.recycle()
    }
}
