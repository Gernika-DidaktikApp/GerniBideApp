package es.didaktikapp.gernikapp.model

import java.sql.Date

/**
 * Clase modelo que representa un alumno en la base de datos.
 *
 * @property usuario Usuario del alumno (parte de la clave primaria)
 * @property nombre Nombre del alumno (parte de la clave primaria)
 * @property añoNacimiento Año de nacimiento del alumno (parte de la clave primaria)
 * @property idioma Idioma preferido del alumno (opcional)
 * @property fechaAlta Fecha de alta en el sistema (opcional)
 * @property fechaBaja Fecha de baja en el sistema (opcional)
 * @property contrasenya Contraseña del alumno (opcional)
 * @property numImagen Número de imagen de perfil (opcional)
 * @property idAplicacion ID de la aplicación (parte de la clave primaria, clave foránea)
 */
data class Alumno(
    val usuario: String,
    val nombre: String,
    val añoNacimiento: String,
    val idioma: String? = null,
    val fechaAlta: Date? = null,
    val fechaBaja: Date? = null,
    val contrasenya: String? = null,
    val numImagen: Int? = null,
    val idAplicacion: Int
)

