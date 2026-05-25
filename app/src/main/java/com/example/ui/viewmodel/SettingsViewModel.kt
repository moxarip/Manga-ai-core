package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.BuildConfig
import com.example.data.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _apiKey = MutableStateFlow(securityManager.getGeminiApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey
    
    val fallbackApiKey: String = BuildConfig.GEMINI_API_KEY

    fun saveApiKey(key: String) {
        securityManager.saveGeminiApiKey(key)
        _apiKey.value = key
    }
}
