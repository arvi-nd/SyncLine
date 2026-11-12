package com.example.model

object ConversationUtils {
    /**
     * Derives a deterministic shared conversation ID for a pair of distinct UIDs.
     * Guarantees that getConversationId(uidA, uidB) == getConversationId(uidB, uidA).
     * If one of the parameters is already a conversation ID containing the other UID,
     * normalizes and returns the valid conversation ID without duplication.
     */
    fun getConversationId(uid1: String, uid2: String): String {
        val clean1 = uid1.trim()
        val clean2 = uid2.trim()
        if (clean1.isBlank() || clean2.isBlank() || clean1 == clean2) return ""

        // If clean2 is already a conversation ID containing clean1 as a participant
        if (clean2.startsWith("${clean1}_")) {
            val other = clean2.removePrefix("${clean1}_")
            return if (clean1 < other) "${clean1}_${other}" else "${other}_${clean1}"
        } else if (clean2.endsWith("_${clean1}")) {
            val other = clean2.removeSuffix("_${clean1}")
            return if (clean1 < other) "${clean1}_${other}" else "${other}_${clean1}"
        }

        // If clean1 is already a conversation ID containing clean2 as a participant
        if (clean1.startsWith("${clean2}_")) {
            val other = clean1.removePrefix("${clean2}_")
            return if (clean2 < other) "${clean2}_${other}" else "${other}_${clean2}"
        } else if (clean1.endsWith("_${clean2}")) {
            val other = clean1.removeSuffix("_${clean2}")
            return if (clean2 < other) "${clean2}_${other}" else "${other}_${clean2}"
        }

        return if (clean1 < clean2) "${clean1}_${clean2}" else "${clean2}_${clean1}"
    }

    /**
     * Extracts partner UID when given an input that might be either a conversation ID (uid1_uid2)
     * or a direct partner UID.
     */
    fun extractPartnerUid(myUid: String, conversationIdOrPartnerUid: String): String {
        val clean = conversationIdOrPartnerUid.trim()
        return when {
            clean.startsWith("${myUid}_") -> clean.removePrefix("${myUid}_")
            clean.endsWith("_${myUid}") -> clean.removeSuffix("_${myUid}")
            else -> clean
        }
    }
}

