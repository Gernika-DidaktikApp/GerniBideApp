package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AudioQuizActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var voiceContainer: View
    private lateinit var quizContainer: View
    private lateinit var btnVolver: Button
    private lateinit var tvCongrats: TextView

    private var answeredCount = 0
    private val totalQuestions = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arbol_audio_quiz)

        voiceContainer = findViewById(R.id.voiceContainer)
        quizContainer = findViewById(R.id.quizContainer)
        btnVolver = findViewById(R.id.btnVolver)
        tvCongrats = findViewById(R.id.tvCongrats)

        // Reproducir audio
        mediaPlayer = MediaPlayer.create(this, R.raw.plaza) // cambiar este audio
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}
