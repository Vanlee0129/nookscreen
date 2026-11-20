package app.orchard.tv.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRetryTest {
    @Test fun failedReadsBackOffAndARecoveryReturnsToHourlyRefresh() {
        val minutes = (1..7).map { Weather.refreshInterval(it) / 60_000 }
        assertEquals(listOf(3L, 6L, 12L, 24L, 48L, 60L, 60L), minutes)
        assertEquals(60L, Weather.refreshInterval(0) / 60_000)
    }
}
