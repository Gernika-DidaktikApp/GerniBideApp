package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para iniciar sesión.
 * POST /api/v1/auth/login-app
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username")
    val nombre: String,

    @Json(name = "password")
    val contrasenya: String
)