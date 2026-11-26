package app.orchard.tv.system

import android.content.ComponentName
import android.content.pm.PackageManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TclQuickPanelTest {
    @Test fun defersToTheFirmwareOnlyWhileItsServiceIsEnabled() {
        val context = RuntimeEnvironment.getApplication()
        val pm = context.packageManager
        val component = ComponentName("com.tcl.suspension", "com.tcl.suspension.DismissService")

        assertFalse(SystemProbe.tclQuickPanel(context))
        shadowOf(pm).addServiceIfNotPresent(component)
        assertTrue(SystemProbe.tclQuickPanel(context))

        pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        assertFalse(SystemProbe.tclQuickPanel(context))
    }
}
