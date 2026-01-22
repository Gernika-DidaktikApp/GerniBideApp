package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.FrontonMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: FrontonMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FrontonMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("fronton_progress", Context.MODE_PRIVATE)

        if (prefs.getBoolean("info_completed", false)) {
            binding.btnFrontonInfo.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("dancing_ball_completed", false)) {
            binding.btnPelota.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("cesta_tip_completed", false)) {
            binding.btnVideoValores.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("values_group_completed", false)) {
            binding.btnBalioak.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }
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
    }
}