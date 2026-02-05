package es.didaktikapp.gernikapp.plaza.models

/**
 * Modelo que representa un producto del mercado con sus características.
 */
data class Product(
    val id: Int,
    val nombre: String,
    val nombreEuskera: String,
    val imagenRes: Int,
    val categoria: ProductCategory,
    val descripcion: String = ""
)
