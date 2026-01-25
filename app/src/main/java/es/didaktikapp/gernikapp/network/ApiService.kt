package es.didaktikapp.gernikapp.network

import es.didaktikapp.gernikapp.ApiConfig
import es.didaktikapp.gernikapp.data.models.CompletarEventoRequest
import es.didaktikapp.gernikapp.data.models.EventoEstadoRequest
import es.didaktikapp.gernikapp.data.models.EventoEstadoResponse
import es.didaktikapp.gernikapp.data.models.HealthResponse
import es.didaktikapp.gernikapp.data.models.LoginRequest
import es.didaktikapp.gernikapp.data.models.LoginResponse
import es.didaktikapp.gernikapp.data.models.PartidaRequest
import es.didaktikapp.gernikapp.data.models.PartidaResponse
import es.didaktikapp.gernikapp.data.models.RegisterRequest
import es.didaktikapp.gernikapp.data.models.RegisterResponse
import es.didaktikapp.gernikapp.data.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface de Retrofit para los endpoints de la API.
 * Cada método corresponde a un endpoint definido en ApiConfig.
 */
interface ApiService {

    // ============ HEALTH CHECK ============

    /**
     * Verifica que la API está funcionando.
     * No requiere autenticación.
     */
    @GET(ApiConfig.HEALTH)
    suspend fun healthCheck(): Response<HealthResponse>

    // ============ AUTENTICACIÓN ============

    /**
     * Login de usuario.
     * Devuelve un token JWT para usar en peticiones autenticadas.
     */
    @POST(ApiConfig.AUTH_LOGIN)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    /**
     * Registro de nuevo usuario.
     * Devuelve los datos del usuario creado.
     */
    @POST(ApiConfig.AUTH_REGISTER)
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>

    // ============ USUARIO ============

    /**
     * Obtiene el perfil del usuario actual.
     * Requiere autenticación (token JWT).
     */
    @GET(ApiConfig.USER_PROFILE)
    suspend fun getUserProfile(): Response<UserResponse>

    /**
     * Actualiza el perfil del usuario actual.
     * Requiere autenticación (token JWT).
     */
    @PUT(ApiConfig.USER_UPDATE)
    suspend fun updateUserProfile(
        @Body userUpdate: UserResponse
    ): Response<UserResponse>

    // ============ PARTIDAS ============

    /**
     * Crea una nueva partida para el usuario.
     */
    @POST(ApiConfig.PARTIDAS_CREATE)
    suspend fun crearPartida(
        @Body request: PartidaRequest
    ): Response<PartidaResponse>

    /**
     * Obtiene una partida por su ID.
     */
    @GET(ApiConfig.PARTIDAS_GET)
    suspend fun getPartida(
        @Path("id") partidaId: String
    ): Response<PartidaResponse>

    // ============ ESTADOS DE EVENTO ============

    /**
     * Inicia un evento dentro de una actividad.
     * Registra automáticamente la fecha de inicio.
     */
    @POST(ApiConfig.EVENTO_ESTADO_INICIAR)
    suspend fun iniciarEvento(
        @Body request: EventoEstadoRequest
    ): Response<EventoEstadoResponse>

    /**
     * Completa un evento con su puntuación.
     * Calcula automáticamente la duración.
     * Si es el último evento, la actividad se completa automáticamente.
     */
    @PUT(ApiConfig.EVENTO_ESTADO_COMPLETAR)
    suspend fun completarEvento(
        @Path("estado_id") estadoId: String,
        @Body request: CompletarEventoRequest
    ): Response<EventoEstadoResponse>
}