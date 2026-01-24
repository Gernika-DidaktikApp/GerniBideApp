package es.didaktikapp.gernikapp

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Singleton que gestiona TODOS los ajustes de la aplicación GernikApp.
 *
 * @author Erlantz
 * @version 1.0
 */
object SettingsManager {

    /** Nombre del archivo SharedPreferences para almacenar configuración */
    private const val PREFS_NAME = "GernikAppSettings"

    /**
     * Aplica TODOS los ajustes guardados en SharedPreferences.
     *
     * @param context Contexto para acceder a SharedPreferences y resources
     */
    fun applyAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        applyDarkMode(prefs.getBoolean("dark_mode", false))
        applyLanguage(context, prefs.getInt("language", 0))
        applyTextSize(context, prefs.getInt("text_size", 1))
    }

    /**
     * Aplica modo oscuro o claro usando AppCompatDelegate.
     *
     * @param enabled true = modo oscuro, false = modo claro
     */
    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /**
     * Cambia el idioma de la aplicación (Euskera/Español).
     *
     * @param context Contexto para actualizar resources
     * @param languageIndex 0=eu, 1=es, resto=Locale por defecto
     */
    private fun applyLanguage(context: Context, languageIndex: Int) {
        val locale = when (languageIndex) {
            0 -> Locale("eu")
            1 -> Locale("es")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Ajusta el tamaño global de texto de la aplicación.
     *
     * @param context
     * @param sizeIndex
     */
    private fun applyTextSize(context: Context, sizeIndex: Int) {
        val scale = when (sizeIndex) {
            0 -> 0.85f
            1 -> 1.0f
            2 -> 1.25f
            else -> 1.0f
        }

        val config = Configuration(context.resources.configuration)
        config.fontScale = scale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

}