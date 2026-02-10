package es.didaktikapp.gernikapp.fronton

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager

/**
 * Actividad informativa del módulo *Frontón*.
 *
 * Muestra un vídeo educativo sobre el Frontón Jai Alai de Gernika y registra
 * el progreso del usuario tanto localmente como en la API.
 *
 * @author Erlantz García
 * @version 1.0
 */
class InfoActivity : BaseMenuActivity() {

    /** Repositorio para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad en la API. */
    private var actividadProgresoId: String? = null

    /** Vista de vídeo que reproduce el contenido informativo. */
    private lateinit var videoView: VideoView

    /** Botón para alternar entre play y pause. */
    private lateinit var btnPlayPause: ImageButton

    /** Barra de progreso del vídeo. */
    private lateinit var seekBar: SeekBar

    /** Texto que muestra el tiempo actual y total del vídeo. */
    private lateinit var tvTime: TextView

    /** Botón para volver al menú anterior. */
    private lateinit var btnBack: Button

    /** Handler para actualizar la SeekBar periódicamente. */
    private val handler = Handler(Looper.getMainLooper())

    /** Indica si el usuario está arrastrando la SeekBar manualmente. */
    private var isTracking = false

    /** @return Layout principal de la actividad. */
    override fun getContentLayoutId() = R.layout.fronton_info

    /**
     * Inicializa la actividad:
     * - Configura repositorios.
     * - Registra el inicio del evento en la API.
     * - Prepara el reproductor de vídeo.
     * - Configura la SeekBar y los controles.
     * - Gestiona el progreso local y habilita el botón de volver.
     */
    override fun onContentInflated() {
        LogManager.write(this@InfoActivity, "InfoActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        videoView = findViewById(R.id.videoFronton)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        btnBack = findViewById(R.id.btnBack)

        val prefs = getSharedPreferences("fronton_progress", MODE_PRIVATE)

        iniciarActividad()

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("info_completed", false)) {
            btnBack.isEnabled = true
        }

        val uri = "android.resource://${packageName}/${R.raw.frontoia}".toUri()
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener {
            seekBar.max = videoView.duration
            updateTimeDisplay()
        }

        videoView.setOnCompletionListener {
            updatePlayPauseButton()
            LogManager.write(this@InfoActivity, "Vídeo Frontón completado")

            // Habilitar botón y guardar progreso al completar el vídeo
            btnBack.isEnabled = true
            prefs.edit {
                putBoolean("info_completed", true)
                putFloat("info_score", 100f)
            }
            ZoneCompletionActivity.launchIfComplete(this@InfoActivity, ZoneConfig.FRONTON)
            completarActividad()
        }

        // Control de reproducción toggle
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            } else {
                videoView.start()
                LogManager.write(this, "Vídeo Frontón iniciado")
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

        btnBack.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Actualiza el icono del botón play/pause según el estado del vídeo.
     */
    private fun updatePlayPauseButton() {
        val iconRes = if (videoView.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        btnPlayPause.setImageResource(iconRes)
    }

    /**
     * Actualiza la SeekBar cada 100 ms mientras el vídeo está reproduciéndose.
     */
    private fun updateSeekBar() {
        if (!isTracking && videoView.isPlaying) {
            seekBar.progress = videoView.currentPosition
            updateTimeDisplay()
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    /**
     * Actualiza el texto del tiempo actual y total del vídeo.
     */
    private fun updateTimeDisplay() {
        val current = videoView.currentPosition / 1000
        val total = videoView.duration / 1000
        tvTime.text = String.format(
            Locale.US,
            "%d:%02d / %d:%02d",
            current / 60, current % 60,
            total / 60, total % 60
        )
    }

    /**
     * Pausa el vídeo y detiene actualizaciones cuando la actividad queda en segundo plano.
     */
    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Limpia callbacks pendientes al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Registra el inicio del evento en la API.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Fronton.ID, Puntos.Fronton.INFO)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@InfoActivity, "API iniciarActividad Frontón INFO")
                }
                is Resource.Error -> {
                    Log.e("Info", "Error: ${result.message}")
                    LogManager.write(this@InfoActivity, "Error iniciarActividad INFO: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Envía la puntuación final (100 puntos) a la API para completar el evento.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    LogManager.write(this@InfoActivity, "API completarActividad Frontón INFO")
                    Log.d("Info", "Completado")
                }
                is Resource.Error -> {
                    Log.e("Info", "Error: ${result.message}")
                    LogManager.write(this@InfoActivity, "Error completarActividad INFO: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

}