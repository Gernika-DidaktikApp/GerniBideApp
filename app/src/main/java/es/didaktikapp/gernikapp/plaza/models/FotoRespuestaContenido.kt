package es.didaktikapp.gernikapp.plaza.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo para parsear el respuesta_contenido de la foto guardada en la API.
 * Representa el JSON: {"url":"https://...", "etiqueta":"TRADIZIOA"}
 *
 * @property url URL de la imagen en Cloudinary
 * @property etiqueta Etiqueta de la foto (TRADIZIOA, KOMUNITATEA, BIZIKIDETZA)
 */
@JsonClass(generateAdapter = true)
data class FotoRespuestaContenido(
    @Json(name = "url")
    val url: String,

    @Json(name = "etiqueta")
    val etiqueta: String
)