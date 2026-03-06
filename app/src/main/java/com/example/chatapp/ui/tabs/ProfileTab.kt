package com.example.chatapp.ui.tabs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatapp.ui.components.InitialsAvatar
import com.example.chatapp.ui.navigation.Screen
import com.example.chatapp.viewmodels.AuthViewModel
import com.example.chatapp.viewmodels.ProfileUiState
import com.example.chatapp.viewmodels.ProfileViewModel
import com.example.chatapp.viewmodels.ThemeViewModel

@Composable
fun ProfileTab(navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by profileViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (!authState.isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message)
            }
        }
        is ProfileUiState.Success -> {
            ProfileContent(
                modifier = Modifier.fillMaxSize(),
                user = state.user,
                onUpdateProfile = { name, status ->
                    profileViewModel.updateProfile(name, status)
                },
                onUpdateImage = { uri ->
                    profileViewModel.updateProfileImageAsBase64(uri, context.contentResolver)
                },
                onLogout = { authViewModel.logout() }
            )
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    user: com.example.chatapp.models.User,
    onUpdateProfile: (String, String) -> Unit,
    onUpdateImage: (Uri) -> Unit,
    onLogout: () -> Unit
) {
    var name by remember(user.name) { mutableStateOf(user.name) }
    var status by remember(user.status) { mutableStateOf(user.status) }
    val themeViewModel: ThemeViewModel = viewModel()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let(onUpdateImage) }
    )

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val profileBitmap = remember(user.profileImage) {
            base64ToBitmap(user.profileImage)
        }

        // ALTERAÇÃO APLICADA AQUI
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "Foto de Perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
        } else {
            InitialsAvatar(
                name = user.name,
                size = 120.dp,
                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            label = { Text("Status") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Modo Escuro",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = { isChecked ->
                    themeViewModel.setTheme(isChecked)
                }
            )
        }

        Divider()

        Button(
            onClick = { onUpdateProfile(name, status) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Alterações")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sair (Logout)")
        }
    }
}

fun base64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrEmpty()) return null
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}