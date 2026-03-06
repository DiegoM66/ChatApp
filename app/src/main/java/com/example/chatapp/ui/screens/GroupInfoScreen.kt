package com.example.chatapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatapp.models.User
import com.example.chatapp.ui.components.InitialsAvatar
import com.example.chatapp.viewmodels.ChatViewModel
import com.example.chatapp.viewmodels.ChatViewModelFactory
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.example.chatapp.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(navController: NavController, chatId: String) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(chatId))
    val chatDetails by viewModel.chatDetails.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid
    val isAdmin = currentUserId == chatDetails?.createdBy

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dados do Grupo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                GroupHeader(
                    imageUrl = chatDetails?.groupImageUrl,
                    groupName = chatDetails?.chatName ?: "Grupo",
                    isAdmin = isAdmin,
                    onImageChange = { uri -> viewModel.updateGroupImage(uri) },
                    onNameChange = { newName -> viewModel.updateGroupName(newName) }
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "${participants.size} participantes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(participants) { user ->
                ParticipantItem(
                    user = user,
                    isAdminView = isAdmin,
                    isGroupCreator = user.userId == chatDetails?.createdBy,
                    onRemoveClick = { viewModel.removeParticipant(user.userId) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (isAdmin) {
                    DeleteGroupButton {
                        viewModel.deleteGroup {
                            navController.popBackStack(Screen.Main.route, false)
                        }
                    }
                } else {
                    LeaveGroupButton {
                        if (currentUserId != null) {
                            viewModel.removeParticipant(currentUserId)
                            navController.popBackStack(Screen.Main.route, false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeader(
    imageUrl: String?,
    groupName: String,
    isAdmin: Boolean,
    onImageChange: (Uri) -> Unit,
    onNameChange: (String) -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let(onImageChange) }
    )

    if (showEditNameDialog) {
        EditNameDialog(
            currentName = groupName,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                onNameChange(newName)
                showEditNameDialog = false
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            val modifier = if (isAdmin) Modifier.clickable { imagePickerLauncher.launch("image/*") } else Modifier
            if (imageUrl.isNullOrEmpty()) {
                InitialsAvatar(name = groupName, size = 120.dp, modifier = modifier)
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto do Grupo",
                    modifier = modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if (isAdmin) Modifier.clickable { showEditNameDialog = true } else Modifier
        ) {
            Text(groupName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (isAdmin) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Edit, contentDescription = "Editar Nome", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Renomear Grupo", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                TextField(value = text, onValueChange = { text = it })
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(text) }) { Text("Salvar") }
                }
            }
        }
    }
}

@Composable
fun ParticipantItem(user: User, isAdminView: Boolean, isGroupCreator: Boolean, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.profileImage.isNotEmpty()) {
            AsyncImage(model = user.profileImage, contentDescription = "Avatar", modifier = Modifier.size(40.dp).clip(CircleShape))
        } else {
            InitialsAvatar(name = user.name, size = 40.dp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.SemiBold)
            if (isGroupCreator) {
                Text("Admin", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (isAdminView && !isGroupCreator) {
            TextButton(onClick = onRemoveClick) { Text("Remover") }
        }
    }
}

@Composable
fun LeaveGroupButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sair do Grupo")
    }
}

@Composable
fun DeleteGroupButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Icon(Icons.Default.Delete, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Excluir Grupo")
    }
}