package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.MockDataProvider
import com.example.engine.TimezoneEngine
import com.example.model.AttachmentType
import com.example.model.Client
import com.example.model.FileAttachment
import com.example.model.Invoice
import com.example.model.Meeting
import com.example.model.Message
import com.example.model.NotificationItem
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.TimezoneCity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

enum class InboxFilter { ALL, UNREAD, STARRED }
enum class ProjectsFilter { ACTIVE, COMPLETED, ALL }
enum class FilesFilter { ALL, SHARED, MY_FILES }

class GlobalChatViewModel : ViewModel() {

    // Theme state (in-memory only)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun setTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    // Clients
    private val _clients = MutableStateFlow(MockDataProvider.clients)
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    fun getClient(clientId: String): Client? {
        return _clients.value.find { it.id == clientId } ?: _clients.value.firstOrNull()
    }

    // Inbox state
    private val _inboxItems = MutableStateFlow(MockDataProvider.inboxPreviews)
    val inboxItems: StateFlow<List<MockDataProvider.InboxItem>> = _inboxItems.asStateFlow()

    private val _inboxFilter = MutableStateFlow(InboxFilter.ALL)
    val inboxFilter: StateFlow<InboxFilter> = _inboxFilter.asStateFlow()

    fun setInboxFilter(filter: InboxFilter) {
        _inboxFilter.value = filter
    }

    // Messages
    private val _messages = MutableStateFlow(MockDataProvider.getInitialMessages())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun sendMessage(conversationId: String, text: String, senderName: String = "You") {
        if (text.isBlank()) return
        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = senderName,
            text = text.trim(),
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true
        )
        _messages.update { it + newMsg }

        // Update inbox preview
        _inboxItems.update { list ->
            list.map { item ->
                if (item.client.id == conversationId) {
                    item.copy(lastMessage = text.trim(), timestamp = "Just now")
                } else item
            }
        }
    }

    // Active Chat Client and Draft Message
    private val _activeChatClientId = MutableStateFlow("c1")
    val activeChatClientId: StateFlow<String> = _activeChatClientId.asStateFlow()

    fun setActiveChatClient(id: String) {
        _activeChatClientId.value = id
    }

    private val _chatDraft = MutableStateFlow("")
    val chatDraft: StateFlow<String> = _chatDraft.asStateFlow()

    fun setChatDraft(text: String) {
        _chatDraft.value = text
    }

    fun sendCurrentMessage(targetClientId: String? = null) {
        val targetId = targetClientId ?: _activeChatClientId.value
        val text = _chatDraft.value
        if (text.isNotBlank()) {
            sendMessage(targetId, text)
            _chatDraft.value = ""
        }
    }

    fun addProjectAndSendToChat(
        conversationId: String,
        title: String,
        clientName: String,
        dueDate: String = "Nov 15",
        status: ProjectStatus = ProjectStatus.IN_PROGRESS,
        colorHexLight: Long = 0xFFE2F6D5L,
        colorHexDark: Long = 0xFF22381EL
    ) {
        val newProject = Project(
            id = "p_${System.currentTimeMillis()}",
            title = title,
            clientName = clientName,
            dueDate = dueDate,
            progress = 0,
            status = status,
            cardColorHexLight = colorHexLight,
            cardColorHexDark = colorHexDark
        )
        _projects.update { it + newProject }

        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = "You",
            text = "",
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true,
            projectCard = newProject
        )
        _messages.update { it + newMsg }

        _inboxItems.update { list ->
            list.map { item ->
                if (item.client.id == conversationId) {
                    item.copy(lastMessage = "Project: $title", timestamp = "Just now")
                } else item
            }
        }
    }

    fun addMeetingAndSendToChat(
        conversationId: String,
        title: String,
        clientName: String,
        dateString: String = "Oct 28",
        time: String = "10:00 AM",
        duration: String = "30 min",
        status: String = "Confirmed",
        notes: String? = null
    ) {
        val newMeeting = Meeting(
            id = "m_${System.currentTimeMillis()}",
            title = title,
            clientName = clientName,
            time = time,
            duration = duration,
            isOnline = true,
            dateString = dateString,
            status = status,
            notes = notes
        )
        _meetings.update { it + newMeeting }

        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = "You",
            text = "",
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true,
            meetingCard = newMeeting
        )
        _messages.update { it + newMsg }

        _inboxItems.update { list ->
            list.map { item ->
                if (item.client.id == conversationId) {
                    item.copy(lastMessage = "Meeting: $title", timestamp = "Just now")
                } else item
            }
        }
    }

    fun addFileAttachmentAndSendToChat(
        conversationId: String,
        fileName: String,
        fileSize: String,
        fileType: String,
        attachmentType: AttachmentType = AttachmentType.PDF
    ) {
        val attachment = FileAttachment(
            id = "f_${System.currentTimeMillis()}",
            name = fileName,
            type = fileType,
            size = fileSize,
            attachmentType = attachmentType
        )
        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = "You",
            text = "",
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true,
            attachment = attachment
        )
        _messages.update { it + newMsg }
        _files.update { it + attachment }

        _inboxItems.update { list ->
            list.map { item ->
                if (item.client.id == conversationId) {
                    item.copy(lastMessage = "📎 $fileName", timestamp = "Just now")
                } else item
            }
        }
    }

    fun sendLinkToChat(
        conversationId: String,
        title: String,
        url: String
    ) {
        val msgText = if (title.isNotBlank()) "🔗 $title\n$url" else "🔗 $url"
        sendMessage(conversationId, msgText)
    }

    // Projects
    private val _projects = MutableStateFlow(MockDataProvider.initialProjects)
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _projectsFilter = MutableStateFlow(ProjectsFilter.ACTIVE)
    val projectsFilter: StateFlow<ProjectsFilter> = _projectsFilter.asStateFlow()

    fun setProjectsFilter(filter: ProjectsFilter) {
        _projectsFilter.value = filter
    }

    fun getProject(projectId: String): Project? {
        return _projects.value.find { it.id == projectId } ?: _projects.value.find { it.id == "p2" }
    }

    fun toggleTask(projectId: String, taskId: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val updatedTasks = proj.tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted)
                        else task
                    }
                    val completedCount = updatedTasks.count { it.isCompleted }
                    val newProgress = if (updatedTasks.isNotEmpty()) {
                        (completedCount * 100) / updatedTasks.size
                    } else proj.progress
                    proj.copy(tasks = updatedTasks, progress = newProgress)
                } else proj
            }
        }
    }

    // Files
    private val _files = MutableStateFlow(MockDataProvider.allFiles)
    val files: StateFlow<List<FileAttachment>> = _files.asStateFlow()

    private val _filesFilter = MutableStateFlow(FilesFilter.ALL)
    val filesFilter: StateFlow<FilesFilter> = _filesFilter.asStateFlow()

    fun setFilesFilter(filter: FilesFilter) {
        _filesFilter.value = filter
    }

    // Meetings
    private val _meetings = MutableStateFlow(MockDataProvider.calendarMeetings)
    val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()

    // Invoices
    private val _invoices = MutableStateFlow(MockDataProvider.initialPastInvoices)
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    fun addInvoiceAndSendToChat(
        conversationId: String,
        title: String,
        clientName: String,
        projectName: String? = null,
        amount: Double = 1250.0,
        dueDate: String = "Oct 31, 2026",
        notes: String? = null
    ) {
        val invoiceCount = _invoices.value.size + 1
        val invNumber = "INV-2024-00$invoiceCount"
        val newInvoice = Invoice(
            id = "inv_${System.currentTimeMillis()}",
            invoiceNumber = invNumber,
            title = title,
            clientName = clientName,
            projectName = projectName,
            amount = amount,
            currency = "USD",
            dueDate = dueDate,
            notes = notes,
            isPaid = false,
            createdAt = "Today"
        )
        _invoices.update { it + newInvoice }

        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = "You",
            text = "",
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true,
            invoiceCard = newInvoice
        )
        _messages.update { it + newMsg }

        _inboxItems.update { list ->
            list.map { item ->
                if (item.client.id == conversationId) {
                    item.copy(lastMessage = "Invoice: $title ($${String.format(java.util.Locale.US, "%,.2f", amount)})", timestamp = "Just now")
                } else item
            }
        }
    }

    fun markInvoicePaid(invoiceId: String) {
        val now = java.time.ZonedDateTime.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", java.util.Locale.US)
        val paidTimestamp = "Paid on ${now.format(formatter)}"
        _invoices.update { list ->
            list.map { if (it.id == invoiceId) it.copy(isPaid = true, paidAt = paidTimestamp) else it }
        }
        _messages.update { list ->
            list.map { msg ->
                if (msg.invoiceCard?.id == invoiceId) {
                    msg.copy(invoiceCard = msg.invoiceCard.copy(isPaid = true, paidAt = paidTimestamp))
                } else msg
            }
        }
    }

    // Notifications
    private val _notifications = MutableStateFlow(MockDataProvider.initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAllNotificationsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    // Global Timezones
    private val _cities = MutableStateFlow(MockDataProvider.globalCities)
    val cities: StateFlow<List<TimezoneCity>> = _cities.asStateFlow()

    // Scheduled messages in-memory confirmation
    private val _lastScheduledNotice = MutableStateFlow<String?>(null)
    val lastScheduledNotice: StateFlow<String?> = _lastScheduledNotice.asStateFlow()

    fun scheduleMessage(client: Client, messageText: String, dateString: String) {
        val notice = "Message scheduled for ${client.name} on $dateString"
        _lastScheduledNotice.value = notice
    }

    fun clearScheduledNotice() {
        _lastScheduledNotice.value = null
    }

    // Debug showcase switcher state
    private val _isDebugDialogOpen = MutableStateFlow(false)
    val isDebugDialogOpen: StateFlow<Boolean> = _isDebugDialogOpen.asStateFlow()

    fun openDebugDialog() {
        _isDebugDialogOpen.value = true
    }

    fun closeDebugDialog() {
        _isDebugDialogOpen.value = false
    }
}
