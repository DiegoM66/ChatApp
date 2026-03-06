package com.example.chatapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

    // Expõe o Flow do DataStore como um StateFlow para a UI observar
    val isDarkMode: StateFlow<Boolean> = settingsDataStore.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false // Valor inicial padrão
        )

    // Função chamada pela UI para alterar e salvar o tema
    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setTheme(isDark)
        }
    }
}