package es.didaktikapp.gernikapp.bunkers

import android.content.Intent
import android.media.MediaPlayer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class SoundGameActivity : BaseMenuActivity() {

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

    private val soundCategories = mapOf(
        R.id.btnSound1 to 0,
        R.id.btnSound2 to 0,
        R.id.btnSound3 to 0,
        R.id.btnSound4 to 1,
        R.id.btnSound5 to 1
    )

    private val soundResources = mapOf(
        R.id.btnSound1 to R.raw.sirenak,
        R.id.btnSound2 to R.raw.bonbak,
        R.id.btnSound3 to R.raw.haurren_negarrak,
        R.id.btnSound4 to R.raw.arnasa,
        R.id.btnSound5 to -1
    )

    override fun getContentLayoutId(): Int = R.layout.bunkers_sound_game

    override fun onContentInflated() {
        tvStars = contentContainer.findViewById(R.id.tvStars)
        tvQuestion = contentContainer.findViewById(R.id.tvQuestion)
        categoryControls = contentContainer.findViewById(R.id.categoryControls)
        tvHistoryMessage = contentContainer.findViewById(R.id.tvHistoryMessage)
        btnNext = contentContainer.findViewById(R.id.btnNextActivity)
        rootLayout = contentContainer.findViewById(R.id.soundGameRoot)

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
            contentContainer.findViewById<ImageButton>(id).setOnClickListener {
                currentSoundId = id
                playSound(id)
                resetSoundButtonColors()
                it.setBackgroundColor(getColor(R.color.btnPrincipal))

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
            mediaPlayer = null
        }
    }

    private fun resetSoundButtonColors() {
        listOf(R.id.btnSound1, R.id.btnSound2, R.id.btnSound3, R.id.btnSound4, R.id.btnSound5).forEach {
            contentContainer.findViewById<ImageButton>(it).setBackgroundResource(R.drawable.bg_boton_secundario)
        }
    }

    private fun setupCategoryButtons() {
        contentContainer.findViewById<Button>(R.id.btnBeldurra).setOnClickListener {
            checkAnswer(0)
        }
        contentContainer.findViewById<Button>(R.id.btnBabesa).setOnClickListener {
            checkAnswer(1)
        }
    }

    private fun checkAnswer(category: Int) {
        if (currentSoundId == -1) return

        val correctCategory = soundCategories[currentSoundId]
        if (category == correctCategory) {
            stars++
            tvStars.text = "⭐ $stars / $totalSounds"

            val targetColor = if (category == 0)
                android.graphics.Color.parseColor("#80F44336")
            else
                android.graphics.Color.parseColor("#802196F3")

            rootLayout.setBackgroundColor(targetColor)

            rootLayout.postDelayed({
                rootLayout.setBackgroundResource(R.drawable.fondo6)
            }, 1000)

            currentSoundId = -1
            resetSoundButtonColors()

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