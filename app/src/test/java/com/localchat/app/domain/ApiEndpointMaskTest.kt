package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiEndpointMaskTest {
    @Test
    fun `display preserves only the endpoint scheme`() {
        assertEquals("https://***", ApiEndpointMask.display("https://api.example.com/v1"))
        assertEquals("http://***", ApiEndpointMask.display("http://localhost:8080/v1"))
        assertEquals("", ApiEndpointMask.display(""))
    }
}
