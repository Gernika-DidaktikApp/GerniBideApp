package es.didaktikapp.gernikapp.bunkers

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.BunkersMainBinding

class MainActivity : BaseMenuActivity() {

    private lateinit var binding: BunkersMainBinding

    override fun onContentInflated() {
        binding = BunkersMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("bunkers_progress", Context.MODE_PRIVATE)

        if (prefs.getBoolean("peace_mural_completed", false)) {
            binding.btnPeaceMural.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("reflection_completed", false)) {
            binding.btnReflection.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("sound_game_completed", false)) {
            binding.btnSoundGame.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }
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