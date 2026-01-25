package es.didaktikapp.gernikapp.arbol

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.ArbolMainBinding

class MainActivity : BaseMenuActivity() {

    private lateinit var binding: ArbolMainBinding

    override fun onContentInflated() {
        binding = ArbolMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)

        if (prefs.getBoolean("audio_quiz_completed", false)) {
            binding.btnAudioQuiz.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("my_tree_completed", false)) {
            binding.btnMyTree.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("puzzle_completed", false)) {
            binding.btnPuzzle.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }
    }

    private fun setupClickListeners() {

        binding.btnAudioQuiz.setOnClickListener {
            startActivity(Intent(this, AudioQuizActivity::class.java))
        }

        binding.btnPuzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        binding.btnMyTree.setOnClickListener {
            startActivity(Intent(this, MyTreeActivity::class.java))
        }
    }
}