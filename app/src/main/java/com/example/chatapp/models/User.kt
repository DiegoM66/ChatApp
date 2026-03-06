package com.example.chatapp.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String = "",
    // NOVO: Status dinâmico e timestamp da última vez online
    val status: String = "offline",
    val lastSeen: Long = 0,
    // NOVO: Lista de contatos (IDs de outros usuários)
    val contacts: List<String> = emptyList()
)