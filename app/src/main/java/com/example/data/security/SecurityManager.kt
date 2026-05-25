package com.example.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityManager(context: Context) {
    private val sharedPrefs = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "manga_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for emulators with broken KeyStore
        context.getSharedPreferences("manga_secure_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun saveGeminiApiKey(key: String) {
        sharedPrefs.edit().putString(KEY_GEMINI_API, key).apply()
    }

    fun getGeminiApiKey(): String? {
        return sharedPrefs.getString(KEY_GEMINI_API, null)
    }

    companion object {
        private const val KEY_GEMINI_API = "gemini_api_key"
    }
}
