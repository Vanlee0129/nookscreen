package app.orchard.tv.system

import android.Manifest
import android.content.ContextWrapper
import android.provider.Settings
import com.xgimi.gmpf.api.DisplayManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class XiaomiBacklightTest {
    private val application = RuntimeEnvironment.getApplication()
    private val context = object : ContextWrapper(application) {
        override fun getClassLoader(): ClassLoader = object : ClassLoader(application.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (name == "com.changhong.tvos.common.TVManager") throw ClassNotFoundException(name)
                return super.loadClass(name, resolve)
            }
        }
    }
    private val settings = application.contentResolver

    @Before fun prepareXiaomiFirmware() {
        // The JVM includes vendor SDK stand-ins for other devices; Xiaomi lacks those interfaces.
        DisplayManager.getInstance().unavailable = true
        shadowOf(application).grantPermissions(Manifest.permission.WRITE_SECURE_SETTINGS)
    }

    @After fun restoreTestSdk() {
        DisplayManager.getInstance().unavailable = false
    }

    @Test fun readsTheFirmwareValueAndKeepsBothBacklightKeysInSync() {
        Settings.Global.putInt(settings, "picture_backlight", 90)
        Settings.Global.putInt(settings, "xiaomi_picture_backlight", 21)

        assertEquals(0.21f, Brightness.level(context)!!, 0.001f)
        assertEquals(0.25f, Brightness.adjust(context, 1)!!, 0.001f)
        assertEquals(25, Settings.Global.getInt(settings, "xiaomi_picture_backlight"))
        assertEquals(25, Settings.Global.getInt(settings, "picture_backlight"))
    }

    @Test fun olderFirmwareKeepsItsExistingBacklightKey() {
        Settings.Global.putInt(settings, "picture_backlight", 21)

        assertEquals(0.20f, Brightness.adjust(context, -1)!!, 0.001f)
        assertEquals(20, Settings.Global.getInt(settings, "picture_backlight"))
        assertNull(Settings.Global.getString(settings, "xiaomi_picture_backlight"))
    }
}
