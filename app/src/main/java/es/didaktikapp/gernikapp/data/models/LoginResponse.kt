package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response del login exitoso.
 * Contiene el token JWT y datos del usuario.
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "token_type")
    val tokenType: String = "bearer",

    @Json(name = "user_id")
    val userId: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "apellido")
    val apellido: String
)