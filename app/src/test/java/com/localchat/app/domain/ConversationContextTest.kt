package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationContextTest {
    @Test
    fun `build includes only messages from the active conversation in chronological order`() {
        val active = listOf(
            ChatMessage("u1", MessageRole.USER, "你好", 20),
            ChatMessage("a1", MessageRole.ASSISTANT, "你好！", 30),
        )

        assertEquals(
            listOf("你好", "你好！"),
            ConversationContext.build(active).map { it.text },
        )
    }
}
