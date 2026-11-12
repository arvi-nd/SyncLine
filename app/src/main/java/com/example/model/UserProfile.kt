package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val username: String = "",
    val timezone: String = "",      // IANA id, e.g. "Asia/Kolkata"
    val countryCode: String = "",   // ISO 3166-1 alpha-2, e.g. "IN"
    val workingStartHour: Int = 9,
    val workingEndHour: Int = 18,
    val availability: String = "Available", // Available, Busy, Do not disturb
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
