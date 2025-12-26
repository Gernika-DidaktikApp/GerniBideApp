package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
    val detail: String,
    val status_code: Int? = null
)