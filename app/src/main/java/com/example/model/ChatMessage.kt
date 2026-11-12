package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val participantUids: List<String> = emptyList(),
    val type: String = "text",
    val invoiceId: String? = null,
    val invoicePreview: Invoice? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

