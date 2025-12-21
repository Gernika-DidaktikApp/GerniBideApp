package es.didaktikapp.gernikapp.dao

import android.content.Context
import es.didaktikapp.gernikapp.database.Conexion
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

    private val conexion = Conexion(context)

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
     * Función auxilir para mapear un ResultSet a un objeto Alumno.
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