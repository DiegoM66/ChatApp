package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import com.example.chatapp.models.Chat
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Ouve em tempo real por conversas onde o usuário atual é um participante
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Tratar erro
                    return@addSnapshotListener
                }

                val chatList = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                _chats.value = chatList
            }
    }
}