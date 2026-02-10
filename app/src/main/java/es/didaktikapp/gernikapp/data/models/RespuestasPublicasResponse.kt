package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response del endpoint GET /api/v1/actividades/{actividad_id}/respuestas-publicas
 *
 * Contiene los mensajes públicos de otros usuarios para una actividad específica.
 * Se usa en MyMessageActivity para mostrar mensajes de paz de otros jugadores.
 *
 * @property actividadId UUID de la actividad
 * @property actividadNombre Nombre de la actividad (ej: "Mi mensaje para el mundo")
 * @property totalRespuestas Total de respuestas disponibles
 * @property respuestas Lista de respuestas públicas (limitada por query param)
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class RespuestasPublicasResponse(
    @Json(name = "actividad_id")
    val actividadId: String,

    @Json(name = "actividad_nombre")
    val actividadNombre: String,

    @Json(name = "total_respuestas")
    val totalRespuestas: Int,

    @Json(name = "respuestas")
    val respuestas: List<RespuestaPublica>
)

/**
 * Respuesta pública individual de un usuario.
 *
 * @property mensaje Contenido del mensaje escrito por el usuario
 * @property fecha Fecha de completado en formato ISO 8601
 * @property usuario Nombre del usuario que escribió el mensaje
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class RespuestaPublica(
    @Json(name = "mensaje")
    val mensaje: String,

    @Json(name = "fecha")
    val fecha: String,

    @Json(name = "usuario")
    val usuario: String
)
