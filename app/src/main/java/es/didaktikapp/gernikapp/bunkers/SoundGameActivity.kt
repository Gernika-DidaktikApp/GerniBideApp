package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R

import android.content.Context
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SoundGameActivity : AppCompatActivity() {

    private lateinit var tvStars: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var categoryControls: View
    private lateinit var tvHistoryMessage: TextView
    private lateinit var btnBack: Button
    private lateinit var rootLayout: View

    private var stars = 0
    private var currentSoundId = -1
    private val totalSounds = 5
    private var answeredSounds = 0
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
        setContentView(R.layout.bunkers_sound_game)

        tvStars = findViewById(R.id.tvStars)
        tvQuestion = findViewById(R.id.tvQuestion)
        categoryControls = findViewById(R.id.categoryControls)
        tvHistoryMessage = findViewById(R.id.tvHistoryMessage)
        btnBack = findViewById(R.id.btnBack)
        rootLayout = findViewById(R.id.soundGameRoot)

        val prefs = getSharedPreferences("bunkers_progress", Context.MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("sound_game_completed", false)) {
            btnBack.isEnabled = true
        }

        setupSoundButtons()
        setupCategoryButtons()

        btnBack.setOnClickListener {
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
                (it as ImageButton).backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.btnPrincipal)
                )

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
            val btn = findViewById<ImageButton>(it)
            // Only reset color for enabled buttons (not answered yet)
            if (btn.isEnabled) {
                btn.backgroundTintList = null
            }
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
        val isCorrect = category == correctCategory

        if (isCorrect) {
            stars++
            tvStars.text = "⭐ $stars / $totalSounds"
        }

        // Disable the answered sound button and make it gray
        val answeredButton = findViewById<ImageButton>(currentSoundId)
        answeredButton.isEnabled = false
        answeredButton.backgroundTintList = ColorStateList.valueOf(
            android.graphics.Color.parseColor("#808080") // Gray
        )
        answeredSounds++

        // Change background color based on correct/incorrect answer
        val targetColor = if (isCorrect)
            android.graphics.Color.parseColor("#8032CD32") // Soft Green for correct
        else
            android.graphics.Color.parseColor("#80FF0000") // Soft Red for incorrect

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

        if (answeredSounds >= totalSounds) {
            tvHistoryMessage.visibility = View.VISIBLE
            btnBack.isEnabled = true

            // Guardar progreso
            val prefs = getSharedPreferences("bunkers_progress", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("sound_game_completed", true).apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
