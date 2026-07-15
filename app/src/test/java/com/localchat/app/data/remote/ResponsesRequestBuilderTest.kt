package com.localchat.app.data.remote

import com.localchat.app.domain.ChatMessage
import com.localchat.app.domain.MessageRole
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsesRequestBuilderTest {
    @Test
    fun `build keeps all turns and uses output text for an assistant`() {
        val payload = ResponsesRequestBuilder.build(
            model = "test-model",
            messages = listOf(
                ChatMessage("u1", MessageRole.USER, "First question", 1),
                ChatMessage("a1", MessageRole.ASSISTANT, "First answer", 2),
                ChatMessage("u2", MessageRole.USER, "Follow-up question", 3),
            ),
            imageDataUrl = null,
        )

        val input = Json.parseToJsonElement(payload).jsonObject.getValue("input").jsonArray
        assertEquals(
            listOf("user", "assistant", "user"),
            input.map { it.jsonObject.getValue("role").jsonPrimitive.content },
        )
        assertEquals(
            "input_text",
            input[0].jsonObject.getValue("content").jsonArray[0].jsonObject.getValue("type").jsonPrimitive.content,
        )
        assertEquals(
            "output_text",
            input[1].jsonObject.getValue("content").jsonArray[0].jsonObject.getValue("type").jsonPrimitive.content,
        )
        assertEquals(
            "input_text",
            input[2].jsonObject.getValue("content").jsonArray[0].jsonObject.getValue("type").jsonPrimitive.content,
        )
    }
}
