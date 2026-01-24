package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response del health check.
 * GET /health
 */
@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status")
    val status: String
) {
    fun isHealthy(): Boolean = status == "healthy"
}