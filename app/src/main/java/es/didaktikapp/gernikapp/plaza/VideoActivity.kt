package es.didaktikapp.gernikapp.plaza

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.net.toUri

/**
 * Activity de reproducción del video informativo sobre la plaza de Gernika.
 */
class VideoActivity : BaseMenuActivity() {

    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isTracking = false

    override fun getContentLayoutId() = R.layout.plaza_video

    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        videoView = findViewById(R.id.videoView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)

        // Check if activity was previously completed
        val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("video_completed", false)) {
            btnBack.isEnabled = true
        }

        iniciarActividad()
        setupVideoPlayer()
        setupVideoControls()
        setupButtons()
    }

    private fun setupVideoPlayer() {
        // Cargar el video desde raw resources
        val videoUri = "android.resource://${packageName}/${R.raw.plaza}".toUri()
        videoView.setVideoURI(videoUri)

        // Mostrar loading mientras se prepara el video
        progressBar.isVisible = true

        videoView.setOnPreparedListener {
            progressBar.isVisible = false
            seekBar.max = videoView.duration
            updateTimeDisplay()
            videoView.start()
            updatePlayPauseButton()
            updateSeekBar()
        }

        // Habilitar botón cuando el video termine
        videoView.setOnCompletionListener {
            enableButtonWithTransition()
            updatePlayPauseButton()
            completarActividad()

            // Save progress
            val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("video_completed", true).apply()
        }

        videoView.setOnErrorListener { _, _, _ ->
            progressBar.isVisible = false
            btnBack.isEnabled = true
            true
        }
    }

    private fun setupVideoControls() {
        // Botón Play/Pause
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            } else {
                videoView.start()
                updateSeekBar()
            }
            updatePlayPauseButton()
        }

        // SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                    updateTimeDisplay()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTracking = false
            }
        })
    }

    private fun updatePlayPauseButton() {
        val iconRes = if (videoView.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        btnPlayPause.setImageResource(iconRes)
    }

    private fun updateSeekBar() {
        if (!isTracking && videoView.isPlaying) {
            seekBar.progress = videoView.currentPosition
            updateTimeDisplay()
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    private fun updateTimeDisplay() {
        val current = videoView.currentPosition / 1000
        val total = videoView.duration / 1000
        tvTime.text = String.format(Locale.US, "%d:%02d / %d:%02d",
            current / 60, current % 60,
            total / 60, total % 60)
    }

    private fun setupButtons() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun enableButtonWithTransition() {
        val transition = ContextCompat.getDrawable(this, R.drawable.bg_boton_primario_transition) as? TransitionDrawable
        if (transition != null) {
            btnBack.background = transition
            btnBack.isEnabled = true
            transition.startTransition(600)
        } else {
            btnBack.isEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Plaza.ID, Puntos.Plaza.VIDEO)) {
                is Resource.Success -> actividadProgresoId = result.data.id
                is Resource.Error -> Log.e("Video", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> Log.d("Video", "Completado")
                is Resource.Error -> Log.e("Video", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}
