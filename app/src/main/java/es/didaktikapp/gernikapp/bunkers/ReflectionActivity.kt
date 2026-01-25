package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

class ReflectionActivity : BaseMenuActivity() {

    // Repositorios para API
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var eventoEstadoId: String? = null

    override fun getContentLayoutId() = R.layout.bunkers_reflection

    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        iniciarEvento()

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
                completarEvento()

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

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(juegoId, Actividades.Bunkers.ID, Actividades.Bunkers.REFLECTION)) {
                is Resource.Success -> eventoEstadoId = result.data.id
                is Resource.Error -> Log.e("Reflection", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(estadoId, 100.0)) {
                is Resource.Success -> Log.d("Reflection", "Completado")
                is Resource.Error -> Log.e("Reflection", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}
