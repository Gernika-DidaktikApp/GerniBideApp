package es.didaktikapp.gernikapp

import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import es.didaktikapp.gernikapp.databinding.ActivitySettingsBinding

/**
 * Activity para la configuración de la aplicación.
 *
 * @author Erlantz
 * @version 1.0
 */
class SettingsActivity : BaseMenuActivity() {

    /** Binding generado para el layout activity_settings.xml */
    private lateinit var binding: ActivitySettingsBinding

    /** SharedPreferences para almacenar configuración persistente */
    private lateinit var prefs: SharedPreferences

    /**
     * Retorna el ID del layout a cargar en BaseMenuActivity.
     */
    override fun getContentLayoutId() = R.layout.activity_settings

    /**
     * Se ejecuta después de inflar el layout. Inicializa binding y configura la UI.
     */
    override fun onContentInflated() {
        binding = ActivitySettingsBinding.bind(contentContainer.getChildAt(0))
        initViews()
        loadSettings()
        setupListeners()
    }

    /**
     * Inicializa todas las vistas del layout y SharedPreferences.
     */
    private fun initViews() {
        prefs = getSharedPreferences("GernikAppSettings", MODE_PRIVATE)
    }

    /**
     * Carga la configuración previamente guardada desde SharedPreferences.
     * Rellena todos los controles con los valores guardados.
     */
    private fun loadSettings() {
        binding.switchMute.isChecked = prefs.getBoolean("mute", false)
        binding.switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        binding.spinnerTextSize.setSelection(prefs.getInt("text_size", 1))
        binding.spinnerLanguage.setSelection(prefs.getInt("language", 0))
    }

    /**
     * Configura los event listeners de la interfaz.
     * Maneja cambios en switches y acciones de botones.
     */
    private fun setupListeners() {
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
            SettingsManager.applyAll(this)
            Toast.makeText(this, getString(R.string.ezarpenak_gordeta), Toast.LENGTH_SHORT).show()
            recreate()
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    /**
     * Guarda toda la configuración actual en SharedPreferences.
     */
    private fun saveSettings() {
        prefs.edit().apply {
            putBoolean("mute", binding.switchMute.isChecked)
            putBoolean("dark_mode", binding.switchDarkMode.isChecked)
            putInt("text_size", binding.spinnerTextSize.selectedItemPosition)
            putInt("language", binding.spinnerLanguage.selectedItemPosition)
            apply()
        }
    }

}