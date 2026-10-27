package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ChatRequest(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderUsername: String = "",
    val senderDisplayName: String = "",
    val senderEmail: String = "",
    val targetUid: String = "",
    val targetUsername: String = "",
    val targetDisplayName: String = "",
    val targetEmail: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
