package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Estados posibles de una actividad.
 *
 * @author Wara Pacheco
 * @version 1.0
 */
object EstadoActividad {
    const val EN_PROGRESO = "en_progreso"
    const val COMPLETADO = "completado"
}

/**
 * Response al iniciar o consultar el estado de una actividad.
 *
 * Cuando se completa el último evento de la actividad, el backend
 * calcula automáticamente la puntuación total y duración.
 *
 * @author Wara Pacheco
 * @version 1
 */
@JsonClass(generateAdapter = true)
data class ActividadEstadoResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "id_juego")
    val idJuego: String,

    @Json(name = "id_actividad")
    val idActividad: String,

    @Json(name = "fecha_inicio")
    val fechaInicio: String,

    @Json(name = "fecha_fin")
    val fechaFin: String? = null,

    @Json(name = "duracion")
    val duracion: Int? = null,

    @Json(name = "estado")
    val estado: String,

    @Json(name = "puntuacion_total")
    val puntuacionTotal: Double = 0.0
) {
    /**
     * Verifica si la actividad está en progreso.
     */
    fun estaEnProgreso(): Boolean = estado == EstadoActividad.EN_PROGRESO

    /**
     * Verifica si la actividad está completada.
     */
    fun estaCompletada(): Boolean = estado == EstadoActividad.COMPLETADO

    /**
     * Obtiene la duración formateada (minutos:segundos).
     */
    fun getDuracionFormateada(): String {
        val segundos = duracion ?: return "--:--"
        val minutos = segundos / 60
        val segs = segundos % 60
        return String.format("%02d:%02d", minutos, segs)
    }
}