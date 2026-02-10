package es.didaktikapp.gernikapp.fronton

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.databinding.FrontonMainBinding
import es.didaktikapp.gernikapp.utils.ZoneConfig

/**
 * Actividad principal del módulo *Frontón*.
 *
 * Esta pantalla actúa como menú de selección para las distintas actividades
 * educativas del Frontón Jai Alai de Gernika.
 *
 * @author Erlantz García
 * @version 1.0
 */
class MainActivity : BaseMenuActivity() {

    /** Binding generado para acceder a las vistas del layout. */
    private lateinit var binding: FrontonMainBinding

    /**
     * Infla el contenido y configura los listeners de los botones.
     */
    override fun onContentInflated() {
        binding = FrontonMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    /**
     * Se ejecuta al volver a la actividad.
     * Actualiza el estado visual de los botones según el progreso guardado.
     */
    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    /**
     * Actualiza el estilo de los botones según si cada actividad
     * del módulo Frontón ha sido completada.
     */
    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("fronton_progress", MODE_PRIVATE)

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

    /**
     * Configura los listeners de los botones para navegar
     * a las distintas actividades del módulo Frontón.
     */
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