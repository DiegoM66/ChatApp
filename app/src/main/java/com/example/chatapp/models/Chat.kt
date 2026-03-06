package com.example.chatapp.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Chat(
    val chatId: String = "",
    val chatName: String = "", // CAMPO ESSENCIAL para o nome do grupo
    val participants: List<String> = emptyList(),
    val group: Boolean = false,
    val lastMessage: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Date? = null,
    val createdBy: String = "",
    val pinnedMessage: Map<String, Any>? = null,
    val groupImageUrl: String? = null
) {
    // Construtor secundário para garantir compatibilidade com Firestore
    constructor() : this(
        chatId = "",
        chatName = "",
        participants = emptyList(),
        group = false,
        lastMessage = "",
        lastMessageTimestamp = null,
        createdBy = "",
        pinnedMessage = null,
        groupImageUrl = null
    )

    // Método auxiliar para obter nome de exibição
    fun getDisplayName(currentUserId: String?, allUsers: List<User> = emptyList()): String {
        return if (group) {
            chatName.takeIf { it.isNotBlank() } ?: "Grupo Sem Nome"
        } else {
            // Para chats individuais, tenta encontrar o nome do outro usuário
            val otherUserId = participants.find { it != currentUserId }
            val otherUser = allUsers.find { it.userId == otherUserId }
            otherUser?.name ?: chatName.takeIf { it.isNotBlank() } ?: "Chat"
        }
    }

    // Verifica se o chat é válido
    fun isValid(): Boolean {
        return chatId.isNotBlank() &&
                participants.isNotEmpty() &&
                (if (group) chatName.isNotBlank() else true)
    }
}