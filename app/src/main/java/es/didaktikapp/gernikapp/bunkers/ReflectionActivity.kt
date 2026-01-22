package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReflectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bunkers_reflection)

        val tvFeedback: TextView = findViewById(R.id.tvFeedback)
        val btnBack: Button = findViewById(R.id.btnBack)
        val prefs = getSharedPreferences("bunkers_progress", Context.MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("reflection_completed", false)) {
            btnBack.isEnabled = true
        }

        val emojiButtons = listOf(
            findViewById<View>(R.id.btnBeldurra),
            findViewById<View>(R.id.btnTristura),
            findViewById<View>(R.id.btnLasaitasuna),
            findViewById<View>(R.id.btnItxaropena)
        )

        emojiButtons.forEach { button ->
            button.setOnClickListener {
                tvFeedback.visibility = View.VISIBLE

                // Marcar como completada y habilitar botón
                btnBack.isEnabled = true
                prefs.edit().putBoolean("reflection_completed", true).apply()

                // Visual feedback for selection
                emojiButtons.forEach {
                    it.alpha = 0.5f
                    it.scaleX = 0.9f
                    it.scaleY = 0.9f
                }
                button.alpha = 1.0f
                button.scaleX = 1.1f
                button.scaleY = 1.1f
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
