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
 * Response al iniciar, completar o consultar el progreso de una actividad.
 *
 * @property id ID del progreso de la actividad
 * @property idJuego ID de la partida
 * @property idPunto ID del punto
 * @property idActividad ID de la actividad
 * @property fechaInicio Fecha de inicio
 * @property fechaFin Fecha de finalización (null si está en progreso)
 * @property duracion Duración en segundos (calculada automáticamente)
 * @property estado Estado: "en_progreso" o "completado"
 * @property puntuacion Puntuación obtenida (null si no está completado)
 * @property respuestaContenido Contenido de la respuesta del usuario (texto, URL, etc.)
 *
 * @author Wara Pacheco
 * @version 1.0
 */
@JsonClass(generateAdapter = true)
data class ActividadProgresoResponse(
    @Json(name = "id")
    val id: String,

    @Json(name = "id_juego")
    val idJuego: String,

    @Json(name = "id_punto")
    val idPunto: String,

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

    @Json(name = "puntuacion")
    val puntuacion: Double? = null,

    @Json(name = "respuesta_contenido")
    val respuestaContenido: String? = null
) {
    /**
     * Verifica si la actividad está en progreso.
     */
    fun estaEnProgreso(): Boolean = estado == EstadoActividad.EN_PROGRESO

    /**
     * Verifica si la actividad está completada.
     */
    fun estaCompletado(): Boolean = estado == EstadoActividad.COMPLETADO

    /**
     * Obtiene la duración formateada (minutos:segundos).
     */
    fun getDuracionFormateada(): String {
        val segundos = duracion ?: return "--:--"
        val minutos = segundos / 60
        val segs = segundos % 60
        return String.format("%02d:%02d", minutos, segs)
    }

    /**
     * Verifica si la respuesta es una URL (imagen de Cloudinary, etc.).
     */
    fun esRespuestaUrl(): Boolean {
        return respuestaContenido?.startsWith("http://") == true ||
                respuestaContenido?.startsWith("https://") == true
    }
}
