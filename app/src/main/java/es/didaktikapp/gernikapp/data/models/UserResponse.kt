package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response con datos del usuario.
 * GET /api/v1/users/me
 */
@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "apellido")
    val apellido: String,

    @Json(name = "clase_id")
    val claseId: String? = null,

    @Json(name = "creation")
    val creation: String? = null,

    @Json(name = "top_score")
    val topScore: Int = 0
)