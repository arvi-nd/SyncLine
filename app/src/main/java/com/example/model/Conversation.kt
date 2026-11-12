package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Conversation(
    val id: String = "",
    val participantUids: List<String> = emptyList(),
    val lastMessage: String = "",
    @ServerTimestamp val lastMessageAt: Timestamp? = null,
    val lastMessageSenderUid: String = "",
    val unreadCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
