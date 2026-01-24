package es.didaktikapp.gernikapp.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Estados posibles de un evento.
 */
object EstadoEvento {
    const val EN_PROGRESO = "en_progreso"
    const val COMPLETADO = "completado"
}

/**
 * Response al iniciar o consultar el estado de un evento.
 *
 * Al completar el evento se calcula automáticamente la duración.
 * Si es el último evento de la actividad, se completa la actividad
 * automáticamente con la suma de puntuaciones.
 */
@JsonClass(generateAdapter = true)
data class EventoEstadoResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "id_juego")
    val idJuego: String,

    @Json(name = "id_actividad")
    val idActividad: String,

    @Json(name = "id_evento")
    val idEvento: String,

    @Json(name = "fecha_inicio")
    val fechaInicio: String,

    @Json(name = "fecha_fin")
    val fechaFin: String? = null,

    @Json(name = "duracion")
    val duracion: Int? = null,

    @Json(name = "estado")
    val estado: String,

    @Json(name = "puntuacion")
    val puntuacion: Double? = null
) {
    /**
     * Verifica si el evento está en progreso.
     */
    fun estaEnProgreso(): Boolean = estado == EstadoEvento.EN_PROGRESO

    /**
     * Verifica si el evento está completado.
     */
    fun estaCompletado(): Boolean = estado == EstadoEvento.COMPLETADO

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