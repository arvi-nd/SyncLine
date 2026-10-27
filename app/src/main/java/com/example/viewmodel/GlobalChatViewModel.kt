package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MockDataProvider
import com.example.data.UserRepository
import com.example.engine.TimezoneEngine
import com.example.model.AttachmentType
import com.example.model.ChatRequest
import com.example.model.Client
import com.example.model.FileAttachment
import com.example.model.Invoice
import com.example.model.Meeting
import com.example.model.Message
import com.example.model.NotificationItem
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.model.TimezoneCity
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

enum class InboxFilter { ALL, UNREAD, STARRED }
enum class ProjectsFilter { ACTIVE, COMPLETED, ALL }
enum class FilesFilter { ALL, SHARED, MY_FILES }

class GlobalChatViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val userRepository by lazy { UserRepository(getApplication<Application>()) }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _chatRequests = MutableStateFlow<List<ChatRequest>>(emptyList())
    val chatRequests: StateFlow<List<ChatRequest>> = _chatRequests.asStateFlow()

    private var profileJob: Job? = null
    private var requestsJob: Job? = null

    init {
        try {
            val user = auth.currentUser
            _currentUser.value = user
            if (user == null) {
                clearUserData()
            } else {
                syncUserProfile(user)
            }
            auth.addAuthStateListener { firebaseAuth ->
                val newUser = firebaseAuth.currentUser
                _currentUser.value = newUser
                if (newUser == null) {
                    clearUserData()
                } else {
                    syncUserProfile(newUser)
                }
            }
        } catch (_: Exception) {}
    }

    private fun syncUserProfile(user: FirebaseUser?) {
        profileJob?.cancel()
        if (user == null) {
            _userProfile.value = null
            return
        }

        val currentUid = user.uid
        val email = user.email ?: ""
        val displayName = user.displayName ?: ""

        profileJob = viewModelScope.launch {
            val profile = userRepository.ensureProfile(
                uid = currentUid,
                email = email,
                fallbackName = displayName
            )
            if (auth.currentUser?.uid == currentUid) {
                _userProfile.value = profile
            }

            userRepository.observeProfile(currentUid).collect { updatedProfile ->
                if (auth.currentUser?.uid == currentUid && updatedProfile != null) {
                    _userProfile.value = updatedProfile
                }
            }
        }

        syncUserClients(user)
        syncChatRequests(user)
    }

    private fun syncChatRequests(user: FirebaseUser?) {
        requestsJob?.cancel()
        if (user == null) {
            _chatRequests.value = emptyList()
            return
        }

        val currentUid = user.uid
        requestsJob = viewModelScope.launch {
            userRepository.observeChatRequests(currentUid).collect { requests ->
                if (auth.currentUser?.uid == currentUid) {
                    _chatRequests.value = requests
                }
            }
        }
    }

    private var clientsJob: Job? = null

    private fun syncUserClients(user: FirebaseUser?) {
        clientsJob?.cancel()
        if (user == null) return

        val currentUid = user.uid
        clientsJob = viewModelScope.launch {
            userRepository.observeUserClients(currentUid).collect { clientProfiles ->
                if (auth.currentUser?.uid != currentUid) return@collect

                val updatedClients = clientProfiles.map { profile ->
                    val initial = profile.displayName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2).joinToString("").uppercase()
                        .ifBlank { profile.username.take(2).uppercase().ifBlank { "CL" } }

                    Client(
                        id = profile.uid,
                        name = profile.displayName.ifBlank { profile.username.ifBlank { "Client" } },
                        firstName = profile.displayName.substringBefore(" "),
                        company = "SyncLine Partner",
                        role = "Client",
                        country = "United States",
                        city = "New York",
                        countryCode = "US",
                        flag = "🇺🇸",
                        timezoneId = "America/New_York",
                        localTimeStr = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
                        statusStr = "Working",
                        workingHours = "Mon - Fri, 9:00 AM - 6:00 PM",
                        avatarBg = androidx.compose.ui.graphics.Color(0xFF9FE870),
                        avatarInitial = initial,
                        isOnline = true,
                        lastSeen = "Online"
                    )
                }

                _clients.value = updatedClients

                val updatedInbox = updatedClients.map { client ->
                    val lastMsg = _messages.value.filter { it.conversationId == client.id }
                        .lastOrNull()?.text ?: "Chat request accepted"
                    MockDataProvider.InboxItem(
                        client = client,
                        lastMessage = lastMsg,
                        timestamp = "Just now",
                        statusText = "New York · Active",
                        unreadCount = 0,
                        isStarred = false
                    )
                }
                _inboxItems.value = updatedInbox
            }
        }
    }

    fun addClientByUsername(
        username: String,
        onError: (String) -> Unit,
        onSuccess: (clientUid: String) -> Unit
    ) {
        val cleanUsername = username.trim().lowercase()
        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid == null) {
            onError("You must be signed in to add a client.")
            return
        }

        val senderProfile = _userProfile.value ?: UserProfile(
            uid = currentUserUid,
            email = auth.currentUser?.email ?: "",
            displayName = auth.currentUser?.displayName ?: "User",
            username = auth.currentUser?.email?.substringBefore("@") ?: "user"
        )

        viewModelScope.launch {
            try {
                val targetProfile = userRepository.getUserByUsername(cleanUsername)
                if (targetProfile == null) {
                    onError("User '@$cleanUsername' not found.")
                    return@launch
                }

                if (targetProfile.uid == currentUserUid) {
                    onError("You cannot add yourself as a client.")
                    return@launch
                }

                val isAlreadyClient = _clients.value.any { it.id == targetProfile.uid }
                if (isAlreadyClient) {
                    onError("'@$cleanUsername' is already in your client list.")
                    return@launch
                }

                val err = userRepository.sendChatRequest(senderProfile, targetProfile)
                if (err == null) {
                    onSuccess(targetProfile.uid)
                } else {
                    onError(err)
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Network error during request submission.")
            }
        }
    }

    fun acceptChatRequest(request: ChatRequest) {
        viewModelScope.launch {
            userRepository.acceptChatRequest(request)
        }
    }

    fun declineChatRequest(requestId: String) {
        viewModelScope.launch {
            userRepository.declineChatRequest(requestId)
        }
    }

    val isUserAuthenticated: Boolean
        get() = try { auth.currentUser != null } catch (_: Exception) { false }

    fun signInWithEmail(
        emailStr: String,
        passStr: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = emailStr.trim()
        val cleanPass = passStr.trim()
        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            onError("Please enter both email address and password.")
            return
        }
        try {
            auth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exc ->
                    val msg = exc.localizedMessage ?: "Authentication failed."
                    if (msg.contains("no user record", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true)) {
                        auth.createUserWithEmailAndPassword(cleanEmail, cleanPass)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { createExc ->
                                onError(createExc.localizedMessage ?: "Could not create account.")
                            }
                    } else {
                        onError(msg)
                    }
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Sign in error.")
        }
    }

    fun signUpWithEmail(
        emailStr: String,
        passStr: String,
        usernameStr: String,
        fullNameStr: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = emailStr.trim()
        val cleanPass = passStr.trim()
        val cleanUsername = usernameStr.trim().lowercase()
        val cleanName = fullNameStr.trim()

        if (cleanName.isEmpty()) {
            onError("Please enter your full name.")
            return
        }
        if (cleanUsername.length < 3) {
            onError("Username must be at least 3 characters long.")
            return
        }
        if (!cleanUsername.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
            onError("Username can only contain letters, numbers, underscores, and dots.")
            return
        }
        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            onError("Please enter email address and password.")
            return
        }
        if (cleanPass.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            val isAvailable = userRepository.isUsernameAvailable(cleanUsername)
            if (!isAvailable) {
                onError("Username '$cleanUsername' is already taken.")
                return@launch
            }

            try {
                auth.createUserWithEmailAndPassword(cleanEmail, cleanPass)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            viewModelScope.launch {
                                userRepository.ensureProfile(
                                    uid = user.uid,
                                    email = cleanEmail,
                                    fallbackName = cleanName,
                                    username = cleanUsername
                                )
                                onSuccess()
                            }
                        } else {
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { exc ->
                        onError(exc.localizedMessage ?: "Registration failed.")
                    }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Registration error.")
            }
        }
    }

    fun signUpWithEmail(
        emailStr: String,
        passStr: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        signUpWithEmail(
            emailStr = emailStr,
            passStr = passStr,
            usernameStr = emailStr.substringBefore("@"),
            fullNameStr = "User",
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun signOut(onComplete: () -> Unit) {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        clearUserData()
        onComplete()
    }

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
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    fun getClient(clientId: String): Client? {
        return _clients.value.find { it.id == clientId } ?: _clients.value.firstOrNull()
    }

    fun addClient(name: String, role: String = "Client", company: String = "SyncLine Partner", avatarUrl: String = ""): Client {
        val newId = "c_${System.currentTimeMillis()}"
        val initial = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "CL" }
        val newClient = Client(
            id = newId,
            name = name,
            firstName = name.substringBefore(" "),
            company = company,
            role = role,
            country = "United States",
            city = "New York",
            countryCode = "US",
            flag = "🇺🇸",
            timezoneId = "America/New_York",
            localTimeStr = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            statusStr = "Working",
            workingHours = "Mon - Fri, 9:00 AM - 6:00 PM",
            avatarBg = androidx.compose.ui.graphics.Color(0xFF9FE870),
            avatarInitial = initial,
            isOnline = true,
            lastSeen = "Online"
        )
        _clients.update { it + newClient }
        val newInboxItem = MockDataProvider.InboxItem(
            client = newClient,
            lastMessage = "Client added to conversation",
            timestamp = "Just now",
            statusText = "New York · Active",
            unreadCount = 0,
            isStarred = false
        )
        _inboxItems.update { it + newInboxItem }
        return newClient
    }

    // Inbox state
    private val _inboxItems = MutableStateFlow<List<MockDataProvider.InboxItem>>(emptyList())
    val inboxItems: StateFlow<List<MockDataProvider.InboxItem>> = _inboxItems.asStateFlow()

    private val _inboxFilter = MutableStateFlow(InboxFilter.ALL)
    val inboxFilter: StateFlow<InboxFilter> = _inboxFilter.asStateFlow()

    fun setInboxFilter(filter: InboxFilter) {
        _inboxFilter.value = filter
    }

    // Messages
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
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
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _projectsFilter = MutableStateFlow(ProjectsFilter.ACTIVE)
    val projectsFilter: StateFlow<ProjectsFilter> = _projectsFilter.asStateFlow()

    fun setProjectsFilter(filter: ProjectsFilter) {
        _projectsFilter.value = filter
    }

    fun getProject(projectId: String): Project? {
        return _projects.value.find { it.id == projectId } ?: _projects.value.firstOrNull()
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
    private val _files = MutableStateFlow<List<FileAttachment>>(emptyList())
    val files: StateFlow<List<FileAttachment>> = _files.asStateFlow()

    private val _filesFilter = MutableStateFlow(FilesFilter.ALL)
    val filesFilter: StateFlow<FilesFilter> = _filesFilter.asStateFlow()

    fun setFilesFilter(filter: FilesFilter) {
        _filesFilter.value = filter
    }

    // Meetings
    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()

    // Invoices
    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
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
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAllNotificationsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun clearUserData() {
        profileJob?.cancel()
        profileJob = null
        clientsJob?.cancel()
        clientsJob = null
        _userProfile.value = null
        _clients.value = emptyList()
        _inboxItems.value = emptyList()
        _messages.value = emptyList()
        _projects.value = emptyList()
        _files.value = emptyList()
        _meetings.value = emptyList()
        _invoices.value = emptyList()
        _notifications.value = emptyList()
        _chatDraft.value = ""
        _lastScheduledNotice.value = null
    }

    fun loadDemoDataForShowcase() {
        _clients.value = MockDataProvider.clients
        _inboxItems.value = MockDataProvider.inboxPreviews
        _messages.value = MockDataProvider.getInitialMessages()
        _projects.value = MockDataProvider.initialProjects
        _files.value = MockDataProvider.allFiles
        _meetings.value = MockDataProvider.calendarMeetings
        _invoices.value = MockDataProvider.initialPastInvoices
        _notifications.value = MockDataProvider.initialNotifications
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
