package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class AudioQuizActivity : BaseMenuActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var voiceContainer: View
    private lateinit var quizContainer: View
    private lateinit var btnStartPuzzle: Button
    private lateinit var tvCongrats: TextView

    private var answeredCount = 0
    private val totalQuestions = 3

    override fun getContentLayoutId(): Int = R.layout.arbol_audio_quiz

    override fun onContentInflated() {
        voiceContainer = contentContainer.findViewById(R.id.voiceContainer)
        quizContainer = contentContainer.findViewById(R.id.quizContainer)
        btnStartPuzzle = contentContainer.findViewById(R.id.btnStartPuzzle)
        tvCongrats = contentContainer.findViewById(R.id.tvCongrats)

        // Temporarily show quiz immediately since audio is missing
        showQuiz()

        // Setup SeekBar
        seekBar = contentContainer.findViewById(R.id.seekBarAudio)
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

        contentContainer.findViewById<ImageButton>(R.id.btnPlay).setOnClickListener {
            if (::mediaPlayer.isInitialized && !mediaPlayer.isPlaying) mediaPlayer.start()
        }
        contentContainer.findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) mediaPlayer.pause()
        }
        contentContainer.findViewById<ImageButton>(R.id.btnStop).setOnClickListener {
            if (::mediaPlayer.isInitialized) {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
                seekBar.progress = 0
            }
        }

        btnStartPuzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
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
            contentContainer.findViewById<Button>(id).setOnClickListener { button ->
                if (questionAnswered) return@setOnClickListener

                if (id == correctId) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.correcto))
                    questionAnswered = true
                    checkCompletion()
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
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
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}