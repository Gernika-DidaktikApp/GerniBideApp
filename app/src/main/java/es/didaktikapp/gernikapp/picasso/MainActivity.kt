package es.didaktikapp.gernikapp.picasso

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.didaktikapp.gernikapp.R
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

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("picasso_progress", Context.MODE_PRIVATE)

        if (prefs.getBoolean("my_message_completed", false)) {
            binding.btnNireMezua.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }
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