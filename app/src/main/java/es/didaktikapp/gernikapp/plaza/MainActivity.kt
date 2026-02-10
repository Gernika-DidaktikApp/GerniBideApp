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
 *
 * @author Arantxa Main
 * @version 1.0
 */
class MainActivity : BaseMenuActivity() {

    /** Binding del layout plaza_main.xml. */
    private lateinit var binding: PlazaMainBinding

    /**
     * Inicializa la actividad:
     * - Registra el inicio en LogManager
     * - Infla el layout
     * - Configura los listeners de los botones del menú
     */
    override fun onContentInflated() {
        LogManager.write(this@MainActivity, "PlazaMainActivity iniciada")

        binding = PlazaMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    /**
     * Se ejecuta cada vez que la Activity vuelve a primer plano.
     * Actualiza el estado visual de las actividades completadas.
     */
    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    /**
     * Actualiza el aspecto visual de los botones según el progreso del usuario.
     *
     * - Cambia el fondo de los botones completados
     * - Muestra el botón de puntuación si la zona Plaza está completada
     */
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

    /**
     * Configura los listeners de los botones del menú principal.
     *
     * Cada botón navega a su actividad correspondiente:
     * - VideoActivity
     * - DragProductsActivity
     * - VerseGameActivity
     * - PhotoMissionActivity
     */
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