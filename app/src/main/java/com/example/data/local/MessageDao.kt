package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: LocalMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<LocalMessage>)

    @Query("SELECT * FROM local_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<LocalMessage>>

    @Query("SELECT * FROM local_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<LocalMessage>>

    @Query("DELETE FROM local_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM local_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: String)
}
