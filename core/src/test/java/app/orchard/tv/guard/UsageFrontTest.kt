package app.orchard.tv.guard

import app.orchard.tv.guard.UsageFront.MOVE_TO_BACKGROUND
import app.orchard.tv.guard.UsageFront.MOVE_TO_FOREGROUND
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageFrontTest {
    private fun pick(vararg e: Pair<Int, Pair<String, Long>>): UsageFront.Front? {
        val p = UsageFront.Picker()
        e.forEach { (type, rest) -> p.add(type, rest.first.substringBefore('/'), rest.first.substringAfter('/'), rest.second) }
        return p.result(now = 2_000L, activitySettleMs = 1_000L)
    }

    @Test fun appBackedOutOfWithNothingAfterIsClosed() {
        // Issue #1023: the file manager moved to the front, then to the back, and nothing came up.
        val f = pick(MOVE_TO_FOREGROUND to ("com.stv.filemanager/.Main" to 100L), MOVE_TO_BACKGROUND to ("com.stv.filemanager/.Main" to 200L))
        assertEquals("com.stv.filemanager", f!!.pkg)
        assertTrue(f.closed)
    }

    @Test fun nextAppComingUpIsInFront() {
        val f = pick(
            MOVE_TO_FOREGROUND to ("a/.A" to 100L),
            MOVE_TO_BACKGROUND to ("a/.A" to 200L),
            MOVE_TO_FOREGROUND to ("b/.B" to 210L),
        )
        assertEquals("b", f!!.pkg)
        assertFalse(f.closed)
    }

    @Test fun anotherActivityGoingBackDoesNotCloseTheFront() {
        // The previous app pauses after the new one resumed on some firmwares.
        val f = pick(MOVE_TO_FOREGROUND to ("b/.B" to 210L), MOVE_TO_BACKGROUND to ("a/.A" to 220L))
        assertFalse(f!!.closed)
    }

    @Test fun noEventsIsUnknown() {
        assertNull(pick())
    }

    @Test fun activityPauseKeepsAppInFrontUntilTheTransitionSettles() {
        val picker = UsageFront.Picker()
        picker.add(MOVE_TO_FOREGROUND, "org.smarttube.stable", ".BrowseActivity", 100L)
        picker.add(MOVE_TO_BACKGROUND, "org.smarttube.stable", ".BrowseActivity", 200L)
        assertFalse(picker.result(now = 1199L, activitySettleMs = 1000L)!!.closed)
        assertTrue(picker.result(now = 1200L, activitySettleMs = 1000L)!!.closed)
        picker.add(MOVE_TO_FOREGROUND, "org.smarttube.stable", ".AppDialogActivity", 1201L)
        val front = picker.result(now = 2300L, activitySettleMs = 1000L)!!
        assertEquals(".AppDialogActivity", front.cls)
        assertFalse(front.closed)
    }
}
