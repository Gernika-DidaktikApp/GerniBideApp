package es.didaktikapp.gernikapp.plazagernika.models

data class Product(
    val id: Int,
    val nombre: String,
    val nombreEuskera: String,
    val imagenRes: Int,
    val categoria: ProductCategory,
    val descripcion: String = ""
)
