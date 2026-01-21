package es.didaktikapp.gernikapp.plaza

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.databinding.PlazaMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: PlazaMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlazaMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnVideo.setOnClickListener {
            startActivity(Intent(this, VideoActivity::class.java))
        }

        binding.btnMercado.setOnClickListener {
            startActivity(Intent(this, DragProductsActivity::class.java))
        }

        binding.btnVersos.setOnClickListener {
            startActivity(Intent(this, VerseGameActivity::class.java))
        }

        binding.btnFotos.setOnClickListener {
            startActivity(Intent(this, PhotoMissionActivity::class.java))
        }
    }
}