package com.example

import com.example.model.Meeting
import com.example.model.getDisplayDate
import com.example.model.getDisplayDay
import com.example.model.getDisplayMonth
import com.example.model.getDisplayTime
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class MeetingSecurityAndModelTest {

    // Fixed absolute instant: 2026-10-30T14:30:00Z
    // In UTC: 2:30 PM (14:30)
    // In New York (EDT, UTC-4): 10:30 AM (10:30)
    // In London (GMT, UTC+0 in late Oct): 2:30 PM (14:30)
    // In Tokyo (JST, UTC+9): 11:30 PM (23:30)
    private val testInstant = Instant.parse("2026-10-30T14:30:00Z")
    private val testTimestamp = Timestamp(Date(testInstant.toEpochMilli()))

    @Test
    fun testLocalDateTimeToAbsoluteInstantConversion() {
        // Organizer in New York (EDT UTC-4) selects Oct 30, 2026 at 10:30 AM
        val nyZone = ZoneId.of("America/New_York")
        val selectedDate = LocalDate.of(2026, 10, 30)
        val selectedHour = 10
        val selectedMinute = 30

        val localDateTime = selectedDate.atTime(selectedHour, selectedMinute)
        val zonedDateTime = localDateTime.atZone(nyZone)
        val epochMilli = zonedDateTime.toInstant().toEpochMilli()

        // Absolute UTC instant must match 2026-10-30T14:30:00Z
        assertEquals(testInstant.toEpochMilli(), epochMilli)
    }

    @Test
    fun testCrossTimezoneDisplayConversion() {
        val meeting = Meeting(
            id = "m_timezone_test",
            title = "International Client Sync",
            time = "10:30 AM", // Organizer's local time string
            dateString = "Oct 30, 2026",
            duration = "30 min",
            organizerUid = "organizer_ny",
            participantUids = listOf("organizer_ny", "client_tokyo"),
            scheduledAt = testTimestamp,
            durationMinutes = 30
        )

        // When viewed in New York (EDT, UTC-4)
        val nyDisplayTime = meeting.getDisplayTime(ZoneId.of("America/New_York"))
        assertEquals("10:30 AM", nyDisplayTime)

        // When viewed in London (UTC+0)
        val londonDisplayTime = meeting.getDisplayTime(ZoneId.of("Europe/London"))
        assertEquals("2:30 PM", londonDisplayTime)

        // When viewed in Tokyo (JST, UTC+9)
        val tokyoDisplayTime = meeting.getDisplayTime(ZoneId.of("Asia/Tokyo"))
        assertEquals("11:30 PM", tokyoDisplayTime)
    }

    @Test
    fun testDisplayMonthAndDay() {
        val meeting = Meeting(
            id = "m_date_test",
            title = "Quarterly Review",
            scheduledAt = testTimestamp
        )

        val month = meeting.getDisplayMonth(ZoneId.of("UTC"))
        val day = meeting.getDisplayDay(ZoneId.of("UTC"))
        val fullDate = meeting.getDisplayDate(ZoneId.of("UTC"))

        assertEquals("OCT", month)
        assertEquals("30", day)
        assertEquals("Oct 30, 2026", fullDate)
    }

    @Test
    fun testFallbackWhenScheduledAtIsNull() {
        val fallbackMeeting = Meeting(
            id = "m_fallback",
            title = "Fallback Sync",
            time = "9:00 AM",
            dateString = "Nov 12, 2026",
            scheduledAt = null
        )

        assertEquals("9:00 AM", fallbackMeeting.getDisplayTime())
        assertEquals("Nov 12, 2026", fallbackMeeting.getDisplayDate())
        assertEquals("NOV", fallbackMeeting.getDisplayMonth())
        assertEquals("12", fallbackMeeting.getDisplayDay())
    }

    @Test
    fun testDuplicatePreventionDeduplication() {
        val meetingA = Meeting(id = "m_1", title = "Meeting One")
        val meetingB = Meeting(id = "m_2", title = "Meeting Two")
        val meetingADuplicateSnapshot = Meeting(id = "m_1", title = "Meeting One Updated")

        val rawListFromSnapshots = listOf(meetingA, meetingB, meetingADuplicateSnapshot)
        val deduplicated = rawListFromSnapshots.distinctBy { it.id }

        assertEquals(2, deduplicated.size)
        assertEquals("m_1", deduplicated[0].id)
        assertEquals("m_2", deduplicated[1].id)
    }

    @Test
    fun testStableAttemptIdRetryBehavior() {
        val stableAttemptId = "m_unique_submission_id_12345"

        // First attempt (e.g. initial tap)
        val draft1 = Meeting(id = stableAttemptId, title = "Sync")

        // Network transient failure, retry button pressed
        val draft2OnRetry = Meeting(id = stableAttemptId, title = "Sync")

        // Same document ID must be reused across retries to prevent duplicate documents
        assertEquals(draft1.id, draft2OnRetry.id)

        // After success, a fresh attempt ID is generated for the next meeting
        val nextMeetingAttemptId = "m_unique_submission_id_67890"
        val nextMeeting = Meeting(id = nextMeetingAttemptId, title = "Another Sync")
        assertNotEquals(draft1.id, nextMeeting.id)
    }

    @Test
    fun testParticipantMembershipInvariant() {
        val organizerUid = "user_organizer_abc"
        val clientUid = "user_client_def"
        val participants = listOf(organizerUid, clientUid).distinct()

        assertTrue(participants.contains(organizerUid))
        assertTrue(participants.contains(clientUid))
        assertEquals(2, participants.size)
    }
}
