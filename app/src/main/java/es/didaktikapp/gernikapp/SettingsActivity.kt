package es.didaktikapp.gernikapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.media.AudioManager
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import es.didaktikapp.gernikapp.databinding.ActivitySettingsBinding
import androidx.core.content.ContextCompat
import androidx.core.content.edit

/**
 * Activity para la configuración de la aplicación.
 *
 * @author Erlantz García
 * @version 1.0
 */
class SettingsActivity : BaseMenuActivity() {

    /** Binding generado para el layout activity_settings.xml */
    private lateinit var binding: ActivitySettingsBinding

    /** SharedPreferences para almacenar configuración persistente */
    private lateinit var prefs: SharedPreferences

    /** Indica si es la carga inicial para evitar disparar listeners innecesariamente. */
    private var isInitialLoad = true

    /** Controla si los event listeners ya han sido configurados. */
    private var listenersSetup = false

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

        LogManager.write(this@SettingsActivity, "SettingsActivity iniciada")

        contentContainer.post {
            loadSettings()
            if (!listenersSetup) {
                setupListeners()
                listenersSetup = true
            }
        }
    }

    /**
     * Inicializa todas las vistas del layout y SharedPreferences.
     */
    private fun initViews() {
        prefs = getSharedPreferences("GernikAppSettings", MODE_PRIVATE)
        saveOriginalVolumes()
        setupSwitchColors()
    }

    /**
     * Configura los colores de los switches para que cambien según su estado.
     */
    private fun setupSwitchColors() {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )

        val thumbColors = intArrayOf(
            ContextCompat.getColor(this, R.color.btnPrincipal),
            ContextCompat.getColor(this, R.color.grayLight)
        )

        val trackColors = intArrayOf(
            ContextCompat.getColor(this, R.color.btnPrincipal),
            ContextCompat.getColor(this, R.color.grayLight)
        )

        binding.switchMute.thumbTintList = ColorStateList(states, thumbColors)
        binding.switchMute.trackTintList = ColorStateList(states, trackColors)
        binding.switchDaltonismo.thumbTintList = ColorStateList(states, thumbColors)
        binding.switchDaltonismo.trackTintList = ColorStateList(states, trackColors)
    }

    /**
     * Guarda los volúmenes originales del sistema en SharedPreferences.
     */
    private fun saveOriginalVolumes() {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (!prefs.contains("original_music_volume")) {
            prefs.edit {
                putInt("original_music_volume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
                putInt("original_notification_volume", audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION))
            }
        }
    }

    /**
     * Carga la configuración previamente guardada desde SharedPreferences.
     * Rellena todos los controles con los valores guardados.
     */
    private fun loadSettings() {
        isInitialLoad = true

        binding.switchMute.isChecked = prefs.getBoolean("mute", false)
        binding.switchDaltonismo.isChecked = prefs.getBoolean("daltonismo", false)
        binding.spinnerTextSize.setSelection(
            prefs.getInt("text_size", 1),
            false
        )
        binding.spinnerLanguage.setSelection(
            prefs.getInt("language", 0),
            false
        )

        // Terminar carga inicial para activar listeners
        isInitialLoad = false
    }

    /**
     * Configura los event listeners de la interfaz.
     * Maneja cambios en switches y acciones de botones.
     */
    private fun setupListeners() {
        // Daltonismo
        binding.switchDaltonismo.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("daltonismo", isChecked) }
            SettingsManager.applyDaltonismo(isChecked)
            LogManager.write(this@SettingsActivity, "Daltonismo cambiado a: $isChecked")
        }

        // Mute
        binding.switchMute.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("mute", isChecked) }
            setAppMuteState(this@SettingsActivity, isChecked)
            LogManager.write(this@SettingsActivity, "Mute cambiado a: $isChecked")
        }

        // Spinner TextSize - Solo guarda, no aplica hasta presionar "Guardar"
        binding.spinnerTextSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialLoad) return

                LogManager.write(this@SettingsActivity, "TextSize seleccionado: $position")
                prefs.edit { putInt("text_size", position) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Spinner Language - Solo guarda, no aplica hasta presionar "Guardar"
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialLoad) return

                LogManager.write(this@SettingsActivity, "Idioma seleccionado: $position")
                prefs.edit { putInt("language", position) }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Boton guardar - Solo aplica cambios de spinners (switches ya se aplicaron)
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()

            // Aplicar solo idioma y tamaño de texto (daltonismo ya se aplicó con el switch)
            val needsRecreate = applySpinnerSettings()

            Toast.makeText(this, getString(R.string.ezarpenak_gordeta), Toast.LENGTH_SHORT).show()
            LogManager.write(this@SettingsActivity, "Configuración guardada por el usuario")

            // Recrear solo si es necesario
            if (needsRecreate) {
                recreate()
            }
        }

        // Acerca de
        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
            LogManager.write(this@SettingsActivity, "Usuario abrió AboutActivity desde Settings")
        }
    }

    /**
     * Guarda toda la configuración actual en SharedPreferences.
     */
    private fun saveSettings() {
        prefs.edit().apply {
            putBoolean("mute", binding.switchMute.isChecked)
            putBoolean("daltonismo", binding.switchDaltonismo.isChecked)
            putInt("text_size", binding.spinnerTextSize.selectedItemPosition)
            putInt("language", binding.spinnerLanguage.selectedItemPosition)
            apply()
        }
    }

    /**
     * Aplica solo los cambios de idioma y tamaño de texto.
     * @return true si necesita recrear la actividad, false si no
     */
    private fun applySpinnerSettings(): Boolean {
        val currentLanguage = prefs.getInt("language", 0)
        val currentTextSize = prefs.getInt("text_size", 1)

        // Aplicar idioma
        val locale = when (currentLanguage) {
            0 -> java.util.Locale("eu")
            1 -> java.util.Locale("es")
            else -> java.util.Locale.getDefault()
        }
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(resources.configuration)
        config.setLocale(locale)

        // Aplicar tamaño de texto
        config.fontScale = when (currentTextSize) {
            0 -> 0.85f
            1 -> 1.0f
            2 -> 1.06f
            else -> 1.0f
        }

        resources.updateConfiguration(config, resources.displayMetrics)

        // Solo necesita recrear si cambió algo
        return true
    }

    /**
     * Activa o desactiva el mute global de la aplicación.
     *
     * @param context Contexto para acceder al AudioManager
     * @param muted `true` para silenciar, `false` para restaurar sonido
     */
    private fun setAppMuteState(context: Context, muted: Boolean) {
        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

        if (muted) {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, true)
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false)
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false)
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, false)
        }
    }
}