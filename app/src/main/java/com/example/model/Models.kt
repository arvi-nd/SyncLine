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

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val title: String,
    val clientName: String,
    val projectName: String? = null,
    val amount: Double,
    val currency: String = "USD",
    val dueDate: String,
    val notes: String? = null,
    val isPaid: Boolean = false,
    val paidAt: String? = null,
    val createdAt: String = "Today"
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isOutgoing: Boolean,
    val isRead: Boolean = true,
    val attachment: FileAttachment? = null,
    val projectCard: Project? = null,
    val meetingCard: Meeting? = null,
    val invoiceCard: Invoice? = null
)

enum class ProjectStatus(val label: String) {
    IN_PROGRESS("In Progress"),
    PLANNING("Planning"),
    COMPLETED("Completed")
}

data class ProjectTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean
)

data class Project(
    val id: String,
    val title: String,
    val clientName: String,
    val dueDate: String,
    val progress: Int, // 0 - 100
    val status: ProjectStatus,
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

data class Meeting(
    val id: String,
    val title: String,
    val clientName: String,
    val time: String,
    val duration: String,
    val isOnline: Boolean = true,
    val dateString: String = "Oct 28",
    val status: String = "Confirmed",
    val notes: String? = null
)

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
