package es.didaktikapp.gernikapp.fronton

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.MapaActivity
import es.didaktikapp.gernikapp.databinding.FrontonMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: FrontonMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FrontonMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnFrontonInfo.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        binding.btnPelota.setOnClickListener {
            startActivity(Intent(this, DancingBallActivity::class.java))
        }

        binding.btnVideoValores.setOnClickListener {
            startActivity(Intent(this, CestaTipActivity::class.java))
        }

        binding.btnBalioak.setOnClickListener {
            startActivity(Intent(this, ValuesGroupActivity::class.java))
        }

        binding.btnVolverMapa.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
            finish()
        }
    }
}