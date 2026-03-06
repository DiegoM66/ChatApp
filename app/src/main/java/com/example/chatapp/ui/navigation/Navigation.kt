package com.example.chatapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chatapp.ui.screens.ChatScreen
import com.example.chatapp.ui.screens.CreateGroupScreen
import com.example.chatapp.ui.screens.GroupInfoScreen // NOVO: Importe a tela
import com.example.chatapp.ui.screens.LoginScreen
import com.example.chatapp.ui.screens.MainScreen
import com.example.chatapp.ui.screens.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object CreateGroup : Screen("create_group_route")
    object Chat : Screen("chat/{chatId}/{chatName}") {
        fun createRoute(chatId: String, chatName: String) = "chat/$chatId/$chatName"
    }
    // NOVO: Rota para a tela de informações do grupo
    object GroupInfo : Screen("group_info/{chatId}") {
        fun createRoute(chatId: String) = "group_info/$chatId"
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Main.route
    } else {
        Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) { LoginScreen(navController = navController) }
        composable(Screen.Register.route) { RegisterScreen(navController = navController) }
        composable(Screen.Main.route) { MainScreen(navController = navController) }
        composable(Screen.CreateGroup.route) { CreateGroupScreen(navController = navController) }

        composable(Screen.Chat.route) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
            ChatScreen(navController = navController, chatId = chatId, chatName = chatName)
        }

        // NOVO: Adiciona o Composable para a tela de informações do grupo
        composable(Screen.GroupInfo.route) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            GroupInfoScreen(navController = navController, chatId = chatId)
        }
    }
}