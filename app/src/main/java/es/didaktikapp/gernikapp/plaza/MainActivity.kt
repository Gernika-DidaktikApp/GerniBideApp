package es.didaktikapp.gernikapp.plaza

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PlazaMainBinding

/**
 * Activity principal del módulo Plaza que gestiona el menú de actividades disponibles.
 */
class MainActivity : BaseMenuActivity() {

    private lateinit var binding: PlazaMainBinding

    override fun onContentInflated() {
        binding = PlazaMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)

        if (prefs.getBoolean("video_completed", false)) {
            binding.btnVideo.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("drag_products_completed", false)) {
            binding.btnMercado.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("verse_game_completed", false)) {
            binding.btnVersos.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("photo_mission_completed", false)) {
            binding.btnFotos.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }
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