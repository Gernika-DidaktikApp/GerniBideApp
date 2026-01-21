package es.didaktikapp.gernikapp.bunkers

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.databinding.BunkersMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: BunkersMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BunkersMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSoundGame.setOnClickListener {
            startActivity(Intent(this, SoundGameActivity::class.java))
        }


        binding.btnPeaceMural.setOnClickListener {
            startActivity(Intent(this, PeaceMuralActivity::class.java))
        }

        binding.btnReflection.setOnClickListener {
            startActivity(Intent(this, ReflectionActivity::class.java))
        }
    }
}