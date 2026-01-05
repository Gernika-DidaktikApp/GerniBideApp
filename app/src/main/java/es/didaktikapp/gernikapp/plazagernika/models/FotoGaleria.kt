package es.didaktikapp.gernikapp.plazagernika.models

import android.graphics.Bitmap

enum class EtiquetaFoto(val etiquetaEuskera: String) {
    TRADIZIOA("Tradizioa"),
    KOMUNITATEA("Komunitatea"),
    BIZIKIDETZA("Bizikidetza")
}

data class FotoGaleria(
    val id: Int,
    val bitmap: Bitmap,
    val etiqueta: EtiquetaFoto,
    val timestamp: Long = System.currentTimeMillis()
)
