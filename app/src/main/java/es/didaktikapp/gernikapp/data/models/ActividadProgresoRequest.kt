package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para iniciar una actividad dentro de un punto.
 * POST /api/v1/actividad-progreso/iniciar
 *
 * @property idJuego ID de la partida
 * @property idPunto ID del punto (antes llamado "actividad")
 * @property idActividad ID de la actividad (antes llamado "evento")
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class ActividadProgresoRequest(
    @Json(name = "id_juego")
    val idJuego: String,

    @Json(name = "id_punto")
    val idPunto: String,

    @Json(name = "id_actividad")
    val idActividad: String
)

