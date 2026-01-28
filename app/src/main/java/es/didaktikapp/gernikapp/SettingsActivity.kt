package es.didaktikapp.gernikapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import es.didaktikapp.gernikapp.databinding.ActivitySettingsBinding
import androidx.core.content.edit

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
    }

    /**
     * Guarda los volúmenes originales del sistema en SharedPreferences.
     */
    private fun saveOriginalVolumes() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
        }

        // Mute
        binding.switchMute.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("mute", isChecked) }
            setAppMuteState(this@SettingsActivity, isChecked)
        }

        // Spinner TextSize
        binding.spinnerTextSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialLoad || position == prefs.getInt("text_size", 1)) return

                prefs.edit { putInt("text_size", position) }
                SettingsManager.applyTextSize(this@SettingsActivity, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Spinner Language
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialLoad || position == prefs.getInt("language", 0)) return

                prefs.edit { putInt("language", position) }
                SettingsManager.applyLanguage(this@SettingsActivity, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Boton guardar
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
            SettingsManager.applyAll(this)
            Toast.makeText(this, getString(R.string.ezarpenak_gordeta), Toast.LENGTH_SHORT).show()
            recreate()
        }

        // Acerca de
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
            putBoolean("daltonismo", binding.switchDaltonismo.isChecked)
            putInt("text_size", binding.spinnerTextSize.selectedItemPosition)
            putInt("language", binding.spinnerLanguage.selectedItemPosition)
            apply()
        }
    }

    /**
     * Activa o desactiva el mute global de la aplicación.
     *
     * @param context Contexto para acceder al AudioManager
     * @param muted `true` para silenciar, `false` para restaurar sonido
     */
    private fun setAppMuteState(context: Context, muted: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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