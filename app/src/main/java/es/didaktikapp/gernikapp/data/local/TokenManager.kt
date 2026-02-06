package es.didaktikapp.gernikapp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import es.didaktikapp.gernikapp.BuildConfig
import org.json.JSONObject

/**
 * Gestor de tokens JWT y datos de sesión del usuario.
 * Usa EncryptedSharedPreferences para almacenamiento seguro.
 *
 * Datos gestionados:
 * - Token JWT de autenticación
 * - Username y userId
 * - JuegoId de la partida activa
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = createEncryptedPreferences(context)

    /**
     * Crea EncryptedSharedPreferences con manejo de errores.
     * Si hay corrupción, borra y recrea.
     *
     * @param context Contexto de la aplicación
     * @return SharedPreferences encriptadas
     */
    private fun createEncryptedPreferences(context: Context): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Si hay datos corruptos, borrar el archivo y reintentar
            Log.w("TokenManager", "EncryptedSharedPreferences corrupto, recreando...", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            try {
                // Borrar el archivo físico
                val prefsFile = java.io.File(context.filesDir.parent, "shared_prefs/$PREFS_NAME.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }
            } catch (ignored: Exception) {}

            // Reintentar creación
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_USERNAME = "username"
        private const val KEY_JUEGO_ID = "juego_id"
        private const val KEY_USER_ID = "user_id"
    }

    /**
     * Guarda el token JWT y extrae el userId automáticamente.
     * Usa commit() para guardado síncrono.
     *
     * @param token Token JWT recibido del servidor
     * @param tokenType Tipo de token (por defecto "bearer")
     */
    fun saveToken(token: String, tokenType: String = "bearer") {
        Log.d("TokenManager", "💾 Guardando token: ${token.take(20)}...")

        // Usar commit() en lugar de apply() para guardado síncrono
        val success = sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .commit()

        Log.d("TokenManager", "💾 Token commit result: $success")

        // Extraer userId del JWT y guardarlo
        extractUserIdFromToken(token)?.let { userId ->
            saveUserId(userId)
        }

        // Verificar que se guardó
        val saved = sharedPreferences.getString(KEY_TOKEN, null)
        Log.d("TokenManager", "✅ Token guardado correctamente: ${saved != null}")
    }

    /**
     * Extrae el userId del payload del JWT.
     * El JWT tiene formato: header.payload.signature
     * El payload es base64 encoded y contiene el campo "sub" con el userId.
     *
     * @param token Token JWT
     * @return userId extraído o null si falla
     */
    private fun extractUserIdFromToken(token: String): String? {
        return try {
            val payload = decodeJwtPayload(token) ?: return null
            payload.optString("sub", null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodifica el payload del JWT y lo devuelve como JSONObject.
     *
     * @param token Token JWT
     * @return JSONObject con el payload o null si falla
     */
    private fun decodeJwtPayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            // Agregar padding si es necesario
            val paddedPayload = when (payload.length % 4) {
                2 -> "$payload=="
                3 -> "$payload="
                else -> payload
            }

            val decodedBytes = Base64.decode(paddedPayload, Base64.URL_SAFE)
            val decodedPayload = String(decodedBytes, Charsets.UTF_8)
            JSONObject(decodedPayload)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifica si el token JWT ha expirado.
     * @return true si el token ha expirado o no existe, false si es válido
     */
    fun isTokenExpired(): Boolean {
        val token = getToken() ?: return true

        return try {
            val payload = decodeJwtPayload(token) ?: return true
            val exp = payload.optLong("exp", 0)

            if (exp == 0L) return true

            val currentTimeSeconds = System.currentTimeMillis() / 1000
            val isExpired = currentTimeSeconds > exp

            if (BuildConfig.DEBUG) {
                val remainingSeconds = exp - currentTimeSeconds
                val remainingHours = remainingSeconds / 3600
                val remainingDays = remainingHours / 24
                Log.d("TokenManager", "⏰ Token expira en: ${remainingDays}d ${remainingHours % 24}h (exp: $exp, now: $currentTimeSeconds)")
            }

            isExpired
        } catch (e: Exception) {
            Log.e("TokenManager", "Error verificando expiración del token", e)
            true
        }
    }

    /**
     * Obtiene el token JWT guardado.
     *
     * @return Token JWT o null si no existe
     */
    fun getToken(): String? {
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        Log.d("TokenManager", "📖 getToken() llamado - Token presente: ${token != null}")
        return token
    }

    /**
     * Guarda el nombre de usuario.
     *
     * @param username Nombre de usuario
     */
    fun saveUsername(username: String) {
        sharedPreferences.edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    /**
     * Obtiene el nombre de usuario guardado.
     *
     * @return Nombre de usuario o null
     */
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    /**
     * Guarda el ID del usuario.
     *
     * @param userId ID del usuario
     */
    fun saveUserId(userId: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    /**
     * Obtiene el ID del usuario guardado.
     *
     * @return ID del usuario o null
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    /**
     * Guarda el ID de la partida activa.
     *
     * @param juegoId ID de la partida
     */
    fun saveJuegoId(juegoId: String) {
        sharedPreferences.edit()
            .putString(KEY_JUEGO_ID, juegoId)
            .apply()
    }

    /**
     * Obtiene el ID de la partida activa.
     *
     * @return ID de la partida o null
     */
    fun getJuegoId(): String? {
        return sharedPreferences.getString(KEY_JUEGO_ID, null)
    }

    /**
     * Verifica si hay una partida activa.
     *
     * @return true si hay partida activa, false en caso contrario
     */
    fun hasActiveGame(): Boolean {
        return getJuegoId() != null
    }

    /**
     * Limpia toda la sesión (token, username, userId, juegoId).
     */
    fun clearSession() {
        sharedPreferences.edit().clear().commit()
        Log.d("TokenManager", "🗑️ Sesión limpiada")
    }

    /**
     * Verifica si hay una sesión activa con un token válido (no expirado).
     * Si el token ha expirado, limpia la sesión automáticamente.
     */
    fun hasActiveSession(): Boolean {
        val token = getToken() ?: return false

        if (isTokenExpired()) {
            Log.w("TokenManager", "⚠️ Token expirado, limpiando sesión...")
            clearSession()
            return false
        }

        return true
    }

    /**
     * Imprime el estado de la sesión en Logcat (solo en DEBUG).
     * Filtra en Logcat por tag: "TokenManager"
     *
     * @param tag Tag para los logs (por defecto "TokenManager")
     */
    fun logSessionState(tag: String = "TokenManager") {
        if (!BuildConfig.DEBUG) return

        val token = getToken()
        val tokenPreview = token?.let {
            if (it.length > 20) "${it.take(10)}...${it.takeLast(10)}" else it
        } ?: "null"

        Log.d(tag, "═══════════════════════════════════════════")
        Log.d(tag, "📱 ESTADO DE SESIÓN")
        Log.d(tag, "═══════════════════════════════════════════")
        Log.d(tag, "🔑 Token: $tokenPreview")
        Log.d(tag, "👤 Username: ${getUsername() ?: "null"}")
        Log.d(tag, "🆔 UserId: ${getUserId() ?: "null"}")
        Log.d(tag, "🎮 JuegoId: ${getJuegoId() ?: "null"}")
        Log.d(tag, "✅ Sesión activa: ${hasActiveSession()}")
        Log.d(tag, "🎯 Partida activa: ${hasActiveGame()}")
        Log.d(tag, "═══════════════════════════════════════════")
    }
}