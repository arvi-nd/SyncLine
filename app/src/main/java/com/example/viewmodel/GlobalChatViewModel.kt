package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MockDataProvider
import com.example.data.UserRepository
import com.example.engine.TimezoneEngine
import com.example.model.AttachmentType
import com.example.model.ChatMessage
import com.example.model.ChatRequest
import com.example.model.Client
import com.example.model.Conversation
import com.example.model.ConversationUtils
import com.example.model.FileAttachment
import com.example.model.Invoice
import com.example.model.formatAmount
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
import kotlinx.coroutines.flow.combine
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

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _inboxItems = MutableStateFlow<List<MockDataProvider.InboxItem>>(emptyList())
    val inboxItems: StateFlow<List<MockDataProvider.InboxItem>> = _inboxItems.asStateFlow()

    private val _inboxFilter = MutableStateFlow(InboxFilter.ALL)
    val inboxFilter: StateFlow<InboxFilter> = _inboxFilter.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _activeChatClientId = MutableStateFlow("")
    val activeChatClientId: StateFlow<String> = _activeChatClientId.asStateFlow()

    private val _chatDraft = MutableStateFlow("")
    val chatDraft: StateFlow<String> = _chatDraft.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _projectsFilter = MutableStateFlow(ProjectsFilter.ACTIVE)
    val projectsFilter: StateFlow<ProjectsFilter> = _projectsFilter.asStateFlow()

    private val _files = MutableStateFlow<List<FileAttachment>>(emptyList())
    val files: StateFlow<List<FileAttachment>> = _files.asStateFlow()

    private val _filesFilter = MutableStateFlow(FilesFilter.ALL)
    val filesFilter: StateFlow<FilesFilter> = _filesFilter.asStateFlow()

    private val _meetings = MutableStateFlow<List<Meeting>>(emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _cities = MutableStateFlow(MockDataProvider.globalCities)
    val cities: StateFlow<List<TimezoneCity>> = _cities.asStateFlow()

    private val _lastScheduledNotice = MutableStateFlow<String?>(null)
    val lastScheduledNotice: StateFlow<String?> = _lastScheduledNotice.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val _chatErrorMessage = MutableStateFlow<String?>(null)
    val chatErrorMessage: StateFlow<String?> = _chatErrorMessage.asStateFlow()

    fun clearChatError() {
        _chatErrorMessage.value = null
    }

    private val _isDebugDialogOpen = MutableStateFlow(false)
    val isDebugDialogOpen: StateFlow<Boolean> = _isDebugDialogOpen.asStateFlow()

    private var profileJob: Job? = null
    private var requestsJob: Job? = null
    private var clientsJob: Job? = null
    private var conversationsJob: Job? = null
    private var realTimeMessagesJob: Job? = null
    private var activeObservedPartnerUid: String? = null

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
                val prevUid = _currentUser.value?.uid
                _currentUser.value = newUser
                if (newUser == null) {
                    clearUserData()
                } else if (newUser.uid != prevUid || _userProfile.value == null) {
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
        syncUserProjects(user)
        syncUserMeetings(user)
        syncUserInvoices(user)
    }

    private var invoicesJob: Job? = null

    private fun syncUserInvoices(user: FirebaseUser?) {
        invoicesJob?.cancel()
        if (user == null) {
            _invoices.value = emptyList()
            return
        }

        val currentUid = user.uid
        invoicesJob = viewModelScope.launch {
            userRepository.observeAllInvoices().collect { allInvoices ->
                if (auth.currentUser?.uid == currentUid) {
                    _invoices.value = allInvoices
                }
            }
        }
    }

    private var projectsJob: Job? = null

    private fun syncUserProjects(user: FirebaseUser?) {
        projectsJob?.cancel()
        if (user == null) {
            _projects.value = emptyList()
            return
        }

        val currentUid = user.uid
        projectsJob = viewModelScope.launch {
            userRepository.observeProjectsForUser(currentUid).collect { userProjects ->
                if (auth.currentUser?.uid == currentUid) {
                    _projects.value = userProjects
                }
            }
        }
    }

    private var meetingsJob: Job? = null

    private fun syncUserMeetings(user: FirebaseUser?) {
        meetingsJob?.cancel()
        if (user == null) {
            _meetings.value = emptyList()
            return
        }

        val currentUid = user.uid
        meetingsJob = viewModelScope.launch {
            userRepository.observeMeetingsForUser(currentUid).collect { userMeetings ->
                if (auth.currentUser?.uid == currentUid) {
                    _meetings.value = userMeetings
                }
            }
        }
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

    private fun syncUserClients(user: FirebaseUser?) {
        clientsJob?.cancel()
        conversationsJob?.cancel()
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
                        firstName = profile.displayName.substringBefore(" ").ifBlank { profile.username },
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

                if ((_activeChatClientId.value.isBlank() || _activeChatClientId.value == "c1") && updatedClients.isNotEmpty()) {
                    val firstClientId = updatedClients.first().id
                    _activeChatClientId.value = firstClientId
                    observeRealTimeMessages(firstClientId)
                }
            }
        }

        conversationsJob = viewModelScope.launch {
            combine(
                _clients,
                userRepository.observeUserConversations(currentUid)
            ) { clientsList, conversationsList ->
                if (auth.currentUser?.uid != currentUid) return@combine emptyList<MockDataProvider.InboxItem>()

                val convMap = conversationsList.associateBy { it.id }

                val itemsFromClients = clientsList.map { client ->
                    val convId = ConversationUtils.getConversationId(currentUid, client.id)
                    val conversation = convMap[convId]
                    val lastMsg = conversation?.lastMessage?.ifBlank { "Chat request accepted" } ?: "Chat request accepted"
                    val timeStr = formatRelativeTimestamp(conversation?.lastMessageAt)
                    val timeSeconds = conversation?.lastMessageAt?.seconds ?: 0L

                    Pair(
                        MockDataProvider.InboxItem(
                            client = client,
                            lastMessage = lastMsg,
                            timestamp = timeStr,
                            statusText = "SyncLine · Active",
                            unreadCount = 0,
                            isStarred = false
                        ),
                        timeSeconds
                    )
                }

                // Add any conversations where client is not yet in clientsList
                val existingClientIds = clientsList.map { it.id }.toSet()
                val extraItems = conversationsList.filter { conv ->
                    val partnerUid = conv.participantUids.find { it != currentUid } ?: ""
                    partnerUid.isNotBlank() && partnerUid !in existingClientIds
                }.map { conv ->
                    val partnerUid = conv.participantUids.find { it != currentUid } ?: ""
                    val partnerProfile = userRepository.getProfile(partnerUid)
                    val name = partnerProfile?.displayName?.ifBlank { partnerProfile.username } ?: "Partner"
                    val initial = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "CL" }
                    val client = Client(
                        id = partnerUid,
                        name = name,
                        firstName = name.substringBefore(" ").ifBlank { name },
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

                    val lastMsg = conv.lastMessage.ifBlank { "Chat request accepted" }
                    val timeStr = formatRelativeTimestamp(conv.lastMessageAt)
                    val timeSeconds = conv.lastMessageAt?.seconds ?: 0L

                    Pair(
                        MockDataProvider.InboxItem(
                            client = client,
                            lastMessage = lastMsg,
                            timestamp = timeStr,
                            statusText = "SyncLine · Active",
                            unreadCount = 0,
                            isStarred = false
                        ),
                        timeSeconds
                    )
                }

                (itemsFromClients + extraItems)
                    .sortedByDescending { it.second }
                    .map { it.first }
                    .distinctBy { it.client.id }
            }.collect { updatedInbox ->
                if (auth.currentUser?.uid == currentUid) {
                    _inboxItems.value = updatedInbox
                }
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
        _chatRequests.update { list ->
            list.map { req ->
                if (req.id == request.id || (request.id.isBlank() && req.senderUid == request.senderUid && req.targetUid == request.targetUid)) {
                    req.copy(status = "ACCEPTED")
                } else req
            }
        }
        viewModelScope.launch {
            val success = userRepository.acceptChatRequest(request)
            if (!success) {
                // If backend call fails, refresh requests from repository
                currentUser.value?.uid?.let { uid ->
                    userRepository.observeChatRequests(uid)
                }
            }
        }
    }

    fun declineChatRequest(requestId: String) {
        _chatRequests.update { list ->
            list.map { req ->
                if (req.id == requestId) {
                    req.copy(status = "DECLINED")
                } else req
            }
        }
        viewModelScope.launch {
            val success = userRepository.declineChatRequest(requestId)
            if (!success) {
                currentUser.value?.uid?.let { uid ->
                    userRepository.observeChatRequests(uid)
                }
            }
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
    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun setTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    // Clients

    fun getClient(clientId: String): Client? {
        val existing = _clients.value.find { it.id == clientId }
        if (existing != null) return existing

        val inboxClient = _inboxItems.value.find { it.client.id == clientId }?.client
        if (inboxClient != null) return inboxClient

        val request = _chatRequests.value.find { it.senderUid == clientId || it.targetUid == clientId }
        if (request != null) {
            val isTarget = request.targetUid == clientId
            val name = if (isTarget) request.targetDisplayName.ifBlank { request.targetUsername }
                       else request.senderDisplayName.ifBlank { request.senderUsername }
            val username = if (isTarget) request.targetUsername else request.senderUsername
            val initial = name.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2).joinToString("").uppercase()
                .ifBlank { username.take(2).uppercase().ifBlank { "CL" } }

            val synthesized = Client(
                id = clientId,
                name = name.ifBlank { "Client" },
                firstName = name.substringBefore(" ").ifBlank { "Client" },
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
            _clients.update { (it + synthesized).distinctBy { c -> c.id } }
            return synthesized
        }

        return _clients.value.firstOrNull()
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
    fun setInboxFilter(filter: InboxFilter) {
        _inboxFilter.value = filter
    }

    // Messages

    // Messages

    fun sendMessage(
        conversationId: String,
        text: String,
        senderName: String = "You",
        onResult: ((Boolean) -> Unit)? = null
    ) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return

        val myName = _userProfile.value?.displayName?.ifBlank { senderName } ?: senderName
        val localMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = myName,
            text = cleanText,
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true,
            isRead = true
        )

        // Optimistically update local message list so user sees their sent message immediately
        _messages.update { current ->
            if (current.none { it.id == localMsg.id || (it.text == cleanText && it.isOutgoing) }) {
                current + localMsg
            } else current
        }

        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            val partnerUid = ConversationUtils.extractPartnerUid(currentUid, conversationId)
            val resolvedPartnerUid = if (partnerUid.isNotBlank() && partnerUid != currentUid) partnerUid else conversationId
            val actualConvId = ConversationUtils.getConversationId(currentUid, resolvedPartnerUid)

            if (actualConvId.isNotBlank()) {
                viewModelScope.launch {
                    try {
                        val success = userRepository.sendMessage(
                            conversationId = actualConvId,
                            senderUid = currentUid,
                            partnerUid = resolvedPartnerUid,
                            senderName = myName,
                            text = cleanText
                        )
                        if (!success) {
                            android.util.Log.w("SyncLineDiagnostics", "[SEND_VIEWMODEL_WARN] RTDB sync pending or unconfirmed for conv=$actualConvId")
                        } else {
                            _chatErrorMessage.value = null
                        }
                        onResult?.invoke(success)
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        android.util.Log.e("SyncLineDiagnostics", "[SEND_VIEWMODEL_FAIL] sendMessage exception: ${e.message}", e)
                        onResult?.invoke(false)
                    }
                }
                return
            }
        }
        onResult?.invoke(true)
    }

    // Active Chat Client and Draft Message
    fun setActiveChatClient(id: String) {
        val currentUid = auth.currentUser?.uid
        val resolvedPartnerUid = if (currentUid != null) ConversationUtils.extractPartnerUid(currentUid, id) else id
        if (_activeChatClientId.value == id && activeObservedPartnerUid == resolvedPartnerUid && realTimeMessagesJob?.isActive == true) {
            // Already actively observing messages for this client; avoid tearing down and re-registering listener
            android.util.Log.d("SyncLineDiagnostics", "setActiveChatClient: already actively observing $id, skipping re-registration")
            return
        }
        _activeChatClientId.value = id
        observeRealTimeMessages(id)
    }

    private fun observeRealTimeMessages(partnerUid: String) {
        realTimeMessagesJob?.cancel()
        val currentUid = auth.currentUser?.uid ?: return
        if (partnerUid.isBlank()) return

        val resolvedPartnerUid = ConversationUtils.extractPartnerUid(currentUid, partnerUid)
        if (resolvedPartnerUid.isBlank() || resolvedPartnerUid == currentUid) return

        val conversationId = ConversationUtils.getConversationId(currentUid, resolvedPartnerUid)
        if (conversationId.isBlank()) return

        activeObservedPartnerUid = resolvedPartnerUid
        android.util.Log.d("SyncLineDiagnostics", "observeRealTimeMessages: attaching listener for conv=$conversationId")

        realTimeMessagesJob = viewModelScope.launch {
            try {
                userRepository.ensureConversationExists(currentUid, resolvedPartnerUid)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                android.util.Log.w("GlobalChatViewModel", "Could not ensure conversation doc: ${e.message}")
            }

            userRepository.observeMessages(conversationId).collect { chatMessages ->
                if (auth.currentUser?.uid != currentUid) return@collect

                val rtdbMessages = chatMessages.map { msg ->
                    val isOutgoing = (msg.senderUid == currentUid)
                    val timeStr = formatTimestamp(msg.createdAt)
                    val invoiceCard = if (msg.type == "invoice_share") {
                        var found = _invoices.value.find { it.id == msg.invoiceId }
                        if (found == null && msg.invoicePreview != null) {
                            found = msg.invoicePreview
                            _invoices.update { (it + found).distinctBy { inv -> inv.id } }
                        }
                        if (found == null && !msg.invoiceId.isNullOrBlank()) {
                            val invId = msg.invoiceId
                            viewModelScope.launch {
                                val fetched = userRepository.getInvoiceById(invId)
                                if (fetched != null) {
                                    _invoices.update { (it + fetched).distinctBy { inv -> inv.id } }
                                    _messages.update { list ->
                                        list.map { m ->
                                            if (m.id == msg.id && m.invoiceCard == null) {
                                                m.copy(invoiceCard = fetched)
                                            } else m
                                        }
                                    }
                                }
                            }
                        }
                        found
                    } else null

                    Message(
                        id = msg.id,
                        conversationId = resolvedPartnerUid,
                        senderName = msg.senderName.ifBlank { if (isOutgoing) "You" else "Partner" },
                        text = msg.text,
                        timestamp = timeStr,
                        isOutgoing = isOutgoing,
                        isRead = true,
                        type = msg.type,
                        invoiceId = msg.invoiceId,
                        invoiceCard = invoiceCard
                    )
                }

                _messages.update { currentList ->
                    val rtdbIds = rtdbMessages.map { it.id }.toSet()
                    // Retain any pending optimistic local messages that haven't yet reflected in RTDB snapshot
                    val localPending = currentList.filter { local ->
                        if (!local.isOutgoing || !local.id.startsWith("msg_")) return@filter false
                        if (rtdbIds.contains(local.id)) return@filter false
                        if (local.projectCard != null || local.meetingCard != null || local.invoiceCard != null) {
                            return@filter true
                        }
                        if (local.text.isNotBlank()) {
                            return@filter rtdbMessages.none { rtdb -> rtdb.text == local.text }
                        }
                        false
                    }
                    (rtdbMessages + localPending).distinctBy { it.id }
                }
            }
        }
    }

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return "Just now"
        return try {
            val instant = java.time.Instant.ofEpochSecond(timestamp.seconds)
            TimezoneEngine.formatCurrentTime("America/New_York", instant)
        } catch (_: Exception) {
            "Just now"
        }
    }

    private fun formatRelativeTimestamp(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return "Just now"
        val seconds = timestamp.seconds
        val nowSeconds = System.currentTimeMillis() / 1000
        val diff = nowSeconds - seconds
        return when {
            diff < 60 -> "Just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            else -> "${diff / 86400}d ago"
        }
    }

    fun setChatDraft(text: String) {
        _chatDraft.value = text
    }

    fun sendCurrentMessage(targetClientId: String? = null, onComplete: ((Boolean) -> Unit)? = null) {
        val text = _chatDraft.value.trim()
        if (text.isBlank()) return

        val targetId = targetClientId?.ifBlank { null }
            ?: _activeChatClientId.value.ifBlank { _clients.value.firstOrNull()?.id ?: "c1" }

        // Clear input composer draft instantly
        _chatDraft.value = ""
        _chatErrorMessage.value = null

        sendMessage(
            conversationId = targetId,
            text = text,
            senderName = _userProfile.value?.displayName?.ifBlank { "You" } ?: "You",
            onResult = { success ->
                onComplete?.invoke(success)
            }
        )
    }

    fun addProjectAndSendToChat(
        conversationId: String,
        title: String,
        clientName: String,
        dueDate: String = "Nov 15",
        status: ProjectStatus = ProjectStatus.IN_PROGRESS,
        tasks: List<String> = emptyList(),
        colorHexLight: Long = 0xFFE2F6D5L,
        colorHexDark: Long = 0xFF22381EL
    ) {
        val projectTasks = tasks.mapIndexed { idx, tTitle ->
            com.example.model.ProjectTask(
                id = "task_${System.currentTimeMillis()}_$idx",
                title = tTitle,
                isCompleted = false
            )
        }

        val newProject = Project(
            id = "p_${System.currentTimeMillis()}",
            title = title,
            clientName = clientName,
            dueDate = dueDate,
            progress = 0,
            status = status,
            tasks = projectTasks,
            cardColorHexLight = colorHexLight,
            cardColorHexDark = colorHexDark
        )
        _projects.update { (it + newProject).distinctBy { p -> p.id } }

        val myName = _userProfile.value?.displayName?.ifBlank { "You" } ?: "You"
        val newMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderName = myName,
            text = "",
            timestamp = TimezoneEngine.formatCurrentTime("America/New_York", Instant.now()),
            isOutgoing = true,
            projectCard = newProject
        )
        _messages.update { current -> (current + newMsg).distinctBy { it.id } }

        _inboxItems.update { list ->
            list.map { item ->
                if (item.client.id == conversationId) {
                    item.copy(lastMessage = "Project: $title", timestamp = "Just now")
                } else item
            }
        }

        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            val partnerUid = ConversationUtils.extractPartnerUid(currentUid, conversationId)
            val resolvedPartnerUid = if (partnerUid.isNotBlank() && partnerUid != currentUid) partnerUid else conversationId
            val actualConvId = ConversationUtils.getConversationId(currentUid, resolvedPartnerUid)

            if (actualConvId.isNotBlank()) {
                viewModelScope.launch {
                    userRepository.sendProjectCardMessage(
                        conversationId = actualConvId,
                        senderUid = currentUid,
                        partnerUid = resolvedPartnerUid,
                        senderName = myName,
                        project = newProject
                    )
                }
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
    fun setProjectsFilter(filter: ProjectsFilter) {
        _projectsFilter.value = filter
    }

    fun getProject(projectId: String): Project? {
        return _projects.value.find { it.id == projectId }
    }

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _isLoadingProject = MutableStateFlow(false)
    val isLoadingProject: StateFlow<Boolean> = _isLoadingProject.asStateFlow()

    private var selectedProjectJob: Job? = null

    fun loadProject(projectId: String) {
        selectedProjectJob?.cancel()
        if (projectId.isBlank()) {
            _selectedProject.value = null
            _isLoadingProject.value = false
            return
        }

        // Search local state first
        val localMatch = _projects.value.find { it.id == projectId }
        if (localMatch != null) {
            _selectedProject.value = localMatch
        }

        _isLoadingProject.value = true
        selectedProjectJob = viewModelScope.launch {
            userRepository.observeProjectFromFirestore(projectId).collect { proj ->
                _selectedProject.value = proj
                _isLoadingProject.value = false
            }
        }
    }

    private val _isCreatingProject = MutableStateFlow(false)
    val isCreatingProject: StateFlow<Boolean> = _isCreatingProject.asStateFlow()

    fun createAndShareProject(
        conversationId: String,
        clientUid: String,
        clientName: String,
        title: String,
        description: String = "",
        startDate: String = "",
        dueDate: String = "",
        tasks: List<String> = emptyList(),
        shouldShareInChat: Boolean = false,
        existingProjectId: String? = null,
        onSuccess: (Project) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_isCreatingProject.value) return
        val currentUid = auth.currentUser?.uid ?: run {
            onError("You must be signed in to create a project.")
            return
        }

        _isCreatingProject.value = true
        viewModelScope.launch {
            try {
                val stableId = existingProjectId?.ifBlank { null }
                    ?: "proj_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}"

                val projectTasks = tasks.mapIndexed { idx, tTitle ->
                    com.example.model.ProjectTask(
                        id = "task_${System.currentTimeMillis()}_$idx",
                        title = tTitle,
                        isCompleted = false
                    )
                }

                val draftProject = Project(
                    id = stableId,
                    ownerUid = currentUid,
                    clientUid = clientUid,
                    participantUids = listOf(currentUid, clientUid).filter { it.isNotBlank() },
                    title = title,
                    clientName = clientName,
                    description = description,
                    startDate = startDate,
                    dueDate = dueDate,
                    progress = 0,
                    status = ProjectStatus.IN_PROGRESS,
                    tasks = projectTasks
                )

                // 1. Save project in Firestore
                val result = userRepository.createProjectInFirestore(draftProject)
                if (result.isFailure) {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to save project."
                    onError(err)
                    return@launch
                }

                val createdProject = result.getOrThrow()
                _projects.update { (it + createdProject).distinctBy { p -> p.id } }

                onSuccess(createdProject)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                onError(e.localizedMessage ?: "Failed to process project creation.")
            } finally {
                _isCreatingProject.value = false
            }
        }
    }

    fun toggleTask(projectId: String, taskId: String) {
        val currentProj = _projects.value.find { it.id == projectId } ?: _selectedProject.value
        val task = currentProj?.tasks?.find { it.id == taskId } ?: return
        val newCompleted = !task.isCompleted

        viewModelScope.launch {
            userRepository.updateProjectTaskInFirestore(projectId, taskId, newCompleted)
        }
    }

    private val _isDeletingProject = MutableStateFlow(false)
    val isDeletingProject: StateFlow<Boolean> = _isDeletingProject.asStateFlow()

    fun deleteProject(
        projectId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (projectId.isBlank()) return
        if (_isDeletingProject.value) return

        _isDeletingProject.value = true
        viewModelScope.launch {
            try {
                val result = userRepository.deleteProjectFromFirestore(projectId)
                if (result.isSuccess) {
                    _projects.update { list -> list.filter { it.id != projectId } }
                    if (_selectedProject.value?.id == projectId) {
                        _selectedProject.value = null
                    }
                    onSuccess()
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete project."
                    onError(err)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                onError(e.localizedMessage ?: "Failed to delete project.")
            } finally {
                _isDeletingProject.value = false
            }
        }
    }

    private val _isSchedulingMeeting = MutableStateFlow(false)
    val isSchedulingMeeting: StateFlow<Boolean> = _isSchedulingMeeting.asStateFlow()

    private val _scheduleMeetingError = MutableStateFlow<String?>(null)
    val scheduleMeetingError: StateFlow<String?> = _scheduleMeetingError.asStateFlow()

    fun clearScheduleMeetingError() {
        _scheduleMeetingError.value = null
    }

    fun scheduleMeeting(
        meetingId: String = "",
        title: String,
        clientName: String,
        clientUid: String,
        dateString: String,
        time: String,
        duration: String,
        durationMinutes: Int,
        scheduledAt: com.google.firebase.Timestamp?,
        notes: String?,
        onSuccess: (Meeting) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            val err = "You must be signed in to schedule a meeting."
            _scheduleMeetingError.value = err
            onError(err)
            return
        }

        if (title.isBlank()) {
            val err = "Please enter a meeting title."
            _scheduleMeetingError.value = err
            onError(err)
            return
        }

        if (_isSchedulingMeeting.value) return
        _isSchedulingMeeting.value = true
        _scheduleMeetingError.value = null

        val stableAttemptId = if (meetingId.isNotBlank()) meetingId else "m_${java.util.UUID.randomUUID()}"
        val participantList = listOf(currentUid, clientUid).filter { it.isNotBlank() }.distinct()

        viewModelScope.launch {
            try {
                val draftMeeting = Meeting(
                    id = stableAttemptId,
                    title = title.trim(),
                    clientName = clientName,
                    time = time,
                    duration = duration,
                    isOnline = true,
                    dateString = dateString,
                    status = "Confirmed",
                    notes = notes?.takeIf { it.isNotBlank() },
                    organizerUid = currentUid,
                    participantUids = participantList,
                    scheduledAt = scheduledAt,
                    durationMinutes = durationMinutes
                )

                val result = userRepository.createMeetingInFirestore(draftMeeting)
                if (result.isFailure) {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to save meeting."
                    _scheduleMeetingError.value = err
                    onError(err)
                    return@launch
                }

                val createdMeeting = result.getOrThrow()
                _meetings.update { (it + createdMeeting).distinctBy { m -> m.id } }
                onSuccess(createdMeeting)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val err = e.localizedMessage ?: "Failed to process meeting scheduling."
                _scheduleMeetingError.value = err
                onError(err)
            } finally {
                _isSchedulingMeeting.value = false
            }
        }
    }

    fun deleteMeeting(
        meetingId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (meetingId.isBlank()) return
        viewModelScope.launch {
            try {
                val result = userRepository.deleteMeetingFromFirestore(meetingId)
                if (result.isSuccess) {
                    _meetings.update { list -> list.filter { it.id != meetingId } }
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to delete meeting.")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                onError(e.localizedMessage ?: "Failed to delete meeting.")
            }
        }
    }

    // Files
    fun setFilesFilter(filter: FilesFilter) {
        _filesFilter.value = filter
    }

    // Meetings

    // Invoices

    fun createAndShareInvoice(
        conversationId: String,
        title: String,
        clientUid: String,
        clientName: String,
        projectId: String? = null,
        projectName: String? = null,
        amount: Double,
        currencyCode: String = "INR",
        dueDate: String,
        notes: String? = null,
        attemptId: String? = null,
        onSuccess: (Invoice) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentUid = auth.currentUser?.uid ?: ""
        if (currentUid.isBlank()) {
            onError("User not authenticated")
            return
        }

        val finalInvoiceId = attemptId?.ifBlank { null } ?: "inv_${System.currentTimeMillis()}"

        val existingInv = _invoices.value.find { it.id == finalInvoiceId }
        val invNumber = existingInv?.invoiceNumber
            ?: "INV-2026-${String.format(java.util.Locale.US, "%04d", _invoices.value.size + 1)}"

        val resolvedClientUid = if (clientUid.isNotBlank() && clientUid != "c1") {
            clientUid
        } else {
            val extracted = com.example.model.ConversationUtils.extractPartnerUid(currentUid, conversationId)
            if (extracted.isNotBlank() && extracted != "c1") extracted else ""
        }

        val participants = listOf(currentUid, resolvedClientUid).filter { it.isNotBlank() }.distinct()

        val newInvoice = Invoice(
            id = finalInvoiceId,
            invoiceNumber = invNumber,
            title = title,
            clientName = clientName.ifBlank { "Sarah Jenkins" },
            clientUid = resolvedClientUid,
            organizerUid = currentUid,
            participantUids = participants,
            projectId = projectId,
            projectName = projectName,
            amount = amount,
            currencyCode = currencyCode,
            currency = currencyCode,
            dueDate = dueDate,
            notes = notes,
            status = "UNPAID",
            isPaid = false,
            createdAt = "Today"
        )

        // Optimistically update local list so card is cached
        _invoices.update { list -> (list + newInvoice).distinctBy { it.id } }

        viewModelScope.launch {
            try {
                val result = userRepository.createInvoiceInFirestore(newInvoice)
                if (result.isSuccess) {
                    val created = result.getOrNull() ?: newInvoice
                    _invoices.update { list -> (list + created).distinctBy { it.id } }

                    val senderName = _userProfile.value?.displayName?.ifBlank { "You" } ?: "You"
                    val convForSend = conversationId.ifBlank { resolvedClientUid }
                    if (convForSend.isNotBlank()) {
                        userRepository.sendInvoiceMessage(
                            conversationId = convForSend,
                            senderName = senderName,
                            invoice = created
                        )
                    }

                    val newMsg = Message(
                        id = "msg_${System.currentTimeMillis()}",
                        conversationId = conversationId,
                        senderName = "You",
                        text = "Invoice: ${created.title} (${created.formatAmount()})",
                        timestamp = "Just now",
                        isOutgoing = true,
                        type = "invoice_share",
                        invoiceId = created.id,
                        invoiceCard = created
                    )
                    _messages.update { (it + newMsg).distinctBy { m -> m.id } }

                    _inboxItems.update { list ->
                        list.map { item ->
                            if (item.client.id == conversationId || item.client.id == resolvedClientUid) {
                                item.copy(
                                    lastMessage = "Invoice: ${created.title} (${created.formatAmount()})",
                                    timestamp = "Just now"
                                )
                            } else item
                        }
                    }

                    onSuccess(created)
                } else {
                    val errMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create invoice in Firestore"
                    onError(errMessage)
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                onError(e.localizedMessage ?: "Failed to create invoice")
            }
        }
    }

    fun addInvoiceAndSendToChat(
        conversationId: String,
        title: String,
        clientName: String,
        projectName: String? = null,
        amount: Double = 1250.0,
        currencyCode: String = "INR",
        dueDate: String = "Oct 31, 2026",
        notes: String? = null,
        attemptId: String? = null
    ) {
        val targetClient = getClient(conversationId)
        val clientUid = targetClient?.id ?: ""
        createAndShareInvoice(
            conversationId = conversationId,
            title = title,
            clientUid = clientUid,
            clientName = clientName,
            projectId = null,
            projectName = projectName,
            amount = amount,
            currencyCode = currencyCode,
            dueDate = dueDate,
            notes = notes,
            attemptId = attemptId
        )
    }

    fun markInvoicePaid(invoiceId: String) {
        val now = java.time.ZonedDateTime.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", java.util.Locale.US)
        val paidTimestamp = "Paid on ${now.format(formatter)}"
        _invoices.update { list ->
            list.map { if (it.id == invoiceId) it.copy(isPaid = true, status = "PAID", paidAt = paidTimestamp) else it }
        }
        _messages.update { list ->
            list.map { msg ->
                if (msg.invoiceCard?.id == invoiceId || msg.invoiceId == invoiceId) {
                    val updatedCard = (msg.invoiceCard ?: _invoices.value.find { it.id == invoiceId })?.copy(
                        isPaid = true,
                        status = "PAID",
                        paidAt = paidTimestamp
                    )
                    msg.copy(invoiceCard = updatedCard)
                } else msg
            }
        }

        viewModelScope.launch {
            userRepository.updateInvoicePaymentStatus(invoiceId, "PAID", paidTimestamp)
        }
    }

    // Notifications
    fun markAllNotificationsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun clearUserData() {
        profileJob?.cancel()
        profileJob = null
        requestsJob?.cancel()
        requestsJob = null
        clientsJob?.cancel()
        clientsJob = null
        conversationsJob?.cancel()
        conversationsJob = null
        realTimeMessagesJob?.cancel()
        realTimeMessagesJob = null
        projectsJob?.cancel()
        projectsJob = null
        meetingsJob?.cancel()
        meetingsJob = null
        invoicesJob?.cancel()
        invoicesJob = null
        activeObservedPartnerUid = null
        userRepository.clearCaches()
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
        _isSendingMessage.value = false
        _chatErrorMessage.value = null
    }

    fun loadDemoDataForShowcase() {
        _clients.value = MockDataProvider.clients
        _inboxItems.value = MockDataProvider.inboxPreviews
        _messages.value = MockDataProvider.getInitialMessages()
        _projects.value = emptyList()
        _files.value = MockDataProvider.allFiles
        _meetings.value = emptyList()
        _invoices.value = MockDataProvider.initialPastInvoices
        _notifications.value = MockDataProvider.initialNotifications
    }

    // Global Timezones

    // Scheduled messages in-memory confirmation

    fun scheduleMessage(client: Client, messageText: String, dateString: String) {
        val notice = "Message scheduled for ${client.name} on $dateString"
        _lastScheduledNotice.value = notice
    }

    fun clearScheduledNotice() {
        _lastScheduledNotice.value = null
    }

    // Debug showcase switcher state
    fun openDebugDialog() {
        _isDebugDialogOpen.value = true
    }

    fun closeDebugDialog() {
        _isDebugDialogOpen.value = false
    }
}
