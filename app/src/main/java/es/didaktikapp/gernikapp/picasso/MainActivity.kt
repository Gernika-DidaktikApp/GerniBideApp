package es.didaktikapp.gernikapp.picasso

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PicassoMainBinding

class MainActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoMainBinding

    override fun onContentInflated() {
        binding = PicassoMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("picasso_progress", Context.MODE_PRIVATE)

        if (prefs.getBoolean("view_interpret_completed", false)) {
            binding.btnIkusiEtaAsmatu.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

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