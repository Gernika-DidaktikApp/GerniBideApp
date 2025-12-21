package es.didaktikapp.gernikapp.database

import android.content.Context
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

class Conexion(context: Context) {
    /**
     * Atributos de la clase
     */
    private val dbUrl:String
    private val dbUser:String
    private val dbPassword:String
    init {
        // Crear instancia de Properties
        val properties = Properties()

        // Cargar el archivo .properties
        try {
            // Usar el Context para acceder a assets
            val inputStream = context.assets.open("configuration.properties")
            properties.load(inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Obtener valores de las propiedades desde el archivo
        dbUrl = properties.getProperty("db_url") ?: throw IllegalArgumentException("db_url no definido")
        dbUser = properties.getProperty("db_user") ?: throw IllegalArgumentException("db_user no definido")
        dbPassword = properties.getProperty("db_password") ?: throw IllegalArgumentException("db_password no definido")
    }

    /**
     * Obtiene una conexión a la base de datos utilizando los parámetros configurados.
     *
     * @return La conexión a la base de datos si es exitosa, o null en caso de error.
     */
    fun obtenerConexion(): Connection? {
        return try {
            DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

   }