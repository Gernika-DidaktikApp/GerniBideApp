package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.UpdateUserRequest
import es.didaktikapp.gernikapp.data.models.UserResponse
import es.didaktikapp.gernikapp.data.models.UserStatsResponse
import es.didaktikapp.gernikapp.network.ApiService
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource

/**
 * Repository para operaciones de usuario.
 * Requiere sesión activa (token JWT).
 */
class UserRepository(context: Context) : BaseRepository(context) {

    private val apiService: ApiService = RetrofitClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    /**
     * Obtiene el perfil del usuario actual.
     * Utiliza el userId guardado en TokenManager.
     */
    suspend fun getUserProfile(): Resource<UserResponse> {
        val userId = tokenManager.getUserId()
            ?: return Resource.Error("No se encontró el ID de usuario")

        return safeApiCall(
            apiCall = { apiService.getUserProfile(userId) }
        )
    }

    /**
     * Actualiza el perfil del usuario actual.
     * Solo envía los campos que se desean actualizar.
     * Utiliza el userId guardado en TokenManager.
     */
    suspend fun updateUserProfile(userUpdate: UpdateUserRequest): Resource<UserResponse> {
        val userId = tokenManager.getUserId()
            ?: return Resource.Error("No se encontró el ID de usuario")

        return safeApiCall(
            apiCall = { apiService.updateUserProfile(userId, userUpdate) }
        )
    }

    /**
     * Obtiene las estadísticas de un usuario.
     */
    suspend fun getUserStats(usuarioId: String): Resource<UserStatsResponse> {
        return safeApiCall(
            apiCall = { apiService.getUserStats(usuarioId) }
        )
    }
}