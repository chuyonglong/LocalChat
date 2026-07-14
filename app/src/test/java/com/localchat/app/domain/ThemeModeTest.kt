package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun `unknown stored mode falls back to system`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStored("unknown"))
    }
}
