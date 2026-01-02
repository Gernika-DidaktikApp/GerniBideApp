package es.didaktikapp.gernikapp.plazagernika

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import es.didaktikapp.gernikapp.R

class PlazaVideoActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var btnSiguiente: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaza_video)

        videoView = findViewById(R.id.videoView)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        progressBar = findViewById(R.id.progressBar)

        setupVideoPlayer()
        setupButtons()
    }

    private fun setupVideoPlayer() {
        // Configurar MediaController para controles de video
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Cargar el video desde raw resources
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.plaza}")
        videoView.setVideoURI(videoUri)

        // Mostrar loading mientras se prepara el video
        progressBar.isVisible = true

        videoView.setOnPreparedListener {
            progressBar.isVisible = false
            videoView.start()
        }

        // Habilitar botón cuando el video termine
        videoView.setOnCompletionListener {
            btnSiguiente.isEnabled = true
        }

        videoView.setOnErrorListener { _, _, _ ->
            progressBar.isVisible = false
            btnSiguiente.isEnabled = true // Permitir continuar incluso si hay error
            true
        }
    }

    private fun setupButtons() {
        btnSiguiente.setOnClickListener {
            val intent = Intent(this, ArrastrProductosActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }
}
