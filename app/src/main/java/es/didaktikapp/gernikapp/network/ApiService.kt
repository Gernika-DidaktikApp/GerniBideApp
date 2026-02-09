package es.didaktikapp.gernikapp.network

import es.didaktikapp.gernikapp.ApiConfig
import es.didaktikapp.gernikapp.data.models.ActividadProgresoRequest
import es.didaktikapp.gernikapp.data.models.ActividadProgresoResponse
import es.didaktikapp.gernikapp.data.models.CompletarActividadRequest
import es.didaktikapp.gernikapp.data.models.HealthResponse
import es.didaktikapp.gernikapp.data.models.LoginRequest
import es.didaktikapp.gernikapp.data.models.LoginResponse
import es.didaktikapp.gernikapp.data.models.PartidaRequest
import es.didaktikapp.gernikapp.data.models.PartidaResponse
import es.didaktikapp.gernikapp.data.models.RegisterRequest
import es.didaktikapp.gernikapp.data.models.RegisterResponse
import es.didaktikapp.gernikapp.data.models.UpdateUserRequest
import es.didaktikapp.gernikapp.data.models.UserResponse
import es.didaktikapp.gernikapp.data.models.UserStatsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interface de Retrofit para los endpoints de la API.
 * Cada método corresponde a un endpoint definido en [ApiConfig].
 *
 * @author Wara Pacheco
 * @version 1.0
 * @see ApiConfig
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
     * Obtiene el perfil de un usuario.
     * Requiere autenticación (token JWT).
     */
    @GET(ApiConfig.USER_PROFILE)
    suspend fun getUserProfile(
        @Path("usuario_id") usuarioId: String
    ): Response<UserResponse>

    /**
     * Actualiza el perfil de un usuario.
     * Requiere autenticación (token JWT).
     * Solo envía los campos que se desean actualizar.
     */
    @PUT(ApiConfig.USER_UPDATE)
    suspend fun updateUserProfile(
        @Path("usuario_id") usuarioId: String,
        @Body userUpdate: UpdateUserRequest
    ): Response<UserResponse>

    /**
     * Obtiene las estadísticas de un usuario.
     * Requiere autenticación (token JWT).
     * Con Token: Solo puede ver sus propias estadísticas.
     */
    @GET(ApiConfig.USER_STATS)
    suspend fun getUserStats(
        @Path("usuario_id") usuarioId: String
    ): Response<UserStatsResponse>

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

    /**
     * Obtiene la partida activa del usuario, o crea una nueva si no existe.
     * Este es el endpoint RECOMENDADO para evitar errores 400 por partidas duplicadas.
     *
     * @param usuarioId ID del usuario
     * @return Response con la partida activa (existente o nueva)
     */
    @POST(ApiConfig.PARTIDAS_OBTENER_O_CREAR)
    suspend fun obtenerOCrearPartidaActiva(
        @Path("usuario_id") usuarioId: String
    ): Response<PartidaResponse>

    // ============ PROGRESO DE ACTIVIDADES ============

    /**
     * Inicia una actividad dentro de un punto.
     * Registra automáticamente la fecha de inicio.
     *
     * @param request Datos de la actividad a iniciar (id_juego, id_punto, id_actividad)
     * @return Response con el progreso de la actividad iniciada
     */
    @POST(ApiConfig.ACTIVIDAD_PROGRESO_INICIAR)
    suspend fun iniciarActividad(
        @Body request: ActividadProgresoRequest
    ): Response<ActividadProgresoResponse>

    /**
     * Completa una actividad con su puntuación y respuesta.
     * Calcula automáticamente la duración.
     *
     * @param progresoId ID del progreso de la actividad (obtenido al iniciar)
     * @param request Datos de completación (puntuacion, respuesta_contenido, etc.)
     * @return Response con el progreso de la actividad completada
     */
    @PUT(ApiConfig.ACTIVIDAD_PROGRESO_COMPLETAR)
    suspend fun completarActividad(
        @Path("progreso_id") progresoId: String,
        @Body request: CompletarActividadRequest
    ): Response<ActividadProgresoResponse>

    /**
     * Obtiene un progreso de actividad por su ID.
     * Requiere autenticación (token JWT).
     *
     * @param progresoId ID del progreso de la actividad
     * @return Response con los datos del progreso
     */
    @GET(ApiConfig.ACTIVIDAD_PROGRESO_GET)
    suspend fun getActividadProgreso(
        @Path("progreso_id") progresoId: String
    ): Response<ActividadProgresoResponse>

    /**
     * Actualiza un progreso de actividad existente.
     * RESTRICCIÓN: Solo se puede actualizar respuesta_contenido si la actividad está completada.
     * Requiere autenticación (token JWT).
     *
     * @param progresoId ID del progreso de la actividad
     * @param request Datos a actualizar (respuesta_contenido, puntuacion, etc.)
     * @return Response con el progreso de la actividad actualizada
     */
    @PUT(ApiConfig.ACTIVIDAD_PROGRESO_UPDATE)
    suspend fun actualizarActividad(
        @Path("progreso_id") progresoId: String,
        @Body request: CompletarActividadRequest
    ): Response<ActividadProgresoResponse>
}