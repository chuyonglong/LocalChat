package com.localchat.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.Index
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations")
data class ConversationEntity(@PrimaryKey val id: String, val title: String, val updatedAt: Long)

@Entity(
    tableName = "messages",
    indices = [Index("conversationId")],
    foreignKeys = [ForeignKey(entity = ConversationEntity::class, parentColumns = ["id"], childColumns = ["conversationId"], onDelete = ForeignKey.CASCADE)],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val imageUri: String?,
    val createdAt: Long,
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC") fun observeConversations(): Flow<List<ConversationEntity>>
    @Query("""
        SELECT * FROM conversations
        WHERE :query = ''
            OR title LIKE '%' || :query || '%'
            OR EXISTS (
                SELECT 1 FROM messages
                WHERE messages.conversationId = conversations.id
                    AND messages.content LIKE '%' || :query || '%'
            )
        ORDER BY updatedAt DESC
    """)
    fun searchConversations(query: String): Flow<List<ConversationEntity>>
    @Query("SELECT * FROM messages WHERE conversationId = :id ORDER BY createdAt") fun observeMessages(id: String): Flow<List<MessageEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveConversation(item: ConversationEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveMessage(item: MessageEntity)
    @Query("DELETE FROM conversations WHERE id = :id") suspend fun deleteConversation(id: String)
}

@Database(entities = [ConversationEntity::class, MessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() { abstract fun chatDao(): ChatDao }
