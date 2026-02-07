package es.didaktikapp.gernikapp.data.repository

import android.content.Context
import android.util.Log
import es.didaktikapp.gernikapp.BuildConfig
import es.didaktikapp.gernikapp.data.models.ActividadProgresoRequest
import es.didaktikapp.gernikapp.data.models.ActividadProgresoResponse
import es.didaktikapp.gernikapp.data.models.CompletarActividadRequest
import es.didaktikapp.gernikapp.data.models.PartidaRequest
import es.didaktikapp.gernikapp.data.models.PartidaResponse
import es.didaktikapp.gernikapp.network.ApiService
import es.didaktikapp.gernikapp.network.RetrofitClient
import es.didaktikapp.gernikapp.utils.Resource

/**
 * Repository para operaciones del juego.
 * Maneja partidas y progreso de actividades.
 *
 * Flujo típico:
 * 1. Crear partida (crearPartida)
 * 2. Para cada actividad de un punto:
 *    - Iniciar actividad (iniciarActividad)
 *    - Completar actividad (completarActividad)
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

    // ============ PROGRESO DE ACTIVIDADES ============

    /**
     * Inicia una actividad dentro de un punto.
     * El servidor registra automáticamente la fecha de inicio.
     *
     * @param idJuego ID de la partida (juego)
     * @param idPunto ID del punto (antes llamado "actividad")
     * @param idActividad ID de la actividad (antes llamado "evento")
     * @return Resource con el progreso de la actividad iniciada (guardar el ID para completarla después)
     */
    suspend fun iniciarActividad(
        idJuego: String,
        idPunto: String,
        idActividad: String
    ): Resource<ActividadProgresoResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🎯 INICIAR ACTIVIDAD - juegoId: $idJuego, puntoId: $idPunto, actividadId: $idActividad")
        }

        val result = safeApiCall(
            apiCall = {
                val request = ActividadProgresoRequest(
                    idJuego = idJuego,
                    idPunto = idPunto,
                    idActividad = idActividad
                )
                apiService.iniciarActividad(request)
            }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ INICIAR ACTIVIDAD - Éxito, progresoId: ${result.data.id}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ INICIAR ACTIVIDAD - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }

    /**
     * Completa una actividad con su puntuación y respuesta.
     * El servidor calcula automáticamente la duración.
     *
     * @param progresoId ID del progreso de la actividad (obtenido al iniciar)
     * @param puntuacion Puntuación obtenida en la actividad
     * @param respuestaContenido Contenido de la respuesta del usuario (texto, URL de imagen, etc.)
     * @return Resource con el progreso de la actividad completada
     */
    suspend fun completarActividad(
        progresoId: String,
        puntuacion: Double,
        respuestaContenido: String? = null
    ): Resource<ActividadProgresoResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🏁 COMPLETAR ACTIVIDAD - progresoId: $progresoId, puntuacion: $puntuacion, respuesta: $respuestaContenido")
        }

        val result = safeApiCall(
            apiCall = {
                val request = CompletarActividadRequest(
                    puntuacion = puntuacion,
                    respuestaContenido = respuestaContenido
                )
                apiService.completarActividad(progresoId, request)
            }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ COMPLETAR ACTIVIDAD - Éxito")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ COMPLETAR ACTIVIDAD - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }

    /**
     * Obtiene el progreso de una actividad por su ID.
     *
     * @param progresoId ID del progreso de la actividad
     * @return Resource con los datos del progreso
     */
    suspend fun getActividadProgreso(progresoId: String): Resource<ActividadProgresoResponse> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "📊 GET ACTIVIDAD PROGRESO - progresoId: $progresoId")
        }

        val result = safeApiCall(
            apiCall = { apiService.getActividadProgreso(progresoId) }
        )

        if (BuildConfig.DEBUG) {
            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ GET ACTIVIDAD PROGRESO - Éxito, respuesta: ${result.data.respuestaContenido}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ GET ACTIVIDAD PROGRESO - Error: ${result.message} (code: ${result.code})")
                }
                is Resource.Loading -> {}
            }
        }

        return result
    }
}
