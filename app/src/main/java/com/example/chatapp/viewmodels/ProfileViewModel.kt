package com.example.chatapp.viewmodels

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.models.User
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

// NOVO: Classe para gerenciar os diferentes estados da UI
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = ProfileUiState.Error("Usuário não autenticado.")
            return
        }

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = ProfileUiState.Error("Falha ao carregar perfil.")
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    _uiState.value = ProfileUiState.Success(user)
                } else {
                    _uiState.value = ProfileUiState.Error("Perfil não encontrado.")
                }
            }
    }

    fun updateProfile(name: String, status: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val updates = mapOf("name" to name, "status" to status)
            db.collection("users").document(userId).update(updates).await()
        }
    }

    // NOVA FUNÇÃO: Atualiza a foto usando Base64
    fun updateProfileImageAsBase64(uri: Uri, contentResolver: ContentResolver) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // 1. Carrega a imagem a partir da Uri
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }

                // 2. Comprime a imagem para um tamanho pequeno (crucial!)
                val resizedBitmap = resizeBitmap(bitmap, 300) // Reduz para 300x300 pixels
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Comprime com 50% de qualidade
                val byteArray = outputStream.toByteArray()

                // 3. Converte para Base64
                val base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

                // 4. Salva a string Base64 no Firestore
                db.collection("users").document(userId).update("profileImage", base64Image).await()

            } catch (e: Exception) {
                // Tratar erro (ex: imagem muito grande, falha na conversão)
            }
        }
    }

    // Função auxiliar para redimensionar o bitmap
    private fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        return try {
            if (source.height >= source.width) {
                if (source.height <= maxLength) return source
                val newHeight = maxLength
                val newWidth = (source.width * (newHeight.toFloat() / source.height)).toInt()
                Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
            } else {
                if (source.width <= maxLength) return source
                val newWidth = maxLength
                val newHeight = (source.height * (newWidth.toFloat() / source.width)).toInt()
                Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
            }
        } catch (e: Exception) {
            source
        }
    }

    fun logout() {
        auth.signOut()
    }
}