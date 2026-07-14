package com.localchat.app.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Suppress("DEPRECATION")
class SettingsStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context, "local_chat_settings", MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    var endpoint: String get() = prefs.getString("endpoint", "") ?: ""; set(value) = prefs.edit().putString("endpoint", value).apply()
    var apiKey: String get() = prefs.getString("api_key", "") ?: ""; set(value) = prefs.edit().putString("api_key", value).apply()
    var model: String get() = prefs.getString("model", "") ?: ""; set(value) = prefs.edit().putString("model", value).apply()
    var themeMode: String get() = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"; set(value) = prefs.edit().putString("theme_mode", value).apply()
}
