package es.didaktikapp.gernikapp.plaza.models

/**
 * Modelo que representa una misión fotográfica con su información y estado.
 */
data class PhotoMission(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val iconoRes: Int,
    var completada: Boolean = false,
    var rutaFoto: String? = null
)
