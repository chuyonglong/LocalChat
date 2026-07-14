# Responses Conversation Context Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make follow-up messages succeed while sending the complete chronological history of only the active conversation.

**Architecture:** Extract request JSON construction into a pure builder. The client uses the builder unchanged for HTTP transport. The builder serializes user items as `input_text`, assistant items as `output_text`, and adds an optional image only to the final user item.

**Tech Stack:** Kotlin, kotlinx.serialization JSON, JUnit 4, OkHttp, ADB.

---

### Task 1: Define and Test Conversation Encoding

**Files:**
- Create: `app/src/test/java/com/localchat/app/data/remote/ResponsesRequestBuilderTest.kt`
- Modify: `app/src/main/java/com/localchat/app/data/remote/ResponsesClient.kt:20-41`

- [ ] **Step 1: Write the failing test**

```kotlin
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
    assertEquals(listOf("user", "assistant", "user"), input.map { it.jsonObject.getValue("role").jsonPrimitive.content })
    assertEquals("input_text", input[0].jsonObject.getValue("content").jsonArray[0].jsonObject.getValue("type").jsonPrimitive.content)
    assertEquals("output_text", input[1].jsonObject.getValue("content").jsonArray[0].jsonObject.getValue("type").jsonPrimitive.content)
    assertEquals("input_text", input[2].jsonObject.getValue("content").jsonArray[0].jsonObject.getValue("type").jsonPrimitive.content)
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.localchat.app.data.remote.ResponsesRequestBuilderTest`

Expected: FAIL because `ResponsesRequestBuilder` does not exist.

- [ ] **Step 3: Implement the minimal builder and call it from `ResponsesClient.stream`**

```kotlin
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
                                put("type", JsonPrimitive(if (message.role == MessageRole.USER) "input_text" else "output_text"))
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
```

- [ ] **Step 4: Run the focused test to verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests com.localchat.app.data.remote.ResponsesRequestBuilderTest`

Expected: PASS, with all three chronological turns retained and the assistant content type equal to `output_text`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/localchat/app/data/remote/ResponsesClient.kt app/src/test/java/com/localchat/app/data/remote/ResponsesRequestBuilderTest.kt
git commit -m "fix: preserve responses conversation context"
```

### Task 2: Verify the Device Regression

**Files:**
- Modify: `app/src/main/java/com/localchat/app/data/remote/ResponsesClient.kt:20-41`
- Test: `app/src/test/java/com/localchat/app/data/remote/ResponsesRequestBuilderTest.kt`

- [ ] **Step 1: Run all JVM tests**

Run: `./gradlew.bat testDebugUnitTest`

Expected: PASS, including the user-assistant-user serialization regression test.

- [ ] **Step 2: Build and install the debug APK**

```bash
./gradlew.bat assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Expected: Build succeeds and ADB reports `Success`.

- [ ] **Step 3: Manually verify a follow-up prompt**

Run: In one conversation, send a first text prompt, wait for an assistant reply, then send a follow-up. Reopen the same conversation.

Expected: The follow-up streams normally, no `请求失败 (502)` appears, and both turns remain visible after reopening.
