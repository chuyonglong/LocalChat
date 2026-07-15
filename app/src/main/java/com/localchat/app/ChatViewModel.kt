package com.localchat.app

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.localchat.app.data.SettingsStore
import com.localchat.app.data.local.AppDatabase
import com.localchat.app.data.local.ConversationEntity
import com.localchat.app.data.local.MessageEntity
import com.localchat.app.data.remote.ResponsesClient
import com.localchat.app.data.remote.ModelsClient
import com.localchat.app.domain.ApiConfiguration
import com.localchat.app.domain.ChatMessage
import com.localchat.app.domain.ConversationContext
import com.localchat.app.domain.MessageRole
import com.localchat.app.domain.MessageFormatting
import com.localchat.app.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "local-chat.db").build()
    private val dao = db.chatDao()
    private val settings = SettingsStore(application)
    private val client = ResponsesClient()
    private val modelsClient = ModelsClient()
    val conversations = dao.observeConversations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun searchConversations(query: String): Flow<List<ConversationEntity>> =
        dao.searchConversations(query.trim())
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()
    private val _currentId = MutableStateFlow<String?>(null)
    val currentId: StateFlow<String?> = _currentId.asStateFlow()
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _models = MutableStateFlow<List<String>>(emptyList())
    val models: StateFlow<List<String>> = _models.asStateFlow()
    private val _modelsLoading = MutableStateFlow(false)
    val modelsLoading: StateFlow<Boolean> = _modelsLoading.asStateFlow()
    private val _themeMode = MutableStateFlow(ThemeMode.fromStored(settings.themeMode))
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun config() = Triple(settings.endpoint, settings.apiKey, settings.model)
    fun saveConfig(endpoint: String, key: String, model: String): String? = when (val result = ApiConfiguration.validateEndpoint(endpoint)) {
        is ApiConfiguration.Validation.Invalid -> result.message
        is ApiConfiguration.Validation.Valid -> { settings.endpoint = result.endpoint; settings.apiKey = key.trim(); settings.model = model.trim(); null }
    }
    fun fetchModels(endpoint: String, key: String) = viewModelScope.launch {
        val valid = ApiConfiguration.validateEndpoint(endpoint)
        if (valid !is ApiConfiguration.Validation.Valid || key.isBlank()) { _error.value = "请先填写有效服务地址和 API Key"; return@launch }
        _modelsLoading.value = true
        try { _models.value = modelsClient.fetch(valid.endpoint, key); if (_models.value.isEmpty()) _error.value = "上游未返回可用模型" }
        catch (e: Exception) { _error.value = e.message ?: "获取模型失败" }
        finally { _modelsLoading.value = false }
    }
    fun selectConversation(id: String) { _currentId.value = id; viewModelScope.launch { dao.observeMessages(id).collect { _messages.value = it } } }
    fun newConversation() { _currentId.value = null; _messages.value = emptyList() }
    fun setThemeMode(mode: ThemeMode) { settings.themeMode = mode.name; _themeMode.value = mode }
    fun deleteConversation(id: String) = viewModelScope.launch { dao.deleteConversation(id); if (_currentId.value == id) newConversation() }

    fun send(text: String, image: Uri?) = viewModelScope.launch {
        val normalizedText = MessageFormatting.normalizeForSend(text)
        val endpoint = settings.endpoint; val key = settings.apiKey; val model = settings.model
        if (endpoint.isBlank() || key.isBlank() || model.isBlank()) { _error.value = "请先在设置中填写服务地址、API Key 和模型"; return@launch }
        val id = _currentId.value ?: UUID.randomUUID().toString().also { _currentId.value = it }
        val now = System.currentTimeMillis(); val userId = UUID.randomUUID().toString()
        dao.saveConversation(ConversationEntity(id, normalizedText.ifBlank { "图片对话" }.take(30), now))
        dao.saveMessage(MessageEntity(userId, id, "USER", normalizedText, image?.toString(), now))
        _messages.value = _messages.value + MessageEntity(userId, id, "USER", normalizedText, image?.toString(), now)
        _isSending.value = true; _error.value = null
        val assistantId = UUID.randomUUID().toString(); var reply = ""
        try {
            val context = ConversationContext.build(_messages.value.map { ChatMessage(it.id, MessageRole.valueOf(it.role), it.content, it.createdAt) })
            client.stream(endpoint, key, model, context, image?.let { toDataUrl(it, getApplication<Application>().contentResolver) }).collect { delta ->
                reply += delta
                _messages.value = _messages.value.filterNot { it.id == assistantId } + MessageEntity(assistantId, id, "ASSISTANT", reply, null, System.currentTimeMillis())
            }
            dao.saveMessage(MessageEntity(assistantId, id, "ASSISTANT", reply.ifBlank { "（未返回文本内容）" }, null, System.currentTimeMillis()))
        } catch (e: Exception) { _error.value = e.message ?: "请求失败" } finally { _isSending.value = false }
    }
    private fun toDataUrl(uri: Uri, resolver: ContentResolver): String = resolver.openInputStream(uri)?.use { input ->
        "data:image/jpeg;base64," + Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
    } ?: error("无法读取图片")
}
