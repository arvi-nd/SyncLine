package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_invoices")
data class LocalInvoice(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val title: String,
    val clientName: String,
    val clientUid: String,
    val organizerUid: String,
    val projectId: String? = null,
    val projectName: String? = null,
    val amount: Double,
    val currencyCode: String = "INR",
    val dueDate: String,
    val notes: String? = null,
    val status: String = "UNPAID",
    val isPaid: Boolean = false,
    val paidAt: String? = null,
    val createdAt: String = "Today",
    val createdAtMillis: Long = System.currentTimeMillis()
)
