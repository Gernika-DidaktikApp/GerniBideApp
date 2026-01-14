package es.didaktikapp.gernikapp.fronton

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
 * @see R.layout.fronton_fronton_info
 */
class FrontonInfoActivity : AppCompatActivity() {

    /**
     * Metodo principal del ciclo de vida de la Activity.
     * Inicializa reproductor de vídeo con recurso raw/frontois.mp4,
     * controles play/pause y botón de navegación para volver hacia atras.
     *
     * @param savedInstanceState Bundle con el estado guardado de la Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_fronton_info)

        val videoView = findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayVideo)

        val uri = "android.resource://${packageName}/${R.raw.frontoia}".toUri()
        videoView.setVideoURI(uri)

        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = getString(R.string.videoa_erreproduzitu)
            } else {
                videoView.start()
                btnPlayPause.text = getString(R.string.videoa_gelditu)
            }
        }

        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }

    }

}