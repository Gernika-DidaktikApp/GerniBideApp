package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response con estadísticas del usuario.
 * GET /api/v1/usuarios/{id}/estadisticas
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class UserStatsResponse(
    @Json(name = "actividades_completadas")
    val actividadesCompletadas: Int = 0,

    @Json(name = "racha_dias")
    val rachaDias: Int = 0,

    @Json(name = "modulos_completados")
    val modulosCompletados: List<String> = emptyList(),

    @Json(name = "ultima_partida")
    val ultimaPartida: String? = null,

    @Json(name = "total_puntos_acumulados")
    val totalPuntosAcumulados: Double = 0.0
)