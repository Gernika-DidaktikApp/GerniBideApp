package es.didaktikapp.gernikapp

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/**
 * Singleton que gestiona TODOS los ajustes de la aplicación GernikApp.
 *
 * @author Erlantz García
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

        applyDaltonismo(prefs.getBoolean("daltonismo", false))
        applyLanguage(context, prefs.getInt("language", 0))
        applyTextSize(context, prefs.getInt("text_size", 1))
    }

    /**
     * Aplica modo daltonismo/contraste alto
     *
     * @param enabled
     */
    fun applyDaltonismo(enabled: Boolean) {
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
    fun applyLanguage(context: Context, languageIndex: Int) {
        val locale = when (languageIndex) {
            0 -> Locale("eu")
            1 -> Locale("es")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        (context as? Activity)?.recreate()
    }

    /**
     * Ajusta el tamaño global de texto de la aplicación.
     *
     * @param context
     * @param sizeIndex
     */
    fun applyTextSize(context: Context, sizeIndex: Int) {
        val scale = when (sizeIndex) {
            0 -> 0.85f
            1 -> 1.0f
            2 -> 1.06f
            else -> 1.0f
        }

        val config = Configuration(context.resources.configuration)
        config.fontScale = scale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        (context as? Activity)?.recreate()
    }
}