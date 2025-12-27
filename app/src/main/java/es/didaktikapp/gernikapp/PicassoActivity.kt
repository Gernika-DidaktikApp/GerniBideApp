package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.didaktikapp.gernikapp.databinding.ActivityPicassoBinding

class PicassoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPicassoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPicassoBinding.inflate(layoutInflater)
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
            startActivity(Intent(this, ViewAndInterpretActivity::class.java))
        }

        binding.btnNireMezua.setOnClickListener {
            // TODO: Implementar navegación a NireMezuaActivity cuando esté disponible
            Toast.makeText(this, "Actividad: Nire mezua munduarentzat", Toast.LENGTH_SHORT).show()
        }
    }
}