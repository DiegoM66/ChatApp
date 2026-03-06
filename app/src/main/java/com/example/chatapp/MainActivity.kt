package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // NOVO: Import necessário
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.ui.navigation.AppNavHost
import com.example.chatapp.ui.navigation.Screen
import com.example.chatapp.ui.theme.ChatAppTheme
import com.example.chatapp.viewmodels.ThemeViewModel // NOVO: Import necessário

class MainActivity : ComponentActivity() {

    // NOVO: Instancia o ViewModel que gerencia o estado do tema
    private val themeViewModel: ThemeViewModel by viewModels()

    private val intentState = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentState.value = intent

        setContent {
            // NOVO: Coleta o estado do tema (true para escuro, false para claro) do ViewModel
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            // ALTERADO: Passa o estado 'isDarkMode' para o ChatAppTheme
            ChatAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    LaunchedEffect(key1 = intentState.value) {
                        handleIntent(intentState.value, navController)
                    }

                    // O seu AppNavHost continua aqui, sem alterações
                    AppNavHost(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intentState.value = intent
    }

    private fun handleIntent(intent: Intent?, navController: NavController) {
        intent?.let {
            val chatId = it.getStringExtra("chatId")
            val chatName = it.getStringExtra("chatName")

            if (!chatId.isNullOrEmpty() && !chatName.isNullOrEmpty()) {
                navController.navigate(Screen.Chat.createRoute(chatId, chatName))
                intentState.value = null
            }
        }
    }
}