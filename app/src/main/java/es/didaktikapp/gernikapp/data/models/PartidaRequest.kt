package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para crear una nueva partida.
 */
@JsonClass(generateAdapter = true)
data class PartidaRequest(
    @Json(name = "id_usuario")
    val idUsuario: String
)