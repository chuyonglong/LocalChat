package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiKeyMaskTest {
    @Test
    fun `mask uses fixed prefix without exposing key length`() {
        assertEquals("sk_**", ApiKeyMask.display("sk-very-secret-value"))
        assertEquals("", ApiKeyMask.display(""))
    }
}
