package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.models.Chat
import com.example.chatapp.models.User
import com.example.chatapp.utils.EncryptionUtils
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateGroupViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        viewModelScope.launch {
            try {
                val currentUserUid = auth.currentUser?.uid
                val result = db.collection("users").get().await()
                val userList = result.toObjects(User::class.java)
                _contacts.value = userList.filter { it.userId != currentUserUid }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar contatos: ${e.message}"
            }
        }
    }

    fun createGroup(groupName: String, selectedContactIds: List<String>, onComplete: () -> Unit) {
        if (groupName.isBlank()) {
            _errorMessage.value = "Nome do grupo não pode estar vazio"
            return
        }

        if (selectedContactIds.isEmpty()) {
            _errorMessage.value = "Selecione pelo menos um participante"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUser = auth.currentUser ?: return@launch
                val participantIds = mutableListOf(currentUser.uid)
                participantIds.addAll(selectedContactIds)
                val newChatId = db.collection("chats").document().id

                val newChat = Chat(
                    chatId = newChatId,
                    chatName = groupName.trim(),
                    participants = participantIds,
                    group = true,
                    lastMessage = EncryptionUtils.encrypt("Grupo criado"), // Criptografa a mensagem inicial
                    createdBy = currentUser.uid
                )

                db.collection("chats").document(newChatId).set(newChat).await()

                val systemMessageId = db.collection("chats").document(newChatId).collection("messages").document().id
                val systemMessage = mapOf(
                    "messageId" to systemMessageId,
                    "senderId" to "system",
                    "text" to "Grupo \"$groupName\" foi criado",
                    "senderName" to "Sistema",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "status" to emptyMap<String, String>()
                )
                db.collection("chats").document(newChatId).collection("messages").document(systemMessageId).set(systemMessage).await()

                _isLoading.value = false
                onComplete()

            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erro ao criar grupo: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}