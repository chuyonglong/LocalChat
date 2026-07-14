package com.localchat.app

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import com.localchat.app.data.local.ConversationEntity
import com.localchat.app.domain.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DrawerContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchMode_filtersHistoryAndBackClearsTheQuery() {
        val conversations = listOf(
            ConversationEntity("coffee", "Coffee chat", 2L),
            ConversationEntity("travel", "Travel chat", 1L),
        )

        composeRule.setContent {
            var query by mutableStateOf("")
            MaterialTheme {
                DrawerContent(
                    conversations = conversations.filter { it.title.contains(query, ignoreCase = true) },
                    onConversation = {},
                    onDeleteConversation = {},
                    onNewChat = {},
                    themeMode = ThemeMode.SYSTEM,
                    onThemeMode = {},
                    onSettings = {},
                    searchQuery = query,
                    onSearchQuery = { query = it },
                )
            }
        }

        composeRule.onNodeWithTag("search-action").performClick()
        composeRule.onNodeWithTag("search-query").performTextInput("Coffee")
        composeRule.onNodeWithTag("conversation-coffee").assertIsDisplayed()
        composeRule.onAllNodesWithTag("conversation-travel").assertCountEquals(0)
        composeRule.onNodeWithTag("search-back").performClick()
        composeRule.onNodeWithTag("conversation-travel").assertIsDisplayed()
    }

    @Test
    fun longPress_requiresConfirmationBeforeDeletingConversation() {
        var selectedId: String? = null
        var deletedId: String? = null

        composeRule.setContent {
            MaterialTheme {
                DrawerContent(
                    conversations = listOf(ConversationEntity("one", "One", 1L)),
                    onConversation = { selectedId = it },
                    onDeleteConversation = { deletedId = it },
                    onNewChat = {},
                    themeMode = ThemeMode.SYSTEM,
                    onThemeMode = {},
                    onSettings = {},
                    searchQuery = "",
                    onSearchQuery = {},
                )
            }
        }

        composeRule.onNodeWithTag("conversation-one").performTouchInput { longClick() }
        composeRule.onNodeWithTag("delete-confirmation").assertIsDisplayed()
        assertEquals(null, selectedId)
        composeRule.onNodeWithTag("delete-cancel").performClick()
        assertEquals(null, deletedId)
        composeRule.onNodeWithTag("conversation-one").performTouchInput { longClick() }
        composeRule.onNodeWithTag("delete-confirm").performClick()
        assertEquals("one", deletedId)
    }
}
