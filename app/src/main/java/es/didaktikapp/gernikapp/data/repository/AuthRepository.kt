package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import android.util.Log
import es.didaktikapp.gernikapp.BuildConfig
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.HealthResponse
import es.didaktikapp.gernikapp.data.models.LoginRequest
import es.didaktikapp.gernikapp.data.models.LoginResponse
import es.didaktikapp.gernikapp.data.models.RegisterRequest
import es.didaktikapp.gernikapp.data.models.RegisterResponse
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.SyncManager

/**
 * Repository para operaciones de autenticación.
 * Maneja login, registro, logout y gestión de sesión.
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class AuthRepository(context: Context) : BaseRepository(context) {

    private val tokenManager = TokenManager(context)

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Verifica si la API está disponible.
     *
     * @return Resource con el estado de salud de la API
     */
    suspend fun healthCheck(): Resource<HealthResponse> {
        return safeApiCall(
            apiCall = { RetrofitClient.getApiService(context).healthCheck() }
        )
    }

    /**
     * Inicia sesión con username y password.
     * Guarda el token JWT si el login es exitoso.
     *
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return Resource con los datos de la sesión
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
     *
     * @param username Nombre de usuario único
     * @param nombre Nombre del usuario
     * @param apellido Apellido del usuario
     * @param password Contraseña
     * @param claseId ID de la clase (opcional)
     * @return Resource con los datos del usuario creado
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
     * Elimina el token, datos del usuario, progreso local y TODAS las configuraciones.
     */
    fun logout() {
        // 1. Limpiar sesión (token, userId, juegoId)
        tokenManager.clearSession()

        // 2. Limpiar progreso de módulos
        SyncManager.clearAllProgress(context)

        // 3. Limpiar configuraciones de la app
        context.getSharedPreferences("GernikAppSettings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🧹 Logout completo - Todos los SharedPreferences limpiados")
        }

        LogManager.write(context, "Sesión cerrada y TODOS los datos locales limpiados")
    }

    /**
     * Verifica si hay una sesión activa (token guardado).
     *
     * @return true si hay sesión activa, false en caso contrario
     */
    fun hasActiveSession(): Boolean {
        return tokenManager.hasActiveSession()
    }

    /**
     * Obtiene el nombre de usuario de la sesión actual.
     *
     * @return Nombre de usuario o null si no hay sesión
     */
    fun getUsername(): String? {
        return tokenManager.getUsername()
    }

    /**
     * Obtiene el token actual (para uso interno).
     *
     * @return Token JWT o null si no hay sesión
     */
    fun getToken(): String? {
        return tokenManager.getToken()
    }
}