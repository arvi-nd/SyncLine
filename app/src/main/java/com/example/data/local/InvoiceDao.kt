package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: LocalInvoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<LocalInvoice>)

    @Query("SELECT * FROM local_invoices WHERE id = :id LIMIT 1")
    fun getInvoiceById(id: String): Flow<LocalInvoice?>

    @Query("SELECT * FROM local_invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceByIdSync(id: String): LocalInvoice?

    @Query("SELECT * FROM local_invoices WHERE clientUid = :clientUid OR clientName = :clientName ORDER BY createdAtMillis DESC")
    fun getInvoicesForClient(clientUid: String, clientName: String): Flow<List<LocalInvoice>>

    @Query("SELECT * FROM local_invoices ORDER BY createdAtMillis DESC")
    fun getAllInvoices(): Flow<List<LocalInvoice>>

    @Query("UPDATE local_invoices SET status = :status, isPaid = :isPaid, paidAt = :paidAt WHERE id = :id")
    suspend fun updateInvoicePaymentStatus(id: String, status: String, isPaid: Boolean, paidAt: String?)

    @Query("DELETE FROM local_invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)

    @Query("DELETE FROM local_invoices")
    suspend fun clearAll()
}
