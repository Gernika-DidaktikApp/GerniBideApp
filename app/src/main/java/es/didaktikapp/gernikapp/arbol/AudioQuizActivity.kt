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
    private var mediaPlayerPosition = 0
    private var quizVisible = false

    // Mapa para guardar las respuestas seleccionadas: pregunta -> botón seleccionado
    private val selectedAnswers = mutableMapOf<Int, Int>() // question index -> button ID
    private val answeredQuestions = mutableSetOf<Int>() // preguntas respondidas

    companion object {
        private const val KEY_EVENTO_ESTADO_ID = "evento_estado_id"
        private const val KEY_CORRECT_ANSWERS = "correct_answers"
        private const val KEY_ANSWERED_COUNT = "answered_count"
        private const val KEY_MEDIA_POSITION = "media_position"
        private const val KEY_QUIZ_VISIBLE = "quiz_visible"
        private const val KEY_SELECTED_ANSWERS = "selected_answers"
        private const val KEY_ANSWERED_QUESTIONS = "answered_questions"
    }

    override fun getContentLayoutId() = R.layout.arbol_audio_quiz

    override fun onContentInflated() {
        // Inicializar repositorios
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        voiceContainer = findViewById(R.id.voiceContainer)
        quizContainer = findViewById(R.id.quizContainer)
        btnVolver = findViewById(R.id.btnVolver)
        tvCongrats = findViewById(R.id.tvCongrats)

        // Restaurar estado si existe (después de rotación)
        if (savedInstanceState != null) {
            eventoEstadoId = savedInstanceState?.getString(KEY_EVENTO_ESTADO_ID)
            correctAnswers = savedInstanceState?.getInt(KEY_CORRECT_ANSWERS, 0) ?: 0
            answeredCount = savedInstanceState?.getInt(KEY_ANSWERED_COUNT, 0) ?: 0
            mediaPlayerPosition = savedInstanceState?.getInt(KEY_MEDIA_POSITION, 0) ?: 0
            quizVisible = savedInstanceState?.getBoolean(KEY_QUIZ_VISIBLE, false) ?: false

            // Restaurar respuestas seleccionadas
            savedInstanceState?.getIntArray(KEY_SELECTED_ANSWERS)?.let { answers ->
                for (i in answers.indices step 2) {
                    if (i + 1 < answers.size) {
                        selectedAnswers[answers[i]] = answers[i + 1]
                    }
                }
            }

            // Restaurar preguntas respondidas
            savedInstanceState?.getIntArray(KEY_ANSWERED_QUESTIONS)?.let { questions ->
                answeredQuestions.addAll(questions.toList())
            }
        }

        // Iniciar evento en la API solo si no hay estado guardado
        if (eventoEstadoId == null) {
            iniciarEvento()
        }

        // Reproducir audio
        mediaPlayer = MediaPlayer.create(this, R.raw.plaza) // cambiar este audio
        mediaPlayer.isLooping = false

        // Restaurar posición del audio si había estado guardado
        if (mediaPlayerPosition > 0) {
            mediaPlayer.seekTo(mediaPlayerPosition)
        }

        // Solo iniciar automáticamente si no venimos de una rotación
        if (!quizVisible) {
            mediaPlayer.start()
        }

        mediaPlayer.setOnCompletionListener {
            showQuiz()
        }

        // Si el quiz ya estaba visible, mostrarlo
        if (quizVisible) {
            showQuiz()
            // Restaurar el progreso visual
            if (answeredCount == totalQuestions) {
                tvCongrats.visibility = View.VISIBLE
                btnVolver.isEnabled = true
            }
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
        quizVisible = true
        voiceContainer.visibility = View.GONE
        quizContainer.visibility = View.VISIBLE
        btnVolver.visibility = View.VISIBLE
    }

    private fun setupQuiz() {
        // Q1 Correct: q1a1
        setupQuestion(0, listOf(R.id.q1a1, R.id.q1a2), R.id.q1a1)
        // Q2 Correct: q2a1
        setupQuestion(1, listOf(R.id.q2a1, R.id.q2a2), R.id.q2a1)
        // Q3 Correct: q3a1
        setupQuestion(2, listOf(R.id.q3a1, R.id.q3a2), R.id.q3a1)

        // Restaurar el estado visual de las respuestas después de configurar los listeners
        restoreQuizState()
    }

    private fun setupQuestion(questionIndex: Int, buttonIds: List<Int>, correctId: Int) {
        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                if (answeredQuestions.contains(questionIndex)) return@setOnClickListener

                answeredQuestions.add(questionIndex)
                selectedAnswers[questionIndex] = id

                if (id == correctId) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.correcto))
                    correctAnswers++
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
                    findViewById<Button>(correctId).setBackgroundColor(
                        ContextCompat.getColor(this, R.color.correcto)
                    )
                }
                checkCompletion()
            }
        }
    }

    private fun restoreQuizState() {
        // Restaurar colores de botones seleccionados
        val questions = listOf(
            Triple(0, listOf(R.id.q1a1, R.id.q1a2), R.id.q1a1),
            Triple(1, listOf(R.id.q2a1, R.id.q2a2), R.id.q2a1),
            Triple(2, listOf(R.id.q3a1, R.id.q3a2), R.id.q3a1)
        )

        questions.forEach { (questionIndex, buttonIds, correctId) ->
            if (answeredQuestions.contains(questionIndex)) {
                val selectedId = selectedAnswers[questionIndex]
                if (selectedId != null) {
                    // Aplicar color al botón seleccionado
                    val selectedButton = findViewById<Button>(selectedId)
                    if (selectedId == correctId) {
                        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.correcto))
                    } else {
                        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
                        // Mostrar también la respuesta correcta
                        findViewById<Button>(correctId).setBackgroundColor(
                            ContextCompat.getColor(this, R.color.correcto)
                        )
                    }
                }
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

    override fun onSaveInstanceState(outState: android.os.Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EVENTO_ESTADO_ID, eventoEstadoId)
        outState.putInt(KEY_CORRECT_ANSWERS, correctAnswers)
        outState.putInt(KEY_ANSWERED_COUNT, answeredCount)
        outState.putBoolean(KEY_QUIZ_VISIBLE, quizVisible)

        // Guardar posición actual del audio
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            outState.putInt(KEY_MEDIA_POSITION, mediaPlayer.currentPosition)
        } else {
            outState.putInt(KEY_MEDIA_POSITION, mediaPlayerPosition)
        }

        // Guardar respuestas seleccionadas (como pares: índice_pregunta, id_botón)
        val answersArray = mutableListOf<Int>()
        selectedAnswers.forEach { (questionIndex, buttonId) ->
            answersArray.add(questionIndex)
            answersArray.add(buttonId)
        }
        outState.putIntArray(KEY_SELECTED_ANSWERS, answersArray.toIntArray())

        // Guardar preguntas respondidas
        outState.putIntArray(KEY_ANSWERED_QUESTIONS, answeredQuestions.toIntArray())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}
