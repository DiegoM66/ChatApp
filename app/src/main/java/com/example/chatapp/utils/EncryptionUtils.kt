// NOVO ARQUIVO: EncryptionUtils.kt
package com.example.chatapp.utils

import android.util.Base64

object EncryptionUtils {
    // ATENÇÃO: Este é um exemplo de criptografia MUITO BÁSICO apenas para fins didáticos.
    // NÃO USE EM PRODUÇÃO. Use bibliotecas criptográficas seguras como a Tink do Google.

    fun encrypt(text: String): String {
        return Base64.encodeToString(text.toByteArray(), Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        return try {
            String(Base64.decode(encryptedText, Base64.DEFAULT))
        } catch (e: IllegalArgumentException) {
            // Se o texto não for um Base64 válido (ex: mensagens antigas não criptografadas)
            encryptedText
        }
    }
}