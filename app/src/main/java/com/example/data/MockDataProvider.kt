package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.AttachmentType
import com.example.model.Client
import com.example.model.FileAttachment
import com.example.model.Invoice
import com.example.model.Meeting
import com.example.model.Message
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.ProjectTask
import com.example.model.TimezoneCity

object MockDataProvider {

    val clients = listOf(
        Client(
            id = "c1",
            name = "Sarah Jenkins",
            firstName = "Sarah",
            company = "Acme Co.",
            role = "Lead Product Designer",
            country = "United Kingdom",
            city = "London",
            countryCode = "GB",
            flag = "🇬🇧",
            timezoneId = "Europe/London",
            localTimeStr = "12:45 PM",
            statusStr = "Working",
            workingHours = "Mon - Fri, 9:00 AM - 6:00 PM",
            avatarBg = Color(0xFF9FE870), // Lime green as in prototype
            avatarInitial = "SJ",
            isOnline = true,
            lastSeen = "Online"
        ),
        Client(
            id = "c2",
            name = "Kenji Sato",
            firstName = "Kenji",
            company = "Sato Interactive",
            role = "Senior Mobile Engineer",
            country = "Japan",
            city = "Tokyo",
            countryCode = "JP",
            flag = "🇯🇵",
            timezoneId = "Asia/Tokyo",
            localTimeStr = "8:45 PM",
            statusStr = "After hours",
            workingHours = "Mon - Fri, 10:00 AM - 7:00 PM",
            avatarBg = Color(0xFFF0F3EC),
            avatarInitial = "KS",
            isOnline = false,
            lastSeen = "1h ago"
        ),
        Client(
            id = "c3",
            name = "Elena Rostova",
            firstName = "Elena",
            company = "Berlin Design Studio",
            role = "VP of Product",
            country = "Germany",
            city = "Berlin",
            countryCode = "DE",
            flag = "🇩🇪",
            timezoneId = "Europe/Berlin",
            localTimeStr = "1:45 PM",
            statusStr = "Working",
            workingHours = "Mon - Fri, 9:00 AM - 5:00 PM",
            avatarBg = Color(0xFFF0F3EC),
            avatarInitial = "ER",
            isOnline = true,
            lastSeen = "Online"
        ),
        Client(
            id = "c4",
            name = "David Miller",
            firstName = "David",
            company = "Miller Media",
            role = "Design Partner",
            country = "United States",
            city = "New York",
            countryCode = "US",
            flag = "🇺🇸",
            timezoneId = "America/New_York",
            localTimeStr = "4:45 AM",
            statusStr = "Sleeping",
            workingHours = "Mon - Fri, 9:00 AM - 6:00 PM",
            avatarBg = Color(0xFFF0F3EC),
            avatarInitial = "DM",
            isOnline = false,
            lastSeen = "4h ago"
        )
    )

    fun getInitialMessages(): List<Message> = listOf(
        Message(
            id = "m1",
            conversationId = "c1",
            senderName = "Sarah Jenkins",
            text = "Hi! The design looks great. Just a few minor changes and we're good to go.",
            timestamp = "10:24 AM",
            isOutgoing = false
        ),
        Message(
            id = "m2",
            conversationId = "c1",
            senderName = "You",
            text = "Perfect, I've updated the files and attached the revised version.",
            timestamp = "10:32 AM",
            isOutgoing = true
        ),
        Message(
            id = "m3",
            conversationId = "c1",
            senderName = "You",
            text = "",
            timestamp = "10:32 AM",
            isOutgoing = true,
            attachment = FileAttachment(
                id = "att1",
                name = "Design_Final_v2.pdf",
                type = "PDF",
                size = "2.4 MB",
                attachmentType = AttachmentType.PDF
            )
        ),
        Message(
            id = "m4",
            conversationId = "c1",
            senderName = "Sarah Jenkins",
            text = "Sent revised invoice for Milestone 3 wireframes & design system assets.",
            timestamp = "2m ago",
            isOutgoing = false,
            invoiceCard = Invoice(
                id = "inv_mock_sarah",
                invoiceNumber = "INV-2024-003",
                title = "Milestone 3 — Wireframes & UI Kit",
                clientName = "Sarah Jenkins",
                projectName = "Mobile Redesign",
                amount = 1850.00,
                currency = "USD",
                dueDate = "Nov 15, 2026",
                notes = "Deliverables: Figma component library, wireframes v3",
                isPaid = false,
                createdAt = "Oct 24"
            )
        )
    )

    val initialPastInvoices = listOf(
        Invoice(
            id = "inv_past_1",
            invoiceNumber = "INV-2024-001",
            title = "Brand Identity & Guidelines",
            clientName = "Sarah Jenkins",
            projectName = "Mobile Redesign",
            amount = 2400.00,
            currency = "USD",
            dueDate = "Oct 15, 2026",
            notes = "Paid via Wise Direct Debit",
            isPaid = true,
            paidAt = "Oct 14, 2026",
            createdAt = "Oct 01"
        ),
        Invoice(
            id = "inv_past_2",
            invoiceNumber = "INV-2024-002",
            title = "Mobile UI Assets & Export",
            clientName = "Sarah Jenkins",
            projectName = "Mobile Redesign",
            amount = 1150.00,
            currency = "USD",
            dueDate = "Oct 22, 2026",
            notes = "Transferred to Wise account",
            isPaid = true,
            paidAt = "Oct 21, 2026",
            createdAt = "Oct 12"
        )
    )

    val inboxPreviews = listOf(
        InboxItem(
            client = clients[0],
            lastMessage = "Sent revised invoice for Milestone 3 wiref...",
            timestamp = "2m ago",
            statusText = "London · 12:45 PM (Working)",
            unreadCount = 1,
            isStarred = true
        ),
        InboxItem(
            client = clients[1],
            lastMessage = "The Compose animation tokens match Figma!",
            timestamp = "1h ago",
            statusText = "Tokyo · 8:45 PM (After hours)",
            unreadCount = 0,
            isStarred = true
        ),
        InboxItem(
            client = clients[2],
            lastMessage = "Approved Milestone 1 payout ($3,000 USD via ...",
            timestamp = "3h ago",
            statusText = "Berlin · 1:45 PM (Working)",
            unreadCount = 0,
            isStarred = false
        ),
        InboxItem(
            client = clients[3],
            lastMessage = "Will review the wireframes first thing in the morning.",
            timestamp = "4h ago",
            statusText = "New York · 4:45 AM (Sleeping)",
            unreadCount = 0,
            isStarred = false
        )
    )

    data class InboxItem(
        val client: Client,
        val lastMessage: String,
        val timestamp: String,
        val statusText: String,
        val unreadCount: Int,
        val isStarred: Boolean
    )

    val initialProjects = listOf(
        Project(
            id = "p1",
            title = "Mobile Redesign",
            clientName = "Acme Co.",
            dueDate = "Due May 30, 2025",
            progress = 0,
            status = ProjectStatus.IN_PROGRESS,
            tasks = listOf(
                ProjectTask("t1", "Design mockups", true),
                ProjectTask("t2", "Client feedback", true),
                ProjectTask("t3", "Final files", false),
                ProjectTask("t4", "Project delivery", false)
            )
        ),
        Project(
            id = "p2",
            title = "Website Redesign",
            clientName = "Acme Co.",
            dueDate = "Due May 30, 2025",
            progress = 65,
            status = ProjectStatus.IN_PROGRESS,
            tasks = listOf(
                ProjectTask("wt1", "Design mockups", true),
                ProjectTask("wt2", "Client feedback", true),
                ProjectTask("wt3", "Final files", false),
                ProjectTask("wt4", "Project delivery", false)
            ),
            files = listOf(
                FileAttachment("f1", "design_v1.fig", "FIGMA", "4.2 MB", AttachmentType.FIGMA),
                FileAttachment("f2", "brand_assets.zip", "ZIP", "12.4 MB", AttachmentType.ZIP)
            )
        ),
        Project(
            id = "p3",
            title = "Brand Strategy",
            clientName = "Nova Studio",
            dueDate = "Due Jun 10, 2025",
            progress = 0,
            status = ProjectStatus.PLANNING,
            tasks = listOf(
                ProjectTask("bt1", "Discovery call", true),
                ProjectTask("bt2", "Market research", false),
                ProjectTask("bt3", "Tone guidelines", false)
            )
        ),
        Project(
            id = "p4",
            title = "Social Media Kit",
            clientName = "Lumen Co.",
            dueDate = "Due May 25, 2025",
            progress = 100,
            status = ProjectStatus.COMPLETED,
            tasks = listOf(
                ProjectTask("st1", "Templates design", true),
                ProjectTask("st2", "Asset export", true)
            )
        )
    )

    val allFiles = listOf(
        FileAttachment("fl1", "Design_Final_v2.pdf", "PDF", "2.4 MB", AttachmentType.PDF),
        FileAttachment("fl2", "Project_Brief.docx", "DOCX", "1.2 MB", AttachmentType.DOCX),
        FileAttachment("fl3", "Logo_Assets.zip", "ZIP", "8.7 MB", AttachmentType.ZIP),
        FileAttachment("fl4", "Meeting_Notes.txt", "TXT", "324 KB", AttachmentType.TXT),
        FileAttachment("fl5", "Reference_Images", "Folder", "12 items", AttachmentType.FOLDER, itemCount = 12)
    )

    val globalCities = listOf(
        TimezoneCity("tz1", "London", "United Kingdom", "Europe/London", "🇬🇧"),
        TimezoneCity("tz2", "Berlin", "Germany", "Europe/Berlin", "🇩🇪"),
        TimezoneCity("tz3", "Tokyo", "Japan", "Asia/Tokyo", "🇯🇵"),
        TimezoneCity("tz4", "New York", "United States", "America/New_York", "🇺🇸"),
        TimezoneCity("tz5", "New Delhi", "India", "Asia/Kolkata", "🇮🇳", isUserLocation = true),
        TimezoneCity("tz6", "Sydney", "Australia", "Australia/Sydney", "🇦🇺")
    )

    val calendarMeetings = listOf(
        Meeting(
            id = "mt1",
            title = "Client Call - Sarah Jenkins",
            clientName = "Sarah Jenkins",
            time = "12:45 PM",
            duration = "30m",
            isOnline = true,
            dateString = "2025-05-28"
        ),
        Meeting(
            id = "mt2",
            title = "Design Review - Acme Co.",
            clientName = "Acme Co.",
            time = "2:00 PM",
            duration = "1h",
            isOnline = true,
            dateString = "2025-05-28"
        )
    )

    val initialNotifications = listOf(
        NotificationItem(
            id = "n1",
            sender = "Sarah Jenkins",
            title = "Sarah Jenkins",
            description = "New message",
            timestamp = "2m",
            isRead = false,
            type = NotificationType.MESSAGE,
            avatarBg = Color(0xFF9FE870),
            avatarInitial = "SJ"
        ),
        NotificationItem(
            id = "n2",
            sender = "Kenji Sato",
            title = "Kenji Sato",
            description = "mentioned you in project",
            timestamp = "1h",
            isRead = false,
            type = NotificationType.MENTION,
            avatarBg = Color(0xFFF0F3EC),
            avatarInitial = "KS"
        ),
        NotificationItem(
            id = "n3",
            sender = "Elena Rostova",
            title = "Elena Rostova",
            description = "Approved Milestone 1 payout",
            timestamp = "3h",
            isRead = false,
            type = NotificationType.PROJECT_UPDATE,
            avatarBg = Color(0xFF9FE870),
            avatarInitial = "ER"
        )
    )
}
