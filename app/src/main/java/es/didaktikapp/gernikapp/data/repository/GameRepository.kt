package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import es.didaktikapp.gernikapp.data.models.ActividadEstadoRequest
import es.didaktikapp.gernikapp.data.models.ActividadEstadoResponse
import es.didaktikapp.gernikapp.data.models.CompletarEventoRequest
import es.didaktikapp.gernikapp.data.models.EventoEstadoRequest
import es.didaktikapp.gernikapp.data.models.EventoEstadoResponse
import es.didaktikapp.gernikapp.data.models.PartidaRequest
import es.didaktikapp.gernikapp.data.models.PartidaResponse
import es.didaktikapp.gernikapp.network.ApiService
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource

/**
 * Repository para operaciones del juego.
 * Maneja partidas, estados de actividad y estados de evento.
 *
 * Flujo típico:
 * 1. Crear partida (crearPartida)
 * 2. Iniciar actividad (iniciarActividad)
 * 3. Para cada evento de la actividad:
 *    - Iniciar evento (iniciarEvento)
 *    - Completar evento (completarEvento)
 * 4. Al completar el último evento, la actividad se completa automáticamente
 */
class GameRepository(context: Context) : BaseRepository(context) {

    private val apiService: ApiService = RetrofitClient.getApiService(context)

    // ============ PARTIDAS ============

    /**
     * Crea una nueva partida para el usuario.
     * @param idUsuario ID del usuario que inicia la partida
     */
    suspend fun crearPartida(idUsuario: String): Resource<PartidaResponse> {
        return safeApiCall(
            apiCall = {
                val request = PartidaRequest(idUsuario = idUsuario)
                apiService.crearPartida(request)
            }
        )
    }

    /**
     * Obtiene una partida por su ID.
     * @param partidaId ID de la partida
     */
    suspend fun getPartida(partidaId: String): Resource<PartidaResponse> {
        return safeApiCall(
            apiCall = { apiService.getPartida(partidaId) }
        )
    }

    // ============ ESTADOS DE ACTIVIDAD ============

    /**
     * Inicia una actividad para un jugador.
     * El servidor registra automáticamente la fecha de inicio.
     *
     * @param idJuego ID de la partida (juego)
     * @param idActividad ID de la actividad a iniciar
     * @return Estado de la actividad iniciada
     */
    suspend fun iniciarActividad(
        idJuego: String,
        idActividad: String
    ): Resource<ActividadEstadoResponse> {
        return safeApiCall(
            apiCall = {
                val request = ActividadEstadoRequest(
                    idJuego = idJuego,
                    idActividad = idActividad
                )
                apiService.iniciarActividad(request)
            }
        )
    }

    /**
     * Obtiene el estado actual de una actividad.
     * Útil para verificar si la actividad se ha completado automáticamente.
     *
     * @param estadoId ID del estado de la actividad
     */
    suspend fun getActividadEstado(estadoId: String): Resource<ActividadEstadoResponse> {
        return safeApiCall(
            apiCall = { apiService.getActividadEstado(estadoId) }
        )
    }

    // ============ ESTADOS DE EVENTO ============

    /**
     * Inicia un evento dentro de una actividad.
     * El servidor registra automáticamente la fecha de inicio.
     *
     * @param idJuego ID de la partida (juego)
     * @param idActividad ID de la actividad
     * @param idEvento ID del evento a iniciar
     * @return Estado del evento iniciado (guardar el ID para completarlo después)
     */
    suspend fun iniciarEvento(
        idJuego: String,
        idActividad: String,
        idEvento: String
    ): Resource<EventoEstadoResponse> {
        return safeApiCall(
            apiCall = {
                val request = EventoEstadoRequest(
                    idJuego = idJuego,
                    idActividad = idActividad,
                    idEvento = idEvento
                )
                apiService.iniciarEvento(request)
            }
        )
    }

    /**
     * Completa un evento con su puntuación.
     * El servidor calcula automáticamente la duración.
     * Si es el último evento de la actividad, ésta se completa automáticamente.
     *
     * @param estadoId ID del estado del evento (obtenido al iniciar)
     * @param puntuacion Puntuación obtenida en el evento
     * @return Estado del evento completado
     */
    suspend fun completarEvento(
        estadoId: String,
        puntuacion: Double
    ): Resource<EventoEstadoResponse> {
        return safeApiCall(
            apiCall = {
                val request = CompletarEventoRequest(puntuacion = puntuacion)
                apiService.completarEvento(estadoId, request)
            }
        )
    }
}