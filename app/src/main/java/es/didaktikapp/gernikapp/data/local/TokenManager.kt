package es.didaktikapp.gernikapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_USERNAME = "username"
    }

    fun saveToken(token: String, tokenType: String = "bearer") {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun hasActiveSession(): Boolean {
        return getToken() != null
    }
}