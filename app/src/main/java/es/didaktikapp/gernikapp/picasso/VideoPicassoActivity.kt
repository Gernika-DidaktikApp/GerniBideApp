package es.didaktikapp.gernikapp.picasso

import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import java.util.Locale

class VideoPicassoActivity : BaseMenuActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnBack: Button
    private lateinit var imageView: ImageView

    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isTracking = false

    override fun getContentLayoutId() = R.layout.picasso_audio

    override fun onContentInflated() {
        LogManager.write(this, "VideoPicassoActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        imageView = findViewById(R.id.ivPicasso)
        seekBar = findViewById(R.id.seekBarAudio)
        tvTime = findViewById(R.id.tvTime)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnBack = findViewById(R.id.btnBack)

        val prefs = getSharedPreferences("picasso_progress", MODE_PRIVATE)

        iniciarActividad()

        if (prefs.getBoolean("audio_picasso_completed", false)) {
            btnBack.isEnabled = true
        }

        // Imagen
        imageView.setImageResource(R.drawable.picasso_kuadroa)

        // Audio
        mediaPlayer = MediaPlayer.create(this, R.raw.picasso_audio)
        seekBar.max = mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            updatePlayPauseButton()
            LogManager.write(this@VideoPicassoActivity, "Audio Picasso completado")

            btnBack.isEnabled = true
            prefs.edit {
                putBoolean("audio_picasso_completed", true)
                putFloat("audio_picasso_score", 100f)
            }
            ZoneCompletionActivity.launchIfComplete(this@VideoPicassoActivity, ZoneConfig.PICASSO)
            completarActividad()
        }

        btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
                updateSeekBar()
            }
            updatePlayPauseButton()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    updateTimeDisplay()
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                isTracking = true
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                isTracking = false
            }
        })

        btnBack.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun updatePlayPauseButton() {
        val icon = if (mediaPlayer.isPlaying)
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play

        btnPlayPause.setImageResource(icon)
    }

    private fun updateSeekBar() {
        if (!isTracking && mediaPlayer.isPlaying) {
            seekBar.progress = mediaPlayer.currentPosition
            updateTimeDisplay()
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    private fun updateTimeDisplay() {
        val current = mediaPlayer.currentPosition / 1000
        val total = mediaPlayer.duration / 1000
        tvTime.text = String.format(
            Locale.US,
            "%d:%02d / %d:%02d",
            current / 60, current % 60,
            total / 60, total % 60
        )
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(
                juegoId,
                Puntos.Picasso.ID,
                Puntos.Picasso.AUDIO_PICASSO
            )) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@VideoPicassoActivity, "API iniciarActividad PICASSO_AUDIO")
                }
                is Resource.Error -> {
                    LogManager.write(this@VideoPicassoActivity, "Error iniciarActividad PICASSO_AUDIO: ${result.message}")
                }
                else -> {}
            }
        }
    }

    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    LogManager.write(this@VideoPicassoActivity, "API completarActividad PICASSO_AUDIO")
                }
                is Resource.Error -> {
                    LogManager.write(this@VideoPicassoActivity, "Error completarActividad PICASSO_AUDIO: ${result.message}")
                }
                else -> {}
            }
        }
    }
}
