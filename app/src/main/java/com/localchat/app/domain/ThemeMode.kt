package com.localchat.app.domain

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStored(value: String): ThemeMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
