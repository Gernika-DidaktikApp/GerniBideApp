package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import android.util.Log
import es.didaktikapp.gernikapp.BuildConfig
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
 * Maneja partidas y estados de evento.
 *
 * Flujo típico:
 * 1. Crear partida (crearPartida)
 * 2. Para cada evento:
 *    - Iniciar evento (iniciarEvento)
 *    - Completar evento (completarEvento)
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class GameRepository(context: Context) : BaseRepository(context) {

    private val apiService: ApiService = RetrofitClient.getApiService(context)

    companion object {
        private const val TAG = "GameRepository"
    }

    // ============ PARTIDAS ============

    /**
     * Crea una nueva partida para el usuario.
     *
     * @param idUsuario ID del usuario que inicia la partida
     * @return Resource con la partida creada
     */
    suspend fun crearPartida(idUsuario: String): Resource<PartidaResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🎮 CREAR PARTIDA - userId: $idUsuario")
        }

        val result = safeApiCall(
            apiCall = {
                val request = PartidaRequest(idUsuario = idUsuario)
                apiService.crearPartida(request)
            }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ CREAR PARTIDA - Éxito, juegoId: ${result.data.id}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ CREAR PARTIDA - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }

    /**
     * Obtiene una partida por su ID.
     *
     * @param partidaId ID de la partida
     * @return Resource con los datos de la partida
     */
    suspend fun getPartida(partidaId: String): Resource<PartidaResponse> {
        return safeApiCall(
            apiCall = { apiService.getPartida(partidaId) }
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
     * @return Resource con el estado del evento iniciado (guardar el ID para completarlo después)
     */
    suspend fun iniciarEvento(
        idJuego: String,
        idActividad: String,
        idEvento: String
    ): Resource<EventoEstadoResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🎯 INICIAR EVENTO - juegoId: $idJuego, actividadId: $idActividad, eventoId: $idEvento")
        }

        val result = safeApiCall(
            apiCall = {
                val request = EventoEstadoRequest(
                    idJuego = idJuego,
                    idActividad = idActividad,
                    idEvento = idEvento
                )
                apiService.iniciarEvento(request)
            }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ INICIAR EVENTO - Éxito, estadoEventoId: ${result.data.id}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ INICIAR EVENTO - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }

    /**
     * Completa un evento con su puntuación.
     * El servidor calcula automáticamente la duración.
     * Si es el último evento de la actividad, ésta se completa automáticamente.
     *
     * @param estadoId ID del estado del evento (obtenido al iniciar)
     * @param puntuacion Puntuación obtenida en el evento
     * @return Resource con el estado del evento completado
     */
    suspend fun completarEvento(
        estadoId: String,
        puntuacion: Double
    ): Resource<EventoEstadoResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🏁 COMPLETAR EVENTO - estadoId: $estadoId, puntuacion: $puntuacion")
        }

        val result = safeApiCall(
            apiCall = {
                val request = CompletarEventoRequest(puntuacion = puntuacion)
                apiService.completarEvento(estadoId, request)
            }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ COMPLETAR EVENTO - Éxito")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ COMPLETAR EVENTO - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }
}