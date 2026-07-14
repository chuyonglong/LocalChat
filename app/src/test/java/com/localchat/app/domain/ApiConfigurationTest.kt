package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiConfigurationTest {
    @Test
    fun `validate normalizes endpoint and rejects an endpoint without http scheme`() {
        assertEquals(
            ApiConfiguration.Validation.Valid("https://example.com/v1"),
            ApiConfiguration.validateEndpoint(" https://example.com/v1 "),
        )
        assertEquals(
            ApiConfiguration.Validation.Valid("https://example.com/v1"),
            ApiConfiguration.validateEndpoint("https://example.com/v1/"),
        )
        assertEquals(
            ApiConfiguration.Validation.Invalid("服务地址必须以 http:// 或 https:// 开头"),
            ApiConfiguration.validateEndpoint("example.com/v1"),
        )
    }
}
