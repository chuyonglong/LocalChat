package com.localchat.app.domain

object ApiKeyMask {
    fun display(apiKey: String): String = if (apiKey.isBlank()) "" else "sk_**"
}
