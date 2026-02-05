package es.didaktikapp.gernikapp.plaza.models

/**
 * Modelo que representa una pregunta del juego de versos con sus opciones y respuesta correcta.
 */
data class VerseQuestion(
    val id: Int,
    val versoInicial: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int,
    val explicacion: String? = null
)
