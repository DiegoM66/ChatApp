package com.example.chatapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.chatapp.ui.components.CustomAppBar
import com.example.chatapp.ui.navigation.Screen // Import necessário
import com.example.chatapp.ui.tabs.ChatsTab
import com.example.chatapp.ui.tabs.ContactsTab
import com.example.chatapp.ui.tabs.ProfileTab

sealed class BottomNavScreen(val route: String, val icon: ImageVector, val title: String) {
    object Chats : BottomNavScreen("chats", Icons.Default.Chat, "Chats")
    object Contacts : BottomNavScreen("contacts", Icons.Default.Contacts, "Contatos")
    object Profile : BottomNavScreen("profile", Icons.Default.Person, "Perfil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Chats) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavScreen.Chats,
                    BottomNavScreen.Contacts,
                    BottomNavScreen.Profile
                )
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CustomAppBar(
                title = when (selectedScreen) {
                    BottomNavScreen.Chats -> "Conversas"
                    BottomNavScreen.Contacts -> "Contatos"
                    BottomNavScreen.Profile -> "Perfil"
                }
            )

            when (selectedScreen) {
                BottomNavScreen.Chats -> ChatsTab(navController = navController) {
                    // ALTERAÇÃO FINAL: Usando a rota segura da classe Screen
                    navController.navigate(Screen.CreateGroup.route)
                }
                BottomNavScreen.Contacts -> ContactsTab(navController)
                BottomNavScreen.Profile -> ProfileTab(navController)
            }
        }
    }
}