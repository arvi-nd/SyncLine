package com.example

import com.example.engine.TimezoneEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TimezoneEngineTest {

    // Fixed test instant: 2025-05-28T18:24:00Z (2:24 PM EDT in New York, 7:24 PM BST in London, 11:54 PM IST in New Delhi)
    private val summerInstant = Instant.parse("2025-05-28T18:24:00Z")

    // Fixed test instant in winter: 2025-01-15T18:24:00Z (1:24 PM EST in New York, 6:24 PM GMT in London)
    private val winterInstant = Instant.parse("2025-01-15T18:24:00Z")

    @Test
    fun testFormatCurrentTime_summerDaylightSaving() {
        val nyTime = TimezoneEngine.formatCurrentTime("America/New_York", summerInstant)
        assertEquals("2:24 PM", nyTime)

        val londonTime = TimezoneEngine.formatCurrentTime("Europe/London", summerInstant)
        assertEquals("7:24 PM", londonTime)

        val berlinTime = TimezoneEngine.formatCurrentTime("Europe/Berlin", summerInstant)
        assertEquals("8:24 PM", berlinTime)
    }

    @Test
    fun testFormatCurrentTime_winterStandardTime() {
        val nyTime = TimezoneEngine.formatCurrentTime("America/New_York", winterInstant)
        assertEquals("1:24 PM", nyTime)

        val londonTime = TimezoneEngine.formatCurrentTime("Europe/London", winterInstant)
        assertEquals("6:24 PM", londonTime)
    }

    @Test
    fun testRegionWithoutDaylightSaving_India() {
        // India (Asia/Kolkata) never changes offset: UTC +5:30 year-round
        val indiaSummer = TimezoneEngine.formatCurrentTime("Asia/Kolkata", summerInstant)
        assertEquals("11:54 PM", indiaSummer)

        val indiaWinter = TimezoneEngine.formatCurrentTime("Asia/Kolkata", winterInstant)
        assertEquals("11:54 PM", indiaWinter)
    }

    @Test
    fun testFormatTimeWithZoneAndOffset() {
        val formattedNY = TimezoneEngine.formatTimeWithZoneAndOffset("America/New_York", summerInstant)
        assertTrue(formattedNY.contains("EDT"))
        assertTrue(formattedNY.contains("UTC -04") || formattedNY.contains("UTC -4"))

        val formattedIndia = TimezoneEngine.formatTimeWithZoneAndOffset("Asia/Kolkata", summerInstant)
        assertTrue(formattedIndia.contains("IST"))
        assertTrue(formattedIndia.contains("+05:30") || formattedIndia.contains("+5:30"))
    }

    @Test
    fun testDaytimeCalculation() {
        // At 18:24 UTC, New York is 14:24 (2:24 PM) -> Daytime
        assertTrue(TimezoneEngine.isDaytime("America/New_York", summerInstant))

        // At 18:24 UTC, New Delhi is 23:54 (11:54 PM) -> Nighttime
        assertFalse(TimezoneEngine.isDaytime("Asia/Kolkata", summerInstant))
    }

    @Test
    fun testWeekendDetection() {
        val sundayInstant = Instant.parse("2025-06-01T12:00:00Z")
        val mondayInstant = Instant.parse("2025-06-02T12:00:00Z")

        assertTrue(TimezoneEngine.isWeekend("America/New_York", sundayInstant))
        assertFalse(TimezoneEngine.isWeekend("America/New_York", mondayInstant))
    }

    @Test
    fun testWorkingHoursOverlap_partialOverlap() {
        // London (9:00 - 18:00 BST) and Berlin (9:00 - 18:00 CEST) have 8 hours overlap (Berlin is 1 hour ahead)
        val testDate = LocalDate.of(2025, 5, 28)
        val overlap = TimezoneEngine.calculateWorkingHoursOverlap(
            userZoneId = "Europe/London",
            clientZoneId = "Europe/Berlin",
            date = testDate
        )

        assertTrue(overlap.hasOverlap)
        assertEquals(8 * 60L, overlap.overlapDurationMinutes)
        assertEquals("8h overlap", overlap.formattedDuration)
    }

    @Test
    fun testWorkingHoursOverlap_identicalTimezones() {
        val testDate = LocalDate.of(2025, 5, 28)
        val overlap = TimezoneEngine.calculateWorkingHoursOverlap(
            userZoneId = "America/New_York",
            clientZoneId = "America/New_York",
            date = testDate
        )

        assertTrue(overlap.hasOverlap)
        assertEquals(9 * 60L, overlap.overlapDurationMinutes)
        assertEquals("9h overlap", overlap.formattedDuration)
    }

    @Test
    fun testWorkingHoursOverlap_distantTimezonesNonOverlapping() {
        val testDate = LocalDate.of(2025, 5, 28)
        // Honolulu and New Delhi have completely disjoint 9 to 18 windows on the same calendar day
        val overlap = TimezoneEngine.calculateWorkingHoursOverlap(
            userZoneId = "Pacific/Honolulu",
            clientZoneId = "Asia/Kolkata",
            date = testDate
        )

        assertFalse(overlap.hasOverlap)
        assertEquals(0L, overlap.overlapDurationMinutes)
    }

    @Test
    fun testConvertScheduledTime() {
        val scheduled = Instant.parse("2025-05-29T13:00:00Z") // 9:00 AM EDT in NY
        val conversion = TimezoneEngine.convertScheduledTime(
            recipientZoneId = "America/New_York",
            userZoneId = "Asia/Kolkata",
            scheduledInstant = scheduled
        )

        assertTrue(conversion.contains("9:00 AM"))
        assertTrue(conversion.contains("6:30 PM"))
    }
}
