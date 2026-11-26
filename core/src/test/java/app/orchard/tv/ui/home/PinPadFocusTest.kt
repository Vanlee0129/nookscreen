package app.orchard.tv.ui.home

import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import app.orchard.tv.ui.components.Pressable
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w960dp-h540dp-land-xhdpi")
class PinPadFocusTest {
    @get:Rule val compose = createEmptyComposeRule()
    private var host: ActivityController<ComponentActivity>? = null
    private lateinit var view: View

    @After fun closeHost() {
        compose.runOnUiThread { host?.close() }
    }

    @Test fun arrowsMoveBetweenDigitsButCannotReachAControlBehindThePad() {
        compose.runOnUiThread {
            val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
            host = controller
            controller.get().setContent {
                view = LocalView.current
                Box(Modifier.fillMaxSize()) {
                    Pressable(onClick = {}, modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) {
                        BasicText("Outside PIN")
                    }
                    PinPad(visible = true, mode = PinMode.Verify, onDone = {}, onCancel = {})
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithText("1").assertIsFocused()
        tap(KeyEvent.KEYCODE_DPAD_UP)
        compose.onNodeWithText("1").assertIsFocused()
        tap(KeyEvent.KEYCODE_DPAD_DOWN)
        compose.onNodeWithText("4").assertIsFocused()
    }

    private fun tap(code: Int) = compose.runOnUiThread {
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }
}
