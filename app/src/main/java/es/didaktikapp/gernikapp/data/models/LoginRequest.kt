package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username")
    val nombre: String,

    @Json(name = "password")
    val contrasenya: String
)