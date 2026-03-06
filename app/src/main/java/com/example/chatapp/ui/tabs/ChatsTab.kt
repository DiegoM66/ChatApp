package com.example.chatapp.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatapp.models.Chat
import com.example.chatapp.ui.components.InitialsAvatar
import com.example.chatapp.ui.navigation.Screen
import com.example.chatapp.utils.EncryptionUtils
import com.example.chatapp.viewmodels.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(navController: NavController, onFabClick: () -> Unit) {
    val chatsViewModel: ChatsViewModel = viewModel()
    val chats by chatsViewModel.chats.collectAsState()

    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredChats = chats.filter {
        it.chatName.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (isSearchVisible) {
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClose = {
                            isSearchVisible = false
                            searchQuery = ""
                        }
                    )
                }
            }
            items(filteredChats) { chat ->
                ChatItem(
                    chatName = chat.chatName,
                    lastMessage = chat.lastMessage,
                    timestamp = chat.lastMessageTimestamp,
                    onClick = {
                        navController.navigate(
                            Screen.Chat.createRoute(chat.chatId, chat.chatName)
                        )
                    }
                )
            }
        }
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova Conversa")
        }
    }
}

@Composable
fun ChatItem(
    chatName: String,
    lastMessage: String,
    timestamp: Date?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InitialsAvatar(name = chatName, size = 50.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chatName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = EncryptionUtils.decrypt(lastMessage), // Decriptografa para exibição
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        timestamp?.let {
            Text(
                text = formatTimestampToTime(it),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Fechar Busca")
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar conversa...") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

fun formatTimestampToTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}