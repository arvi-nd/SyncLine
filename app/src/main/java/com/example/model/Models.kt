package com.example.model

import androidx.compose.ui.graphics.Color

data class Client(
    val id: String,
    val name: String,
    val firstName: String = name.substringBefore(" "),
    val company: String,
    val role: String,
    val country: String,
    val city: String = "",
    val countryCode: String,
    val flag: String,
    val timezoneId: String, // e.g. "Europe/London"
    val localTimeStr: String = "12:45 PM",
    val statusStr: String = "Working",
    val workingHours: String = "9:00 AM - 6:00 PM",
    val avatarBg: Color,
    val avatarInitial: String,
    val isOnline: Boolean,
    val lastSeen: String = "Online"
)

enum class AttachmentType {
    PDF, DOCX, ZIP, TXT, FIGMA, FOLDER, IMAGE
}

data class FileAttachment(
    val id: String,
    val name: String,
    val type: String, // "PDF", "DOCX", etc.
    val size: String,
    val attachmentType: AttachmentType,
    val itemCount: Int? = null
)

@com.google.firebase.firestore.IgnoreExtraProperties
data class Invoice(
    @com.google.firebase.firestore.DocumentId val id: String = "",
    val invoiceNumber: String = "",
    val title: String = "",
    val clientName: String = "",
    val clientUid: String = "",
    val organizerUid: String = "",
    val participantUids: List<String> = emptyList(),
    val projectId: String? = null,
    val projectName: String? = null,
    val amount: Double = 0.0,
    val currencyCode: String = "INR",
    val currency: String = "INR",
    val dueDate: String = "",
    val notes: String? = null,
    val status: String = "UNPAID",
    val isPaid: Boolean = false,
    val paidAt: String? = null,
    val createdAt: String = "Today",
    @com.google.firebase.firestore.ServerTimestamp val createdAtTs: com.google.firebase.Timestamp? = null
)

fun Invoice.getCurrencySymbol(): String {
    return when (currencyCode.uppercase()) {
        "INR", "₹" -> "₹"
        "GBP", "£" -> "£"
        "USD", "$" -> "$"
        else -> when (currency.uppercase()) {
            "INR", "₹" -> "₹"
            "GBP", "£" -> "£"
            else -> "$"
        }
    }
}

fun Invoice.formatAmount(): String {
    val symbol = getCurrencySymbol()
    return String.format(java.util.Locale.US, "%s%,.2f", symbol, amount)
}

data class ProjectPreview(
    val id: String = "",
    val title: String = "",
    val clientName: String = "",
    val dueDate: String = "",
    val progress: Int = 0,
    val status: String = "IN_PROGRESS",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isOutgoing: Boolean,
    val isRead: Boolean = true,
    val type: String = "text",
    val projectId: String? = null,
    val projectPreview: ProjectPreview? = null,
    val attachment: FileAttachment? = null,
    val projectCard: Project? = null,
    val meetingCard: Meeting? = null,
    val invoiceId: String? = null,
    val invoiceCard: Invoice? = null,
    val status: String = "sent", // sending, sent, failed
    val sentAtMillis: Long = 0L,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToName: String? = null,
    val deleted: Boolean = false
)

enum class ProjectStatus(val label: String) {
    IN_PROGRESS("In Progress"),
    PLANNING("Planning"),
    COMPLETED("Completed")
}

data class ProjectTask(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false
)

data class Project(
    val id: String = "",
    val ownerUid: String = "",
    val clientUid: String = "",
    val participantUids: List<String> = emptyList(),
    val title: String = "",
    val clientName: String = "",
    val description: String = "",
    val startDate: String = "",
    val dueDate: String = "",
    val progress: Int = 0, // 0 - 100
    val status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val tasks: List<ProjectTask> = emptyList(),
    val files: List<FileAttachment> = emptyList(),
    val cardColorHexLight: Long = 0xFFE2F6D5L,
    val cardColorHexDark: Long = 0xFF22381EL
)

data class TimezoneCity(
    val id: String,
    val cityName: String,
    val countryName: String,
    val timezoneId: String,
    val flag: String,
    val isUserLocation: Boolean = false
)

@com.google.firebase.firestore.IgnoreExtraProperties
data class Meeting(
    @com.google.firebase.firestore.DocumentId val id: String = "",
    val title: String = "",
    val clientName: String = "",
    val time: String = "",
    val duration: String = "",
    val isOnline: Boolean = true,
    val dateString: String = "",
    val status: String = "Confirmed",
    val notes: String? = null,
    val organizerUid: String = "",
    val participantUids: List<String> = emptyList(),
    val scheduledAt: com.google.firebase.Timestamp? = null,
    val durationMinutes: Int = 30,
    @com.google.firebase.firestore.ServerTimestamp val createdAt: com.google.firebase.Timestamp? = null
)

fun Meeting.getDisplayTime(zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String {
    val ts = scheduledAt ?: return time
    val zdt = ts.toDate().toInstant().atZone(zoneId)
    val hour = zdt.hour
    val minute = zdt.minute
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = minute.toString().padStart(2, '0')
    return "$displayHour:$displayMinute $amPm"
}

fun Meeting.getDisplayDate(zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String {
    val ts = scheduledAt ?: return dateString
    val zdt = ts.toDate().toInstant().atZone(zoneId)
    return java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US).format(zdt)
}

fun Meeting.getDisplayMonth(zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String {
    val ts = scheduledAt ?: return dateString.trim().split(" ", ",").firstOrNull()?.take(3)?.uppercase() ?: "OCT"
    val zdt = ts.toDate().toInstant().atZone(zoneId)
    return java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.US).format(zdt).uppercase()
}

fun Meeting.getDisplayDay(zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String {
    val ts = scheduledAt ?: return dateString.trim().split(" ", ",").getOrNull(1)?.filter { it.isDigit() }?.ifBlank { "1" } ?: "1"
    val zdt = ts.toDate().toInstant().atZone(zoneId)
    return java.time.format.DateTimeFormatter.ofPattern("d", java.util.Locale.US).format(zdt)
}

enum class NotificationType {
    MESSAGE, MENTION, PROJECT_UPDATE, FILE, SYSTEM
}

data class NotificationItem(
    val id: String,
    val sender: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val isRead: Boolean,
    val type: NotificationType,
    val avatarBg: Color? = null,
    val avatarInitial: String = ""
)
