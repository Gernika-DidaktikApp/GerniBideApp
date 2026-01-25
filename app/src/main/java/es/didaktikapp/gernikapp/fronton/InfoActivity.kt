package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Activity informativa del Frontón Jai Alai de Gernika.
 */
class InfoActivity : BaseMenuActivity() {

    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var eventoEstadoId: String? = null

    override fun getContentLayoutId() = R.layout.fronton_info

    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        val videoView = findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayVideo)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val prefs = getSharedPreferences("fronton_progress", Context.MODE_PRIVATE)

        iniciarEvento()

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("info_completed", false)) {
            btnBack.isEnabled = true
        }

        val uri = "android.resource://${packageName}/${R.raw.frontoia}".toUri()
        videoView.setVideoURI(uri)

        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = getString(R.string.videoa_erreproduzitu)
            } else {
                videoView.start()
                btnPlayPause.text = getString(R.string.videoa_gelditu)

                // Habilitar botón y guardar progreso al reproducir el vídeo
                btnBack.isEnabled = true
                prefs.edit().putBoolean("info_completed", true).apply()
                completarEvento()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

    }

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(juegoId, Actividades.Fronton.ID, Actividades.Fronton.INFO)) {
                is Resource.Success -> eventoEstadoId = result.data.id
                is Resource.Error -> Log.e("Info", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(estadoId, 100.0)) {
                is Resource.Success -> Log.d("Info", "Completado")
                is Resource.Error -> Log.e("Info", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

}