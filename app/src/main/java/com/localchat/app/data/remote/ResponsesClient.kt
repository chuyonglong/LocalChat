package com.localchat.app.data.remote

import com.localchat.app.domain.ChatMessage
import com.localchat.app.domain.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ResponsesClient(private val client: OkHttpClient = OkHttpClient()) {
    fun stream(endpoint: String, apiKey: String, model: String, messages: List<ChatMessage>, imageDataUrl: String?): Flow<String> = flow {
        val payload = ResponsesRequestBuilder.build(model, messages, imageDataUrl)
        val request = Request.Builder().url(endpoint.trimEnd('/') + "/responses")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("请求失败 (${response.code}): ${response.body.string().take(200)}")
            response.body.source().use { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: continue
                    if (line.startsWith("data: ")) {
                        ResponsesStreamParser.extractTextDelta(line.removePrefix("data: "))?.let { emit(it) }
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}

internal object ResponsesRequestBuilder {
    fun build(model: String, messages: List<ChatMessage>, imageDataUrl: String?): String =
        buildJsonObject {
            put("model", JsonPrimitive(model))
            put("stream", JsonPrimitive(true))
            put("input", buildJsonArray {
                messages.forEachIndexed { index, message ->
                    add(buildJsonObject {
                        put("role", JsonPrimitive(if (message.role == MessageRole.USER) "user" else "assistant"))
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put(
                                    "type",
                                    JsonPrimitive(if (message.role == MessageRole.USER) "input_text" else "output_text"),
                                )
                                put("text", JsonPrimitive(message.text))
                            })
                            if (index == messages.lastIndex && message.role == MessageRole.USER && imageDataUrl != null) {
                                add(buildJsonObject {
                                    put("type", JsonPrimitive("input_image"))
                                    put("image_url", JsonPrimitive(imageDataUrl))
                                })
                            }
                        })
                    })
                }
            })
        }.toString()
}
