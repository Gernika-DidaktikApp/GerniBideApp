package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.models.PerfilProgresoResponse
import es.didaktikapp.gernikapp.data.models.UpdateUserRequest
import es.didaktikapp.gernikapp.data.models.UserResponse
import es.didaktikapp.gernikapp.data.models.UserStatsResponse
import es.didaktikapp.gernikapp.network.ApiService
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource

/**
 * Repository para operaciones de usuario.
 * Requiere sesión activa (token JWT).
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class UserRepository(context: Context) : BaseRepository(context) {

    private val apiService: ApiService = RetrofitClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    /**
     * Obtiene el perfil del usuario actual.
     * Utiliza el userId guardado en TokenManager.
     *
     * @return Resource con los datos del usuario
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
     *
     * @param userUpdate Datos a actualizar (campos opcionales)
     * @return Resource con los datos del usuario actualizado
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
     *
     * @param usuarioId ID del usuario
     * @return Resource con las estadísticas del usuario
     */
    suspend fun getUserStats(usuarioId: String): Resource<UserStatsResponse> {
        return safeApiCall(
            apiCall = { apiService.getUserStats(usuarioId) }
        )
    }

    /**
     * Obtiene el perfil completo del usuario con progreso detallado.
     * Incluye todas las actividades por módulo y sus estados.
     *
     * @return Resource con el perfil y progreso completo del usuario
     */
    suspend fun getPerfilProgreso(): Resource<PerfilProgresoResponse> {
        val userId = tokenManager.getUserId()

        if (userId == null) {
            return Resource.Error("No hay sesión activa")
        }

        return safeApiCall(
            apiCall = { apiService.getPerfilProgreso(userId) },
            onSuccess = { response ->
                LogManager.write(context, "Perfil y progreso obtenido correctamente")
                response
            }
        )
    }
}