package com.example.engine

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimezoneEngine {

    val TIME_FORMAT_12H: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)

    /**
     * Formats current time for a given IANA Zone ID.
     */
    fun formatCurrentTime(zoneId: String, instant: Instant = Instant.now()): String {
        return try {
            val zdt = instant.atZone(ZoneId.of(zoneId))
            zdt.format(TIME_FORMAT_12H)
        } catch (_: Exception) {
            "12:00 PM"
        }
    }

    /**
     * Returns the formatted time string with timezone code and UTC offset.
     * e.g., "2:24 PM · EST (UTC -5)" or "7:24 PM · BST (UTC +1)"
     */
    fun formatTimeWithZoneAndOffset(zoneId: String, instant: Instant = Instant.now()): String {
        return try {
            val zone = ZoneId.of(zoneId)
            val zdt = instant.atZone(zone)
            val time = zdt.format(TIME_FORMAT_12H)
            val offset = zdt.offset.id
            val formattedOffset = if (offset == "Z") "UTC +0" else "UTC ${offset.replace(":00", "")}"
            val zoneShort = zone.rules.getStandardOffset(instant).let {
                // Short display or name
                when (zoneId) {
                    "America/New_York" -> if (zone.rules.isDaylightSavings(instant)) "EDT" else "EST"
                    "Europe/London" -> if (zone.rules.isDaylightSavings(instant)) "BST" else "GMT"
                    "Europe/Berlin" -> if (zone.rules.isDaylightSavings(instant)) "CEST" else "CET"
                    "Asia/Kolkata" -> "IST"
                    "Australia/Sydney" -> if (zone.rules.isDaylightSavings(instant)) "AEDT" else "AEST"
                    else -> zdt.zone.id.substringAfterLast("/")
                }
            }
            "$time · $zoneShort ($formattedOffset)"
        } catch (_: Exception) {
            "12:00 PM"
        }
    }

    /**
     * Determines whether the current local time in that zone is daytime (6:00 AM - 6:00 PM).
     */
    fun isDaytime(zoneId: String, instant: Instant = Instant.now()): Boolean {
        return try {
            val zdt = instant.atZone(ZoneId.of(zoneId))
            val hour = zdt.hour
            hour in 6..17
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Checks if given instant is on a weekend in that time zone.
     */
    fun isWeekend(zoneId: String, instant: Instant = Instant.now()): Boolean {
        return try {
            val zdt = instant.atZone(ZoneId.of(zoneId))
            zdt.dayOfWeek == DayOfWeek.SATURDAY || zdt.dayOfWeek == DayOfWeek.SUNDAY
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Calculates the shared working hour overlap between user's zone and client's zone.
     * Default work window is 9:00 AM to 6:00 PM in each party's local time.
     * Returns human-readable summary, e.g., "2h 30m overlap" or "No overlap".
     */
    fun calculateWorkingHoursOverlap(
        userZoneId: String,
        clientZoneId: String,
        workStartHour: Int = 9,
        workEndHour: Int = 18,
        date: LocalDate = LocalDate.now()
    ): OverlapResult {
        return try {
            val userZone = ZoneId.of(userZoneId)
            val clientZone = ZoneId.of(clientZoneId)

            // User's working interval in UTC
            val userStart = date.atTime(LocalTime.of(workStartHour, 0)).atZone(userZone).toInstant()
            val userEnd = date.atTime(LocalTime.of(workEndHour, 0)).atZone(userZone).toInstant()

            // Client's working interval in UTC
            val clientStart = date.atTime(LocalTime.of(workStartHour, 0)).atZone(clientZone).toInstant()
            val clientEnd = date.atTime(LocalTime.of(workEndHour, 0)).atZone(clientZone).toInstant()

            val overlapStart = maxOf(userStart, clientStart)
            val overlapEnd = minOf(userEnd, clientEnd)

            if (overlapStart.isBefore(overlapEnd)) {
                val duration = Duration.between(overlapStart, overlapEnd)
                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60
                val text = if (minutes == 0L) "${hours}h overlap" else "${hours}h ${minutes}m overlap"

                // Overlap window in user's local time
                val startLocal = overlapStart.atZone(userZone).format(TIME_FORMAT_12H)
                val endLocal = overlapEnd.atZone(userZone).format(TIME_FORMAT_12H)
                val windowText = "$startLocal - $endLocal"

                OverlapResult(
                    overlapDurationMinutes = duration.toMinutes(),
                    formattedDuration = text,
                    localWindow = windowText,
                    hasOverlap = true
                )
            } else {
                OverlapResult(
                    overlapDurationMinutes = 0,
                    formattedDuration = "No working hour overlap",
                    localWindow = "Outside shared hours",
                    hasOverlap = false
                )
            }
        } catch (_: Exception) {
            OverlapResult(0, "Overlap unavailable", "", false)
        }
    }

    /**
     * Converts a local time from recipient's time zone to user's time zone.
     * e.g., "May 29, 2025 · 9:00 AM (EST) = 6:30 PM (IST)"
     */
    fun convertScheduledTime(
        recipientZoneId: String,
        userZoneId: String,
        scheduledInstant: Instant
    ): String {
        return try {
            val recipientZone = ZoneId.of(recipientZoneId)
            val userZone = ZoneId.of(userZoneId)

            val recTime = scheduledInstant.atZone(recipientZone).format(TIME_FORMAT_12H)
            val recCode = when (recipientZoneId) {
                "America/New_York" -> "EST"
                "Europe/London" -> "BST"
                "Europe/Berlin" -> "CEST"
                "Asia/Kolkata" -> "IST"
                "Australia/Sydney" -> "AEST"
                else -> recipientZone.id.substringAfterLast("/")
            }

            val usrTime = scheduledInstant.atZone(userZone).format(TIME_FORMAT_12H)
            val usrCode = when (userZoneId) {
                "Asia/Kolkata" -> "IST"
                "America/New_York" -> "EST"
                "Europe/London" -> "BST"
                else -> userZone.id.substringAfterLast("/")
            }

            "Will be sent at $recTime ($recCode) = $usrTime ($usrCode)"
        } catch (_: Exception) {
            "Scheduled delivery"
        }
    }

    data class OverlapResult(
        val overlapDurationMinutes: Long,
        val formattedDuration: String,
        val localWindow: String,
        val hasOverlap: Boolean
    )
}
