package com.localchat.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsesStreamParserTest {
    @Test
    fun `extractTextDelta returns text only for response output delta events`() {
        assertEquals(
            "你好",
            ResponsesStreamParser.extractTextDelta(
                "{\"type\":\"response.output_text.delta\",\"delta\":\"你好\"}",
            ),
        )
        assertEquals(null, ResponsesStreamParser.extractTextDelta("{\"type\":\"response.completed\"}"))
    }
}
