package es.didaktikapp.gernikapp

import android.content.SharedPreferences
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast

class SettingsActivity : BaseMenuActivity() {

    private lateinit var switchMute: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var spinnerTextSize: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSaveSettings: Button
    private lateinit var prefs: SharedPreferences

    override fun getContentLayoutId(): Int = R.layout.activity_settings

    override fun onContentInflated() {
        initViews()
        loadSettings()
        setupListeners()
    }

    private fun initViews() {
        switchMute = contentContainer.findViewById(R.id.switchMute)
        switchDarkMode = contentContainer.findViewById(R.id.switchDarkMode)
        spinnerTextSize = contentContainer.findViewById(R.id.spinnerTextSize)
        spinnerLanguage = contentContainer.findViewById(R.id.spinnerLanguage)
        btnSaveSettings = contentContainer.findViewById(R.id.btnSaveSettings)

        prefs = getSharedPreferences("GernikAppSettings", MODE_PRIVATE)
    }

    private fun loadSettings() {
        switchMute.isChecked = prefs.getBoolean("mute", false)
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        spinnerTextSize.setSelection(prefs.getInt("text_size", 1))
        spinnerLanguage.setSelection(prefs.getInt("language", 0))
    }

    private fun setupListeners() {
        btnSaveSettings.setOnClickListener {
            saveSettings()
            Toast.makeText(this, getString(R.string.ezarpenak_gordeta), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

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