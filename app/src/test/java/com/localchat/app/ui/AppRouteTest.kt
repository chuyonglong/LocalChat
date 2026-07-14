package com.localchat.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTest {
    @Test
    fun `settings route returns to chat when closed`() {
        assertEquals(AppRoute.Chat, AppRoute.Settings.close())
    }
}
