package com.localchat.app.domain

object ApiEndpointMask {
    fun display(endpoint: String): String = when {
        endpoint.startsWith("https://") -> "https://***"
        endpoint.startsWith("http://") -> "http://***"
        else -> ""
    }
}
