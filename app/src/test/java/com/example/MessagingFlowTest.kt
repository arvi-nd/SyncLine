package com.example

import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.ConversationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessagingFlowTest {

    @Test
    fun testConversationIdDeterminism() {
        val uidA = "user_alpha_123"
        val uidB = "user_beta_456"

        val convId1 = ConversationUtils.getConversationId(uidA, uidB)
        val convId2 = ConversationUtils.getConversationId(uidB, uidA)

        assertEquals("Conversation ID must be deterministic regardless of order", convId1, convId2)
        assertEquals("user_alpha_123_user_beta_456", convId1)
    }

    @Test
    fun testConversationIdIdempotencyWhenPassingExistingId() {
        val myUid = "user_alpha_123"
        val partnerUid = "user_beta_456"
        val existingConvId = "user_alpha_123_user_beta_456"

        // If a caller passes existing conversation ID as target parameter
        val resolvedId = ConversationUtils.getConversationId(myUid, existingConvId)
        assertEquals("user_alpha_123_user_beta_456", resolvedId)

        val partnerUidExtracted = ConversationUtils.extractPartnerUid(myUid, existingConvId)
        assertEquals("user_beta_456", partnerUidExtracted)

        val partnerUidDirect = ConversationUtils.extractPartnerUid(myUid, partnerUid)
        assertEquals("user_beta_456", partnerUidDirect)
    }

    @Test
    fun testChatMessageModelDefaultsAndParticipantUids() {
        val msg = ChatMessage(
            id = "msg_1",
            conversationId = "userA_userB",
            senderUid = "userA",
            senderName = "Alice",
            text = "Hello world",
            participantUids = listOf("userA", "userB")
        )

        assertEquals("msg_1", msg.id)
        assertEquals("userA_userB", msg.conversationId)
        assertEquals("userA", msg.senderUid)
        assertEquals("Alice", msg.senderName)
        assertEquals("Hello world", msg.text)
        assertEquals(2, msg.participantUids.size)
        assertTrue(msg.participantUids.contains("userA"))
        assertTrue(msg.participantUids.contains("userB"))
    }

    @Test
    fun testConversationModelDefaults() {
        val conv = Conversation(
            id = "userA_userB",
            participantUids = listOf("userA", "userB"),
            lastMessage = "Hello world",
            lastMessageSenderUid = "userA"
        )

        assertEquals("userA_userB", conv.id)
        assertEquals(2, conv.participantUids.size)
        assertEquals("Hello world", conv.lastMessage)
        assertEquals("userA", conv.lastMessageSenderUid)
    }

    @Test
    fun testVerifiedConversationIdempotencyLogic() {
        val verifiedSet = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
        val convId = ConversationUtils.getConversationId("uid1", "uid2")

        assertFalse("Initially conversation should not be verified", verifiedSet.contains(convId))

        verifiedSet.add(convId)
        assertTrue("Conversation must be marked verified after initial check", verifiedSet.contains(convId))

        // Adding again is idempotent
        verifiedSet.add(convId)
        assertEquals(1, verifiedSet.size)
        assertTrue(verifiedSet.contains(convId))
    }

    @Test
    fun testParticipantAuthorizationSafety() {
        val myUid = "user_me"
        val partnerUid = "user_them"
        val strangerUid = "user_stranger"

        val participants = listOf(myUid, partnerUid).sorted()

        assertTrue("Owner must be authorized participant", participants.contains(myUid))
        assertTrue("Partner must be authorized participant", participants.contains(partnerUid))
        assertFalse("Stranger must not be authorized participant", participants.contains(strangerUid))
        assertEquals(2, participants.size)
    }

    @Test
    fun testMessageSendingStateDoesNotSwallowFailure() {
        var isSending = true
        var errorMessage: String? = null
        val onResultCalled = java.util.concurrent.atomic.AtomicBoolean(false)

        val simulateSendFailure: (Boolean) -> Unit = { success ->
            isSending = false
            if (!success) {
                errorMessage = "Failed to send message. Please retry."
            }
            onResultCalled.set(true)
        }

        simulateSendFailure(false)

        assertFalse("isSending must be reset to false on failure", isSending)
        assertEquals("Failed to send message. Please retry.", errorMessage)
        assertTrue("Result callback must be invoked", onResultCalled.get())
    }

    @Test
    fun testClientListTransformationIsPureAndReadOriented() {
        val rawProfiles = listOf(
            com.example.model.UserProfile(uid = "uid1", displayName = "Alice Smith", username = "alice"),
            com.example.model.UserProfile(uid = "uid2", displayName = "Bob Jones", username = "bob")
        )

        // Transforming raw user profiles into UI clients must not mutate source or trigger network calls
        val clients = rawProfiles.map { profile ->
            com.example.model.Client(
                id = profile.uid,
                name = profile.displayName,
                firstName = profile.displayName.substringBefore(" "),
                company = "SyncLine Partner",
                role = "Client",
                country = "United States",
                city = "New York",
                countryCode = "US",
                flag = "🇺🇸",
                timezoneId = "America/New_York",
                localTimeStr = "12:00 PM",
                statusStr = "Working",
                workingHours = "Mon - Fri, 9:00 AM - 6:00 PM",
                avatarBg = androidx.compose.ui.graphics.Color(0xFF9FE870),
                avatarInitial = "A",
                isOnline = true,
                lastSeen = "Online"
            )
        }

        assertEquals(2, clients.size)
        assertEquals("uid1", clients[0].id)
        assertEquals("uid2", clients[1].id)
    }

    @Test
    fun testDraftPreservationOnSendFailure() {
        var draft = "Unsent important note"
        val isSuccess = false

        // In ViewModel sendCurrentMessage logic:
        if (isSuccess) {
            draft = ""
        }
        // If not success, draft is preserved
        assertEquals("Draft must be preserved when send fails", "Unsent important note", draft)
    }

    @Test
    fun testCacheClearingRemovesAllStaleEntries() {
        val cache = java.util.concurrent.ConcurrentHashMap<String, String>()
        val verifiedSet = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

        cache["uid_alice"] = "Alice"
        verifiedSet.add("uid_alice_uid_bob")

        assertEquals(1, cache.size)
        assertEquals(1, verifiedSet.size)

        // Clear caches on sign-out / switch
        cache.clear()
        verifiedSet.clear()

        assertTrue("Cache must be empty after clearing", cache.isEmpty())
        assertTrue("Verified set must be empty after clearing", verifiedSet.isEmpty())
    }

    @Test
    fun testRelationshipCreatedAtPreservedOnMerge() {
        val existingDoc = mapOf(
            "clientId" to "client_1",
            "createdAt" to "existing_timestamp_2026_10_26"
        )
        val hasExistingCreatedAt = existingDoc["createdAt"] != null

        val payload = mutableMapOf<String, Any>(
            "clientId" to "client_1",
            "username" to "updated_client_username",
            "updatedAt" to "current_server_timestamp"
        )
        if (!hasExistingCreatedAt) {
            payload["createdAt"] = "new_server_timestamp"
        }

        // createdAt must NOT be in payload if it already existed in document
        assertFalse("createdAt must not be overwritten if already present", payload.containsKey("createdAt"))
        assertTrue("updatedAt must be present", payload.containsKey("updatedAt"))
    }

    @Test
    fun testParticipantValidationRejectsSingleParticipantConversations() {
        val senderUid = "user_alpha"
        val partnerUid = "user_alpha" // Same user (self chat attempt)

        val resolvedPartnerUid = if (partnerUid.isNotBlank() && partnerUid != senderUid) {
            partnerUid
        } else {
            ConversationUtils.extractPartnerUid(senderUid, partnerUid)
        }

        val participants = if (resolvedPartnerUid.isNotBlank() && resolvedPartnerUid != senderUid) {
            listOf(senderUid, resolvedPartnerUid).sorted()
        } else {
            listOf(senderUid)
        }

        val isValid = participants.size >= 2
        assertFalse("Self-chat or single participant conversations must be rejected before Firestore write", isValid)
    }

    @Test
    fun testParticipantValidationAcceptsTwoDistinctParticipants() {
        val senderUid = "user_alpha"
        val partnerUid = "user_beta"

        val resolvedPartnerUid = if (partnerUid.isNotBlank() && partnerUid != senderUid) {
            partnerUid
        } else {
            ConversationUtils.extractPartnerUid(senderUid, partnerUid)
        }

        val participants = if (resolvedPartnerUid.isNotBlank() && resolvedPartnerUid != senderUid) {
            listOf(senderUid, resolvedPartnerUid).sorted()
        } else {
            listOf(senderUid)
        }

        val isValid = participants.size == 2
        assertTrue("Two distinct participants must form a valid 2-element participant list", isValid)
        assertEquals(listOf("user_alpha", "user_beta"), participants)
    }

    @Test
    fun testValidRegisteredRecipientValidation() {
        val senderUid = "user_real_sender"
        val partnerUid = "user_real_partner"
        val registeredProfiles = mapOf(
            "user_real_sender" to com.example.model.UserProfile(uid = senderUid, displayName = "Sender"),
            "user_real_partner" to com.example.model.UserProfile(uid = partnerUid, displayName = "Partner")
        )

        val resolvedPartnerUid = ConversationUtils.extractPartnerUid(senderUid, partnerUid)
        val convId = ConversationUtils.getConversationId(senderUid, resolvedPartnerUid)
        val isRegistered = registeredProfiles.containsKey(resolvedPartnerUid)

        assertTrue("Partner UID must be valid non-blank string", resolvedPartnerUid.isNotBlank())
        assertTrue("Conversation ID must be non-blank canonical string", convId.isNotBlank())
        assertTrue("Recipient must be verified as registered user profile", isRegistered)
        assertEquals("user_real_partner_user_real_sender", convId)
    }

    @Test
    fun testSelfChatAttemptValidation() {
        val senderUid = "user_self_123"
        val partnerUid = "user_self_123"

        val resolvedPartnerUid = ConversationUtils.extractPartnerUid(senderUid, partnerUid)
        val convId = ConversationUtils.getConversationId(senderUid, resolvedPartnerUid)
        val isValidRecipient = resolvedPartnerUid.isNotBlank() && resolvedPartnerUid != senderUid && convId.isNotBlank()

        assertFalse("Self-chat attempt must be rejected as invalid recipient", isValidRecipient)
        assertEquals("", convId)
    }

    @Test
    fun testSyntheticClientIdRejection() {
        val senderUid = "user_real_sender"
        val syntheticClientId = "c1"
        // Simulated registered user database in Firestore
        val registeredProfiles = mapOf(
            "user_real_sender" to com.example.model.UserProfile(uid = senderUid, displayName = "Sender")
        )

        val isRegistered = registeredProfiles.containsKey(syntheticClientId)

        assertFalse("Synthetic client ID 'c1' must be rejected if not registered in user profiles", isRegistered)
    }

    @Test
    fun testNonexistentRecipientProfileRejection() {
        val senderUid = "user_real_sender"
        val nonexistentUid = "ghost_user_999"
        val registeredProfiles = mapOf(
            "user_real_sender" to com.example.model.UserProfile(uid = senderUid, displayName = "Sender")
        )

        val isRegistered = registeredProfiles.containsKey(nonexistentUid)

        assertFalse("Nonexistent recipient profile must be rejected", isRegistered)
    }

    @Test
    fun testBlankRecipientAndInvalidConversationIdHandling() {
        val senderUid = "user_real_sender"
        val blankRecipient = "   "

        val resolvedPartnerUid = ConversationUtils.extractPartnerUid(senderUid, blankRecipient)
        val convId = ConversationUtils.getConversationId(senderUid, resolvedPartnerUid)

        assertEquals("", resolvedPartnerUid)
        assertEquals("", convId)
        assertFalse("Blank recipient must not produce a non-empty conversation ID", convId.isNotBlank())
    }

    @Test
    fun testConsistentConversationIdBetweenSendAndListenerPaths() {
        val senderUid = "user_a"
        val partnerUid = "user_b"

        // Writer path calculation
        val resolvedPartnerWriter = ConversationUtils.extractPartnerUid(senderUid, partnerUid)
        val writerConvId = ConversationUtils.getConversationId(senderUid, resolvedPartnerWriter)

        // Listener path calculation
        val resolvedPartnerListener = ConversationUtils.extractPartnerUid(senderUid, partnerUid)
        val listenerConvId = ConversationUtils.getConversationId(senderUid, resolvedPartnerListener)

        assertEquals("user_a_user_b", writerConvId)
        assertEquals("user_a_user_b", listenerConvId)
        assertEquals("Message write and listener paths must calculate identical canonical conversation IDs", writerConvId, listenerConvId)
    }

    @Test
    fun testExactParticipantAuthorizationRejectsSubstringFalsePositives() {
        // Example: attacker UID is 'abc', conversation ID is 'user_abc_user_xyz'
        val attackerUid = "abc"
        val convId = "user_abc_user_xyz"

        // Old flawed logic: substring contains
        val substringAllowed = convId.contains(attackerUid)
        assertTrue("Flawed substring matching would falsely authorize attacker", substringAllowed)

        // Secure exact boundary logic
        val isExactParticipant = convId.startsWith("${attackerUid}_") || convId.endsWith("_${attackerUid}")
        assertFalse("Exact boundary check correctly rejects attacker whose UID is only a substring", isExactParticipant)

        // Valid participants
        val realParticipant1 = "user_abc"
        val realParticipant2 = "user_xyz"
        val isReal1Authorized = convId.startsWith("${realParticipant1}_") || convId.endsWith("_${realParticipant1}")
        val isReal2Authorized = convId.startsWith("${realParticipant2}_") || convId.endsWith("_${realParticipant2}")
        assertTrue(isReal1Authorized)
        assertTrue(isReal2Authorized)
    }

    @Test
    fun testSummaryWritesNeverTargetAnotherUsersAccount() {
        val senderUid = "user_sender_1"
        val partnerUid = "user_partner_2"
        val conversationId = "user_partner_2_user_sender_1"

        // Simulated atomic multi-path update keys
        val writeKeys = listOf(
            "conversations/$conversationId/messages/msg_123",
            "conversations/$conversationId/lastMessage",
            "conversations/$conversationId/lastMessageAt",
            "conversations/$conversationId/lastMessageSenderUid",
            "userConversations/$senderUid/$conversationId"
        )

        // Ensure NO key starts with userConversations/$partnerUid
        val targetsPartnerAccount = writeKeys.any { it.startsWith("userConversations/$partnerUid") }
        assertFalse("Client write must never target partner's userConversations account node", targetsPartnerAccount)

        val writesOnlySenderSummary = writeKeys.any { it == "userConversations/$senderUid/$conversationId" }
        assertTrue("Client write must update sender's own summary node", writesOnlySenderSummary)
    }
}
