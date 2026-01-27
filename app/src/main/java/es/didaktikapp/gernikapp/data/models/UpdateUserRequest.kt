package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para actualizar perfil de usuario.
 * PUT /api/v1/usuarios/{id}
 *
 * Todos los campos son opcionales.
 * Solo se envían los campos que se desean actualizar.
 */
@JsonClass(generateAdapter = true)
data class UpdateUserRequest(
    @Json(name = "username")
    val username: String? = null,

    @Json(name = "nombre")
    val nombre: String? = null,

    @Json(name = "apellido")
    val apellido: String? = null,

    @Json(name = "password")
    val password: String? = null,

    @Json(name = "id_clase")
    val idClase: String? = null
)