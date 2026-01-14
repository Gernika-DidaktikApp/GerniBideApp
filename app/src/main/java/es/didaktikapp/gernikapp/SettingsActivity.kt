package es.didaktikapp.gernikapp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchMute: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var spinnerTextSize: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSaveSettings: Button
    private lateinit var prefs: SharedPreferences

    /**
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadSettings()
        setupListeners()
    }

    private fun initViews() {
        switchMute = findViewById(R.id.switchMute)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        spinnerTextSize = findViewById(R.id.spinnerTextSize)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

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
            Toast.makeText(this, "Ezarpenerak gordeta!", Toast.LENGTH_SHORT).show()
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