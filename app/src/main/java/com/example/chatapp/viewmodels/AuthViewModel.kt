package com.example.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class para representar o estado da autenticação na UI
data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        // Verifica se o usuário já está logado ao iniciar a ViewModel
        if (auth.currentUser != null) {
            _authState.value = AuthState(isAuthenticated = true)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState(isAuthenticated = true)
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // Salva as informações adicionais do usuário no Firestore
                    val newUser = User(
                        userId = firebaseUser.uid,
                        name = name,
                        email = email,
                        status = "online"
                    )
                    db.collection("users").document(firebaseUser.uid).set(newUser).await()
                    _authState.value = AuthState(isAuthenticated = true)
                } else {
                    _authState.value = AuthState(error = "Falha ao criar usuário.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState(isAuthenticated = false)
    }
}