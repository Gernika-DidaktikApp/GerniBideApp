package es.didaktikapp.gernikapp.plazagernika.models

data class VerseQuestion(
    val id: Int,
    val versoInicial: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int,
    val explicacion: String? = null
)
