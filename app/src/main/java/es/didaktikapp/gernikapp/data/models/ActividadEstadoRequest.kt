package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para iniciar una actividad.
 * Se envía a POST /api/v1/actividad-estados/iniciar
 */
@JsonClass(generateAdapter = true)
data class ActividadEstadoRequest(
    @Json(name = "id_juego")
    val idJuego: String,

    @Json(name = "id_actividad")
    val idActividad: String
)