package es.didaktikapp.gernikapp.plaza

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.databinding.PlazaMainBinding
import es.didaktikapp.gernikapp.utils.ZoneConfig

/**
 * Activity principal del módulo Plaza que gestiona el menú de actividades disponibles.
 */
class MainActivity : BaseMenuActivity() {

    private lateinit var binding: PlazaMainBinding

    override fun onContentInflated() {
        LogManager.write(this@MainActivity, "PlazaMainActivity iniciada")

        binding = PlazaMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    private fun updateCompletedActivities() {
        LogManager.write(this@MainActivity, "Actualizando actividades completadas del módulo Plaza")

        val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)

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

        // Mostrar botón de puntuación si la zona está completa
        if (ZoneCompletionActivity.isZoneComplete(this, ZoneConfig.PLAZA)) {
            binding.btnPuntuazioa.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.btnVideo.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a VideoActivity")
            startActivity(Intent(this, VideoActivity::class.java))
        }

        binding.btnMercado.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a DragProductsActivity")
            startActivity(Intent(this, DragProductsActivity::class.java))
        }

        binding.btnVersos.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a VerseGameActivity")
            startActivity(Intent(this, VerseGameActivity::class.java))
        }

        binding.btnFotos.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a PhotoMissionActivity")
            startActivity(Intent(this, PhotoMissionActivity::class.java))
        }

        binding.btnPuntuazioa.setOnClickListener {
            startActivity(Intent(this, ZoneCompletionActivity::class.java).apply {
                putExtra("zone_prefs_name", ZoneConfig.PLAZA.prefsName)
            })
        }
    }
}