package com.localchat.app.ui

sealed interface AppRoute {
    data object Chat : AppRoute
    data object Settings : AppRoute {
        fun close(): AppRoute = Chat
    }
}
