package app.orchard.tv.guard

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.os.UserManager
import android.provider.Settings
import app.orchard.tv.BuildConfig
import app.orchard.tv.dream.AerialDream
import app.orchard.tv.settings.AerialSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SystemScreensaverRestoreTest {
    @Test fun switchingFromNeverToSystemReturnsTheDreamAndEnablesIt() {
        val context = RuntimeEnvironment.getApplication()
        shadowOf(context).grantPermissions(Manifest.permission.WRITE_SECURE_SETTINGS)
        shadowOf(context.getSystemService(UserManager::class.java)).setUserUnlocked(true)
        val resolver = context.contentResolver
        val firmware = "tv.firmware/tv.firmware.Dream"
        val appDream = ComponentName(context, AerialDream::class.java).flattenToString()
        Settings.Secure.putString(resolver, "screensaver_default_component", firmware)
        Settings.Secure.putString(resolver, "screensaver_components", appDream)
        Settings.Secure.putInt(resolver, "screensaver_enabled", 1)
        val wallpaper = context.getSharedPreferences("wallpaper", Context.MODE_PRIVATE)
        AerialSettings.init(context)
        val previousDelay = AerialSettings.screensaverMinutes
        try {
            AerialSettings.chooseScreensaverMinutes(AerialSettings.NEVER)
            assertEquals(if (BuildConfig.PREVIEW) 1 else 0, Settings.Secure.getInt(resolver, "screensaver_enabled"))

            wallpaper.edit().putString("screensaver", "system").apply()
            GuardSetup.screensaverModeChanged(context)
            assertEquals(1, Settings.Secure.getInt(resolver, "screensaver_enabled"))
            assertEquals(if (BuildConfig.PREVIEW) appDream else firmware, Settings.Secure.getString(resolver, "screensaver_components"))

            wallpaper.edit().putString("screensaver", "aerial").apply()
            GuardSetup.screensaverModeChanged(context)
            assertEquals(appDream, Settings.Secure.getString(resolver, "screensaver_components"))
            assertEquals(if (BuildConfig.PREVIEW) 1 else 0, Settings.Secure.getInt(resolver, "screensaver_enabled"))
        } finally {
            AerialSettings.chooseScreensaverMinutes(previousDelay)
        }
    }
}
