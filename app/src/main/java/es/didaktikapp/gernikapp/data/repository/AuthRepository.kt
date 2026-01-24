package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.HealthResponse
import es.didaktikapp.gernikapp.data.models.LoginRequest
import es.didaktikapp.gernikapp.data.models.LoginResponse
import es.didaktikapp.gernikapp.data.models.RegisterRequest
import es.didaktikapp.gernikapp.data.models.RegisterResponse
import es.didaktikapp.gernikapp.network.ApiService
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource

/**
 * Repository para operaciones de autenticación.
 * Maneja login, registro, logout y gestión de sesión.
 */
class AuthRepository(context: Context) : BaseRepository(context) {

    private val apiService: ApiService = RetrofitClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    /**
     * Verifica si la API está disponible.
     */
    suspend fun healthCheck(): Resource<HealthResponse> {
        return safeApiCall(
            apiCall = { apiService.healthCheck() }
        )
    }

    /**
     * Inicia sesión con username y password.
     * Guarda el token JWT si el login es exitoso.
     */
    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return safeApiCall(
            apiCall = {
                val loginRequest = LoginRequest(
                    nombre = username,
                    contrasenya = password
                )
                apiService.login(loginRequest)
            },
            onSuccess = { loginResponse ->
                tokenManager.saveToken(loginResponse.accessToken, loginResponse.tokenType)
                tokenManager.saveUsername(username)
                loginResponse
            }
        )
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
                apiService.register(registerRequest)
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