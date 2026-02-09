package es.didaktikapp.gernikapp.bunkers

import android.os.Handler
import android.os.Looper
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
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager

/**
 * Actividad del vídeo informativo del módulo *Bunkers*.
 *
 * Reproduce un vídeo educativo y registra el progreso del usuario
 * tanto localmente como en la API.
 *
 * @author Erlantz
 * @version 1.0
 */
class VideoBunkerActivity : BaseMenuActivity() {

    /** Repositorio para comunicación con la API. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad en la API. */
    private var actividadProgresoId: String? = null

    /** Vista de vídeo. */
    private lateinit var videoView: VideoView

    /** Botón play/pause. */
    private lateinit var btnPlayPause: ImageButton

    /** Barra de progreso del vídeo. */
    private lateinit var seekBar: SeekBar

    /** Texto que muestra tiempo actual / total. */
    private lateinit var tvTime: TextView

    /** Botón para volver al menú. */
    private lateinit var btnBack: Button

    /** Handler para actualizar la SeekBar periódicamente. */
    private val handler = Handler(Looper.getMainLooper())

    /** Indica si el usuario está arrastrando la SeekBar. */
    private var isTracking = false

    /** Layout asociado a esta actividad. */
    override fun getContentLayoutId() = R.layout.bunkers_video_bunker

    /**
     * Inicializa la actividad:
     * - Configura repositorios
     * - Carga el vídeo
     * - Configura controles
     * - Registra inicio en la API
     * - Gestiona progreso local
     */
    override fun onContentInflated() {
        LogManager.write(this@VideoBunkerActivity, "VideoBunkerActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        videoView = findViewById(R.id.videoFronton)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)
        btnBack = findViewById(R.id.btnBack)

        val prefs = getSharedPreferences("bunkers_progress", MODE_PRIVATE)

        iniciarActividad()

        if (prefs.getBoolean("video_bunker_completed", false)) {
            btnBack.isEnabled = true
        }

        val uri = "android.resource://${packageName}/${R.raw.babeslekuak_videoa}".toUri()
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener {
            seekBar.max = videoView.duration
            updateTimeDisplay()
        }

        // Evento al completar el vídeo
        videoView.setOnCompletionListener {
            updatePlayPauseButton()
            LogManager.write(this@VideoBunkerActivity, "Vídeo Bunkers completado")

            btnBack.isEnabled = true
            prefs.edit {
                putBoolean("video_bunker_completed", true)
                putFloat("video_bunker_score", 100f)
            }

            completarActividad()
        }

        // Botón play/pause
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            } else {
                videoView.start()
                LogManager.write(this, "Vídeo Bunkers iniciado")
                updateSeekBar()
            }
            updatePlayPauseButton()
        }

        // Control de SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
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
     * Pausa el vídeo al salir de la actividad.
     */
    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) videoView.pause()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Limpia callbacks al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Registra el inicio del evento en la API.
     * Si no existe un juegoId, no se puede registrar.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(
                juegoId,
                Puntos.Bunkers.ID,
                Puntos.Bunkers.VIDEO_BUNKER
            )) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@VideoBunkerActivity, "API iniciarActividad BUNKERS_VIDEO")
                }
                is Resource.Error -> {
                    LogManager.write(this@VideoBunkerActivity, "Error iniciarActividad BUNKERS_VIDEO: ${result.message}")
                }
                is Resource.Loading -> {}
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
                    LogManager.write(this@VideoBunkerActivity, "API completarActividad BUNKERS_VIDEO")
                }
                is Resource.Error -> {
                    LogManager.write(this@VideoBunkerActivity, "Error completarActividad BUNKERS_VIDEO: ${result.message}")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
