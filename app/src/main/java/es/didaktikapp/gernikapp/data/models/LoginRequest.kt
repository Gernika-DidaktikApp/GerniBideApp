package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val nombre: String,
    val contrasenya: String
)