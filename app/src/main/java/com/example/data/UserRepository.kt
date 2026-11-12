package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.ChatMessage
import com.example.model.ChatRequest
import com.example.model.Conversation
import com.example.model.ConversationUtils
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceDao
import com.example.data.local.LocalInvoice
import com.example.data.local.LocalMessage
import com.example.data.local.MessageDao
import com.example.model.Invoice
import com.example.model.formatAmount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(private val db: FirebaseFirestore, context: Context? = null) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        context.applicationContext
    )

    private val localDb: AppDatabase? = context?.let { AppDatabase.getInstance(it) }
    private val messageDao: MessageDao? = localDb?.messageDao()
    private val invoiceDao: InvoiceDao? = localDb?.invoiceDao()

    private val usersRef = db.collection("users")
    private val requestsRef = db.collection("chat_requests")
    private val invoicesRef = db.collection("invoices")

    private val rtdb: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://gen-lang-client-0693758525-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (_: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    // In-memory caches to eliminate redundant Firestore reads and prevent query storms
    private val profileCache = java.util.concurrent.ConcurrentHashMap<String, UserProfile>()
    private val verifiedConversations = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun markConversationVerified(conversationId: String) {
        if (conversationId.isNotBlank()) {
            verifiedConversations.add(conversationId)
        }
    }

    fun isConversationVerified(conversationId: String): Boolean {
        return verifiedConversations.contains(conversationId)
    }

    suspend fun getProfile(uid: String, forceRefresh: Boolean = false): UserProfile? {
        if (uid.isBlank()) return null
        if (!forceRefresh) profileCache[uid]?.let { return it }
        return try {
            val doc = usersRef.document(uid).get().await()
            if (doc.exists()) {
                val profile = doc.toObject(UserProfile::class.java)
                if (profile != null) {
                    profileCache[uid] = profile
                }
                profile
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error getting user profile for $uid: ${e.message}", e)
            null
        }
    }

    suspend fun isRegisteredUser(uid: String): Boolean {
        if (uid.isBlank()) return false
        return getProfile(uid) != null
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        val clean = username.trim().lowercase()
        if (clean.length < 3) return false
        val currentUser = com.google.firebase.Firebase.auth.currentUser
        if (currentUser == null) {
            // Unauthenticated callers cannot read protected user indices;
            // collision check will be validated during account setup.
            return true
        }
        return try {
            !db.collection("usernames").document(clean).get().await().exists()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error checking username availability: ${e.message}", e)
            true
        }
    }

    suspend fun ensureProfile(
        uid: String,
        email: String,
        fallbackName: String = "",
        username: String = ""
    ): UserProfile {
        if (uid.isBlank()) return UserProfile()
        return try {
            val existing = getProfile(uid)
            if (existing != null) {
                ensureUsernameIndex(existing)
                val updates = mutableMapOf<String, Any>()
                if (existing.email.isBlank() && email.isNotBlank()) {
                    updates["email"] = email
                }
                if (existing.username.isBlank() && username.isNotBlank()) {
                    updates["username"] = username.trim().lowercase()
                }
                // Share the device timezone and country so contacts see real local times.
                val deviceZone = java.util.TimeZone.getDefault().id
                val deviceCountry = java.util.Locale.getDefault().country
                if (existing.timezone.isBlank() && deviceZone.isNotBlank()) updates["timezone"] = deviceZone
                if (existing.countryCode.isBlank() && deviceCountry.isNotBlank()) updates["countryCode"] = deviceCountry
                if (updates.isNotEmpty()) {
                    updates["updatedAt"] = FieldValue.serverTimestamp()
                    usersRef.document(uid).update(updates).await()
                    profileCache.remove(uid)
                    existing.copy(
                        timezone = if (existing.timezone.isBlank()) deviceZone else existing.timezone,
                        countryCode = if (existing.countryCode.isBlank()) deviceCountry else existing.countryCode,
                        email = if (existing.email.isBlank()) email else existing.email,
                        username = if (existing.username.isBlank()) username.trim().lowercase() else existing.username
                    )
                } else {
                    existing
                }
            } else {
                val name = fallbackName.ifBlank {
                    email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }.ifBlank { "User" }

                var cleanUsername = username.trim().lowercase().ifBlank {
                    email.substringBefore("@").lowercase().replace(Regex("[^a-z0-9_.]"), "")
                }
                // A username derived from an email (e.g. Google sign-in) may already be taken.
                if (cleanUsername.length < 3) cleanUsername = "user$cleanUsername"
                try {
                    if (db.collection("usernames").document(cleanUsername).get().await().exists()) {
                        cleanUsername += (100..999).random()
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }

                val payload = mutableMapOf<String, Any?>(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to name,
                    "username" to cleanUsername,
                    "timezone" to java.util.TimeZone.getDefault().id,
                    "countryCode" to java.util.Locale.getDefault().country,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                usersRef.document(uid).set(payload).await()

                if (cleanUsername.isNotBlank()) {
                    try {
                        val indexDoc = mapOf(
                            "uid" to uid,
                            "username" to cleanUsername,
                            "displayName" to name,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                        db.collection("usernames").document(cleanUsername).set(indexDoc).await()
                    } catch (e: Exception) {
                        Log.e("UserRepository", "Error creating username index for $cleanUsername: ${e.message}", e)
                    }
                }

                UserProfile(
                    uid = uid,
                    email = email,
                    displayName = name,
                    username = cleanUsername
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error ensuring user profile for $uid: ${e.message}", e)
            UserProfile(uid = uid, email = email, displayName = fallbackName, username = username)
        }
    }

    /** Older profiles may have no entry in the username index; create it once if it is free. */
    private suspend fun ensureUsernameIndex(profile: UserProfile) {
        val clean = profile.username.trim().lowercase()
        if (clean.length < 3) return
        try {
            val ref = db.collection("usernames").document(clean)
            if (!ref.get().await().exists()) {
                ref.set(
                    mapOf(
                        "uid" to profile.uid,
                        "username" to clean,
                        "displayName" to profile.displayName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    suspend fun getUserByUsername(username: String): UserProfile? {
        val clean = username.trim().lowercase()
        if (clean.length < 3) return null
        return try {
            val doc = db.collection("usernames").document(clean).get().await()
            if (doc.exists()) {
                val cUid = doc.getString("uid") ?: ""
                val name = doc.getString("displayName") ?: ""
                val email = doc.getString("email") ?: ""
                UserProfile(uid = cUid, displayName = name, username = clean, email = email)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user by username '$clean': ${e.message}", e)
            null
        }
    }

    /**
     * Writes both participants' inbox entries in one multi-path update: the sender's own entry
     * (unread cleared) and the partner's entry (preview refreshed, unread counter incremented).
     */
    private fun putInboxEntries(
        updates: MutableMap<String, Any>,
        conversationId: String,
        senderUid: String,
        partnerUid: String,
        preview: String
    ) {
        updates["userConversations/$senderUid/$conversationId"] = mapOf<String, Any>(
            "id" to conversationId,
            "partnerUid" to partnerUid,
            "lastMessage" to preview,
            "lastMessageAt" to ServerValue.TIMESTAMP,
            "lastMessageSenderUid" to senderUid,
            "unread" to 0
        )
        val partnerPath = "userConversations/$partnerUid/$conversationId"
        updates["$partnerPath/id"] = conversationId
        updates["$partnerPath/partnerUid"] = senderUid
        updates["$partnerPath/lastMessage"] = preview
        updates["$partnerPath/lastMessageAt"] = ServerValue.TIMESTAMP
        updates["$partnerPath/lastMessageSenderUid"] = senderUid
        updates["$partnerPath/unread"] = ServerValue.increment(1)
    }

    /** Clears the unread counter of a conversation for the signed-in user. */
    suspend fun markConversationRead(myUid: String, conversationId: String) {
        if (myUid.isBlank() || conversationId.isBlank()) return
        try {
            rtdb.getReference("userConversations/$myUid/$conversationId/unread").setValue(0).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error clearing unread for $conversationId: ${e.message}")
        }
    }

    /** Saves the fields the user edits in Settings. Null arguments are left unchanged. */
    suspend fun updateProfileSettings(
        uid: String,
        displayName: String? = null,
        timezone: String? = null,
        workingStartHour: Int? = null,
        workingEndHour: Int? = null,
        availability: String? = null
    ): Boolean {
        if (uid.isBlank()) return false
        val updates = mutableMapOf<String, Any>()
        displayName?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["displayName"] = it }
        timezone?.takeIf { it.isNotBlank() }?.let { updates["timezone"] = it }
        workingStartHour?.let { updates["workingStartHour"] = it }
        workingEndHour?.let { updates["workingEndHour"] = it }
        availability?.let { updates["availability"] = it }
        if (updates.isEmpty()) return true
        updates["updatedAt"] = FieldValue.serverTimestamp()
        return try {
            usersRef.document(uid).update(updates).await()
            profileCache.remove(uid)
            // Keep the name in the username index in step so new contacts see the new name.
            val newName = updates["displayName"] as? String
            val username = getProfile(uid, forceRefresh = true)?.username?.trim()?.lowercase().orEmpty()
            if (newName != null && username.length >= 3) {
                try {
                    db.collection("usernames").document(username)
                        .update(mapOf("displayName" to newName, "updatedAt" to FieldValue.serverTimestamp())).await()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }
            }
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error updating profile settings: ${e.message}", e)
            false
        }
    }

    fun clearCaches() {
        profileCache.clear()
        verifiedConversations.clear()
        Log.d("SyncLineDiagnostics", "UserRepository: profile and conversation caches cleared")
    }

    suspend fun addClientRelationship(myUid: String, clientProfile: UserProfile): Boolean {
        if (myUid.isBlank() || clientProfile.uid.isBlank()) return false
        if (myUid == clientProfile.uid) return false
        return try {
            val clientDocRef = usersRef.document(myUid).collection("clients").document(clientProfile.uid)
            val existing = try {
                clientDocRef.get().await()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                null
            }
            val payload = mutableMapOf<String, Any>(
                "clientId" to clientProfile.uid,
                "uid" to clientProfile.uid,
                "username" to clientProfile.username,
                "displayName" to clientProfile.displayName,
                "email" to clientProfile.email,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existing == null || !existing.exists() || existing.get("createdAt") == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            clientDocRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error adding client relationship: ${e.message}", e)
            false
        }
    }

    fun observeUserClients(myUid: String): Flow<List<UserProfile>> {
        if (myUid.isBlank()) return flowOf(emptyList())

        val clientsRef = usersRef.document(myUid).collection("clients")
        val subcollectionFlow = clientsRef.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                val cUid = doc.getString("uid") ?: doc.id
                val username = doc.getString("username") ?: ""
                val name = doc.getString("displayName") ?: ""
                val email = doc.getString("email") ?: ""
                if (cUid.isNotBlank()) {
                    UserProfile(uid = cUid, displayName = name, username = username, email = email)
                } else null
            }
        }.catch { e ->
            Log.e("UserRepository", "Error observing user clients for $myUid: ${e.message}", e)
            emit(emptyList())
        }

        val incomingAcceptedFlow = requestsRef
            .whereEqualTo("targetUid", myUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatRequest::class.java)
                    .filter { it.status == "ACCEPTED" }
                    .map { req ->
                        UserProfile(
                            uid = req.senderUid,
                            displayName = req.senderDisplayName.ifBlank { req.senderUsername },
                            username = req.senderUsername,
                            email = req.senderEmail
                        )
                    }
            }
            .catch { emit(emptyList()) }

        val outgoingAcceptedFlow = requestsRef
            .whereEqualTo("senderUid", myUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ChatRequest::class.java)
                    .filter { it.status == "ACCEPTED" }
                    .map { req ->
                        UserProfile(
                            uid = req.targetUid,
                            displayName = req.targetDisplayName.ifBlank { req.targetUsername },
                            username = req.targetUsername,
                            email = req.targetEmail
                        )
                    }
            }
            .catch { emit(emptyList()) }

        return combine(subcollectionFlow, incomingAcceptedFlow, outgoingAcceptedFlow) { sub, inc, out ->
            (sub + inc + out)
                .filter { it.uid.isNotBlank() && it.uid != myUid }
                .distinctBy { it.uid }
        }
    }

    fun observeProfile(uid: String): Flow<UserProfile?> {
        val docRef = usersRef.document(uid)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    profileCache[uid] = profile
                }
                profile
            } else {
                null
            }
        }.catch { e ->
            Log.e("UserRepository", "Error observing profile for $uid: ${e.message}", e)
            emit(null)
        }
    }

    suspend fun sendChatRequest(senderProfile: UserProfile, targetProfile: UserProfile): String? {
        if (senderProfile.uid.isBlank() || targetProfile.uid.isBlank()) {
            return "Invalid user profiles."
        }
        if (senderProfile.uid == targetProfile.uid) {
            return "You cannot send a chat request to yourself."
        }
        val requestId = "${senderProfile.uid}_${targetProfile.uid}"
        val requestRef = requestsRef.document(requestId)

        return try {
            val existing = requestRef.get().await()
            if (existing.exists()) {
                val currentStatus = existing.getString("status") ?: "PENDING"
                if (currentStatus == "PENDING") {
                    return "Chat request is already pending for '@${targetProfile.username}'."
                } else if (currentStatus == "ACCEPTED") {
                    return "You are already connected with '@${targetProfile.username}'."
                }
            }

            val payload = mapOf(
                "senderUid" to senderProfile.uid,
                "senderUsername" to senderProfile.username,
                "senderDisplayName" to senderProfile.displayName,
                "senderEmail" to senderProfile.email,
                "targetUid" to targetProfile.uid,
                "targetUsername" to targetProfile.username,
                "targetDisplayName" to targetProfile.displayName,
                "targetEmail" to targetProfile.email,
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            requestRef.set(payload).await()
            Log.d("SyncLineDiagnostics", "Chat request sent: id=$requestId")
            null
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sending chat request: ${e.message}", e)
            e.localizedMessage ?: "Failed to send chat request."
        }
    }

    /**
     * Atomically accepts a chat request by updating status, establishing bilateral
     * client relationships, and initializing the shared conversation document.
     */
    suspend fun acceptChatRequest(request: ChatRequest): Boolean {
        val senderUid = request.senderUid.trim()
        val targetUid = request.targetUid.trim()
        val resolvedId = request.id.ifBlank { if (senderUid.isNotBlank() && targetUid.isNotBlank()) "${senderUid}_${targetUid}" else "" }
        if (resolvedId.isBlank()) return false

        val requestRef = requestsRef.document(resolvedId)
        val conversationId = ConversationUtils.getConversationId(senderUid, targetUid)

        return try {
            // Status change and both contact documents are committed together, so a half-accepted
            // request can never exist.
            val batch = db.batch()
            val updatePayload = mutableMapOf<String, Any>(
                "status" to "ACCEPTED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (senderUid.isNotBlank()) updatePayload["senderUid"] = senderUid
            if (targetUid.isNotBlank()) updatePayload["targetUid"] = targetUid
            if (request.senderUsername.isNotBlank()) updatePayload["senderUsername"] = request.senderUsername
            if (request.targetUsername.isNotBlank()) updatePayload["targetUsername"] = request.targetUsername
            batch.set(requestRef, updatePayload, com.google.firebase.firestore.SetOptions.merge())

            if (targetUid.isNotBlank() && senderUid.isNotBlank()) {
                batch.set(
                    usersRef.document(targetUid).collection("clients").document(senderUid),
                    mapOf(
                        "clientId" to senderUid,
                        "uid" to senderUid,
                        "username" to request.senderUsername,
                        "displayName" to request.senderDisplayName,
                        "email" to request.senderEmail,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                batch.set(
                    usersRef.document(senderUid).collection("clients").document(targetUid),
                    mapOf(
                        "clientId" to targetUid,
                        "uid" to targetUid,
                        "username" to request.targetUsername,
                        "displayName" to request.targetDisplayName,
                        "email" to request.targetEmail,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
            batch.commit().await()

            // 4. Initialize shared conversation in RTDB and cache
            if (conversationId.isNotBlank()) {
                try {
                    val updates = mutableMapOf<String, Any>(
                        "conversations/$conversationId/participants/$senderUid" to true,
                        "conversations/$conversationId/participants/$targetUid" to true,
                        "conversations/$conversationId/lastMessage" to "Chat request accepted",
                        "conversations/$conversationId/lastMessageAt" to ServerValue.TIMESTAMP,
                        "conversations/$conversationId/lastMessageSenderUid" to targetUid
                    )
                    for ((owner, partner) in listOf(targetUid to senderUid, senderUid to targetUid)) {
                        updates["userConversations/$owner/$conversationId"] = mapOf<String, Any>(
                            "id" to conversationId,
                            "partnerUid" to partner,
                            "lastMessage" to "Chat request accepted",
                            "lastMessageAt" to ServerValue.TIMESTAMP,
                            "lastMessageSenderUid" to targetUid,
                            "unread" to 0
                        )
                    }
                    rtdb.reference.updateChildren(updates).await()
                    verifiedConversations.add(conversationId)
                    Log.d("SyncLineDiagnostics", "acceptChatRequest: initialized RTDB conversation $conversationId")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("UserRepository", "Non-fatal: could not init RTDB conversation: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error accepting chat request $resolvedId: ${e.message}", e)
            false
        }
    }

    suspend fun declineChatRequest(requestId: String): Boolean {
        if (requestId.isBlank()) return false
        val requestRef = requestsRef.document(requestId)
        return try {
            requestRef.set(
                mapOf(
                    "status" to "DECLINED",
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error declining chat request $requestId: ${e.message}", e)
            false
        }
    }

    fun observeChatRequests(myUid: String): Flow<List<ChatRequest>> {
        if (myUid.isBlank()) return flowOf(emptyList())

        val incomingFlow = requestsRef
            .whereEqualTo("targetUid", myUid)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(ChatRequest::class.java) }
            .catch { emit(emptyList()) }

        val outgoingFlow = requestsRef
            .whereEqualTo("senderUid", myUid)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(ChatRequest::class.java) }
            .catch { emit(emptyList()) }

        return combine(incomingFlow, outgoingFlow) { incoming, outgoing ->
            (incoming + outgoing).distinctBy { it.id }.sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
    }

    /**
     * Ensures that the shared conversation metadata exists in RTDB for two participant UIDs.
     */
    suspend fun ensureConversationExists(myUid: String, partnerUid: String): String {
        if (myUid.isBlank() || partnerUid.isBlank() || myUid == partnerUid) return ""
        val conversationId = ConversationUtils.getConversationId(myUid, partnerUid)
        if (conversationId.isBlank()) return ""

        if (verifiedConversations.contains(conversationId)) {
            Log.d("SyncLineDiagnostics", "ensureConversationExists: conversation $conversationId verified in cache")
            return conversationId
        }

        try {
            val convParticipantsRef = rtdb.getReference("conversations").child(conversationId).child("participants")
            val snapshot = convParticipantsRef.get().await()
            val updates = mutableMapOf<String, Any>()
            if (!snapshot.exists()) {
                // Initialize trusted participants and shared metadata
                updates["conversations/$conversationId/participants/$myUid"] = true
                updates["conversations/$conversationId/participants/$partnerUid"] = true
                updates["conversations/$conversationId/lastMessage"] = "Conversation started"
                updates["conversations/$conversationId/lastMessageAt"] = ServerValue.TIMESTAMP
                updates["conversations/$conversationId/lastMessageSenderUid"] = myUid
            }

            // Write only to current user's userConversations (never cross-account)
            val metaSender = mapOf<String, Any>(
                "id" to conversationId,
                "partnerUid" to partnerUid,
                "lastMessage" to "Conversation started",
                "lastMessageAt" to ServerValue.TIMESTAMP,
                "lastMessageSenderUid" to myUid
            )
            updates["userConversations/$myUid/$conversationId"] = metaSender

            rtdb.reference.updateChildren(updates).await()
            verifiedConversations.add(conversationId)
            Log.d("SyncLineDiagnostics", "[ENSURE_CONV_RTDB_SUCCESS] Initialized RTDB conv $conversationId")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SyncLineDiagnostics", "[ENSURE_CONV_RTDB_FAIL] Exception: ${e.javaClass.simpleName}, Msg: ${e.message}", e)
        }
        return conversationId
    }

    /**
     * Persists a message to Firebase Realtime Database and updates conversation metadata atomically.
     */
    suspend fun sendMessage(
        conversationId: String,
        senderUid: String,
        partnerUid: String = "",
        senderName: String,
        text: String,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToName: String? = null
    ): Boolean {
        val cleanText = text.trim()
        if (conversationId.isBlank() || senderUid.isBlank() || cleanText.isBlank()) {
            Log.e("SyncLineDiagnostics", "[SEND_MESSAGE_FAIL] Stage: VALIDATION. Reason: Blank parameter. convId='$conversationId', senderUid='$senderUid'")
            return false
        }
        if (cleanText.length > 2000) {
            Log.e("SyncLineDiagnostics", "[SEND_MESSAGE_FAIL] Stage: VALIDATION. Reason: Text too long (${cleanText.length} chars)")
            return false
        }

        val resolvedPartnerUid = if (partnerUid.isNotBlank() && partnerUid != senderUid) {
            partnerUid
        } else {
            ConversationUtils.extractPartnerUid(senderUid, conversationId)
        }

        if (resolvedPartnerUid.isBlank() || resolvedPartnerUid == senderUid) {
            Log.e("SyncLineDiagnostics", "[SEND_MESSAGE_FAIL] Stage: VALIDATION. Reason: Invalid partner UID. Sender='$senderUid', Partner='$resolvedPartnerUid'")
            return false
        }

        val expectedConvId = ConversationUtils.getConversationId(senderUid, resolvedPartnerUid)
        if (expectedConvId.isBlank() || expectedConvId != conversationId) {
            Log.e("SyncLineDiagnostics", "[SEND_MESSAGE_FAIL] Stage: VALIDATION. Reason: Noncanonical conversation ID. Given='$conversationId', Expected='$expectedConvId'")
            return false
        }

        var failedMsgId: String? = null
        return try {
            val messagesRef = rtdb.getReference("conversations").child(conversationId).child("messages")
            val newMsgRef = messagesRef.push()
            val msgId = newMsgRef.key ?: "msg_${System.currentTimeMillis()}"
            failedMsgId = msgId

            val msgPayload = mutableMapOf<String, Any>(
                "id" to msgId,
                "conversationId" to conversationId,
                "senderUid" to senderUid,
                "senderName" to senderName,
                "text" to cleanText,
                "createdAt" to ServerValue.TIMESTAMP
            )
            if (!replyToId.isNullOrBlank()) {
                msgPayload["replyTo"] = mapOf(
                    "id" to replyToId,
                    "text" to (replyToText ?: "").take(120),
                    "name" to (replyToName ?: "")
                )
            }

            // Save outgoing message to local Room database
            messageDao?.insertMessage(
                LocalMessage(
                    id = msgId,
                    conversationId = conversationId,
                    senderUid = senderUid,
                    senderName = senderName,
                    text = cleanText,
                    timestamp = System.currentTimeMillis(),
                    isOutgoing = true,
                    replyToId = replyToId?.takeIf { it.isNotBlank() },
                    replyToText = replyToText?.take(120),
                    replyToName = replyToName
                )
            )

            val updates = mutableMapOf<String, Any>()
            // Ensure participants are registered if not yet verified
            if (!verifiedConversations.contains(conversationId)) {
                updates["conversations/$conversationId/participants/$senderUid"] = true
                updates["conversations/$conversationId/participants/$resolvedPartnerUid"] = true
            }

            // Shared conversation metadata under /conversations/{conversationId}
            updates["conversations/$conversationId/messages/$msgId"] = msgPayload
            updates["conversations/$conversationId/lastMessage"] = cleanText
            updates["conversations/$conversationId/lastMessageAt"] = ServerValue.TIMESTAMP
            updates["conversations/$conversationId/lastMessageSenderUid"] = senderUid

            // Sender's own summary entry
            putInboxEntries(updates, conversationId, senderUid, resolvedPartnerUid, cleanText)

            rtdb.reference.updateChildren(updates).await()
            verifiedConversations.add(conversationId)
            Log.d("SyncLineDiagnostics", "[RTDB_SEND_SUCCESS] msgId=$msgId in conv=$conversationId")
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SyncLineDiagnostics", "[RTDB_SEND_FAIL] Exception: ${e.javaClass.simpleName}, Msg: ${e.message}", e)
            failedMsgId?.let { try { messageDao?.deleteMessageById(it) } catch (_: Exception) {} }
            false
        }
    }

    /**
     * Observes messages for a conversation, syncing RTDB courier messages to local Room database.
     */
    fun observeMessages(conversationId: String, limit: Long = 100): Flow<List<ChatMessage>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid ?: ""
        Log.d("SyncLineDiagnostics", "observeMessages: registering RTDB courier listener for conversationId=$conversationId")
        val messagesRef = rtdb.getReference("conversations").child(conversationId).child("messages").limitToLast(limit.toInt())

        val rtdbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val incomingList = mutableListOf<LocalMessage>()
                for (child in snapshot.children) {
                    val msgId = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                    val cId = child.child("conversationId").getValue(String::class.java) ?: conversationId
                    val sUid = child.child("senderUid").getValue(String::class.java) ?: ""
                    val sName = child.child("senderName").getValue(String::class.java) ?: ""
                    val txt = child.child("text").getValue(String::class.java) ?: ""
                    val createdEpoch = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val type = child.child("type").getValue(String::class.java) ?: "text"
                    val invId = child.child("invoiceId").getValue(String::class.java)
                    val isDeleted = child.child("deleted").getValue(Boolean::class.java) == true
                    val replyToId = child.child("replyTo").child("id").getValue(String::class.java)
                    val replyToText = child.child("replyTo").child("text").getValue(String::class.java)
                    val replyToName = child.child("replyTo").child("name").getValue(String::class.java)

                    var parsedPreviewInvoice: Invoice? = null
                    if (type == "invoice_share" && child.hasChild("invoicePreview")) {
                        val p = child.child("invoicePreview")
                        val previewId = p.child("id").getValue(String::class.java) ?: invId ?: ""
                        if (previewId.isNotBlank()) {
                            val invNumber = p.child("invoiceNumber").getValue(String::class.java) ?: ""
                            val invTitle = p.child("title").getValue(String::class.java) ?: ""
                            val cName = p.child("clientName").getValue(String::class.java) ?: ""
                            val cUid = p.child("clientUid").getValue(String::class.java) ?: ""
                            val orgUid = p.child("organizerUid").getValue(String::class.java) ?: ""
                            val pId = p.child("projectId").getValue(String::class.java)
                            val pName = p.child("projectName").getValue(String::class.java)
                            val amt = p.child("amount").getValue(Double::class.java)
                                ?: (p.child("amount").getValue(Long::class.java)?.toDouble() ?: 0.0)
                            val currCode = p.child("currencyCode").getValue(String::class.java) ?: "INR"
                            val dDate = p.child("dueDate").getValue(String::class.java) ?: ""
                            val nts = p.child("notes").getValue(String::class.java)
                            val st = p.child("status").getValue(String::class.java) ?: "UNPAID"
                            val isP = p.child("isPaid").getValue(Boolean::class.java) ?: false
                            val pAt = p.child("paidAt").getValue(String::class.java)
                            val cAt = p.child("createdAt").getValue(String::class.java) ?: "Today"

                            parsedPreviewInvoice = Invoice(
                                id = previewId,
                                invoiceNumber = invNumber,
                                title = invTitle,
                                clientName = cName,
                                clientUid = cUid,
                                organizerUid = orgUid,
                                projectId = pId,
                                projectName = pName,
                                amount = amt,
                                currencyCode = currCode,
                                currency = currCode,
                                dueDate = dDate,
                                notes = nts,
                                status = st,
                                isPaid = isP,
                                paidAt = pAt,
                                createdAt = cAt
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val existing = invoiceDao?.getInvoiceByIdSync(previewId)
                                    if (existing == null) {
                                        invoiceDao?.insertInvoice(
                                            LocalInvoice(
                                                id = previewId,
                                                invoiceNumber = invNumber,
                                                title = invTitle,
                                                clientName = cName,
                                                clientUid = cUid,
                                                organizerUid = orgUid,
                                                projectId = pId,
                                                projectName = pName,
                                                amount = amt,
                                                currencyCode = currCode,
                                                dueDate = dDate,
                                                notes = nts,
                                                status = st,
                                                isPaid = isP,
                                                paidAt = pAt,
                                                createdAt = cAt,
                                                createdAtMillis = createdEpoch
                                            )
                                        )
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }

                    if (msgId.isNotBlank()) {
                        incomingList.add(
                            LocalMessage(
                                id = msgId,
                                conversationId = cId,
                                senderUid = sUid,
                                senderName = sName,
                                text = if (isDeleted) "" else txt,
                                timestamp = createdEpoch,
                                isOutgoing = (sUid == currentUid),
                                type = type,
                                invoiceId = invId,
                                replyToId = replyToId,
                                replyToText = replyToText,
                                replyToName = replyToName,
                                deleted = isDeleted
                            )
                        )
                    }
                }

                if (incomingList.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            messageDao?.insertMessages(incomingList)
                        } catch (e: Exception) {
                            Log.e("UserRepository", "Error saving incoming messages to Room: ${e.message}")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SyncLineDiagnostics", "RTDB observeMessages cancelled for $conversationId: ${error.message}")
            }
        }

        messagesRef.addValueEventListener(rtdbListener)

        // Observe local messages from Room DB flow for persistent offline rendering
        val roomDaoFlow = messageDao?.getMessagesForConversation(conversationId)
        val roomJob = CoroutineScope(Dispatchers.IO).launch {
            if (roomDaoFlow != null) {
                roomDaoFlow.collect { localList ->
                    val uiList = localList.map { local ->
                        var invCardPreview: Invoice? = null
                        if (local.type == "invoice_share" && !local.invoiceId.isNullOrBlank()) {
                            val cachedLocalInv = invoiceDao?.getInvoiceByIdSync(local.invoiceId)
                            if (cachedLocalInv != null) {
                                invCardPreview = Invoice(
                                    id = cachedLocalInv.id,
                                    invoiceNumber = cachedLocalInv.invoiceNumber,
                                    title = cachedLocalInv.title,
                                    clientName = cachedLocalInv.clientName,
                                    clientUid = cachedLocalInv.clientUid,
                                    organizerUid = cachedLocalInv.organizerUid,
                                    projectId = cachedLocalInv.projectId,
                                    projectName = cachedLocalInv.projectName,
                                    amount = cachedLocalInv.amount,
                                    currencyCode = cachedLocalInv.currencyCode,
                                    currency = cachedLocalInv.currencyCode,
                                    dueDate = cachedLocalInv.dueDate,
                                    notes = cachedLocalInv.notes,
                                    status = cachedLocalInv.status,
                                    isPaid = cachedLocalInv.isPaid,
                                    paidAt = cachedLocalInv.paidAt,
                                    createdAt = cachedLocalInv.createdAt
                                )
                            }
                        }
                        ChatMessage(
                            id = local.id,
                            conversationId = local.conversationId,
                            senderUid = local.senderUid,
                            senderName = local.senderName,
                            text = local.text,
                            type = local.type,
                            invoiceId = local.invoiceId,
                            invoicePreview = invCardPreview,
                            replyToId = local.replyToId,
                            replyToText = local.replyToText,
                            replyToName = local.replyToName,
                            deleted = local.deleted,
                            createdAt = com.google.firebase.Timestamp(local.timestamp / 1000, ((local.timestamp % 1000) * 1_000_000).toInt())
                        )
                    }.sortedWith(
                        compareBy<ChatMessage> { it.createdAt?.seconds ?: Long.MAX_VALUE }
                            .thenBy { it.id }
                    )
                    trySend(uiList)
                }
            } else {
                // Fallback if Room DB context not provided
                trySend(emptyList())
            }
        }

        awaitClose {
            messagesRef.removeEventListener(rtdbListener)
            roomJob.cancel()
        }
    }


    /** Soft-deletes one of your own messages: it stays in the thread as "This message was deleted". */
    suspend fun deleteMessage(conversationId: String, messageId: String): Boolean {
        if (conversationId.isBlank() || messageId.isBlank()) return false
        return try {
            rtdb.reference.updateChildren(
                mapOf(
                    "conversations/$conversationId/messages/$messageId/deleted" to true,
                    "conversations/$conversationId/messages/$messageId/text" to ""
                )
            ).await()
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error deleting message $messageId: ${e.message}")
            false
        }
    }

    /** Whether the other person is typing in this conversation right now. */
    fun observeTyping(conversationId: String, partnerUid: String): Flow<Boolean> = callbackFlow {
        val ref = rtdb.getReference("conversations/$conversationId/typing/$partnerUid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** The moment (epoch millis) the other person last had this conversation open. */
    fun observeReadAt(conversationId: String, partnerUid: String): Flow<Long> = callbackFlow {
        val ref = rtdb.getReference("conversations/$conversationId/readAt/$partnerUid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Long::class.java) ?: 0L)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setTyping(conversationId: String, myUid: String, typing: Boolean) {
        if (conversationId.isBlank() || myUid.isBlank()) return
        try {
            val ref = rtdb.getReference("conversations/$conversationId/typing/$myUid")
            if (typing) {
                ref.onDisconnect().removeValue()
                ref.setValue(true).await()
            } else {
                ref.removeValue().await()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    suspend fun markSeen(conversationId: String, myUid: String) {
        if (conversationId.isBlank() || myUid.isBlank()) return
        try {
            rtdb.getReference("conversations/$conversationId/readAt/$myUid").setValue(ServerValue.TIMESTAMP).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    /**
     * Observes real-time user conversations from Firebase Realtime Database for inbox sorting and bilateral discovery.
     */
    fun observeUserConversations(myUid: String): Flow<List<Conversation>> = callbackFlow {
        if (myUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d("SyncLineDiagnostics", "observeUserConversations: registering RTDB listener for myUid=$myUid")
        val userConvRef = rtdb.getReference("userConversations").child(myUid)

        var userConvList = emptyList<Conversation>()

        fun emitCombined() {
            val mergedMap = mutableMapOf<String, Conversation>()

            for (conv in userConvList) {
                mergedMap[conv.id] = conv
            }

            val result = mergedMap.values.sortedByDescending { it.lastMessageAt?.seconds ?: 0L }
            trySend(result)
        }

        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Conversation>()
                for (child in snapshot.children) {
                    val convId = child.child("id").getValue(String::class.java) ?: child.key ?: ""
                    val partnerUid = child.child("partnerUid").getValue(String::class.java) ?: ""
                    val lastMsg = child.child("lastMessage").getValue(String::class.java) ?: ""
                    val lastMsgAt = child.child("lastMessageAt").getValue(Long::class.java)
                    val lastSenderUid = child.child("lastMessageSenderUid").getValue(String::class.java) ?: ""
                    val unread = (child.child("unread").getValue(Long::class.java) ?: 0L).toInt().coerceAtLeast(0)
                    val timestamp = if (lastMsgAt != null) com.google.firebase.Timestamp(lastMsgAt / 1000, 0) else null

                    val participants = if (partnerUid.isNotBlank()) {
                        listOf(myUid, partnerUid).sorted()
                    } else if (convId.contains("_")) {
                        convId.split("_").take(2).sorted()
                    } else {
                        emptyList()
                    }

                    if (convId.isNotBlank()) {
                        verifiedConversations.add(convId)
                    }

                    list.add(
                        Conversation(
                            id = convId,
                            participantUids = participants,
                            lastMessage = lastMsg,
                            lastMessageAt = timestamp,
                            lastMessageSenderUid = lastSenderUid,
                            unreadCount = unread
                        )
                    )
                }
                userConvList = list
                emitCombined()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SyncLineDiagnostics", "RTDB observeUserConversations cancelled for $myUid: ${error.message}")
            }
        }

        userConvRef.addValueEventListener(userListener)

        awaitClose {
            userConvRef.removeEventListener(userListener)
        }
    }

    suspend fun sendProjectCardMessage(
        conversationId: String,
        senderUid: String,
        partnerUid: String = "",
        senderName: String,
        project: com.example.model.Project
    ): Boolean {
        if (conversationId.isBlank() || senderUid.isBlank()) return false

        val resolvedPartnerUid = if (partnerUid.isNotBlank() && partnerUid != senderUid) {
            partnerUid
        } else {
            ConversationUtils.extractPartnerUid(senderUid, conversationId)
        }

        // The caller may pass just the other person's id; chat rules only accept "<uidA>_<uidB>".
        val convIdToUse = if (resolvedPartnerUid.isNotBlank()) {
            ConversationUtils.getConversationId(senderUid, resolvedPartnerUid)
        } else conversationId
        if (convIdToUse.isBlank()) return false

        return try {
            val messagesRef = rtdb.getReference("conversations").child(convIdToUse).child("messages")
            val newMsgRef = messagesRef.push()
            val msgId = newMsgRef.key ?: "msg_${System.currentTimeMillis()}"

            val tasksPayload = project.tasks.map { t ->
                mapOf(
                    "id" to t.id,
                    "title" to t.title,
                    "isCompleted" to t.isCompleted
                )
            }

            val projectCardPayload = mapOf<String, Any>(
                "id" to project.id,
                "title" to project.title,
                "clientName" to project.clientName,
                "dueDate" to project.dueDate,
                "progress" to project.progress,
                "status" to project.status.name,
                "cardColorHexLight" to project.cardColorHexLight,
                "cardColorHexDark" to project.cardColorHexDark,
                "tasks" to tasksPayload
            )

            val msgPayload = mapOf<String, Any>(
                "id" to msgId,
                "conversationId" to conversationId,
                "senderUid" to senderUid,
                "senderName" to senderName,
                "text" to "",
                "createdAt" to ServerValue.TIMESTAMP,
                "projectCard" to projectCardPayload
            )

            val updates = mutableMapOf<String, Any>()
            if (!verifiedConversations.contains(conversationId)) {
                updates["conversations/$conversationId/participants/$senderUid"] = true
                updates["conversations/$conversationId/participants/$resolvedPartnerUid"] = true
            }

            updates["conversations/$conversationId/messages/$msgId"] = msgPayload
            updates["conversations/$conversationId/lastMessage"] = "Project: ${project.title}"
            updates["conversations/$conversationId/lastMessageAt"] = ServerValue.TIMESTAMP
            updates["conversations/$conversationId/lastMessageSenderUid"] = senderUid

            putInboxEntries(updates, conversationId, senderUid, resolvedPartnerUid, "Project: ${project.title}")

            rtdb.reference.updateChildren(updates).await()
            verifiedConversations.add(conversationId)
            Log.d("SyncLineDiagnostics", "[RTDB_SEND_PROJECT_SUCCESS] msgId=$msgId in conv=$conversationId")
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SyncLineDiagnostics", "[RTDB_SEND_PROJECT_FAIL] Exception: ${e.javaClass.simpleName}, Msg: ${e.message}", e)
            false
        }
    }

    fun updateProjectTasksInChat(
        conversationId: String,
        projectId: String,
        tasks: List<com.example.model.ProjectTask>,
        progress: Int
    ) {
        if (conversationId.isBlank() || projectId.isBlank()) return
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid ?: return
        val partnerUid = ConversationUtils.extractPartnerUid(currentUid, conversationId)
        val actualConvId = ConversationUtils.getConversationId(currentUid, partnerUid)
        if (actualConvId.isBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val msgRef = rtdb.getReference("conversations").child(actualConvId).child("messages")
                val snapshot = msgRef.get().await()
                for (child in snapshot.children) {
                    val pCardId = child.child("projectCard").child("id").getValue(String::class.java)
                    if (pCardId == projectId) {
                        val tasksPayload = tasks.map { t ->
                            mapOf("id" to t.id, "title" to t.title, "isCompleted" to t.isCompleted)
                        }
                        child.ref.child("projectCard").child("tasks").setValue(tasksPayload)
                        child.ref.child("projectCard").child("progress").setValue(progress)
                        break
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("UserRepository", "Error updating project tasks in RTDB: ${e.message}")
            }
        }
    }

    private val projectsRef = db.collection("projects")

    suspend fun createProjectInFirestore(project: com.example.model.Project): Result<com.example.model.Project> {
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User unauthenticated"))
        val docRef = if (project.id.isNotBlank()) projectsRef.document(project.id) else projectsRef.document()
        val stableId = docRef.id

        val participants = (listOf(currentUid) + (if (project.clientUid.isNotBlank()) listOf(project.clientUid) else emptyList())).distinct()

        val tasksPayload = project.tasks.map { t ->
            val taskId = if (t.id.isNotBlank()) t.id else java.util.UUID.randomUUID().toString()
            mapOf("id" to taskId, "title" to t.title, "isCompleted" to t.isCompleted)
        }

        val completedCount = project.tasks.count { it.isCompleted }
        val calcProgress = if (project.tasks.isNotEmpty()) ((completedCount * 100) / project.tasks.size) else 0

        val payload = mapOf(
            "id" to stableId,
            "ownerUid" to currentUid,
            "clientUid" to project.clientUid,
            "participantUids" to participants,
            "title" to project.title,
            "clientName" to project.clientName,
            "description" to project.description,
            "startDate" to project.startDate,
            "dueDate" to project.dueDate,
            "progress" to calcProgress,
            "status" to project.status.name,
            "tasks" to tasksPayload,
            "cardColorHexLight" to project.cardColorHexLight,
            "cardColorHexDark" to project.cardColorHexDark,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            val savedTasks = project.tasks.mapIndexed { idx, t ->
                val tid = (tasksPayload[idx]["id"] as? String) ?: t.id
                t.copy(id = tid)
            }
            val savedProject = project.copy(
                id = stableId,
                ownerUid = currentUid,
                participantUids = participants,
                progress = calcProgress,
                tasks = savedTasks
            )
            Result.success(savedProject)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error creating project in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProjectFromFirestore(projectId: String): Result<Unit> {
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User unauthenticated"))
        if (projectId.isBlank()) return Result.failure(IllegalArgumentException("Invalid project ID"))

        return try {
            val docRef = projectsRef.document(projectId)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                return Result.success(Unit)
            }
            val ownerUid = doc.getString("ownerUid") ?: ""
            @Suppress("UNCHECKED_CAST")
            val participants = (doc.get("participantUids") as? List<String>) ?: emptyList()
            if (currentUid != ownerUid && !participants.contains(currentUid)) {
                return Result.failure(IllegalStateException("Unauthorized to delete project $projectId"))
            }

            docRef.delete().await()
            Log.d("UserRepository", "Successfully deleted project $projectId from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error deleting project $projectId from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getProjectFromFirestore(projectId: String): com.example.model.Project? {
        if (projectId.isBlank()) return null
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid ?: return null
        return try {
            val doc = projectsRef.document(projectId).get().await()
            if (doc.exists()) {
                val ownerUid = doc.getString("ownerUid") ?: ""
                @Suppress("UNCHECKED_CAST")
                val participants = (doc.get("participantUids") as? List<String>) ?: emptyList()
                if (currentUid != ownerUid && !participants.contains(currentUid)) {
                    Log.w("UserRepository", "Unauthorized access attempt to project $projectId by $currentUid")
                    return null
                }
                parseProjectFromDoc(doc)
            } else null
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error getting project $projectId from Firestore: ${e.message}", e)
            null
        }
    }

    fun observeProjectFromFirestore(projectId: String): Flow<com.example.model.Project?> {
        if (projectId.isBlank()) return flowOf(null)
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid ?: return flowOf(null)
        return projectsRef.document(projectId).snapshots().map { doc ->
            if (doc.exists()) {
                val ownerUid = doc.getString("ownerUid") ?: ""
                @Suppress("UNCHECKED_CAST")
                val participants = (doc.get("participantUids") as? List<String>) ?: emptyList()
                if (currentUid == ownerUid || participants.contains(currentUid)) {
                    parseProjectFromDoc(doc)
                } else null
            } else null
        }.catch { e ->
            Log.e("UserRepository", "Error observing project $projectId: ${e.message}", e)
            emit(null)
        }
    }

    fun observeProjectsForUser(uid: String): Flow<List<com.example.model.Project>> {
        if (uid.isBlank()) return flowOf(emptyList())
        return projectsRef.whereArrayContains("participantUids", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { parseProjectFromDoc(it) }
            }
            .catch { e ->
                Log.e("UserRepository", "Error observing user projects for $uid: ${e.message}", e)
                emit(emptyList())
            }
    }

    suspend fun updateProjectTaskInFirestore(projectId: String, taskId: String, isCompleted: Boolean): Boolean {
        if (projectId.isBlank() || taskId.isBlank()) return false
        val docRef = projectsRef.document(projectId)
        return try {
            val doc = docRef.get().await()
            if (!doc.exists()) return false

            val currentProject = parseProjectFromDoc(doc) ?: return false
            val updatedTasks = currentProject.tasks.map { t ->
                if (t.id == taskId) t.copy(isCompleted = isCompleted) else t
            }
            val completedCount = updatedTasks.count { it.isCompleted }
            val newProgress = if (updatedTasks.isNotEmpty()) (completedCount * 100) / updatedTasks.size else 0

            val tasksPayload = updatedTasks.map { t ->
                mapOf("id" to t.id, "title" to t.title, "isCompleted" to t.isCompleted)
            }

            val updates = mapOf<String, Any>(
                "tasks" to tasksPayload,
                "progress" to newProgress,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error updating task $taskId in project $projectId: ${e.message}", e)
            false
        }
    }

    private fun parseProjectFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): com.example.model.Project? {
        val id = doc.getString("id") ?: doc.id
        val title = doc.getString("title") ?: return null
        val clientName = doc.getString("clientName") ?: ""
        val description = doc.getString("description") ?: ""
        val startDate = doc.getString("startDate") ?: ""
        val dueDate = doc.getString("dueDate") ?: ""
        val ownerUid = doc.getString("ownerUid") ?: ""
        val clientUid = doc.getString("clientUid") ?: ""
        @Suppress("UNCHECKED_CAST")
        val participantUids = (doc.get("participantUids") as? List<String>) ?: emptyList()
        val progress = (doc.getLong("progress") ?: 0L).toInt()
        val statusStr = doc.getString("status") ?: "IN_PROGRESS"
        val status = try { com.example.model.ProjectStatus.valueOf(statusStr) } catch (_: Exception) { com.example.model.ProjectStatus.IN_PROGRESS }

        @Suppress("UNCHECKED_CAST")
        val rawTasks = (doc.get("tasks") as? List<Map<String, Any>>) ?: emptyList()
        val tasks = rawTasks.map { tm ->
            com.example.model.ProjectTask(
                id = tm["id"] as? String ?: "",
                title = tm["title"] as? String ?: "",
                isCompleted = tm["isCompleted"] as? Boolean ?: false
            )
        }

        return com.example.model.Project(
            id = id,
            ownerUid = ownerUid,
            clientUid = clientUid,
            participantUids = participantUids,
            title = title,
            clientName = clientName,
            description = description,
            startDate = startDate,
            dueDate = dueDate,
            progress = progress,
            status = status,
            tasks = tasks
        )
    }

    private val meetingsRef = db.collection("meetings")

    suspend fun createMeetingInFirestore(meeting: com.example.model.Meeting): Result<com.example.model.Meeting> {
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User unauthenticated"))
        val docRef = if (meeting.id.isNotBlank()) meetingsRef.document(meeting.id) else meetingsRef.document()
        val stableId = docRef.id

        val participants = (listOf(currentUid) + meeting.participantUids).filter { it.isNotBlank() }.distinct()

        val payload = mutableMapOf<String, Any>(
            "id" to stableId,
            "title" to meeting.title,
            "description" to (meeting.notes ?: ""),
            "organizerUid" to (if (meeting.organizerUid.isNotBlank()) meeting.organizerUid else currentUid),
            "participantUids" to participants,
            "durationMinutes" to meeting.durationMinutes,
            "status" to meeting.status,
            "isOnline" to meeting.isOnline,
            "clientName" to meeting.clientName,
            "dateString" to meeting.dateString,
            "time" to meeting.time,
            "duration" to meeting.duration,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (meeting.scheduledAt != null) {
            payload["scheduledAt"] = meeting.scheduledAt
        }

        return try {
            docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            val savedMeeting = meeting.copy(
                id = stableId,
                organizerUid = if (meeting.organizerUid.isNotBlank()) meeting.organizerUid else currentUid,
                participantUids = participants
            )
            Result.success(savedMeeting)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error creating meeting in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMeetingFromFirestore(meetingId: String): Result<Unit> {
        val currentUid = com.google.firebase.Firebase.auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User unauthenticated"))
        if (meetingId.isBlank()) return Result.failure(IllegalArgumentException("Invalid meeting ID"))

        return try {
            val docRef = meetingsRef.document(meetingId)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                return Result.success(Unit)
            }
            val organizerUid = doc.getString("organizerUid") ?: ""
            if (currentUid != organizerUid) {
                return Result.failure(IllegalStateException("Unauthorized: Only the organizer can delete meeting $meetingId"))
            }

            docRef.delete().await()
            Log.d("UserRepository", "Successfully deleted meeting $meetingId from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error deleting meeting $meetingId from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun observeMeetingsForUser(uid: String): Flow<List<com.example.model.Meeting>> {
        if (uid.isBlank()) return flowOf(emptyList())
        return meetingsRef.whereArrayContains("participantUids", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { parseMeetingFromDoc(it) }
                    .distinctBy { it.id }
                    .sortedWith(compareBy<com.example.model.Meeting> { it.scheduledAt?.toDate()?.time ?: Long.MAX_VALUE })
            }
            .catch { e ->
                Log.e("UserRepository", "Error observing user meetings for $uid: ${e.message}", e)
                emit(emptyList())
            }
    }

    private fun parseMeetingFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): com.example.model.Meeting? {
        val id = doc.getString("id") ?: doc.id
        val title = doc.getString("title") ?: return null
        val description = doc.getString("description") ?: doc.getString("notes") ?: ""
        val organizerUid = doc.getString("organizerUid") ?: ""
        @Suppress("UNCHECKED_CAST")
        val participantUids = (doc.get("participantUids") as? List<String>) ?: emptyList()
        val durationMinutes = (doc.getLong("durationMinutes") ?: 30L).toInt()
        val status = doc.getString("status") ?: "Confirmed"
        val isOnline = doc.getBoolean("isOnline") ?: true
        val scheduledAt = doc.getTimestamp("scheduledAt")
        val createdAt = doc.getTimestamp("createdAt")
        val clientName = doc.getString("clientName") ?: ""
        val dateString = doc.getString("dateString") ?: ""
        val time = doc.getString("time") ?: ""
        val duration = doc.getString("duration") ?: "${durationMinutes} min"

        return com.example.model.Meeting(
            id = id,
            title = title,
            clientName = clientName,
            time = time,
            duration = duration,
            isOnline = isOnline,
            dateString = dateString,
            status = status,
            notes = if (description.isNotBlank()) description else null,
            organizerUid = organizerUid,
            participantUids = participantUids,
            scheduledAt = scheduledAt,
            durationMinutes = durationMinutes,
            createdAt = createdAt
        )
    }

    // ==========================================
    // Invoices: Firestore, RTDB & Room Cache
    // ==========================================

    suspend fun createInvoiceInFirestore(invoice: Invoice): Result<Invoice> {
        return try {
            val currentUid = Firebase.auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val invoiceId = invoice.id.ifBlank { "inv_${System.currentTimeMillis()}" }
            val resolvedClientUid = invoice.clientUid.ifBlank { "" }
            val participants = if (invoice.participantUids.isNotEmpty()) {
                invoice.participantUids
            } else {
                listOf(currentUid, resolvedClientUid).filter { it.isNotBlank() }
            }

            val invoiceData = mutableMapOf<String, Any>(
                "id" to invoiceId,
                "invoiceNumber" to invoice.invoiceNumber,
                "title" to invoice.title,
                "clientName" to invoice.clientName,
                "clientUid" to resolvedClientUid,
                "organizerUid" to currentUid,
                "participantUids" to participants,
                "amount" to invoice.amount,
                "currencyCode" to invoice.currencyCode,
                "currency" to invoice.currencyCode,
                "dueDate" to invoice.dueDate,
                "status" to "UNPAID",
                "isPaid" to false,
                "createdAt" to invoice.createdAt,
                "createdAtTs" to FieldValue.serverTimestamp()
            )
            if (!invoice.projectId.isNullOrBlank()) invoiceData["projectId"] = invoice.projectId
            if (!invoice.projectName.isNullOrBlank()) invoiceData["projectName"] = invoice.projectName
            if (!invoice.notes.isNullOrBlank()) invoiceData["notes"] = invoice.notes
            if (!invoice.paidAt.isNullOrBlank()) invoiceData["paidAt"] = invoice.paidAt

            // 1. Write to Firestore as Source of Truth
            invoicesRef.document(invoiceId).set(invoiceData).await()

            val finalInvoice = invoice.copy(
                id = invoiceId,
                clientUid = resolvedClientUid,
                organizerUid = currentUid,
                participantUids = participants,
                status = "UNPAID",
                isPaid = false
            )

            // 2. Cache in local Room database
            invoiceDao?.insertInvoice(
                LocalInvoice(
                    id = finalInvoice.id,
                    invoiceNumber = finalInvoice.invoiceNumber,
                    title = finalInvoice.title,
                    clientName = finalInvoice.clientName,
                    clientUid = finalInvoice.clientUid,
                    organizerUid = finalInvoice.organizerUid,
                    projectId = finalInvoice.projectId,
                    projectName = finalInvoice.projectName,
                    amount = finalInvoice.amount,
                    currencyCode = finalInvoice.currencyCode,
                    dueDate = finalInvoice.dueDate,
                    notes = finalInvoice.notes,
                    status = finalInvoice.status,
                    isPaid = finalInvoice.isPaid,
                    paidAt = finalInvoice.paidAt,
                    createdAt = finalInvoice.createdAt,
                    createdAtMillis = System.currentTimeMillis()
                )
            )

            Result.success(finalInvoice)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error creating invoice in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendInvoiceMessage(
        conversationId: String,
        senderName: String,
        invoice: Invoice
    ): Boolean {
        val senderUid = Firebase.auth.currentUser?.uid
        if (senderUid == null || conversationId.isBlank()) return false

        val resolvedPartnerUid = ConversationUtils.extractPartnerUid(senderUid, conversationId)
        // The caller may pass just the other person's id; chat rules only accept "<uidA>_<uidB>".
        val conversationId = ConversationUtils.getConversationId(senderUid, resolvedPartnerUid)
        if (conversationId.isBlank()) return false

        return try {
            val messagesRef = rtdb.getReference("conversations").child(conversationId).child("messages")
            val newMsgRef = messagesRef.push()
            val msgId = newMsgRef.key ?: "msg_${System.currentTimeMillis()}"
            val cleanText = "Invoice: ${invoice.title} (${invoice.formatAmount()})"

            val invoicePreviewMap = mutableMapOf<String, Any>(
                "id" to invoice.id,
                "invoiceNumber" to invoice.invoiceNumber,
                "title" to invoice.title,
                "clientName" to invoice.clientName,
                "clientUid" to invoice.clientUid,
                "organizerUid" to invoice.organizerUid,
                "participantUids" to invoice.participantUids,
                "amount" to invoice.amount,
                "currencyCode" to invoice.currencyCode,
                "currency" to invoice.currencyCode,
                "dueDate" to invoice.dueDate,
                "status" to invoice.status,
                "isPaid" to invoice.isPaid,
                "createdAt" to invoice.createdAt
            )
            if (!invoice.projectId.isNullOrBlank()) invoicePreviewMap["projectId"] = invoice.projectId
            if (!invoice.projectName.isNullOrBlank()) invoicePreviewMap["projectName"] = invoice.projectName
            if (!invoice.notes.isNullOrBlank()) invoicePreviewMap["notes"] = invoice.notes
            if (!invoice.paidAt.isNullOrBlank()) invoicePreviewMap["paidAt"] = invoice.paidAt

            val msgPayload = mapOf<String, Any>(
                "id" to msgId,
                "conversationId" to conversationId,
                "senderUid" to senderUid,
                "senderName" to senderName,
                "text" to "", // the card is the message; cleanText is only the inbox preview
                "type" to "invoice_share",
                "invoiceId" to invoice.id,
                "invoicePreview" to invoicePreviewMap,
                "createdAt" to ServerValue.TIMESTAMP
            )

            // Save outgoing message to local Room database
            messageDao?.insertMessage(
                LocalMessage(
                    id = msgId,
                    conversationId = conversationId,
                    senderUid = senderUid,
                    senderName = senderName,
                    text = "",
                    timestamp = System.currentTimeMillis(),
                    isOutgoing = true,
                    type = "invoice_share",
                    invoiceId = invoice.id
                )
            )

            val updates = mutableMapOf<String, Any>()
            if (!verifiedConversations.contains(conversationId)) {
                updates["conversations/$conversationId/participants/$senderUid"] = true
                updates["conversations/$conversationId/participants/$resolvedPartnerUid"] = true
            }

            updates["conversations/$conversationId/messages/$msgId"] = msgPayload
            updates["conversations/$conversationId/lastMessage"] = cleanText
            updates["conversations/$conversationId/lastMessageAt"] = ServerValue.TIMESTAMP
            updates["conversations/$conversationId/lastMessageSenderUid"] = senderUid

            putInboxEntries(updates, conversationId, senderUid, resolvedPartnerUid, cleanText)

            rtdb.reference.updateChildren(updates).await()
            verifiedConversations.add(conversationId)
            Log.d("SyncLineDiagnostics", "[RTDB_INVOICE_SEND_SUCCESS] msgId=$msgId in conv=$conversationId")
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("SyncLineDiagnostics", "[RTDB_INVOICE_SEND_FAIL] Exception: ${e.javaClass.simpleName}, Msg: ${e.message}", e)
            false
        }
    }

    suspend fun updateInvoicePaymentStatus(
        invoiceId: String,
        status: String = "PAID",
        paidAt: String? = null
    ): Result<Unit> {
        return try {
            val isPaid = (status == "PAID")
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "isPaid" to isPaid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (paidAt != null) {
                updates["paidAt"] = paidAt
            }

            // Write targeted update to Firestore
            invoicesRef.document(invoiceId).update(updates).await()

            // Update local Room database
            invoiceDao?.updateInvoicePaymentStatus(
                id = invoiceId,
                status = status,
                isPaid = isPaid,
                paidAt = paidAt
            )

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error updating invoice payment status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Edits the details of an invoice you issued. Everyone on the invoice sees the change live. */
    suspend fun updateInvoiceDetails(
        invoiceId: String,
        title: String,
        amount: Double,
        dueDate: String,
        notes: String?
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "amount" to amount,
                "dueDate" to dueDate,
                "notes" to (notes ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            invoicesRef.document(invoiceId).update(updates).await()
            invoiceDao?.updateInvoiceDetails(invoiceId, title, amount, dueDate, notes?.takeIf { it.isNotBlank() })
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error updating invoice $invoiceId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Edits a meeting you organise. Participants see the new time and title live. */
    suspend fun updateMeetingDetails(
        meetingId: String,
        title: String,
        notes: String?,
        scheduledAt: com.google.firebase.Timestamp,
        durationMinutes: Int,
        dateString: String,
        time: String,
        duration: String
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "title" to title,
                "description" to (notes ?: ""),
                "scheduledAt" to scheduledAt,
                "durationMinutes" to durationMinutes,
                "dateString" to dateString,
                "time" to time,
                "duration" to duration,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            meetingsRef.document(meetingId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error updating meeting $meetingId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Marks a meeting as cancelled but keeps it, so participants see that it was called off. */
    suspend fun cancelMeeting(meetingId: String): Result<Unit> {
        return try {
            meetingsRef.document(meetingId).update(
                mapOf("status" to "Cancelled", "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error cancelling meeting $meetingId: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getInvoiceById(invoiceId: String): Invoice? {
        if (invoiceId.isBlank()) return null
        // 1. Try local Room cache first
        val local = invoiceDao?.getInvoiceByIdSync(invoiceId)
        if (local != null) {
            return Invoice(
                id = local.id,
                invoiceNumber = local.invoiceNumber,
                title = local.title,
                clientName = local.clientName,
                clientUid = local.clientUid,
                organizerUid = local.organizerUid,
                projectId = local.projectId,
                projectName = local.projectName,
                amount = local.amount,
                currencyCode = local.currencyCode,
                currency = local.currencyCode,
                dueDate = local.dueDate,
                notes = local.notes,
                status = local.status,
                isPaid = local.isPaid,
                paidAt = local.paidAt,
                createdAt = local.createdAt
            )
        }

        // 2. Fallback to Firestore
        return try {
            val doc = invoicesRef.document(invoiceId).get().await()
            if (doc.exists()) {
                val inv = parseInvoiceFromDoc(doc)
                if (inv != null) {
                    invoiceDao?.insertInvoice(
                        LocalInvoice(
                            id = inv.id,
                            invoiceNumber = inv.invoiceNumber,
                            title = inv.title,
                            clientName = inv.clientName,
                            clientUid = inv.clientUid,
                            organizerUid = inv.organizerUid,
                            projectId = inv.projectId,
                            projectName = inv.projectName,
                            amount = inv.amount,
                            currencyCode = inv.currencyCode,
                            dueDate = inv.dueDate,
                            notes = inv.notes,
                            status = inv.status,
                            isPaid = inv.isPaid,
                            paidAt = inv.paidAt,
                            createdAt = inv.createdAt
                        )
                    )
                }
                inv
            } else null
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error getting invoice by id: ${e.message}")
            null
        }
    }

    fun observeInvoicesForClient(clientUid: String, clientName: String): Flow<List<Invoice>> {
        val roomFlow = invoiceDao?.getInvoicesForClient(clientUid, clientName)
        if (roomFlow != null) {
            return roomFlow.map { list ->
                list.map { local ->
                    Invoice(
                        id = local.id,
                        invoiceNumber = local.invoiceNumber,
                        title = local.title,
                        clientName = local.clientName,
                        clientUid = local.clientUid,
                        organizerUid = local.organizerUid,
                        projectId = local.projectId,
                        projectName = local.projectName,
                        amount = local.amount,
                        currencyCode = local.currencyCode,
                        currency = local.currencyCode,
                        dueDate = local.dueDate,
                        notes = local.notes,
                        status = local.status,
                        isPaid = local.isPaid,
                        paidAt = local.paidAt,
                        createdAt = local.createdAt,
                        createdAtTs = com.google.firebase.Timestamp(java.util.Date(local.createdAtMillis))
                    )
                }
            }
        }
        return flowOf(emptyList())
    }

    /** Live view of every invoice you issued or were billed for, straight from the shared database. */
    fun observeInvoicesRemote(uid: String): Flow<List<Invoice>> {
        if (uid.isBlank()) return flowOf(emptyList())
        return invoicesRef.whereArrayContains("participantUids", uid)
            .snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { parseInvoiceFromDoc(it) } }
            .catch { e ->
                Log.e("UserRepository", "Error observing invoices for $uid: ${e.message}", e)
                emit(emptyList())
            }
    }

    /** Writes invoices received from the shared database into the local cache the UI reads from. */
    suspend fun cacheInvoices(invoices: List<Invoice>) {
        val dao = invoiceDao ?: return
        if (invoices.isEmpty()) return
        try {
            dao.insertInvoices(invoices.map { inv ->
                val created = inv.createdAtTs?.toDate()?.time
                    ?: dao.getInvoiceByIdSync(inv.id)?.createdAtMillis
                    ?: System.currentTimeMillis()
                LocalInvoice(
                    id = inv.id,
                    invoiceNumber = inv.invoiceNumber,
                    title = inv.title,
                    clientName = inv.clientName,
                    clientUid = inv.clientUid,
                    organizerUid = inv.organizerUid,
                    projectId = inv.projectId,
                    projectName = inv.projectName,
                    amount = inv.amount,
                    currencyCode = inv.currencyCode,
                    dueDate = inv.dueDate,
                    notes = inv.notes,
                    status = inv.status,
                    isPaid = inv.isPaid,
                    paidAt = inv.paidAt,
                    createdAt = inv.createdAt,
                    createdAtMillis = created
                )
            })
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error caching invoices: ${e.message}")
        }
    }

    /** Forget cached invoices when the account changes so one account never sees another's. */
    suspend fun clearLocalInvoices() {
        try { invoiceDao?.clearAll() } catch (_: Exception) {}
    }

    fun observeAllInvoices(): Flow<List<Invoice>> {
        val roomFlow = invoiceDao?.getAllInvoices()
        if (roomFlow != null) {
            return roomFlow.map { list ->
                list.map { local ->
                    Invoice(
                        id = local.id,
                        invoiceNumber = local.invoiceNumber,
                        title = local.title,
                        clientName = local.clientName,
                        clientUid = local.clientUid,
                        organizerUid = local.organizerUid,
                        projectId = local.projectId,
                        projectName = local.projectName,
                        amount = local.amount,
                        currencyCode = local.currencyCode,
                        currency = local.currencyCode,
                        dueDate = local.dueDate,
                        notes = local.notes,
                        status = local.status,
                        isPaid = local.isPaid,
                        paidAt = local.paidAt,
                        createdAt = local.createdAt,
                        createdAtTs = com.google.firebase.Timestamp(java.util.Date(local.createdAtMillis))
                    )
                }
            }
        }
        return flowOf(emptyList())
    }

    private fun parseInvoiceFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Invoice? {
        val id = doc.getString("id") ?: doc.id
        val title = doc.getString("title") ?: return null
        val invoiceNumber = doc.getString("invoiceNumber") ?: ""
        val clientName = doc.getString("clientName") ?: ""
        val clientUid = doc.getString("clientUid") ?: ""
        val organizerUid = doc.getString("organizerUid") ?: ""
        @Suppress("UNCHECKED_CAST")
        val participantUids = (doc.get("participantUids") as? List<String>) ?: emptyList()
        val projectId = doc.getString("projectId")
        val projectName = doc.getString("projectName")
        val amount = doc.getDouble("amount") ?: (doc.getLong("amount")?.toDouble() ?: 0.0)
        val currencyCode = doc.getString("currencyCode") ?: "INR"
        val dueDate = doc.getString("dueDate") ?: ""
        val notes = doc.getString("notes")
        val status = doc.getString("status") ?: "UNPAID"
        val isPaid = doc.getBoolean("isPaid") ?: (status == "PAID")
        val paidAt = doc.getString("paidAt")
        val createdAt = doc.getString("createdAt") ?: "Today"

        return Invoice(
            id = id,
            invoiceNumber = invoiceNumber,
            title = title,
            clientName = clientName,
            clientUid = clientUid,
            organizerUid = organizerUid,
            participantUids = participantUids,
            projectId = projectId,
            projectName = projectName,
            amount = amount,
            currencyCode = currencyCode,
            currency = currencyCode,
            dueDate = dueDate,
            notes = notes,
            status = status,
            isPaid = isPaid,
            paidAt = paidAt,
            createdAt = createdAt,
            createdAtTs = doc.getTimestamp("createdAtTs")
        )
    }
}
