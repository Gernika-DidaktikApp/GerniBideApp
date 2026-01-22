package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.didaktikapp.gernikapp.databinding.PicassoMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: PicassoMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = PicassoMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnKolorezBakea.setOnClickListener {
            startActivity(Intent(this, ColorPeaceActivity::class.java))
        }

        binding.btnIkusiEtaAsmatu.setOnClickListener {
            startActivity(Intent(this, ViewInterpretActivity::class.java))
        }

        binding.btnNireMezua.setOnClickListener {
            startActivity(Intent(this, MyMessageActivity::class.java))
        }
    }
}