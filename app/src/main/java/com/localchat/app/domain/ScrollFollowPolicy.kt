package com.localchat.app.domain

object ScrollFollowPolicy {
    fun shouldFollow(streaming: Boolean, wasAtBottom: Boolean, userMessageAdded: Boolean = false): Boolean =
        userMessageAdded || (streaming && wasAtBottom)
}
