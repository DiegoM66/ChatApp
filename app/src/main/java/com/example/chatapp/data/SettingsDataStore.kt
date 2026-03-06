package com.example.chatapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Cria uma instância do DataStore para o app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    // Chave para armazenar o valor booleano do tema escuro
    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    // Fluxo (Flow) que emite o estado atual do tema (true se escuro, false se claro)
    val theme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false // Retorna false (tema claro) como padrão
    }

    // Função para salvar a preferência do tema
    suspend fun setTheme(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDarkMode
        }
    }
}