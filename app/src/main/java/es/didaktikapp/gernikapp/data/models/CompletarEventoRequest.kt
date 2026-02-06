package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para completar un evento con su puntuación.
 * PUT /api/v1/evento-estados/{estado_id}/completar
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class CompletarEventoRequest(
    @Json(name = "puntuacion")
    val puntuacion: Double
)