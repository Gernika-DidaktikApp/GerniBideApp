package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response al crear o consultar una partida.
 */
@JsonClass(generateAdapter = true)
data class PartidaResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "id_usuario")
    val idUsuario: String,

    @Json(name = "fecha_inicio")
    val fechaInicio: String? = null,

    @Json(name = "fecha_fin")
    val fechaFin: String? = null,

    @Json(name = "puntuacion_total")
    val puntuacionTotal: Double? = null,

    @Json(name = "estado")
    val estado: String? = null
)