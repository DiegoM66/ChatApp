package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.models.Chat
import com.example.chatapp.models.User
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUserId = auth.currentUser?.uid

    // Mantém a lista de contatos que o usuário ADICIONOU
    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts = _contacts.asStateFlow()

    // NOVO: Mantém uma lista de TODOS os usuários do app para busca de nomes/fotos
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

    private val _searchedUser = MutableStateFlow<User?>(null)
    val searchedUser = _searchedUser.asStateFlow()

    private val _searchResultMsg = MutableStateFlow<String?>(null)
    val searchResultMsg = _searchResultMsg.asStateFlow()

    init {
        loadContacts()
        // NOVO: Carrega todos os usuários em tempo real
        fetchAllUsers()
    }

    private fun loadContacts() {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                val contactIds = snapshot?.get("contacts") as? List<String>
                if (contactIds.isNullOrEmpty()) {
                    _contacts.value = emptyList()
                } else {
                    // Busca os documentos dos usuários que estão na lista de contatos
                    db.collection("users").whereIn("userId", contactIds)
                        .addSnapshotListener { documents, _ ->
                            if (documents != null) {
                                _contacts.value = documents.toObjects(User::class.java)
                            }
                        }
                }
            }
    }

    // NOVA FUNÇÃO: Busca todos os usuários para popular a lista allUsers
    private fun fetchAllUsers() {
        if (currentUserId == null) return
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                // Idealmente, tratar o erro aqui
                return@addSnapshotListener
            }
            _allUsers.value = snapshot.toObjects(User::class.java)
        }
    }

    fun searchUserByEmail(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _searchResultMsg.value = "Por favor, insira um email."
                return@launch
            }
            if (email.trim().lowercase() == auth.currentUser?.email) {
                _searchResultMsg.value = "Você não pode adicionar a si mesmo."
                _searchedUser.value = null
                return@launch
            }

            val query = db.collection("users")
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                _searchResultMsg.value = "Nenhum usuário encontrado com este email."
                _searchedUser.value = null
            } else {
                val foundUser = query.documents.first().toObject(User::class.java)
                _searchedUser.value = foundUser
                _searchResultMsg.value = null
            }
        }
    }

    fun addContact(contactToAdd: User) {
        if (currentUserId == null) return
        viewModelScope.launch {
            db.collection("users").document(currentUserId)
                .update("contacts", FieldValue.arrayUnion(contactToAdd.userId))
                .await()
            _searchResultMsg.value = "'${contactToAdd.name}' foi adicionado aos seus contatos."
            _searchedUser.value = null
        }
    }

    fun createChatWith(contact: User, onChatCreated: (chatId: String, chatName: String) -> Unit) {
        if (currentUserId == null) return
        viewModelScope.launch {
            try {
                // Garante uma ordem consistente para o ID do chat 1-para-1
                val chatId = if (currentUserId > contact.userId) "$currentUserId-${contact.userId}" else "${contact.userId}-$currentUserId"
                val chatRef = db.collection("chats").document(chatId)
                val chatDoc = chatRef.get().await()

                if (!chatDoc.exists()) {
                    val newChat = Chat(
                        chatId = chatId,
                        chatName = contact.name, // Nome inicial do chat
                        participants = listOf(currentUserId, contact.userId),
                        group = false,
                        lastMessage = "Conversa iniciada.",
                        createdBy = currentUserId
                    )
                    chatRef.set(newChat).await()
                }
                // O nome passado para a navegação é o nome atual do contato
                onChatCreated(chatId, contact.name)
            } catch (e: Exception) {
                _searchResultMsg.value = "Erro ao criar conversa: ${e.message}"
            }
        }
    }

    fun clearSearchResultMessage() {
        _searchResultMsg.value = null
    }
}