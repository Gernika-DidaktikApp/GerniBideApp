package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.MapaActivity
import es.didaktikapp.gernikapp.databinding.ArbolMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ArbolMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ArbolMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnInteractive.setOnClickListener {
            startActivity(Intent(this, InteractiveActivity::class.java))
        }

        binding.btnAudioQuiz.setOnClickListener {
            startActivity(Intent(this, AudioQuizActivity::class.java))
        }

        binding.btnPuzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        binding.btnMyTree.setOnClickListener {
            startActivity(Intent(this, MyTreeActivity::class.java))
        }

        binding.btnVolverMapa.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
            finish()
        }
    }
}