package es.didaktikapp.gernikapp.fronton

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
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
 * Actividad del módulo *Frontón* que muestra un vídeo educativo y un pequeño
 * cuestionario tipo test relacionado con el contenido visualizado.
 *
 * @author Erlantz
 * @version 1.0
 */
class CestaTipActivity : BaseMenuActivity() {

    /** Repositorio para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad en la API. */
    private var actividadProgresoId: String? = null

    /** Vista de vídeo que reproduce el contenido educativo. */
    private lateinit var videoView: VideoView

    /** Botón para alternar entre play y pause. */
    private lateinit var btnPlayPause: ImageButton

    /** Barra de progreso del vídeo. */
    private lateinit var seekBar: SeekBar

    /** Texto que muestra el tiempo actual y total del vídeo. */
    private lateinit var tvTime: TextView

    /** Handler para actualizar la SeekBar periódicamente. */
    private val handler = Handler(Looper.getMainLooper())

    /** Indica si el usuario está arrastrando la SeekBar manualmente.*/
    private var isTracking = false

    /** @return Layout principal de la actividad. */
    override fun getContentLayoutId() = R.layout.fronton_cesta_tip

    /**
     * Inicializa la actividad:
     * - Configura repositorios.
     * - Registra el inicio del evento en la API.
     * - Prepara el reproductor de vídeo.
     * - Configura la SeekBar y los controles.
     * - Configura el cuestionario y el botón de confirmación.
     */
    override fun onContentInflated() {
        LogManager.write(this@CestaTipActivity, "CestaTipActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)
        iniciarActividad()

        videoView = findViewById(R.id.videoFronton)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)

        val uri = "android.resource://${packageName}/${R.raw.fronton_jarduerarako_bideoa}".toUri()
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener {
            seekBar.max = videoView.duration
            updateTimeDisplay()
        }

        videoView.setOnCompletionListener {
            updatePlayPauseButton()
        }

        // Control de reproducción toggle
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

        val radioGroup = findViewById<RadioGroup>(R.id.opcionesGroup)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val prefs = getSharedPreferences("fronton_progress", MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("cesta_tip_completed", false)) {
            btnBack.isEnabled = true
        }

        btnConfirmar.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            LogManager.write(this, "Respuesta seleccionada en CestaTip: $selectedId")
            if (selectedId != -1) {
                // Comparar por ID en vez de texto para que funcione en cualquier idioma
                val correctIds = listOf(R.id.op1, R.id.op2) // Lankidetza y Errespetua
                if (selectedId in correctIds) {
                    LogManager.write(this@CestaTipActivity, "Respuesta correcta en CestaTip")
                    Toast.makeText(this, getString(R.string.erantzun_zuzena), Toast.LENGTH_SHORT).show()
                } else {
                    LogManager.write(this@CestaTipActivity, "Respuesta incorrecta en CestaTip")
                    Toast.makeText(this, getString(R.string.erantzun_okerra), Toast.LENGTH_SHORT).show()
                }

                // Deshabilitar para que solo se pueda contestar una vez
                btnConfirmar.isEnabled = false
                for (i in 0 until radioGroup.childCount) {
                    radioGroup.getChildAt(i).isEnabled = false
                }

                btnBack.isEnabled = true
                prefs.edit {
                    putBoolean("cesta_tip_completed", true)
                    putFloat("cesta_tip_score", 100f)
                }
                ZoneCompletionActivity.launchIfComplete(this@CestaTipActivity, ZoneConfig.FRONTON)
                completarActividad()

            } else {
                Toast.makeText(this, getString(R.string.hautatu_aukera_bat), Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Registra el inicio del evento en la API.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Fronton.ID, Puntos.Fronton.CESTA_TIP)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@CestaTipActivity, "API iniciarActividad CESTA_TIP")
                }
                is Resource.Error -> {
                    Log.e("CestaTip", "Error: ${result.message}")
                    LogManager.write(this@CestaTipActivity, "Error iniciarActividad CESTA_TIP: ${result.message}")
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
                    Log.d("CestaTip", "Completado")
                    LogManager.write(this@CestaTipActivity, "API completarActividad CESTA_TIP")
                }
                is Resource.Error -> {
                    Log.e("CestaTip", "Error: ${result.message}")
                    LogManager.write(this@CestaTipActivity, "Error completarActividad CESTA_TIP: ${result.message}")
                }
                is Resource.Loading -> { }
            }
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
}