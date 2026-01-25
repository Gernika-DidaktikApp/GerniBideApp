package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import android.util.Log
import es.didaktikapp.gernikapp.BuildConfig
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.HealthResponse
import es.didaktikapp.gernikapp.data.models.LoginRequest
import es.didaktikapp.gernikapp.data.models.LoginResponse
import es.didaktikapp.gernikapp.data.models.RegisterRequest
import es.didaktikapp.gernikapp.data.models.RegisterResponse
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource

/**
 * Repository para operaciones de autenticación.
 * Maneja login, registro, logout y gestión de sesión.
 */
class AuthRepository(context: Context) : BaseRepository(context) {

    private val tokenManager = TokenManager(context)

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Verifica si la API está disponible.
     */
    suspend fun healthCheck(): Resource<HealthResponse> {
        return safeApiCall(
            apiCall = { RetrofitClient.getApiService(context).healthCheck() }
        )
    }

    /**
     * Inicia sesión con username y password.
     * Guarda el token JWT si el login es exitoso.
     */
    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🔐 LOGIN - Iniciando login para usuario: $username")
        }

        val result = safeApiCall(
            apiCall = {
                val loginRequest = LoginRequest(
                    nombre = username,
                    contrasenya = password
                )
                RetrofitClient.getApiService(context).login(loginRequest)
            },
            onSuccess = { loginResponse ->
                // Guardar token y datos del usuario de la respuesta
                tokenManager.saveToken(loginResponse.accessToken, loginResponse.tokenType)
                tokenManager.saveUsername(loginResponse.username)
                tokenManager.saveUserId(loginResponse.userId)

                // Resetear RetrofitClient para que use el nuevo token
                RetrofitClient.reset()

                loginResponse
            }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ LOGIN - Éxito para usuario: $username")
                    tokenManager.logSessionState(TAG)
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ LOGIN - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }

    /**
     * Registra un nuevo usuario.
     */
    suspend fun register(
        username: String,
        nombre: String,
        apellido: String,
        password: String,
        claseId: String? = null
    ): Resource<RegisterResponse> {
        return safeApiCall(
            apiCall = {
                val registerRequest = RegisterRequest(
                    username = username,
                    nombre = nombre,
                    apellido = apellido,
                    password = password,
                    claseId = claseId
                )
                RetrofitClient.getApiService(context).register(registerRequest)
            }
        )
    }

    /**
     * Cierra la sesión actual.
     * Elimina el token y datos del usuario.
     */
    fun logout() {
        tokenManager.clearSession()
    }

    /**
     * Verifica si hay una sesión activa (token guardado).
     */
    fun hasActiveSession(): Boolean {
        return tokenManager.hasActiveSession()
    }

    /**
     * Obtiene el nombre de usuario de la sesión actual.
     */
    fun getUsername(): String? {
        return tokenManager.getUsername()
    }

    /**
     * Obtiene el token actual (para uso interno).
     */
    fun getToken(): String? {
        return tokenManager.getToken()
    }
}