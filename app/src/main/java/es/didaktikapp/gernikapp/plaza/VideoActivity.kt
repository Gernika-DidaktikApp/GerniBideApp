package es.didaktikapp.gernikapp.plaza

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
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.net.toUri
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager

/**
 * Actividad de reproducción del vídeo informativo sobre la plaza de Gernika.
 * Incluye controles personalizados de reproducción (play/pause, barra de progreso)
 * y registra el progreso del usuario en la API al completar la visualización.
 *
 * @author Arantxa Main
 * @version 1.0
 */
class VideoActivity : BaseMenuActivity() {

    /** Vista que reproduce el vídeo del módulo Plaza. */
    private lateinit var videoView: VideoView

    /** Botón para reproducir o pausar el vídeo. */
    private lateinit var btnPlayPause: ImageButton

    /** Barra de progreso que muestra y controla la posición del vídeo. */
    private lateinit var seekBar: SeekBar

    /** Texto que muestra el tiempo actual y total del vídeo. */
    private lateinit var tvTime: TextView

    /** Botón para volver al menú principal del módulo Plaza. */
    private lateinit var btnBack: Button

    /** Indicador de carga mostrado mientras el vídeo se prepara. */
    private lateinit var progressBar: ProgressBar

    /** Repositorio para gestionar el progreso de la actividad en la API. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión que contiene tokens y el juegoId necesario para la API. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad devuelto por la API. */
    private var actividadProgresoId: String? = null

    /** Handler para actualizar la SeekBar en tiempo real. */
    private val handler = Handler(Looper.getMainLooper())

    /** Indica si el usuario está moviendo manualmente la SeekBar. */
    private var isTracking = false

    /** Devuelve el layout asociado a esta actividad. */
    override fun getContentLayoutId() = R.layout.plaza_video

    /**
     * Inicializa la actividad:
     * - Registra inicio en LogManager
     * - Inicializa repositorios y vistas
     * - Comprueba progreso previo
     * - Inicia actividad en la API
     * - Configura reproductor y controles
     */
    override fun onContentInflated() {
        LogManager.write(this@VideoActivity, "VideoActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        videoView = findViewById(R.id.videoView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)

        // Check if activity was previously completed
        val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)
        if (prefs.getBoolean("video_completed", false)) {
            btnBack.isEnabled = true
        }

        iniciarActividad()
        setupVideoPlayer()
        setupVideoControls()
        setupButtons()
    }

    /**
     * Configura el reproductor de vídeo:
     * - Carga el vídeo desde res/raw
     * - Muestra un indicador de carga mientras se prepara
     * - Inicia reproducción automática al estar listo
     * - Registra finalización y guarda progreso
     */
    private fun setupVideoPlayer() {
        // Cargar el video desde raw resources
        val videoUri = "android.resource://${packageName}/${R.raw.plaza}".toUri()
        videoView.setVideoURI(videoUri)

        // Mostrar loading mientras se prepara el video
        progressBar.isVisible = true

        videoView.setOnPreparedListener {
            LogManager.write(this@VideoActivity, "Video preparado, duración: ${videoView.duration} ms")
            progressBar.isVisible = false
            seekBar.max = videoView.duration
            updateTimeDisplay()
            videoView.start()
            updatePlayPauseButton()
            updateSeekBar()
        }

        // Habilitar botón cuando el video termine
        videoView.setOnCompletionListener {
            LogManager.write(this@VideoActivity, "Vídeo completado por el usuario")
            enableButtonWithTransition()
            updatePlayPauseButton()
            completarActividad()

            // Save progress
            val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)
            prefs.edit {
                putBoolean("video_completed", true)
                putFloat("video_score", 100f)
            }
            ZoneCompletionActivity.launchIfComplete(this@VideoActivity, ZoneConfig.PLAZA)
        }

        videoView.setOnErrorListener { _, _, _ ->
            progressBar.isVisible = false
            btnBack.isEnabled = true
            true
        }
    }

    /**
     * Configura los controles del vídeo:
     * - Botón Play/Pause
     * - SeekBar interactiva
     */
    private fun setupVideoControls() {
        // Botón Play/Pause
        btnPlayPause.setOnClickListener {
            LogManager.write( this@VideoActivity, if (videoView.isPlaying) "Vídeo pausado por el usuario" else "Vídeo reanudado por el usuario" )
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
                    LogManager.write(this@VideoActivity, "SeekBar movida por el usuario a $progress ms")
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

    /**
     * Actualiza el icono del botón Play/Pause según el estado del vídeo.
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
     * Actualiza la SeekBar mientras el vídeo está reproduciéndose.
     */
    private fun updateSeekBar() {
        if (!isTracking && videoView.isPlaying) {
            seekBar.progress = videoView.currentPosition
            updateTimeDisplay()
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    /**
     * Actualiza el texto que muestra el tiempo actual y total del vídeo.
     */
    private fun updateTimeDisplay() {
        val current = videoView.currentPosition / 1000
        val total = videoView.duration / 1000
        tvTime.text = String.format(Locale.US, "%d:%02d / %d:%02d",
            current / 60, current % 60,
            total / 60, total % 60)
    }

    /**
     * Configura el botón de retroceso para volver al menú Plaza.
     */
    private fun setupButtons() {
        btnBack.setOnClickListener {
            LogManager.write(this@VideoActivity, "Usuario salió de VideoActivity")
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Activa el botón de retroceso con una animación de transición.
     */
    private fun enableButtonWithTransition() {
        val transition = ContextCompat.getDrawable(this, R.drawable.bg_boton_secundario_transition) as? TransitionDrawable
        if (transition != null) {
            btnBack.background = transition
            btnBack.isEnabled = true
            transition.startTransition(600)
        } else {
            btnBack.isEnabled = true
        }
    }

    /**
     * Pausa el vídeo y detiene actualizaciones cuando la actividad entra en segundo plano.
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
     * Inicia la actividad en la API del juego.
     * Guarda el ID de progreso devuelto.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Plaza.ID, Puntos.Plaza.VIDEO)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@VideoActivity, "API iniciarActividad PLAZA_VIDEO id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("Video", "Error: ${result.message}")
                    LogManager.write(this@VideoActivity, "Error iniciarActividad PLAZA_VIDEO: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa la actividad en la API enviando una puntuación de 100.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    Log.d("Video", "Completado")
                    LogManager.write(this@VideoActivity, "API completarActividad PLAZA_VIDEO puntuación=100")
                }
                is Resource.Error -> {
                    Log.e("Video", "Error: ${result.message}")
                    LogManager.write(this@VideoActivity, "Error completarActividad PLAZA_VIDEO: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}
