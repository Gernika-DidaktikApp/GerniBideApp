package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.widget.Button
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class MainActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.arbol_main

    override fun onContentInflated() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        contentContainer.findViewById<Button>(R.id.btnInteractive).setOnClickListener {
            startActivity(Intent(this, InteractiveActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnAudioQuiz).setOnClickListener {
            startActivity(Intent(this, AudioQuizActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnPuzzle).setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnMyTree).setOnClickListener {
            startActivity(Intent(this, MyTreeActivity::class.java))
        }
    }
}