package com.localchat.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollFollowPolicyTest {
    @Test
    fun `stream updates follow only while already at bottom`() {
        assertTrue(ScrollFollowPolicy.shouldFollow(streaming = true, wasAtBottom = true))
        assertFalse(ScrollFollowPolicy.shouldFollow(streaming = true, wasAtBottom = false))
    }

    @Test
    fun `new user message always returns to bottom`() {
        assertTrue(ScrollFollowPolicy.shouldFollow(streaming = false, wasAtBottom = false, userMessageAdded = true))
    }
}
