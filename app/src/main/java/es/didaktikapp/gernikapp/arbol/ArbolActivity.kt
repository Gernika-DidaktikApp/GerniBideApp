package com.example.didaktikarbol

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

class ArbolActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arbol)

        voiceContainer = findViewById(R.id.voiceContainer)
        quizContainer = findViewById(R.id.quizContainer)
        btnStartPuzzle = findViewById(R.id.btnStartPuzzle)
        tvCongrats = findViewById(R.id.tvCongrats)

        // Reproducir audio
        mediaPlayer = MediaPlayer.create(this, R.raw.genikako_arbola)
        mediaPlayer.isLooping = false
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            showQuiz()
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
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
}
