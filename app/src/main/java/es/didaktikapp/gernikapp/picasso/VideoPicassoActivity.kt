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

/**
 * Activity del módulo Picasso encargada de reproducir el audio explicativo sobre la obra de Picasso.
 *
 * @author Erlantz García
 * @version 1.0
 */
class VideoPicassoActivity : BaseMenuActivity() {

    /** Reproductor de audio que reproduce la narración de Picasso. */
    private lateinit var mediaPlayer: MediaPlayer

    /** Barra de progreso que muestra y controla la posición del audio. */
    private lateinit var seekBar: SeekBar

    /** Texto que muestra el tiempo actual y total del audio. */
    private lateinit var tvTime: TextView

    /** Botón para reproducir o pausar el audio. */
    private lateinit var btnPlayPause: ImageButton

    /** Botón para volver al menú principal del módulo Picasso. */
    private lateinit var btnBack: Button

    /** Imagen mostrada durante la reproducción del audio (cuadro de Picasso). */
    private lateinit var imageView: ImageView

    /** Repositorio para gestionar el inicio y finalización de actividades del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión que contiene tokens y el juegoId necesario para la API. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad devuelto por la API al iniciarla. */
    private var actividadProgresoId: String? = null

    /** Handler que actualiza la barra de progreso del audio en tiempo real. */
    private val handler = Handler(Looper.getMainLooper())

    /** Indica si el usuario está moviendo manualmente la SeekBar. */
    private var isTracking = false

    /**
     * Devuelve el layout asociado a esta actividad (picasso_audio.xml).
     */
    override fun getContentLayoutId() = R.layout.picasso_audio

    /**
     * Inicializa la actividad:
     * - Registra el inicio en el LogManager
     * - Configura vistas y listeners
     * - Inicia la actividad en la API
     * - Prepara el reproductor de audio y su SeekBar
     * - Marca la actividad como completada al finalizar el audio
     */
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

        imageView.setImageResource(R.drawable.picasso_kuadroa)

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

    /**
     * Actualiza el icono del botón de reproducción según el estado del MediaPlayer.
     */
    private fun updatePlayPauseButton() {
        val icon = if (mediaPlayer.isPlaying)
            android.R.drawable.ic_media_pause
        else
            android.R.drawable.ic_media_play

        btnPlayPause.setImageResource(icon)
    }

    /**
     * Actualiza la SeekBar mientras el audio está reproduciéndose.
     */
    private fun updateSeekBar() {
        if (!isTracking && mediaPlayer.isPlaying) {
            seekBar.progress = mediaPlayer.currentPosition
            updateTimeDisplay()
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    /**
     * Actualiza el texto que muestra el tiempo actual y total del audio.
     */
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

    /**
     * Pausa el audio cuando la actividad entra en segundo plano
     * y detiene las actualizaciones de la SeekBar.
     */
    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Libera los recursos del MediaPlayer al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Inicia la actividad en la API del juego.
     * Guarda el ID de progreso devuelto para completarla más tarde.
     */
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

    /**
     * Completa la actividad en la API enviando una puntuación de 100.
     * Se llama automáticamente cuando el audio termina.
     */
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
