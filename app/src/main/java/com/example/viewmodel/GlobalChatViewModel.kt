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
    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("syncline_prefs", android.content.Context.MODE_PRIVATE)
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _chatRequests = MutableStateFlow<List<ChatRequest>>(emptyList())
    val chatRequests: StateFlow<List<ChatRequest>> = _chatRequests.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
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

    private val _partnerTyping = MutableStateFlow(false)
    val partnerTyping: StateFlow<Boolean> = _partnerTyping.asStateFlow()

    private val _partnerReadAt = MutableStateFlow(0L)
    val partnerReadAt: StateFlow<Long> = _partnerReadAt.asStateFlow()

    private val _replyingTo = MutableStateFlow<Message?>(null)
    val replyingTo: StateFlow<Message?> = _replyingTo.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val _chatErrorMessage = MutableStateFlow<String?>(null)
    val chatErrorMessage: StateFlow<String?> = _chatErrorMessage.asStateFlow()

    fun clearChatError() {
        _chatErrorMessage.value = null
    }

    private val _isDebugDialogOpen = MutableStateFlow(false)
    val isDebugDialogOpen: StateFlow<Boolean> = _isDebugDialogOpen.asStateFlow()

    // Starred chats are a personal, per-device preference.
    private val _starredIds = MutableStateFlow(prefs.getStringSet("starred_chats", emptySet()) ?: emptySet())

    fun toggleStar(clientId: String) {
        val next = _starredIds.value.let { if (clientId in it) it - clientId else it + clientId }
        _starredIds.value = next
        prefs.edit().putStringSet("starred_chats", next).apply()
    }

    /** Clears the unread badge once the conversation with [partnerUid] is on screen. */
    fun markChatRead(partnerUid: String) {
        val myUid = auth.currentUser?.uid ?: return
        val convId = ConversationUtils.getConversationId(myUid, ConversationUtils.extractPartnerUid(myUid, partnerUid))
        if (convId.isBlank()) return
        viewModelScope.launch { userRepository.markConversationRead(myUid, convId) }
    }

    private fun flagEmoji(countryCode: String): String {
        if (countryCode.length != 2 || !countryCode.all { it.isLetter() }) return "🌐"
        return countryCode.uppercase().map { String(Character.toChars(0x1F1E6 + (it - 'A'))) }.joinToString("")
    }

    private fun formatHour(hour: Int): String {
        val h = ((hour % 24) + 24) % 24
        val display = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
        return "$display:00 ${if (h < 12) "AM" else "PM"}"
    }

    /** Builds the contact shown in the app from a real profile (timezone, country, working hours). */
    private fun buildClient(profile: UserProfile): Client {
        val name = profile.displayName.ifBlank { profile.username.ifBlank { "Client" } }
        val initial = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("").uppercase()
            .ifBlank { profile.username.take(2).uppercase().ifBlank { "CL" } }
        val zoneId = try { java.time.ZoneId.of(profile.timezone.ifBlank { "UTC" }).id } catch (_: Exception) { "UTC" }
        val region = profile.countryCode.uppercase()
        val countryName = if (region.length == 2) java.util.Locale("", region).displayCountry else ""
        val city = zoneId.substringAfterLast('/').replace('_', ' ')
        val now = java.time.ZonedDateTime.now(java.time.ZoneId.of(zoneId))
        val isWeekday = now.dayOfWeek != java.time.DayOfWeek.SATURDAY && now.dayOfWeek != java.time.DayOfWeek.SUNDAY
        val working = isWeekday && now.hour >= profile.workingStartHour && now.hour < profile.workingEndHour
        return Client(
            id = profile.uid,
            name = name,
            firstName = name.substringBefore(" ").ifBlank { name },
            company = "SyncLine Partner",
            role = "Client",
            country = countryName.ifBlank { "Unknown location" },
            city = city,
            countryCode = region,
            flag = flagEmoji(region),
            timezoneId = zoneId,
            localTimeStr = TimezoneEngine.formatCurrentTime(zoneId, Instant.now()),
            statusStr = when {
                profile.availability == "Do not disturb" -> "Do not disturb"
                profile.availability == "Busy" -> "Busy"
                working -> "Working"
                else -> "After hours"
            },
            workingHours = "Mon - Fri, ${formatHour(profile.workingStartHour)} - ${formatHour(profile.workingEndHour)}",
            avatarBg = androidx.compose.ui.graphics.Color(0xFF9FE870),
            avatarInitial = initial,
            isOnline = working && profile.availability == "Available",
            lastSeen = when {
                profile.availability != "Available" -> profile.availability
                working -> "Available now"
                else -> "Outside working hours"
            }
        )
    }

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
    private var invoicesRemoteJob: Job? = null

    private fun syncUserInvoices(user: FirebaseUser?) {
        invoicesJob?.cancel()
        if (user == null) {
            _invoices.value = emptyList()
            return
        }

        val currentUid = user.uid
        // Changes made on the other phone (paid, edited, cancelled) arrive here and refresh the local cache.
        invoicesRemoteJob?.cancel()
        invoicesRemoteJob = viewModelScope.launch {
            userRepository.observeInvoicesRemote(currentUid).collect { remote ->
                if (auth.currentUser?.uid == currentUid) userRepository.cacheInvoices(remote)
            }
        }
        invoicesJob = viewModelScope.launch {
            userRepository.observeAllInvoices().collect { cached ->
                val allInvoices = cached.filter { it.organizerUid == currentUid || it.clientUid == currentUid }
                if (auth.currentUser?.uid == currentUid) {
                    _invoices.value = allInvoices
                    // Keep invoice cards already in the chat in step with edits made elsewhere.
                    val latest = allInvoices.associateBy { it.id }
                    _messages.update { list ->
                        list.map { msg ->
                            val id = msg.invoiceCard?.id ?: msg.invoiceId
                            val fresh = id?.let { latest[it] }
                            if (fresh != null && fresh != msg.invoiceCard) msg.copy(invoiceCard = fresh) else msg
                        }
                    }
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
                    com.example.reminders.MeetingReminders.sync(getApplication(), userMeetings)
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
                    val full = userRepository.getProfile(profile.uid, forceRefresh = true)
                    buildClient(
                        if (full != null) full.copy(displayName = full.displayName.ifBlank { profile.displayName })
                        else profile
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
                userRepository.observeUserConversations(currentUid),
                _starredIds
            ) { clientsList, conversationsList, starredIds ->
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
                            unreadCount = conversation?.unreadCount ?: 0,
                            isStarred = client.id in starredIds
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
                    val client = buildClient(partnerProfile ?: UserProfile(uid = partnerUid, displayName = "Partner"))

                    val lastMsg = conv.lastMessage.ifBlank { "Chat request accepted" }
                    val timeStr = formatRelativeTimestamp(conv.lastMessageAt)
                    val timeSeconds = conv.lastMessageAt?.seconds ?: 0L

                    Pair(
                        MockDataProvider.InboxItem(
                            client = client,
                            lastMessage = lastMsg,
                            timestamp = timeStr,
                            statusText = "SyncLine · Active",
                            unreadCount = conv.unreadCount,
                            isStarred = client.id in starredIds
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
                        onError("No account found for this email. Tap Sign up to create one.")
                    } else {
                        onError(msg)
                    }
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Sign in error.")
        }
    }

    fun sendPasswordReset(emailStr: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cleanEmail = emailStr.trim()
        if (cleanEmail.isEmpty()) {
            onError("Enter your email address first, then tap Forgot password.")
            return
        }
        try {
            auth.sendPasswordResetEmail(cleanEmail)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it.localizedMessage ?: "Could not send the reset email.") }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Could not send the reset email.")
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it.localizedMessage ?: "Google sign-in failed.") }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Google sign-in failed.")
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

    // Theme state (saved on the device)
    fun toggleTheme() {
        setTheme(!_isDarkTheme.value)
    }

    fun setTheme(dark: Boolean) {
        _isDarkTheme.value = dark
        prefs.edit().putBoolean("dark_theme", dark).apply()
    }

    fun updateProfileSettings(
        displayName: String? = null,
        timezone: String? = null,
        workingStartHour: Int? = null,
        workingEndHour: Int? = null,
        availability: String? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        viewModelScope.launch {
            val ok = userRepository.updateProfileSettings(
                uid, displayName, timezone, workingStartHour, workingEndHour, availability
            )
            onResult(ok)
        }
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
            val synthesized = buildClient(UserProfile(uid = clientId, displayName = name, username = username))
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
        reply: Message? = _replyingTo.value,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return

        val myName = _userProfile.value?.displayName?.ifBlank { senderName } ?: senderName
        val now = System.currentTimeMillis()
        val localMsg = Message(
            id = "msg_$now",
            conversationId = conversationId,
            senderName = myName,
            text = cleanText,
            timestamp = TimezoneEngine.formatCurrentTime(java.time.ZoneId.systemDefault().id, Instant.now()),
            isOutgoing = true,
            isRead = true,
            status = "sending",
            sentAtMillis = now,
            replyToId = reply?.id,
            replyToText = reply?.text?.take(120),
            replyToName = reply?.senderName
        )
        _replyingTo.value = null
        notifyTyping(false)

        // Show the message immediately; it is replaced by the real one once it is stored.
        _messages.update { current -> current + localMsg }

        fun markFailed() {
            _messages.update { list -> list.map { if (it.id == localMsg.id) it.copy(status = "failed") else it } }
            _chatErrorMessage.value = "Message not sent. Tap it to try again."
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
                            text = cleanText,
                            replyToId = reply?.id,
                            replyToText = reply?.text,
                            replyToName = reply?.senderName
                        )
                        if (success) _chatErrorMessage.value = null else markFailed()
                        onResult?.invoke(success)
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        android.util.Log.e("SyncLineDiagnostics", "[SEND_VIEWMODEL_FAIL] sendMessage exception: ${e.message}", e)
                        markFailed()
                        onResult?.invoke(false)
                    }
                }
                return
            }
        }
        markFailed()
        onResult?.invoke(false)
    }

    /** Sends a message that failed earlier, keeping its reply context. */
    fun retryMessage(messageId: String) {
        val failed = _messages.value.firstOrNull { it.id == messageId && it.status == "failed" } ?: return
        _messages.update { list -> list.filterNot { it.id == messageId } }
        val reply = failed.replyToId?.let {
            Message(id = it, conversationId = failed.conversationId, senderName = failed.replyToName ?: "", text = failed.replyToText ?: "", timestamp = "", isOutgoing = false)
        }
        sendMessage(conversationId = failed.conversationId, text = failed.text, reply = reply)
    }

    fun startReply(message: Message) {
        if (message.deleted || message.status == "failed") return
        _replyingTo.value = message
    }

    fun cancelReply() {
        _replyingTo.value = null
    }

    /** Deletes one of your own messages for everyone, or discards a message that never sent. */
    fun deleteMessage(message: Message) {
        if (!message.isOutgoing) return
        if (message.id.startsWith("msg_")) {
            _messages.update { list -> list.filterNot { it.id == message.id } }
            return
        }
        val myUid = auth.currentUser?.uid ?: return
        val partner = ConversationUtils.extractPartnerUid(myUid, message.conversationId)
        val convId = ConversationUtils.getConversationId(myUid, partner)
        viewModelScope.launch {
            if (!userRepository.deleteMessage(convId, message.id)) {
                _chatErrorMessage.value = "Could not delete the message. Try again."
            }
        }
    }

    /** Tells the other person that this conversation has been seen up to now. */
    fun markChatSeen() {
        val myUid = auth.currentUser?.uid ?: return
        val partner = activeObservedPartnerUid ?: return
        val convId = ConversationUtils.getConversationId(myUid, partner)
        viewModelScope.launch { userRepository.markSeen(convId, myUid) }
    }

    private var typingJob: Job? = null
    private var typingSent = false

    private fun notifyTyping(active: Boolean) {
        val myUid = auth.currentUser?.uid ?: return
        val partner = activeObservedPartnerUid ?: return
        val convId = ConversationUtils.getConversationId(myUid, partner)
        if (convId.isBlank()) return
        typingJob?.cancel()
        if (active) {
            if (!typingSent) {
                typingSent = true
                viewModelScope.launch { userRepository.setTyping(convId, myUid, true) }
            }
            // Typing state clears itself when the user stops.
            typingJob = viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                typingSent = false
                userRepository.setTyping(convId, myUid, false)
            }
        } else if (typingSent) {
            typingSent = false
            viewModelScope.launch { userRepository.setTyping(convId, myUid, false) }
        }
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

            _partnerTyping.value = false
            _partnerReadAt.value = 0L
            launch { userRepository.observeTyping(conversationId, resolvedPartnerUid).collect { _partnerTyping.value = it } }
            launch { userRepository.observeReadAt(conversationId, resolvedPartnerUid).collect { _partnerReadAt.value = it } }

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
                        invoiceCard = invoiceCard,
                        sentAtMillis = (msg.createdAt?.seconds ?: 0L) * 1000 + (msg.createdAt?.nanoseconds ?: 0) / 1_000_000,
                        replyToId = msg.replyToId,
                        replyToText = msg.replyToText,
                        replyToName = msg.replyToName,
                        deleted = msg.deleted
                    )
                }

                _messages.update { currentList ->
                    val rtdbIds = rtdbMessages.map { it.id }.toSet()
                    // Retain any pending optimistic local messages that haven't yet reflected in RTDB snapshot
                    val localPending = currentList.filter { local ->
                        if (!local.isOutgoing || !local.id.startsWith("msg_")) return@filter false
                        if (local.status == "failed") return@filter true
                        if (rtdbIds.contains(local.id)) return@filter false
                        // A locally added invoice message is dropped once the real one has arrived.
                        if (!local.invoiceId.isNullOrBlank()) {
                            return@filter rtdbMessages.none { it.invoiceId == local.invoiceId }
                        }
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
        notifyTyping(text.isNotBlank())
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
                        val delivered = userRepository.sendInvoiceMessage(
                            conversationId = convForSend,
                            senderName = senderName,
                            invoice = created
                        )
                        if (!delivered) {
                            _chatErrorMessage.value = "The invoice was saved but could not be sent to the chat. Check your connection and try again."
                        }
                    }

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

    // ---- Invoices: edit and cancel (issuer only, while unpaid) ----

    fun editInvoice(
        invoiceId: String,
        title: String,
        amount: Double,
        dueDate: String,
        notes: String?,
        onResult: (Boolean) -> Unit = {}
    ) {
        val current = _invoices.value.firstOrNull { it.id == invoiceId } ?: run { onResult(false); return }
        if (current.organizerUid != auth.currentUser?.uid || current.isPaid) { onResult(false); return }
        val cleanNotes = notes?.trim()?.takeIf { it.isNotEmpty() }
        val updated = current.copy(title = title.trim(), amount = amount, dueDate = dueDate, notes = cleanNotes)
        applyInvoiceChange(updated)
        viewModelScope.launch {
            val ok = userRepository.updateInvoiceDetails(invoiceId, updated.title, amount, dueDate, cleanNotes).isSuccess
            if (!ok) applyInvoiceChange(current)
            onResult(ok)
        }
    }

    fun cancelInvoice(invoiceId: String, onResult: (Boolean) -> Unit = {}) {
        val current = _invoices.value.firstOrNull { it.id == invoiceId } ?: run { onResult(false); return }
        if (current.organizerUid != auth.currentUser?.uid || current.isPaid) { onResult(false); return }
        applyInvoiceChange(current.copy(status = "CANCELLED", isPaid = false))
        viewModelScope.launch {
            val ok = userRepository.updateInvoicePaymentStatus(invoiceId, "CANCELLED", null).isSuccess
            if (!ok) applyInvoiceChange(current)
            onResult(ok)
        }
    }

    private fun applyInvoiceChange(updated: Invoice) {
        _invoices.update { list -> list.map { if (it.id == updated.id) updated else it } }
        _messages.update { list ->
            list.map { msg ->
                if (msg.invoiceCard?.id == updated.id || msg.invoiceId == updated.id) msg.copy(invoiceCard = updated) else msg
            }
        }
    }

    // ---- Meetings: edit and cancel (organiser only) ----

    fun editMeeting(
        meeting: Meeting,
        title: String,
        notes: String?,
        scheduledAtMillis: Long,
        durationMinutes: Int,
        onResult: (Boolean) -> Unit = {}
    ) {
        if (meeting.organizerUid != auth.currentUser?.uid || title.isBlank()) { onResult(false); return }
        val zone = java.time.ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(scheduledAtMillis).atZone(zone)
        val dateString = start.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US))
        val timeString = start.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
        val durationLabel = "$durationMinutes min"
        val ts = com.google.firebase.Timestamp(java.util.Date(scheduledAtMillis))
        val cleanNotes = notes?.trim()?.takeIf { it.isNotEmpty() }
        val updated = meeting.copy(
            title = title.trim(), notes = cleanNotes, scheduledAt = ts, durationMinutes = durationMinutes,
            dateString = dateString, time = timeString, duration = durationLabel
        )
        _meetings.update { list -> list.map { if (it.id == meeting.id) updated else it } }
        viewModelScope.launch {
            val ok = userRepository.updateMeetingDetails(
                meeting.id, updated.title, cleanNotes, ts, durationMinutes, dateString, timeString, durationLabel
            ).isSuccess
            if (!ok) _meetings.update { list -> list.map { if (it.id == meeting.id) meeting else it } }
            onResult(ok)
        }
    }

    fun cancelMeeting(meeting: Meeting, onResult: (Boolean) -> Unit = {}) {
        if (meeting.organizerUid != auth.currentUser?.uid) { onResult(false); return }
        _meetings.update { list -> list.map { if (it.id == meeting.id) it.copy(status = "Cancelled") else it } }
        viewModelScope.launch {
            val ok = userRepository.cancelMeeting(meeting.id).isSuccess
            if (!ok) _meetings.update { list -> list.map { if (it.id == meeting.id) meeting else it } }
            onResult(ok)
        }
    }

    val currentUid: String? get() = auth.currentUser?.uid

    val reminderLeadMinutes: Int get() = com.example.reminders.MeetingReminders.leadMinutes(getApplication())

    fun setReminderLead(minutes: Int) {
        com.example.reminders.MeetingReminders.setLeadMinutes(getApplication(), minutes)
        com.example.reminders.MeetingReminders.sync(getApplication(), _meetings.value)
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
        invoicesRemoteJob?.cancel()
        invoicesRemoteJob = null
        viewModelScope.launch { userRepository.clearLocalInvoices() }
        activeObservedPartnerUid = null
        com.example.reminders.MeetingReminders.sync(getApplication(), emptyList())
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
        _replyingTo.value = null
        _partnerTyping.value = false
        _partnerReadAt.value = 0L
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
