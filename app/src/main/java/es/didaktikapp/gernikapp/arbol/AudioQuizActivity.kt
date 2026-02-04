package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.content.Context
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
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

class AudioQuizActivity : BaseMenuActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var voiceContainer: View
    private lateinit var quizContainer: View
    private lateinit var btnVolver: Button
    private lateinit var tvCongrats: TextView

    // Repositorios para API
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    // Estado del evento
    private var eventoEstadoId: String? = null
    private var correctAnswers = 0

    private var answeredCount = 0
    private val totalQuestions = 3

    override fun getContentLayoutId() = R.layout.arbol_audio_quiz

    override fun onContentInflated() {
        // Inicializar repositorios
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        voiceContainer = findViewById(R.id.voiceContainer)
        quizContainer = findViewById(R.id.quizContainer)
        btnVolver = findViewById(R.id.btnVolver)
        tvCongrats = findViewById(R.id.tvCongrats)

        // Iniciar evento en la API
        iniciarEvento()

        // Reproducir audio
        mediaPlayer = MediaPlayer.create(this, R.raw.genikako_arbola)
        mediaPlayer.isLooping = false
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            showQuiz()
        }

        // Setup SeekBar
        seekBar = findViewById(R.id.seekBarAudio)
        if (::mediaPlayer.isInitialized) {
            seekBar.max = mediaPlayer.duration
        }

        runnable = Runnable {
            if (::mediaPlayer.isInitialized) {
                try {
                    seekBar.progress = mediaPlayer.currentPosition
                } catch (e: Exception) {}
            }
            handler.postDelayed(runnable, 500)
        }
        handler.postDelayed(runnable, 500)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    mediaPlayer.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            if (!mediaPlayer.isPlaying) mediaPlayer.start()
        }
        findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        }
        findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
            seekBar.progress = 0
        }

        btnVolver.setOnClickListener {
            finish()
        }

        setupQuiz()
    }

    private fun showQuiz() {
        voiceContainer.visibility = View.GONE
        quizContainer.visibility = View.VISIBLE
        btnVolver.visibility = View.VISIBLE
    }

    private fun setupQuiz() {
        // Q1 Correct: q1a1
        setupQuestion(listOf(R.id.q1a1, R.id.q1a2), R.id.q1a1)
        // Q2 Correct: q2a1
        setupQuestion(listOf(R.id.q2a1, R.id.q2a2), R.id.q2a1)
        // Q3 Correct: q3a1
        setupQuestion(listOf(R.id.q3a1, R.id.q3a2), R.id.q3a1)
    }

    private fun setupQuestion(buttonIds: List<Int>, correctId: Int) {
        var questionAnswered = false
        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                if (questionAnswered) return@setOnClickListener

                questionAnswered = true
                if (id == correctId) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.correcto))
                    correctAnswers++ // Contar respuesta correcta
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
                    // Mostrar la respuesta correcta
                    findViewById<Button>(correctId).setBackgroundColor(
                        ContextCompat.getColor(this, R.color.correcto)
                    )
                }
                checkCompletion()
            }
        }
    }

    private fun checkCompletion() {
        answeredCount++
        if (answeredCount == totalQuestions) {
            tvCongrats.visibility = View.VISIBLE
            btnVolver.isEnabled = true

            // Guardar progreso en SharedPreferences
            val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("audio_quiz_completed", true).apply()

            // Completar evento en la API con la puntuación
            completarEvento()
        }
    }

    /**
     * Inicia el evento en la API al entrar a la sub-actividad.
     */
    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId()

        if (juegoId == null) {
            Log.e("AudioQuiz", "No hay juegoId guardado")
            return
        }

        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(
                idJuego = juegoId,
                idActividad = Actividades.Arbol.ID,
                idEvento = Actividades.Arbol.AUDIO_QUIZ
            )) {
                is Resource.Success -> {
                    eventoEstadoId = result.data.id
                    Log.d("AudioQuiz", "Evento iniciado: $eventoEstadoId")
                }
                is Resource.Error -> {
                    Log.e("AudioQuiz", "Error al iniciar evento: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento en la API con la puntuación obtenida.
     * Puntuación = (respuestas correctas / total) * 100
     */
    private fun completarEvento() {
        val estadoId = eventoEstadoId

        if (estadoId == null) {
            Log.e("AudioQuiz", "No hay eventoEstadoId guardado")
            return
        }

        // Calcular puntuación (aciertos * 100)
        val puntuacion = correctAnswers * 100.0

        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(
                estadoId = estadoId,
                puntuacion = puntuacion
            )) {
                is Resource.Success -> {
                    Log.d("AudioQuiz", "Evento completado con puntuación: $puntuacion")
                }
                is Resource.Error -> {
                    Log.e("AudioQuiz", "Error al completar evento: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}
