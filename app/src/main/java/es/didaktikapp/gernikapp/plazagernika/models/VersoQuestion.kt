package es.didaktikapp.gernikapp.plazagernika.models

data class VersoQuestion(
    val id: Int,
    val versoInicial: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int,
    val explicacion: String? = null
)
