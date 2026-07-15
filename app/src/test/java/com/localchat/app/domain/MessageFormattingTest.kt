package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MessageFormattingTest {
    @Test
    fun `normalizeForSend removes only trailing whitespace`() {
        assertEquals("  hello\n  world", MessageFormatting.normalizeForSend("  hello\n  world  \n\t"))
    }

    @Test
    fun `codeLanguage recognizes supported aliases and falls back to plain text`() {
        assertEquals(CodeLanguage.JSON, CodeLanguage.fromFenceInfo("json"))
        assertEquals(CodeLanguage.KOTLIN, CodeLanguage.fromFenceInfo("kt title"))
        assertEquals(CodeLanguage.JAVASCRIPT, CodeLanguage.fromFenceInfo("typescript"))
        assertEquals(CodeLanguage.PLAIN, CodeLanguage.fromFenceInfo("rust"))
    }
}
