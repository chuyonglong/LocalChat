package com.localchat.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.activity.ComponentActivity
import com.localchat.app.data.local.MessageEntity
import org.junit.Rule
import org.junit.Test

class MessageListAutoScrollTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun latestMessageIsVisibleWhenHistoryIsDisplayed() {
        val messages = (0..30).map { index ->
            MessageEntity(
                id = index.toString(),
                conversationId = "conversation",
                role = "ASSISTANT",
                content = "message-$index",
                imageUri = null,
                createdAt = index.toLong(),
            )
        }

        composeRule.setContent {
            MaterialTheme {
                MessageList(messages, Modifier.fillMaxSize())
            }
        }

        composeRule.onNodeWithText("message-30").assertIsDisplayed()
    }
}
