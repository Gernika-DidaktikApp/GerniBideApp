package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import androidx.core.content.edit

/**
 * Activity que gestiona un cuestionario de audio dentro del módulo "Árbol" del juego.
 *
 * Esta clase reproduce un audio educativo, monitorea su progreso con una barra de seguimiento (SeekBar)
 * y, tras finalizar, muestra un quiz interactivo con tres preguntas de opción múltiple.
 *
 * Durante el flujo:
 * - Se inicializa y controla la reproducción del audio.
 * - Se supervisa el evento en la API a través de `GameRepository`.
 * - Se calcula y guarda la puntuación en el servidor al finalizar.
 *
 * @author Telmo Castillo
 * @since 2026
 */
class AudioQuizActivity : BaseMenuActivity() {

    /** Manejador del audio principal del cuestionario. */
    private lateinit var mediaPlayer: MediaPlayer

    /** Barra de progreso del audio. */
    private lateinit var seekBar: SeekBar

    /** TextView para mostrar el tiempo de reproducción. */
    private lateinit var tvTime: TextView

    /** Botón toggle para play/pause. */
    private lateinit var btnPlayPause: ImageButton

    /** Runnable que actualiza la barra de reproducción periódicamente. */
    private lateinit var runnable: Runnable

    /** Handler para programar actualizaciones del SeekBar en el hilo principal. */
    private var handler = Handler(Looper.getMainLooper())

    /** Flag para controlar si el usuario está arrastrando el SeekBar. */
    private var isTracking = false

    /** Contenedor de la interfaz de reproducción de voz. */
    private lateinit var voiceContainer: View

    /** Contenedor del cuestionario. */
    private lateinit var quizContainer: View

    /** Botón de retorno al menú anterior. */
    private lateinit var btnVolver: Button

    /** Texto de mensaje de felicitación tras finalizar el quiz. */
    private lateinit var tvCongrats: TextView

    /** Repositorio que gestiona comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor para operaciones con token y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** Identificador del estado del evento activo en la API. */
    private var actividadProgresoId: String? = null

    /** Número de respuestas correctas. */
    private var correctAnswers = 0

    /** Número de preguntas respondidas hasta el momento. */
    private var answeredCount = 0

    /** Número total de preguntas del cuestionario. */
    private val totalQuestions = 3

    /** @return Layout principal del contenido de la actividad. */
    override fun getContentLayoutId() = R.layout.arbol_audio_quiz

    /**
     * Inicializa la lógica principal una vez que el contenido ha sido inflado.
     * Configura los repositorios, la reproducción del audio, el monitoreo con la SeekBar
     * y el comportamiento del cuestionario.
     */
    override fun onContentInflated() {
        LogManager.write(this@AudioQuizActivity, "AudioQuizActivity iniciada")

        // Inicializar repositorios
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        voiceContainer = findViewById(R.id.voiceContainer)
        quizContainer = findViewById(R.id.quizContainer)
        btnVolver = findViewById(R.id.btnVolver)
        tvCongrats = findViewById(R.id.tvCongrats)
        seekBar = findViewById(R.id.seekBarAudio)
        tvTime = findViewById(R.id.tvTime)
        btnPlayPause = findViewById(R.id.btnPlayPause)

        // Iniciar evento en la API
        iniciarActividad()

        // Configurar y reproducir audio educativo
        mediaPlayer = MediaPlayer.create(this, R.raw.genikako_arbola)
        mediaPlayer.isLooping = false
        seekBar.max = mediaPlayer.duration
        mediaPlayer.start()
        LogManager.write(this@AudioQuizActivity, "Audio educativo del Árbol iniciado")
        updatePlayPauseButton()

        // Mostrar quiz al terminar el audio
        mediaPlayer.setOnCompletionListener {
            updatePlayPauseButton()
            LogManager.write(this, "Audio educativo del Árbol finalizado, mostrando quiz")
            showQuiz()
        }

        // Configuración del SeekBar
        runnable = Runnable {
            if (!isTracking && ::mediaPlayer.isInitialized) {
                try {
                    seekBar.progress = mediaPlayer.currentPosition
                    updateTimeDisplay()
                } catch (e: Exception) {}
            }
            handler.postDelayed(runnable, 100)
        }
        handler.postDelayed(runnable, 100)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(progress)
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

        // Control de reproducción toggle
        btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
                handler.postDelayed(runnable, 100)
            }
            updatePlayPauseButton()
        }

        btnVolver.setOnClickListener {
            finish()
        }

        setupQuiz()
    }

    /**
     * Cambia la visibilidad de los contenedores para mostrar el cuestionario
     * una vez que ha terminado el audio.
     */
    private fun showQuiz() {
        voiceContainer.visibility = View.GONE
        quizContainer.visibility = View.VISIBLE
        btnVolver.visibility = View.VISIBLE
    }

    /**
     * Actualiza el ícono del botón play/pause según el estado del MediaPlayer.
     */
    private fun updatePlayPauseButton() {
        val iconRes = if (mediaPlayer.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        btnPlayPause.setImageResource(iconRes)
    }

    /**
     * Actualiza el TextView de tiempo con el formato MM:SS / MM:SS.
     */
    private fun updateTimeDisplay() {
        val current = mediaPlayer.currentPosition / 1000
        val total = mediaPlayer.duration / 1000
        tvTime.text = String.format(
            java.util.Locale.US,
            "%d:%02d / %d:%02d",
            current / 60, current % 60,
            total / 60, total % 60
        )
    }

    /**
     * Configura las preguntas y respuestas del cuestionario.
     * Define qué botón es correcto para cada conjunto de opciones.
     */
    private fun setupQuiz() {
        setupQuestion(listOf(R.id.q1a1, R.id.q1a2), R.id.q1a1)
        setupQuestion(listOf(R.id.q2a1, R.id.q2a2), R.id.q2a1)
        setupQuestion(listOf(R.id.q3a1, R.id.q3a2), R.id.q3a1)
    }

    /**
     * Configura el comportamiento de una pregunta del quiz.
     *
     * @param buttonIds Lista de IDs de los botones posibles.
     * @param correctId ID del botón que representa la respuesta correcta.
     */
    private fun setupQuestion(buttonIds: List<Int>, correctId: Int) {
        var questionAnswered = false
        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                if (questionAnswered) return@setOnClickListener

                questionAnswered = true
                if (id == correctId) {
                    LogManager.write(this@AudioQuizActivity, "Respuesta correcta en Árbol (pregunta respondida)")
                    (button as Button).backgroundTintList = ContextCompat.getColorStateList(this, R.color.correcto)
                    correctAnswers++
                } else {
                    LogManager.write(this@AudioQuizActivity, "Respuesta incorrecta en Árbol (pregunta respondida)")
                    (button as Button).backgroundTintList = ContextCompat.getColorStateList(this, R.color.error)
                    findViewById<Button>(correctId).backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.correcto)
                }
                checkCompletion()
            }
        }
    }

    /**
     * Verifica si se han respondido todas las preguntas.
     * En ese caso, muestra el mensaje de felicitación, guarda el progreso localmente,
     * y reporta la finalización del evento a la API.
     */
    private fun checkCompletion() {
        answeredCount++
        if (answeredCount == totalQuestions) {
            LogManager.write(this@AudioQuizActivity, "Quiz del Árbol completado con $correctAnswers respuestas correctas")
            tvCongrats.visibility = View.VISIBLE
            btnVolver.isEnabled = true

            val score = correctAnswers * 100f
            val prefs = getSharedPreferences("arbol_progress", MODE_PRIVATE)
            prefs.edit {
                putBoolean("audio_quiz_completed", true)
                putFloat("audio_quiz_score", score)
            }
            ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.ARBOL)

            completarActividad()
        }
    }

    /**
     * Inicia el evento asociado a esta actividad en la API.
     * Se ejecuta al entrar en la subactividad.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId()

        if (juegoId == null) {
            Log.e("AudioQuiz", "No hay juegoId guardado")
            return
        }

        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(
                idJuego = juegoId,
                idPunto = Puntos.Arbol.ID,
                idActividad = Puntos.Arbol.AUDIO_QUIZ
            )) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    Log.d("AudioQuiz", "Evento iniciado: $actividadProgresoId")
                    LogManager.write(this@AudioQuizActivity, "API iniciarActividad Árbol AUDIO_QUIZ: id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("AudioQuiz", "Error al iniciar evento: ${result.message}")
                    LogManager.write(this@AudioQuizActivity, "Error iniciarActividad Árbol: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento en la API registrando la puntuación obtenida.
     *
     * La puntuación se calcula como `(respuestasCorrectas * 100)`.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId

        if (estadoId == null) {
            Log.e("AudioQuiz", "No hay actividadProgresoId guardado")
            return
        }

        val puntuacion = correctAnswers * 100.0

        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(
                progresoId = estadoId,
                puntuacion = puntuacion
            )) {
                is Resource.Success -> {
                    Log.d("AudioQuiz", "Evento completado con puntuación: $puntuacion")
                    LogManager.write(this@AudioQuizActivity, "API completarActividad Árbol con puntuación $puntuacion")
                }
                is Resource.Error -> {
                    Log.e("AudioQuiz", "Error al completar evento: ${result.message}")
                    LogManager.write(this@AudioQuizActivity, "Error completarActividad Árbol: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Libera los recursos del reproductor multimedia y
     * cancela actualizaciones pendientes al destruir la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}