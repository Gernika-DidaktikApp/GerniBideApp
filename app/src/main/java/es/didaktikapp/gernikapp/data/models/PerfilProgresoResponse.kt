package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response del endpoint GET /api/v1/usuarios/{usuario_id}/perfil-progreso
 *
 * Contiene el perfil completo del usuario con progreso detallado de
 * TODAS las actividades organizadas por puntos/módulos.
 *
 * @property usuario Información básica del usuario
 * @property estadisticas Estadísticas globales de progreso
 * @property puntos Lista de puntos/módulos con sus actividades
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class PerfilProgresoResponse(
    @Json(name = "usuario")
    val usuario: UsuarioInfo,

    @Json(name = "estadisticas")
    val estadisticas: EstadisticasGlobales,

    @Json(name = "puntos")
    val puntos: List<PuntoProgreso>
)

/**
 * Información básica del usuario.
 *
 * Contiene datos de perfil del usuario registrado en el sistema.
 *
 * @property id UUID único del usuario en la base de datos
 * @property username Nombre de usuario único para autenticación
 * @property nombre Nombre real del usuario
 * @property apellido Apellido del usuario
 * @property idClase UUID de la clase a la que pertenece (opcional)
 * @property creation Fecha de creación de la cuenta en formato ISO 8601
 * @property topScore Puntuación máxima alcanzada por el usuario
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class UsuarioInfo(
    @Json(name = "id")
    val id: String,

    @Json(name = "username")
    val username: String,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "apellido")
    val apellido: String,

    @Json(name = "id_clase")
    val idClase: String?,

    @Json(name = "creation")
    val creation: String,

    @Json(name = "top_score")
    val topScore: Int
)

/**
 * Estadísticas globales de progreso del usuario.
 *
 * Contiene métricas agregadas de todas las actividades y módulos completados.
 * Se usa para mostrar el progreso general en el perfil y dashboard.
 *
 * @property totalActividadesDisponibles Total de actividades en el sistema
 * @property actividadesCompletadas Número de actividades completadas por el usuario
 * @property porcentajeProgresoGlobal Porcentaje de progreso total (0.0 - 100.0)
 * @property totalPuntosAcumulados Suma de todos los puntos obtenidos
 * @property rachaDias Número de días consecutivos con actividad
 * @property ultimaPartida Fecha y hora de la última partida en formato ISO 8601
 * @property puntosCompletados Número de módulos/puntos completados al 100%
 * @property totalPuntosDisponibles Total de módulos/puntos en el sistema (5)
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class EstadisticasGlobales(
    @Json(name = "total_actividades_disponibles")
    val totalActividadesDisponibles: Int,

    @Json(name = "actividades_completadas")
    val actividadesCompletadas: Int,

    @Json(name = "porcentaje_progreso_global")
    val porcentajeProgresoGlobal: Double,

    @Json(name = "total_puntos_acumulados")
    val totalPuntosAcumulados: Double,

    @Json(name = "racha_dias")
    val rachaDias: Int,

    @Json(name = "ultima_partida")
    val ultimaPartida: String?,

    @Json(name = "puntos_completados")
    val puntosCompletados: Int,

    @Json(name = "total_puntos_disponibles")
    val totalPuntosDisponibles: Int
)

/**
 * Información de progreso de un punto/módulo temático.
 *
 * Representa un módulo del juego (Árbol, Bunkers, Picasso, Plaza, Frontón)
 * con sus actividades asociadas y métricas de progreso.
 *
 * @property idPunto UUID único del punto/módulo en la base de datos
 * @property nombrePunto Nombre del módulo (ej: "arbol", "bunkers", "mercado")
 * @property totalActividades Total de actividades disponibles en el módulo
 * @property actividadesCompletadas Número de actividades completadas del módulo
 * @property porcentajeCompletado Porcentaje de completado del módulo (0.0 - 100.0)
 * @property puntosObtenidos Suma de puntos obtenidos en las actividades del módulo
 * @property estado Estado del módulo: "no_iniciado", "en_progreso", "completado"
 * @property actividades Lista detallada de actividades del módulo con sus estados
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class PuntoProgreso(
    @Json(name = "id_punto")
    val idPunto: String,

    @Json(name = "nombre_punto")
    val nombrePunto: String,

    @Json(name = "total_actividades")
    val totalActividades: Int,

    @Json(name = "actividades_completadas")
    val actividadesCompletadas: Int,

    @Json(name = "porcentaje_completado")
    val porcentajeCompletado: Double,

    @Json(name = "puntos_obtenidos")
    val puntosObtenidos: Double,

    @Json(name = "estado")
    val estado: String, // "no_iniciado", "en_progreso", "completado"

    @Json(name = "actividades")
    val actividades: List<ActividadDetalle>
)

/**
 * Detalle completo de una actividad individual.
 *
 * Representa el estado y progreso de una actividad específica dentro de un módulo.
 * Incluye información de completado, puntuación obtenida y tiempo de duración.
 *
 * @property idActividad UUID único de la actividad en la base de datos
 * @property nombreActividad Nombre descriptivo de la actividad (ej: "Audio Quiz", "Puzzle")
 * @property estado Estado actual: "no_iniciada", "en_progreso", "completada"
 * @property puntuacion Puntuación obtenida (0.0 - 300.0), null si no está completada
 * @property fechaCompletado Fecha de completado en formato ISO 8601, null si no está completada
 * @property duracionSegundos Tiempo que tomó completar la actividad en segundos, null si no está completada
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class ActividadDetalle(
    @Json(name = "id_actividad")
    val idActividad: String,

    @Json(name = "nombre_actividad")
    val nombreActividad: String,

    @Json(name = "estado")
    val estado: String, // "no_iniciada", "en_progreso", "completada"

    @Json(name = "puntuacion")
    val puntuacion: Double?,

    @Json(name = "fecha_completado")
    val fechaCompletado: String?,

    @Json(name = "duracion_segundos")
    val duracionSegundos: Int?
)
