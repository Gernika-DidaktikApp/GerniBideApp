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
 */
data class FotoGaleria(
    val id: Int,
    val bitmap: Bitmap,
    val etiqueta: EtiquetaFoto,
    val timestamp: Long = System.currentTimeMillis()
)
