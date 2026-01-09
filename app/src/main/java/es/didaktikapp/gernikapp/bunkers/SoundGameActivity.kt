package com.example.didaktikarbol

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SoundGameActivity : AppCompatActivity() {

    private lateinit var tvStars: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var categoryControls: View
    private lateinit var tvHistoryMessage: TextView
    private lateinit var btnNext: Button
    private lateinit var rootLayout: View

    private var stars = 0
    private var currentSoundId = -1
    private val totalSounds = 5
    private var mediaPlayer: MediaPlayer? = null

    // Mapping of sound buttons to "correct" category (0 for Beldurra, 1 for Babesa)
    private val soundCategories = mapOf(
        R.id.btnSound1 to 0, // Sirens -> Beldurra
        R.id.btnSound2 to 0, // Bombs -> Beldurra
        R.id.btnSound3 to 0, // Crying -> Beldurra
        R.id.btnSound4 to 1, // Breathing -> Babesa
        R.id.btnSound5 to 1  // Silence -> Babesa
    )

    private val soundResources = mapOf(
        R.id.btnSound1 to R.raw.sirenak,
        R.id.btnSound2 to R.raw.bonbak,
        R.id.btnSound3 to R.raw.haurren_negarrak,
        R.id.btnSound4 to R.raw.arnasa,
        R.id.btnSound5 to -1 // Special case for silence
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_game)

        tvStars = findViewById(R.id.tvStars)
        tvQuestion = findViewById(R.id.tvQuestion)
        categoryControls = findViewById(R.id.categoryControls)
        tvHistoryMessage = findViewById(R.id.tvHistoryMessage)
        btnNext = findViewById(R.id.btnNextActivity)
        rootLayout = findViewById(R.id.soundGameRoot)

        setupSoundButtons()
        setupCategoryButtons()

        btnNext.setOnClickListener {
            startActivity(Intent(this, ReflectionActivity::class.java))
            finish()
        }
    }

    private fun setupSoundButtons() {
        val buttons = listOf(
            R.id.btnSound1, R.id.btnSound2, R.id.btnSound3, R.id.btnSound4, R.id.btnSound5
        )

        buttons.forEach { id ->
            findViewById<ImageButton>(id).setOnClickListener {
                currentSoundId = id
                playSound(id)
                // Highlight selected sound
                resetSoundButtonColors()
                it.setBackgroundColor(getColor(R.color.btnPrincipal))
                
                // Show question and category selection
                tvQuestion.visibility = View.VISIBLE
                categoryControls.visibility = View.VISIBLE
            }
        }
    }

    private fun playSound(id: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        
        val resId = soundResources[id] ?: return
        if (resId != -1) {
            mediaPlayer = MediaPlayer.create(this, resId)
            mediaPlayer?.start()
        } else {
            // Silence - do nothing or play very short silence
            mediaPlayer = null
        }
    }

    private fun resetSoundButtonColors() {
        listOf(R.id.btnSound1, R.id.btnSound2, R.id.btnSound3, R.id.btnSound4, R.id.btnSound5).forEach {
            findViewById<ImageButton>(it).setBackgroundResource(R.drawable.bg_boton_secundario)
        }
    }

    private fun setupCategoryButtons() {
        findViewById<Button>(R.id.btnBeldurra).setOnClickListener {
            checkAnswer(0)
        }
        findViewById<Button>(R.id.btnBabesa).setOnClickListener {
            checkAnswer(1)
        }
    }

    private fun checkAnswer(category: Int) {
        if (currentSoundId == -1) return

        val correctCategory = soundCategories[currentSoundId]
        if (category == correctCategory) {
            stars++
            tvStars.text = "⭐ $stars / $totalSounds"
            
            // Change background color based on selection with a smooth transition
            val targetColor = if (category == 0) 
                android.graphics.Color.parseColor("#80F44336") // Soft Red
            else 
                android.graphics.Color.parseColor("#802196F3") // Soft Blue

            rootLayout.setBackgroundColor(targetColor)
            
            // Fade back to previous state after a delay
            rootLayout.postDelayed({
                rootLayout.setBackgroundResource(R.drawable.fondo6)
            }, 1000)

            currentSoundId = -1
            resetSoundButtonColors()
            
            // Hide question until next sound
            tvQuestion.visibility = View.INVISIBLE
            categoryControls.visibility = View.INVISIBLE

            if (stars >= totalSounds) {
                tvHistoryMessage.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
