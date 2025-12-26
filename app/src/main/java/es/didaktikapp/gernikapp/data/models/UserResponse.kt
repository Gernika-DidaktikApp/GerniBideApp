package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val created_at: String? = null
)