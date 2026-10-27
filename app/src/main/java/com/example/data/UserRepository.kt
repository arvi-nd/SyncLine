package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.UserProfile
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.example.model.ChatRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val usersRef = db.collection("users")

    suspend fun getProfile(uid: String): UserProfile? {
        if (uid.isBlank()) return null
        return try {
            val doc = usersRef.document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(UserProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Error getting user profile for $uid: ${e.message}", e)
            null
        }
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        val clean = username.trim().lowercase()
        if (clean.length < 3) return false
        return try {
            val query = usersRef.whereEqualTo("username", clean).limit(1).get().await()
            query.isEmpty
        } catch (e: Exception) {
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
                val updates = mutableMapOf<String, Any>()
                if (existing.email.isBlank() && email.isNotBlank()) {
                    updates["email"] = email
                }
                if (existing.username.isBlank() && username.isNotBlank()) {
                    updates["username"] = username.trim().lowercase()
                }
                if (updates.isNotEmpty()) {
                    updates["updatedAt"] = FieldValue.serverTimestamp()
                    usersRef.document(uid).update(updates).await()
                    existing.copy(
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

                val cleanUsername = username.trim().lowercase().ifBlank {
                    email.substringBefore("@").lowercase().replace(Regex("[^a-z0-9_.]"), "")
                }

                val payload = mutableMapOf<String, Any?>(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to name,
                    "username" to cleanUsername,
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
                            "email" to email,
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
                val query = usersRef.whereEqualTo("username", clean).limit(1).get().await()
                if (!query.isEmpty) {
                    query.documents.first().toObject(UserProfile::class.java)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user by username '$clean': ${e.message}", e)
            null
        }
    }

    suspend fun addClientRelationship(myUid: String, clientProfile: UserProfile): Boolean {
        if (myUid.isBlank() || clientProfile.uid.isBlank()) return false
        if (myUid == clientProfile.uid) return false
        return try {
            val clientDocRef = usersRef.document(myUid).collection("clients").document(clientProfile.uid)
            val payload = mapOf(
                "clientId" to clientProfile.uid,
                "uid" to clientProfile.uid,
                "username" to clientProfile.username,
                "displayName" to clientProfile.displayName,
                "email" to clientProfile.email,
                "createdAt" to FieldValue.serverTimestamp()
            )
            clientDocRef.set(payload).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding client relationship: ${e.message}", e)
            false
        }
    }

    fun observeUserClients(myUid: String): Flow<List<UserProfile>> {
        val clientsRef = usersRef.document(myUid).collection("clients")
        return clientsRef.snapshots().map { snapshot ->
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
    }

    fun observeProfile(uid: String): Flow<UserProfile?> {
        val docRef = usersRef.document(uid)
        return docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
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
        val requestRef = db.collection("chat_requests").document(requestId)

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
            null // null means success (no error string)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sending chat request: ${e.message}", e)
            e.localizedMessage ?: "Failed to send chat request."
        }
    }

    suspend fun acceptChatRequest(request: ChatRequest): Boolean {
        if (request.id.isBlank()) return false
        val requestRef = db.collection("chat_requests").document(request.id)
        return try {
            requestRef.update(
                mapOf(
                    "status" to "ACCEPTED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Establish bilateral client connection for both users
            val sender = UserProfile(
                uid = request.senderUid,
                username = request.senderUsername,
                displayName = request.senderDisplayName,
                email = request.senderEmail
            )
            val target = UserProfile(
                uid = request.targetUid,
                username = request.targetUsername,
                displayName = request.targetDisplayName,
                email = request.targetEmail
            )

            addClientRelationship(request.senderUid, target)
            addClientRelationship(request.targetUid, sender)
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error accepting chat request ${request.id}: ${e.message}", e)
            false
        }
    }

    suspend fun declineChatRequest(requestId: String): Boolean {
        if (requestId.isBlank()) return false
        val requestRef = db.collection("chat_requests").document(requestId)
        return try {
            requestRef.update(
                mapOf(
                    "status" to "DECLINED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error declining chat request $requestId: ${e.message}", e)
            false
        }
    }

    fun observeChatRequests(myUid: String): Flow<List<ChatRequest>> {
        if (myUid.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())

        val incomingFlow = db.collection("chat_requests")
            .whereEqualTo("targetUid", myUid)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(ChatRequest::class.java) }
            .catch { emit(emptyList()) }

        val outgoingFlow = db.collection("chat_requests")
            .whereEqualTo("senderUid", myUid)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(ChatRequest::class.java) }
            .catch { emit(emptyList()) }

        return combine(incomingFlow, outgoingFlow) { incoming, outgoing ->
            (incoming + outgoing).distinctBy { it.id }.sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
    }
}
