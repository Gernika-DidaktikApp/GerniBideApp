package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para registrar un nuevo usuario.
 * POST /api/v1/usuarios
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username")
    val username: String,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "apellido")
    val apellido: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "codigo_clase")
    val codigoClase: String? = null
)