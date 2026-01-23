package es.didaktikapp.gernikapp

import android.content.SharedPreferences
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast

/**
 * Activity para la configuración de la aplicación.
 */
class SettingsActivity : BaseMenuActivity() {

    private lateinit var switchMute: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var spinnerTextSize: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSaveSettings: Button
    private lateinit var prefs: SharedPreferences

    override fun getContentLayoutId() = R.layout.activity_settings

    override fun onContentInflated() {
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