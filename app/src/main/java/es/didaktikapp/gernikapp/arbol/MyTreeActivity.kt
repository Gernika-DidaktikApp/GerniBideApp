package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MyTreeActivity : AppCompatActivity() {

    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arbol_my_tree)

        btnBack = findViewById(R.id.btnBack)

        // Si ya estaba completada, habilitar botón
        val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("my_tree_completed", false)) {
            btnBack.isEnabled = true
        }

        setupButton(R.id.btnFriendship, R.color.valueFriendship, prefs)
        setupButton(R.id.btnFreedom, R.color.valueFreedom, prefs)
        setupButton(R.id.btnSolidarity, R.color.valueSolidarity, prefs)
        setupButton(R.id.btnRespect, R.color.valueRespect, prefs)
        setupButton(R.id.btnPeace, R.color.valuePeace, prefs)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupButton(buttonId: Int, colorId: Int, prefs: android.content.SharedPreferences) {
        findViewById<Button>(buttonId).setOnClickListener { button ->
            // Marcar como completada y habilitar botón
            btnBack.isEnabled = true
            prefs.edit().putBoolean("my_tree_completed", true).apply()

            val intent = Intent(this, InteractiveActivity::class.java).apply {
                putExtra("EXTRA_VALUE_TEXT", (button as Button).text.toString())
                putExtra("EXTRA_VALUE_COLOR", ContextCompat.getColor(this@MyTreeActivity, colorId))
            }
            startActivity(intent)
        }
    }
}
