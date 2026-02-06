package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response del health check.
 * GET /health
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status")
    val status: String
) {
    fun isHealthy(): Boolean = status == "healthy"
}