package es.didaktikapp.gernikapp.plaza.models

import android.graphics.Bitmap

/**
 * Etiquetas disponibles para clasificar las fotos en la misión fotográfica.
 */
enum class EtiquetaFoto(val etiquetaEuskera: String) {
    TRADIZIOA("Tradizioa"),
    KOMUNITATEA("Komunitatea"),
    BIZIKIDETZA("Bizikidetza")
}

/**
 * Modelo que representa una foto guardada en la galería con su etiqueta.
 *
 * @property id Identificador único de la foto
 * @property bitmap Imagen en formato Bitmap (puede ser null si se carga desde URL)
 * @property etiqueta Categoría de la foto
 * @property url URL de la imagen en Cloudinary (opcional)
 * @property timestamp Marca de tiempo de creación
 */
data class FotoGaleria(
    val id: Int,
    val bitmap: Bitmap? = null,
    val etiqueta: EtiquetaFoto,
    val url: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
