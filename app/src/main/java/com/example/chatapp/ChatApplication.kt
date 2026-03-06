// chatapplication.kt (renomeie para seguir a convenção Kotlin)

package com.example.chatapp

import android.app.Application
import androidx.emoji2.text.EmojiCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.Firebase

class ChatApplication : Application() { // Renomeado de chatapplication
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Habilitar persistência offline do Firestore
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        Firebase.firestore.firestoreSettings = settings

        // Inicializar EmojiCompat
        EmojiCompat.init(this)
    }
}