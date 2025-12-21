package es.didaktikapp.gernikapp.dao

import android.content.Context
import es.didaktikapp.gernikapp.database.ConexionDB
import es.didaktikapp.gernikapp.model.Alumno
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Clase DAO (Data Access Object) para gestionar operaciones CRUD
 * de la tabla alumno en la base de datos.
 *
 * @property context Contexto de Android necesario para la conexión a la base de datos
 */
class DaoAlumno(private val context: Context) {

    private val conexion = ConexionDB(context)

    /**
     * Inserta un nuevo alumno en la base de datos.
     *
     * @param alumno Objeto Alumno a insertar
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    fun insertarAlumno(alumno: Alumno): Boolean {
        val sql = """
            INSERT INTO alumno (usuario, nombre, año_nacimiento, idioma, fecha_alta, 
                                fecha_baja, contrasenya, num_imagen, id_aplicacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null

        return try {
            connection = conexion.obtenerConexion()
            preparedStatement = connection?.prepareStatement(sql)

            preparedStatement?.apply {
                setString(1, alumno.usuario)
                setString(2, alumno.nombre)
                setString(3, alumno.añoNacimiento)
                setString(4, alumno.idioma)
                setDate(5, alumno.fechaAlta)
                setDate(6, alumno.fechaBaja)
                setString(7, alumno.contrasenya)
                if (alumno.numImagen != null) {
                    setInt(8, alumno.numImagen)
                } else {
                    setNull(8, java.sql.Types.INTEGER)
                }
                setInt(9, alumno.idAplicacion)
            }

            val rowsAffected = preparedStatement?.executeUpdate() ?: 0
            rowsAffected > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            try {
                preparedStatement?.close()
                connection?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Obtiene un alumno por su clave primaria compuesta.
     *
     * @param usuario Usuario del alumno
     * @param nombre Nombre del alumno
     * @param añoNacimiento Año de nacimiento del alumno
     * @param idAplicacion ID de la aplicación
     * @return El objeto Alumno si se encuentra, null en caso contrario
     */
    fun obtenerAlumnoPorClave(
        usuario: String,
        nombre: String,
        añoNacimiento: String,
        idAplicacion: Int
    ): Alumno? {
        val sql = """
            SELECT * FROM alumno 
            WHERE usuario = ? AND nombre = ? AND año_nacimiento = ? AND id_aplicacion = ?
        """.trimIndent()

        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        return try {
            connection = conexion.obtenerConexion()
            preparedStatement = connection?.prepareStatement(sql)

            preparedStatement?.apply {
                setString(1, usuario)
                setString(2, nombre)
                setString(3, añoNacimiento)
                setInt(4, idAplicacion)
            }

            resultSet = preparedStatement?.executeQuery()

            if (resultSet?.next() == true) {
                mapearAlumno(resultSet)
            } else {
                null
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        } finally {
            try {
                resultSet?.close()
                preparedStatement?.close()
                connection?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Verifica las credenciales de un alumno (login).
     *
     * @param usuario Usuario del alumno
     * @param contrasenya Contraseña del alumno
     * @return El objeto Alumno si las credenciales son correctas, null en caso contrario
     */
    fun validarCredenciales(usuario: String, contrasenya: String): Alumno? {
        val sql = "SELECT * FROM alumno WHERE usuario = ? AND contrasenya = ?"

        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        return try {
            connection = conexion.obtenerConexion()
            preparedStatement = connection?.prepareStatement(sql)

            preparedStatement?.apply {
                setString(1, usuario)
                setString(2, contrasenya)
            }

            resultSet = preparedStatement?.executeQuery()

            if (resultSet?.next() == true) {
                mapearAlumno(resultSet)
            } else {
                null
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        } finally {
            try {
                resultSet?.close()
                preparedStatement?.close()
                connection?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Función auxiliar para mapear un ResultSet a un objeto Alumno.
     *
     * @param resultSet ResultSet con los datos del alumno
     * @return Objeto Alumno mapeado, o null si hay error
     */
    private fun mapearAlumno(resultSet: ResultSet): Alumno? {
        return try {
            Alumno(
                usuario = resultSet.getString("usuario"),
                nombre = resultSet.getString("nombre"),
                añoNacimiento = resultSet.getString("año_nacimiento"),
                idioma = resultSet.getString("idioma"),
                fechaAlta = resultSet.getDate("fecha_alta"),
                fechaBaja = resultSet.getDate("fecha_baja"),
                contrasenya = resultSet.getString("contrasenya"),
                numImagen = resultSet.getInt("num_imagen").takeIf { !resultSet.wasNull() },
                idAplicacion = resultSet.getInt("id_aplicacion")
            )
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }
}