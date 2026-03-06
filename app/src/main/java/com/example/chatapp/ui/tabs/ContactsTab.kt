package com.example.chatapp.ui.tabs

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatapp.models.User
import com.example.chatapp.ui.components.InitialsAvatar
import com.example.chatapp.ui.navigation.Screen
import com.example.chatapp.viewmodels.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsTab(navController: NavController) {
    val viewModel: ContactsViewModel = viewModel()
    val contacts by viewModel.contacts.collectAsState()
    val searchedUser by viewModel.searchedUser.collectAsState()
    val searchResultMsg by viewModel.searchResultMsg.collectAsState()

    var emailQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(searchResultMsg) {
        searchResultMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSearchResultMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SearchUserSection(
            emailQuery = emailQuery,
            onEmailQueryChange = { emailQuery = it },
            onSearchClick = { viewModel.searchUserByEmail(emailQuery) }
        )

        searchedUser?.let { user ->
            SearchResultItem(
                user = user,
                onAddClick = { viewModel.addContact(user) }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            "Meus Contatos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(contacts) { user ->
                ContactItem(user = user) {
                    viewModel.createChatWith(user) { chatId, chatName ->
                        navController.navigate(Screen.Chat.createRoute(chatId, chatName))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserSection(
    emailQuery: String,
    onEmailQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = emailQuery,
            onValueChange = onEmailQueryChange,
            label = { Text("Buscar por email") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.Search, contentDescription = "Buscar Contato")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultItem(user: User, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ALTERAÇÃO APLICADA AQUI
                if (user.profileImage.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImage,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    InitialsAvatar(name = user.name, size = 40.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(user.name, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Contato")
            }
        }
    }
}

@Composable
fun ContactItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ALTERAÇÃO APLICADA AQUI
        if (user.profileImage.isNotEmpty()) {
            AsyncImage(
                model = user.profileImage,
                contentDescription = "Avatar do Contato",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            InitialsAvatar(name = user.name, size = 50.dp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = user.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = user.status,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}