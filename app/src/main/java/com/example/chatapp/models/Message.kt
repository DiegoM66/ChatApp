package com.example.chatapp.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null,
    // MODIFICADO: Status agora é um mapa para rastrear quem leu
    // Ex: { "userId1": "read", "userId2": "delivered" }
    val status: Map<String, String> = emptyMap(),
    val senderName: String = "" // Útil para grupos e notificações
)