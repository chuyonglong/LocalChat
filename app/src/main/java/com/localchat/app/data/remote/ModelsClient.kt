package com.localchat.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

object ModelsParser {
    private val json = Json { ignoreUnknownKeys = true }
    fun parseIds(payload: String): List<String> = json.parseToJsonElement(payload)
        .jsonObject["data"]?.jsonArray.orEmpty()
        .mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNull }
        .distinct()
        .sorted()
}

class ModelsClient(private val client: OkHttpClient = OkHttpClient()) {
    suspend fun fetch(endpoint: String, apiKey: String): List<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(endpoint.trimEnd('/') + "/models")
            .header("Authorization", "Bearer $apiKey").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("无法获取模型 (${response.code})")
            ModelsParser.parseIds(response.body.string())
        }
    }
}
