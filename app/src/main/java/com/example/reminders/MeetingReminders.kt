package com.example.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.MainActivity
import com.example.model.Meeting
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Local reminders shown shortly before a meeting starts. Alarms are rebuilt every time the
 * meeting list changes, so edits and cancellations are picked up automatically.
 */
object MeetingReminders {
    const val CHANNEL_ID = "meeting_reminders"
    const val DEFAULT_LEAD_MINUTES = 15
    val leadOptions = listOf(0, 5, 15, 30, 60) // 0 = off

    private const val PREFS = "syncline_prefs"
    private const val KEY_LEAD = "reminder_lead_min"
    private const val KEY_SCHEDULED = "reminder_ids"
    private const val EXTRA_ID = "meeting_id"
    private const val EXTRA_TITLE = "meeting_title"
    private const val EXTRA_START = "meeting_start"

    fun leadMinutes(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LEAD, DEFAULT_LEAD_MINUTES)

    fun setLeadMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_LEAD, minutes).apply()
    }

    fun label(minutes: Int): String = if (minutes <= 0) "Off" else "$minutes min before"

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Schedules a reminder for every upcoming, active meeting and removes the ones that no longer apply. */
    fun sync(context: Context, meetings: List<Meeting>) {
        val app = context.applicationContext
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val alarms = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val lead = leadMinutes(app)
        val now = System.currentTimeMillis()

        val wanted = if (lead <= 0) emptyList() else meetings.filter { m ->
            val start = m.scheduledAt?.toDate()?.time ?: return@filter false
            !m.status.equals("Cancelled", true) && !m.status.equals("Declined", true) &&
                start - lead * 60_000L > now
        }
        val wantedIds = wanted.map { it.id }.toSet()

        // Drop alarms for meetings that were cancelled, moved into the past or deleted.
        val previous = prefs.getStringSet(KEY_SCHEDULED, emptySet()) ?: emptySet()
        (previous - wantedIds).forEach { id -> alarms.cancel(pendingIntent(app, id, "", 0L)) }

        wanted.forEach { m ->
            val start = m.scheduledAt!!.toDate().time
            alarms.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                start - lead * 60_000L,
                pendingIntent(app, m.id, m.title, start)
            )
        }
        prefs.edit().putStringSet(KEY_SCHEDULED, wantedIds).apply()
    }

    private fun pendingIntent(context: Context, id: String, title: String, start: Long): PendingIntent {
        val intent = Intent(context, MeetingReminderReceiver::class.java)
            .putExtra(EXTRA_ID, id).putExtra(EXTRA_TITLE, title).putExtra(EXTRA_START, start)
        return PendingIntent.getBroadcast(
            context, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    internal fun show(context: Context, id: String, title: String, start: Long) {
        if (!canNotify(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Meeting reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val minutes = ((start - System.currentTimeMillis()) / 60_000L).coerceAtLeast(0)
        val at = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL_ID)
        else @Suppress("DEPRECATION") Notification.Builder(context)
        manager.notify(
            id.hashCode(),
            builder
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title.ifBlank { "Meeting" })
                .setContentText(if (minutes <= 1) "Starting now · $at" else "Starts in $minutes min · $at")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
        )
    }

    internal fun readExtras(intent: Intent) =
        Triple(intent.getStringExtra(EXTRA_ID).orEmpty(), intent.getStringExtra(EXTRA_TITLE).orEmpty(), intent.getLongExtra(EXTRA_START, 0L))
}

class MeetingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val (id, title, start) = MeetingReminders.readExtras(intent)
        if (id.isNotBlank() && start > 0L) MeetingReminders.show(context, id, title, start)
    }
}
