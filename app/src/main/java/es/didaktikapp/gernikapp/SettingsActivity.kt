package es.didaktikapp.gernikapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity para la configuración de la aplicación.
 * Permite al usuario configurar:
 * - Sonido (activado/desactivado)
 * - Tamaño de la letra
 * - Modo oscuro
 * - Idioma (Euskera/Castellano)
 *
 * @author Erlantz
 * @version 1.0
 * @see AppCompatActivity
 */
class SettingsActivity : AppCompatActivity() {

    /** Switch para activar/desactivar sonido */
    private lateinit var switchMute: Switch

    /** Switch para modo oscuro */
    private lateinit var switchDarkMode: Switch

    /** Spinner para seleccionar tamaño de la letra */
    private lateinit var spinnerTextSize: Spinner

    /** Spinner para seleccionar idioma */
    private lateinit var spinnerLanguage: Spinner

    /** Botón para guardar configuración */
    private lateinit var btnSaveSettings: Button

    /** SharedPreferences para almacenar configuración de la app */
    private lateinit var prefs: SharedPreferences

    /**
     * Metodo principal del ciclo de vida de la Activity.
     * Inicializa las vistas, carga la configuración previa y configura los listeners.
     *
     * @param savedInstanceState Bundle con el estado previo de la Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadSettings()
        setupListeners()
    }

    /**
     * Inicializa todas las vistas del layout y SharedPreferences.
     * Vincula las variables con los elementos del layout activity_settings.xml
     */
    private fun initViews() {
        switchMute = findViewById(R.id.switchMute)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        spinnerTextSize = findViewById(R.id.spinnerTextSize)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

        prefs = getSharedPreferences("GernikAppSettings", MODE_PRIVATE)
    }

    /**
     * Carga la configuración previamente guardada desde SharedPreferences.
     * Establece los valores por defecto si no existen ajustes previos.
     */
    private fun loadSettings() {
        switchMute.isChecked = prefs.getBoolean("mute", false)
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        spinnerTextSize.setSelection(prefs.getInt("text_size", 1))
        spinnerLanguage.setSelection(prefs.getInt("language", 0))
    }

    /**
     * Configura los event listeners de la interfaz.
     */
    private fun setupListeners() {
        btnSaveSettings.setOnClickListener {
            saveSettings()
            Toast.makeText(this, getString(R.string.ezarpenak_gordeta), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Guarda toda la configuración actual en SharedPreferences.
     */
    private fun saveSettings() {
        prefs.edit().apply {
            putBoolean("mute", switchMute.isChecked)
            putBoolean("dark_mode", switchDarkMode.isChecked)
            putInt("text_size", spinnerTextSize.selectedItemPosition)
            putInt("language", spinnerLanguage.selectedItemPosition)
            apply()
        }
    }

}