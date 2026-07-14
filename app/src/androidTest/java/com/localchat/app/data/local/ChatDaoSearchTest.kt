package com.localchat.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatDaoSearchTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ChatDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.chatDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun searchConversations_matchesTitlesAndMessagesOnceInUpdatedOrder() = runBlocking {
        dao.saveConversation(ConversationEntity("older", "旅行计划", 100L))
        dao.saveConversation(ConversationEntity("newer", "普通标题", 200L))
        dao.saveMessage(MessageEntity("one", "older", "USER", "包含咖啡", null, 1L))
        dao.saveMessage(MessageEntity("two", "older", "ASSISTANT", "咖啡推荐", null, 2L))
        dao.saveMessage(MessageEntity("three", "newer", "USER", "咖啡店", null, 3L))

        assertEquals(listOf("newer", "older"), dao.searchConversations("咖啡").first().map { it.id })
        assertEquals(listOf("older"), dao.searchConversations("旅行").first().map { it.id })
    }

    @Test
    fun searchConversations_returnsNoMatchesForUnknownText() = runBlocking {
        dao.saveConversation(ConversationEntity("id", "旅行计划", 100L))

        assertEquals(emptyList<ConversationEntity>(), dao.searchConversations("不存在").first())
    }
}
