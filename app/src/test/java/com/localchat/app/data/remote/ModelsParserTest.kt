package com.localchat.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsParserTest {
    @Test
    fun `parseIds returns sorted model identifiers from OpenAI models response`() {
        val response = """{"data":[{"id":"gpt-4o-mini"},{"id":"gpt-4o"}]}"""

        assertEquals(listOf("gpt-4o", "gpt-4o-mini"), ModelsParser.parseIds(response))
    }
}
