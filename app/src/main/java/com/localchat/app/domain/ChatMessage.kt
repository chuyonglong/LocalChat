package com.localchat.app.domain

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val text: String,
    val createdAt: Long,
)

enum class MessageRole {
    USER,
    ASSISTANT,
}
