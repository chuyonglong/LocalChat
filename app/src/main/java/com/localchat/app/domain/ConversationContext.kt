package com.localchat.app.domain

object ConversationContext {
    fun build(messages: List<ChatMessage>): List<ChatMessage> =
        messages.sortedBy(ChatMessage::createdAt)
}
