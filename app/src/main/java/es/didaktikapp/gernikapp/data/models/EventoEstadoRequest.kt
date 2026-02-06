package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para iniciar un evento dentro de una actividad.
 * POST /api/v1/evento-estados/iniciar
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class EventoEstadoRequest(
    @Json(name = "id_juego")
    val idJuego: String,

    @Json(name = "id_actividad")
    val idActividad: String,

    @Json(name = "id_evento")
    val idEvento: String
)