package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Request para completar una actividad con su puntuación y respuesta.
 * PUT /api/v1/actividad-progreso/{progreso_id}/completar
 *
 * @property puntuacion Puntuación obtenida en la actividad
 * @property respuestaContenido Contenido de la respuesta del usuario (texto, URL de imagen, etc.)
 * @property deviceType Tipo de dispositivo (ej: "Android", "iOS")
 * @property appVersion Versión de la aplicación (ej: "1.0.0")
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class CompletarActividadRequest(
    @Json(name = "puntuacion")
    val puntuacion: Double,

    @Json(name = "respuesta_contenido")
    val respuestaContenido: String? = null,

    @Json(name = "device_type")
    val deviceType: String? = "Android",

    @Json(name = "app_version")
    val appVersion: String? = "1.0.0"
)
