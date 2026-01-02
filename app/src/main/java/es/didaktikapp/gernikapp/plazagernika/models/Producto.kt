package es.didaktikapp.gernikapp.plazagernika.models

data class Producto(
    val id: Int,
    val nombre: String,
    val nombreEuskera: String,
    val imagenRes: Int,
    val categoria: CategoriaProducto,
    val descripcion: String = ""
)
