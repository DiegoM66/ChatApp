package com.example.chatapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatapp.models.Message
import com.example.chatapp.ui.navigation.Screen
import com.example.chatapp.viewmodels.ChatViewModel
import com.example.chatapp.viewmodels.ChatViewModelFactory
import com.example.chatapp.viewmodels.ContactsViewModel
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    chatId: String,
    chatName: String
) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(chatId))
    val messages by viewModel.messages.collectAsState()
    val pinnedMessage by viewModel.pinnedMessage.collectAsState()
    val chatDetails by viewModel.chatDetails.collectAsState()
    val participants by viewModel.participants.collectAsState()

    // CORREÇÃO: ViewModel para buscar dados atualizados dos usuários
    val contactsViewModel: ContactsViewModel = viewModel()
    val allUsers by contactsViewModel.allUsers.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid

    val listState = rememberLazyListState()

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // CORREÇÃO: Calcula o nome correto para exibir no título
    val displayName = remember(chatDetails, allUsers, currentUserId, chatName) {
        when {
            chatDetails?.group == true -> {
                // Para grupos, usa sempre o chatName do banco de dados
                chatDetails?.chatName ?: chatName
            }
            else -> {
                // Para chats individuais, busca o nome atualizado do outro usuário
                val otherUserId = chatDetails?.participants?.find { it != currentUserId }
                val otherUser = allUsers.find { it.userId == otherUserId }

                // Prioriza o nome atual do usuário, senão usa o chatName como fallback
                otherUser?.name?.takeIf { it.isNotBlank() } ?: chatName
            }
        }
    }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { message ->
                message.text.contains(searchQuery, ignoreCase = true) ||
                        message.senderName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            if (isSearchVisible) {
                SearchTopAppBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        isSearchVisible = false
                        searchQuery = ""
                    },
                    resultsCount = filteredMessages.size,
                    totalCount = messages.size
                )
            } else {
                TopAppBar(
                    title = {
                        // CORREÇÃO: Usa o displayName calculado
                        Text(displayName)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar mensagens")
                        }
                        // Só mostra o ícone de informações se for um grupo
                        if (chatDetails?.group == true) {
                            IconButton(onClick = { navController.navigate(Screen.GroupInfo.createRoute(chatId)) }) {
                                Icon(Icons.Default.Info, contentDescription = "Informações do Grupo")
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pinnedMessage != null) {
                PinnedMessageHeader(pinnedMessage!!) { viewModel.unpinMessage() }
            }

            if (searchQuery.isNotBlank() && !isSearchVisible) {
                FilterIndicator(
                    query = searchQuery,
                    resultsCount = filteredMessages.size,
                    onClear = { searchQuery = "" }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredMessages) { message ->
                    // CORREÇÃO: Busca o nome atualizado do remetente da mensagem
                    val senderDisplayName = remember(message.senderId, allUsers, participants) {
                        // Primeiro tenta encontrar na lista de todos os usuários (mais atual)
                        val senderFromAllUsers = allUsers.find { it.userId == message.senderId }

                        // Se não encontrar, busca nos participantes do chat
                        val senderFromParticipants = participants.find { it.userId == message.senderId }

                        // Usa o nome mais atual encontrado, ou o nome salvo na mensagem como fallback
                        senderFromAllUsers?.name
                            ?: senderFromParticipants?.name
                            ?: message.senderName
                    }

                    MessageBubble(
                        message = message.copy(senderName = senderDisplayName),
                        searchQuery = searchQuery,
                        onLongPress = { viewModel.pinMessage(it) }
                    )
                }

                if (filteredMessages.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma mensagem encontrada para \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            if (!isSearchVisible) {
                MessageInput(onSendMessage = { text -> viewModel.sendMessage(text) })
            }
        }
    }
}

// Restante das funções permanecem iguais
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    resultsCount: Int,
    totalCount: Int
) {
    TopAppBar(
        title = {
            Column {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Buscar mensagens...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (query.isNotBlank()) {
                    Text(
                        text = "$resultsCount de $totalCount resultados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Fechar busca")
            }
        },
        actions = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                }
            }
        }
    )
}

@Composable
fun FilterIndicator(
    query: String,
    resultsCount: Int,
    onClear: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Filtrando por: \"$query\" ($resultsCount)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remover filtro",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun PinnedMessageHeader(pinnedMessage: Map<String, Any>, onUnpin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mensagem fixada",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = pinnedMessage["text"] as? String ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onUnpin) {
            Icon(Icons.Default.Close, contentDescription = "Desafixar")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    searchQuery: String,
    onLongPress: (Message) -> Unit
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isSentByMe = message.senderId == currentUserId
    val alignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isSentByMe) 64.dp else 0.dp,
                end = if (isSentByMe) 0.dp else 64.dp
            ),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { onLongPress(message) }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (!isSentByMe) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (message.mediaUrl != null) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Mídia",
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            if (message.text.isNotEmpty()) {
                Text(
                    text = highlightSearchQuery(message.text, searchQuery),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun highlightSearchQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }
    return buildAnnotatedString {
        val textLower = text.lowercase(Locale.getDefault())
        val queryLower = query.lowercase(Locale.getDefault())
        var startIndex = 0
        while (startIndex < text.length) {
            val index = textLower.indexOf(queryLower, startIndex)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            }
            append(text.substring(startIndex, index))
            withStyle(
                style = SpanStyle(
                    background = MaterialTheme.colorScheme.tertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            startIndex = index + query.length
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Surface(
        shadowElevation = 4.dp,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Anexar")
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite uma mensagem...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text.trim())
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatTimestamp(date: Date?): String {
    return date?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
    } ?: ""
}