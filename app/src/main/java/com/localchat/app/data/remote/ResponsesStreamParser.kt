package com.localchat.app.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ResponsesStreamParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun extractTextDelta(payload: String): String? = runCatching {
        val event = json.parseToJsonElement(payload).jsonObject
        if (event["type"]?.jsonPrimitive?.content != "response.output_text.delta") return null
        event["delta"]?.jsonPrimitive?.content
    }.getOrNull()
}
