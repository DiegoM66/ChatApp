package com.example.chatapp.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatapp.models.Chat
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.example.chatapp.utils.EncryptionUtils
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatViewModel(private val chatId: String) : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _pinnedMessage = MutableStateFlow<Map<String, Any>?>(null)
    val pinnedMessage = _pinnedMessage.asStateFlow()

    private val _chatDetails = MutableStateFlow<Chat?>(null)
    val chatDetails = _chatDetails.asStateFlow()

    private val _participants = MutableStateFlow<List<User>>(emptyList())
    val participants = _participants.asStateFlow()

    init {
        listenForMessages()
        listenForChatDetails()
    }

    private fun listenForChatDetails() {
        db.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, _ ->
                val chat = snapshot?.toObject(Chat::class.java)
                _chatDetails.value = chat
                _pinnedMessage.value = chat?.pinnedMessage
                chat?.participants?.let { fetchParticipants(it) }
            }
    }

    private fun fetchParticipants(userIds: List<String>) {
        if (userIds.isEmpty()) {
            _participants.value = emptyList()
            return
        }
        db.collection("users").whereIn("userId", userIds)
            .addSnapshotListener { snapshot, _ ->
                _participants.value = snapshot?.toObjects(User::class.java) ?: emptyList()
            }
    }

    private fun listenForMessages() {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val messageList = snapshot?.documents?.mapNotNull { document ->
                    val message = document.toObject(Message::class.java)
                        ?: return@mapNotNull null
                    val decryptedText = if (message.senderId != "system") {
                        EncryptionUtils.decrypt(message.text)
                    } else {
                        message.text
                    }
                    message.copy(text = decryptedText)
                } ?: emptyList()
                _messages.value = messageList
            }
    }

    fun getSenderName(senderId: String): String {
        val participant = _participants.value.find { it.userId == senderId }
        return participant?.name ?: "Usuário"
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            val messageId = UUID.randomUUID().toString()
            val senderName = getSenderName(currentUser.uid)

            val message = Message(
                messageId = messageId,
                senderId = currentUser.uid,
                text = EncryptionUtils.encrypt(text),
                senderName = senderName,
                status = mapOf(currentUser.uid to "read")
            )

            db.collection("chats").document(chatId).collection("messages").document(messageId)
                .set(message)
                .addOnSuccessListener {
                    db.collection("chats").document(chatId).update(
                        "lastMessage", EncryptionUtils.encrypt(text), // Criptografa a última mensagem
                        "lastMessageTimestamp", FieldValue.serverTimestamp()
                    )
                }
        }
    }

    fun pinMessage(message: Message) {
        val pinnedData = mapOf(
            "messageId" to message.messageId,
            "text" to message.text,
            "senderName" to message.senderName
        )
        db.collection("chats").document(chatId).update("pinnedMessage", pinnedData)
    }

    fun unpinMessage() {
        db.collection("chats").document(chatId).update("pinnedMessage", FieldValue.delete())
    }

    fun updateGroupName(newName: String) {
        if (newName.isNotBlank()) {
            db.collection("chats").document(chatId).update("chatName", newName)
        }
    }

    fun updateGroupImage(uri: Uri) {
        viewModelScope.launch {
            val storageRef = storage.reference.child("group_images/$chatId.jpg")
            try {
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                db.collection("chats").document(chatId).update("groupImageUrl", downloadUrl)
            } catch (e: Exception) {
                // Tratar erro
            }
        }
    }

    fun removeParticipant(userId: String) {
        db.collection("chats").document(chatId)
            .update("participants", FieldValue.arrayRemove(userId))
    }

    fun deleteGroup(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("chats").document(chatId).delete().await()
                onComplete()
            } catch (e: Exception) {
                // Tratar erro
            }
        }
    }
}

class ChatViewModelFactory(private val chatId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}