package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.databinding.FrontonMainBinding
import es.didaktikapp.gernikapp.utils.ZoneConfig

class MainActivity : BaseMenuActivity() {

    private lateinit var binding: FrontonMainBinding

    override fun onContentInflated() {
        binding = FrontonMainBinding.inflate(layoutInflater, contentContainer, true)
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

        // Mostrar botón de puntuación si la zona está completa
        if (ZoneCompletionActivity.isZoneComplete(this, ZoneConfig.FRONTON)) {
            binding.btnPuntuazioa.visibility = View.VISIBLE
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

        binding.btnPuntuazioa.setOnClickListener {
            startActivity(Intent(this, ZoneCompletionActivity::class.java).apply {
                putExtra("zone_prefs_name", ZoneConfig.FRONTON.prefsName)
            })
        }
    }
}