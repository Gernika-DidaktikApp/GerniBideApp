package es.didaktikapp.gernikapp

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor centralizado para el registro de eventos y mensajes de depuración de la aplicación.
 *
 * Este objeto permite escribir líneas de log persistentes en un archivo interno
 * (`app_logs.txt`) ubicado en el directorio privado de la aplicación.
 *
 * @author Erlantz García
 * @version 1.0
 */
object LogManager {

    /**
     * Escribe una línea de log en el archivo persistente `app_logs.txt`.
     *
     * @param context Contexto necesario para acceder al directorio interno de archivos.
     * @param message Mensaje que se desea registrar en el archivo de logs.
     */
    fun write(context: Context, message: String) {
        val file = File(context.filesDir, "app_logs.txt")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "[$timestamp] $message"
        // Guardar en archivo
        file.appendText("$line\n")
        // Mostrar en Logcat (tag:DIDAKTIKAPP)
        android.util.Log.d("DIDAKTIKAPP", line)
    }
}