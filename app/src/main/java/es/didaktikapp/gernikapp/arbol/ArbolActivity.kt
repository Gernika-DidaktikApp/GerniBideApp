package es.didaktikapp.gernikapp.arbol

import android.content.Intent
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
import es.didaktikapp.gernikapp.R

class ArbolActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var voiceContainer: View
    private lateinit var quizContainer: View
    private lateinit var btnStartPuzzle: Button
    private lateinit var tvCongrats: TextView
    private lateinit var btnToQuiz: Button

    private var answeredCount = 0
    private val totalQuestions = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arbol)

        voiceContainer = findViewById(R.id.voiceContainer)
        quizContainer = findViewById(R.id.quizContainer)
        btnStartPuzzle = findViewById(R.id.btnStartPuzzle)
        tvCongrats = findViewById(R.id.tvCongrats)
        btnToQuiz = findViewById(R.id.btnToQuiz)

        // El botón para ir al cuestionario empieza oculto
        btnToQuiz.visibility = View.GONE

        // Reproducir audio
        val resId = resources.getIdentifier("genikako_arbola", "raw", packageName)
        if (resId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer.isLooping = false
                mediaPlayer.start()

                mediaPlayer.setOnCompletionListener {
                    // Solo cuando termina el audio mostramos el botón para continuar
                    btnToQuiz.visibility = View.VISIBLE
                }

                // Setup SeekBar
                seekBar = findViewById(R.id.seekBarAudio)
                seekBar.max = mediaPlayer.duration

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
                    if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) mediaPlayer.start()
                }
                findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
                    if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) mediaPlayer.pause()
                }
                findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
                    if (::mediaPlayer.isInitialized) {
                        mediaPlayer.pause()
                        mediaPlayer.seekTo(0)
                        seekBar.progress = 0
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                btnToQuiz.visibility = View.VISIBLE // Si falla el audio, permitimos continuar
            }
        } else {
            // Si el archivo no existe, ocultamos controles y permitimos continuar
            findViewById<View>(R.id.mediaControls).visibility = View.GONE
            findViewById<View>(R.id.seekBarAudio).visibility = View.GONE
            btnToQuiz.visibility = View.VISIBLE
        }

        // Al pulsar el botón (que solo aparece al terminar el audio), vamos al quiz
        btnToQuiz.setOnClickListener {
            showQuiz()
        }

        btnStartPuzzle.setOnClickListener {
            val intent = Intent(this, PuzzleActivity::class.java)
            startActivity(intent)
        }

        setupQuiz()
    }

    private fun showQuiz() {
        voiceContainer.visibility = View.GONE
        quizContainer.visibility = View.VISIBLE
    }

    private fun setupQuiz() {
        setupQuestion(listOf(R.id.q1a1, R.id.q1a2), R.id.q1a1)
        setupQuestion(listOf(R.id.q2a1, R.id.q2a2), R.id.q2a1)
        setupQuestion(listOf(R.id.q3a1, R.id.q3a2), R.id.q3a1)
    }

    private fun setupQuestion(buttonIds: List<Int>, correctId: Int) {
        var questionAnswered = false
        buttonIds.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                if (questionAnswered) return@setOnClickListener
                
                if (id == correctId) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.correct))
                    questionAnswered = true
                    checkCompletion()
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.incorrect))
                }
            }
        }
    }

    private fun checkCompletion() {
        answeredCount++
        if (answeredCount == totalQuestions) {
            tvCongrats.visibility = View.VISIBLE
            btnStartPuzzle.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
        handler.removeCallbacks(runnable)
    }
}
