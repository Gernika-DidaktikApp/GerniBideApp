package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Error simple de la API.
 * Formato: { "detail": "mensaje de error" }
 */
@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "detail")
    val detail: String,

    @Json(name = "status_code")
    val statusCode: Int? = null
)

/**
 * Error de validación de la API (422 Unprocessable Entity).
 * Formato: { "detail": [{ "type": "...", "loc": [...], "msg": "..." }] }
 */
@JsonClass(generateAdapter = true)
data class ValidationError(
    @Json(name = "detail")
    val detail: List<ValidationErrorDetail>
) {
    /**
     * Obtiene un mensaje legible de todos los errores de validación.
     */
    fun getReadableMessage(): String {
        return detail.joinToString("\n") { error ->
            val field = error.loc.lastOrNull() ?: "campo"
            "$field: ${error.msg}"
        }
    }
}

/**
 * Detalle de un error de validación individual.
 */
@JsonClass(generateAdapter = true)
data class ValidationErrorDetail(
    @Json(name = "type")
    val type: String,

    @Json(name = "loc")
    val loc: List<String>,

    @Json(name = "msg")
    val msg: String
)