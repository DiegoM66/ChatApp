package com.example.chatapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatapp.models.User
import com.example.chatapp.ui.components.InitialsAvatar
import com.example.chatapp.viewmodels.CreateGroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(navController: NavController) {
    val viewModel: CreateGroupViewModel = viewModel()
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current

    var groupName by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }

    val isFormValid = groupName.isNotBlank() && selectedContacts.isNotEmpty()

    // Mostra mensagens de erro como Toast
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Grupo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isFormValid && !isLoading) {
                        viewModel.createGroup(
                            groupName = groupName.trim(),
                            selectedContactIds = selectedContacts.toList()
                        ) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(imageVector = Icons.Default.Done, contentDescription = "Criar Grupo")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Campo de nome do grupo
            OutlinedTextField(
                value = groupName,
                onValueChange = {
                    if (it.length <= 50) { // Limita o nome do grupo
                        groupName = it
                    }
                },
                label = { Text("Nome do grupo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                enabled = !isLoading,
                supportingText = {
                    Text("${groupName.length}/50 caracteres")
                },
                isError = groupName.isBlank() && groupName.isNotEmpty()
            )

            // Contador de participantes selecionados
            if (selectedContacts.isNotEmpty()) {
                Text(
                    text = "${selectedContacts.size} participante(s) selecionado(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Selecione os participantes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (contacts.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum contato encontrado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                items(contacts) { contact ->
                    ContactSelectItem(
                        user = contact,
                        isSelected = selectedContacts.contains(contact.userId),
                        enabled = !isLoading,
                        onToggleSelection = {
                            selectedContacts = if (selectedContacts.contains(contact.userId)) {
                                selectedContacts - contact.userId
                            } else {
                                selectedContacts + contact.userId
                            }
                        }
                    )
                }

                // Espaço extra no final para não ficar atrás do FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ContactSelectItem(
    user: User,
    isSelected: Boolean,
    enabled: Boolean = true,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggleSelection)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(
                if (!enabled) Modifier else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar do contato
        if (user.profileImage.isNullOrEmpty()) {
            InitialsAvatar(name = user.name, size = 40.dp)
        } else {
            AsyncImage(
                model = user.profileImage,
                contentDescription = "Avatar do Contato",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            if (user.status.isNotEmpty()) {
                Text(
                    text = user.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
            }
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = null, // Controlado pelo click na Row
            enabled = enabled
        )
    }
}