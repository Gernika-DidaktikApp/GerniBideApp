package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.widget.Button
import android.widget.VideoView
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

/**
 * Activity informativa del Frontón Jai Alai de Gernika.
 */
class InfoActivity : BaseMenuActivity() {

    override fun getContentLayoutId() = R.layout.fronton_info

    override fun onContentInflated() {
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