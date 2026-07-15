package com.localchat.app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.localchat.app.data.local.MessageEntity
import com.localchat.app.domain.QuickConfigImport
import com.localchat.app.domain.ApiKeyMask
import com.localchat.app.domain.ApiEndpointMask
import com.localchat.app.domain.ThemeMode
import com.localchat.app.domain.MarkdownBlock
import com.localchat.app.domain.MarkdownDocument
import com.localchat.app.ui.AppRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalChatApp(vm: ChatViewModel = viewModel()) {
    var route by remember { mutableStateOf<AppRoute>(AppRoute.Chat) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val conversations by vm.conversations.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by vm.searchConversations(searchQuery).collectAsState(initial = emptyList())
    val themeMode by vm.themeMode.collectAsState()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(Modifier.widthIn(max = 360.dp)) {
                DrawerContent(
                    conversations = if (searchQuery.isBlank()) conversations else searchResults,
                    onConversation = { vm.selectConversation(it); scope.launch { drawerState.close() } },
                    onDeleteConversation = vm::deleteConversation,
                    onNewChat = { vm.newConversation(); scope.launch { drawerState.close() } },
                    themeMode = themeMode,
                    onThemeMode = vm::setThemeMode,
                    onSettings = {
                        route = AppRoute.Settings; scope.launch { drawerState.close() }
                    },
                    searchQuery = searchQuery,
                    onSearchQuery = { searchQuery = it },
                )
            }
        },
    ) {
        when (route) {
            AppRoute.Chat -> ChatScreen(vm, onOpenDrawer = { scope.launch { drawerState.open() } })
            AppRoute.Settings -> SettingsScreen(vm, onBack = { route = AppRoute.Settings.close() })
        }
    }
}

@Composable
internal fun DrawerContent(
    conversations: List<com.localchat.app.data.local.ConversationEntity>,
    onConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onSettings: () -> Unit,
    searchQuery: String,
    onSearchQuery: (String) -> Unit,
) {
    var showThemeMenu by remember { mutableStateOf(false) }
    var searchMode by remember { mutableStateOf(false) }
    var conversationToDelete by remember { mutableStateOf<com.localchat.app.data.local.ConversationEntity?>(null) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(searchMode) {
        if (searchMode) searchFocusRequester.requestFocus()
    }

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        if (searchMode) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        searchMode = false
                        onSearchQuery("")
                    },
                    modifier = Modifier.testTag("search-back"),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQuery,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocusRequester)
                        .testTag("search-query"),
                    singleLine = true,
                    placeholder = { Text("搜索聊天") },
                )
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("LocalChat", style = MaterialTheme.typography.headlineSmall)
                IconButton(onSettings) { Icon(Icons.Default.Settings, "设置和 API") }
            }
            NavigationDrawerItem(
                label = { Text("搜索聊天") },
                selected = false,
                icon = { Icon(Icons.Default.Search, null) },
                onClick = { searchMode = true },
                modifier = Modifier.testTag("search-action"),
            )
            NavigationDrawerItem(
                label = { Text("新建聊天") },
                selected = false,
                icon = { Icon(Icons.Default.Edit, null) },
                onClick = onNewChat,
            )
            Box {
                NavigationDrawerItem(
                    label = { Text("主题") },
                    selected = false,
                    icon = { Icon(Icons.Default.BrightnessAuto, null) },
                    onClick = { showThemeMenu = true },
                )
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false },
                ) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> "跟随系统"
                                        ThemeMode.LIGHT -> "浅色模式"
                                        ThemeMode.DARK -> "深色模式"
                                    },
                                )
                            },
                            onClick = {
                                onThemeMode(mode)
                                showThemeMenu = false
                            },
                        )
                    }
                }
            }
            Text(
                "最近",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
        }

        LazyColumn(Modifier.weight(1f)) {
            if (searchMode && conversations.isEmpty()) {
                item { Text("没有匹配的聊天", modifier = Modifier.padding(16.dp)) }
            } else {
                items(conversations, key = { it.id }) { conversation ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .combinedClickable(
                                onClick = { onConversation(conversation.id) },
                                onLongClick = { conversationToDelete = conversation },
                            )
                            .testTag("conversation-${conversation.id}"),
                        color = Color.Transparent,
                    ) {
                        Text(
                            conversation.title,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        )
                    }
                }
            }
        }
    }

    conversationToDelete?.let { conversation ->
        AlertDialog(
            modifier = Modifier.testTag("delete-confirmation"),
            onDismissRequest = { conversationToDelete = null },
            title = { Text("删除聊天？") },
            text = { Text("将删除“${conversation.title}”及其所有消息。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConversation(conversation.id)
                        conversationToDelete = null
                    },
                    modifier = Modifier.testTag("delete-confirm"),
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(
                    onClick = { conversationToDelete = null },
                    modifier = Modifier.testTag("delete-cancel"),
                ) { Text("取消") }
            },
        )
    }
}

@Composable
private fun LegacyDrawerContent(
    conversations: List<com.localchat.app.data.local.ConversationEntity>,
    onConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onSettings: () -> Unit,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("LocalChat", style = MaterialTheme.typography.headlineSmall)
            IconButton(onSettings) { Icon(Icons.Default.Settings, "设置与 API") }
        }
        NavigationDrawerItem(
            label = { Text("搜索聊天") },
            selected = false,
            icon = { Icon(Icons.Default.Search, null) },
            onClick = { })
        NavigationDrawerItem(
            label = { Text("新建聊天") },
            selected = false,
            icon = { Icon(Icons.Default.Edit, null) },
            onClick = onNewChat
        )
        Box {
            NavigationDrawerItem(
                label = { Text("主题") },
                selected = false,
                icon = { Icon(Icons.Default.BrightnessAuto, null) },
                onClick = { showThemeDialog = true },
            )
            DropdownMenu(
                expanded = showThemeDialog,
                onDismissRequest = { showThemeDialog = false },
            ) {
                ThemeMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (mode) {
                                    ThemeMode.SYSTEM -> "跟随系统"
                                    ThemeMode.LIGHT -> "浅色模式"
                                    ThemeMode.DARK -> "深色模式"
                                },
                            )
                        },
                        onClick = {
                            onThemeMode(mode)
                            showThemeDialog = false
                        },
                    )
                }
            }
        }
        Text(
            "最近",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        LazyColumn(Modifier.weight(1f)) {
            items(conversations) { conversation ->
                NavigationDrawerItem(label = {
                    Text(
                        conversation.title,
                        maxLines = 1
                    )
                }, selected = false, onClick = { onConversation(conversation.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(vm: ChatViewModel, onOpenDrawer: () -> Unit) {
    val messages by vm.messages.collectAsState()
    val sending by vm.isSending.collectAsState()
    val error by vm.error.collectAsState()
    var draft by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<Uri?>(null) }
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { image = it }
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onOpenDrawer) {
                        Icon(
                            Icons.Default.Menu,
                            "打开聊天记录"
                        )
                    }
                },
                actions = {
                    if (messages.isNotEmpty()) IconButton(vm::newConversation) {
                        Icon(
                            Icons.Default.Edit,
                            "新建聊天"
                        )
                    }
                })
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (messages.isEmpty()) EmptyChat(Modifier.fillMaxSize()) else MessageList(
                messages,
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 88.dp)
            )
            if (image != null) Text(
                "已附加图片",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 82.dp)
            )
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 82.dp)
                )
            }
            Composer(
                draft = draft,
                hasImage = image != null,
                sending = sending,
                modifier = Modifier.align(Alignment.BottomCenter),
                onDraft = { draft = it },
                onImage = { picker.launch("image/*") },
            ) { vm.send(draft, image); draft = ""; image = null }
        }
    }
}

@Composable
private fun EmptyChat(modifier: Modifier) = Box(modifier)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=892dp,dpi=420",
)
@Composable
private fun HomePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Menu, "Open chat history")
                        }
                    },
                )
            },
        ) { padding ->
            Box(Modifier
                .fillMaxSize()
                .padding(padding)) {
                EmptyChat(Modifier.fillMaxSize())
                Composer(
                    draft = "",
                    hasImage = false,
                    sending = false,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onDraft = {},
                    onImage = {},
                    onSend = {},
                )
            }
        }
    }
}

@Composable
internal fun MessageList(messages: List<MessageEntity>, modifier: Modifier) {
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, messages.lastOrNull()?.content) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    LazyColumn(
        modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(messages) { message ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = if (message.role == "USER") Arrangement.End else Arrangement.Start
            ) {
                if (message.role == "USER") {
                    Surface(
                        modifier = Modifier.widthIn(max = 340.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(message.content, Modifier.weight(1f).padding(start = 14.dp, top = 10.dp, bottom = 10.dp), style = MaterialTheme.typography.bodyLarge)
                            IconButton({ clipboard.setText(AnnotatedString(message.content)) }) { Icon(Icons.Default.ContentCopy, "复制消息") }
                        }
                    }
                } else {
                    MarkdownMessage(message.content, Modifier.widthIn(max = 340.dp), clipboard::setText)
                }
            }
        }
    }
}

@Composable
private fun MarkdownMessage(content: String, modifier: Modifier, copy: (AnnotatedString) -> Unit) = Column(modifier) {
    MarkdownDocument.parse(content).blocks.forEach { block ->
        when (block) {
            is MarkdownBlock.Heading -> Text(block.text, style = if (block.level <= 2) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium)
            is MarkdownBlock.Paragraph -> Text(block.text, style = MaterialTheme.typography.bodyLarge)
            is MarkdownBlock.ListItem -> Text("• ${block.text}")
            is MarkdownBlock.TaskItem -> Text("${if (block.checked) "☑" else "☐"} ${block.text}")
            is MarkdownBlock.Quote -> Text(block.blocks.joinToString("\n") { it.toString() }, modifier = Modifier.padding(start = 12.dp))
            is MarkdownBlock.Table -> block.rows.forEach { Text(it.joinToString(" | "), fontFamily = FontFamily.Monospace) }
            is MarkdownBlock.Code -> Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(block.language.name.lowercase(), Modifier.weight(1f)); IconButton({ copy(AnnotatedString(block.content)) }) { Icon(Icons.Default.ContentCopy, "复制代码") } }
                    Text(block.content, fontFamily = FontFamily.Monospace)
                }
            }
            MarkdownBlock.Rule -> HorizontalDivider()
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    hasImage: Boolean,
    sending: Boolean,
    modifier: Modifier,
    onDraft: (String) -> Unit,
    onImage: () -> Unit,
    onSend: () -> Unit
) = Box(
    modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp)
        .padding(top = 2.dp, bottom = 24.dp),
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 3.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onImage) { Icon(Icons.Default.Add, "添加图片") }
            OutlinedTextField(
                draft,
                onDraft,
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                placeholder = { Text("问问 LocalChat") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                ),
            )
            IconButton({ if (!sending && (draft.isNotBlank() || hasImage)) onSend() }) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    "发送"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(vm: ChatViewModel, onBack: () -> Unit) {
    val old = vm.config();
    val models by vm.models.collectAsState();
    val loading by vm.modelsLoading.collectAsState();
    val apiError by vm.error.collectAsState()
    var endpoint by remember { mutableStateOf(ApiEndpointMask.display(old.first)) };
    var endpointEditing by remember { mutableStateOf(false) };
    var key by remember { mutableStateOf(ApiKeyMask.display(old.second)) };
    var keyEditing by remember { mutableStateOf(false) };
    var model by remember { mutableStateOf(old.third) };
    var saveError by remember { mutableStateOf<String?>(null) };
    var quickImport by remember { mutableStateOf("") };
    var showModels by remember { mutableStateOf(false) }
    val effectiveEndpoint = if (endpointEditing) endpoint else old.first
    val effectiveKey = if (keyEditing) key else old.second
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置与 API") },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "返回"
                        )
                    }
                },
                actions = {
                    TextButton({
                        saveError = vm.saveConfig(
                            effectiveEndpoint,
                            effectiveKey,
                            model
                        ); if (saveError == null) onBack()
                    }) { Text("保存") }
                })
        }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            OutlinedTextField(
                quickImport,
                { quickImport = it },
                Modifier.fillMaxWidth(),
                label = { Text("快速导入配置（每行一个值）") },
                placeholder = { Text("API 地址\nAPI Key\n模型（可选）") },
                minLines = 3,
                maxLines = 4
            )
            TextButton({
                when (val result = QuickConfigImport.parse(quickImport, model)) {
                    is QuickConfigImport.Result.Success -> {
                        endpoint = result.endpoint; endpointEditing = true; key = result.apiKey; keyEditing = true; model =
                            result.model; saveError = null
                    }

                    is QuickConfigImport.Result.Error -> saveError = result.message
                }
            }, Modifier.fillMaxWidth()) { Text("自动填入") }
            HorizontalDivider()
            OutlinedTextField(
                endpoint,
                { endpoint = it },
                Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (it.isFocused && !endpointEditing) {
                            endpointEditing = true; endpoint = ""
                        }
                    },
                label = { Text("服务地址（含 /v1）") })
            OutlinedTextField(
                key,
                { key = it },
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .onFocusChanged {
                        if (it.isFocused && !keyEditing) {
                            keyEditing = true; key = ""
                        }
                    },
                label = { Text("API Key") })
            OutlinedTextField(
                model,
                { model = it },
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("模型名称") })
            TextButton(
                { showModels = true; vm.fetchModels(effectiveEndpoint, effectiveKey) },
                enabled = !loading
            ) { Text(if (loading) "正在获取模型…" else "获取上游模型") }
            if (showModels && models.isNotEmpty()) {
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp
                ) {
                    LazyColumn {
                        items(models) { id ->
                            TextButton({
                                model = id; showModels = false
                            }, Modifier.fillMaxWidth()) { Text(id) }
                        }
                    }
                }
            }
            (saveError ?: apiError)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
