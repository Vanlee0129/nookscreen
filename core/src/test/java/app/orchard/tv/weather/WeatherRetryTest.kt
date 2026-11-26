package app.orchard.tv.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRetryTest {
    @Test fun failedReadsBackOffAndARecoveryReturnsToHourlyRefresh() {
        val minutes = (1..7).map { Weather.refreshInterval(it) / 60_000 }
        assertEquals(listOf(3L, 6L, 12L, 24L, 48L, 60L, 60L), minutes)
        assertEquals(60L, Weather.refreshInterval(0) / 60_000)
    }

    @Test fun cachedReadingsGetALabelAtThreeHoursAndExpireAtTwelve() {
        val hour = 60 * 60 * 1000L
        val readAt = 40 * hour
        val reading = WeatherNow(18, WeatherKind.Cloudy, true, readAt = readAt)
        assertFalse(Weather.visibleAt(0L, 3 * hour))
        assertFalse(Weather.visibleAt(readAt, readAt - 1))
        assertTrue(Weather.visibleAt(readAt, readAt))
        assertNull(Weather.hoursOld(reading, readAt + 3 * hour - 1))
        assertEquals(3, Weather.hoursOld(reading, readAt + 3 * hour))
        assertTrue(Weather.visibleAt(readAt, readAt + 12 * hour - 1))
        assertEquals(11, Weather.hoursOld(reading, readAt + 12 * hour - 1))
        assertFalse(Weather.visibleAt(readAt, readAt + 12 * hour))
        assertNull(Weather.hoursOld(reading, readAt + 12 * hour))
    }
}
