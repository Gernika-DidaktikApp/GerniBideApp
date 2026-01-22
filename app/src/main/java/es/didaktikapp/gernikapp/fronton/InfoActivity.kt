package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.R

/**
 * Activity informativa del Frontón Jai Alai de Gernika.
 * Muestra un vídeo descriptivo histórico del frontón más importante del mundo
 * donde se juega a Cesta Punta, junto con descripción detallada.
 *
 * @author Erlantz
 * @version 1.0
 * @see AppCompatActivity
 * @see R.layout.fronton_info
 */
class InfoActivity : AppCompatActivity() {

    /**
     * Metodo principal del ciclo de vida de la Activity.
     * Inicializa reproductor de vídeo con recurso raw/frontois.mp4,
     * controles play/pause y botón de navegación para volver hacia atras.
     *
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_info)

        val videoView = findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayVideo)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val prefs = getSharedPreferences("fronton_progress", Context.MODE_PRIVATE)

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
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

    }

}