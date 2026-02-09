package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.databinding.PicassoMainBinding
import es.didaktikapp.gernikapp.utils.ZoneConfig

/**
 * Activity principal del módulo Picasso - Guernica.
 * Muestra el menú de actividades relacionadas con la obra artística.
 *
 * Actividades disponibles:
 * - ColorPeaceActivity: Colorear el Guernica
 * - ViewInterpretActivity: Quiz de interpretación de elementos
 * - MyMessageActivity: Escribir mensajes de paz
 *
 * @property binding ViewBinding del layout picasso_main.xml
 *
 * Condiciones:
 * - Requiere SharedPreferences "picasso_progress" para el estado de actividades completadas
 * - Hereda de BaseMenuActivity para incluir el menú flotante FAB
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class MainActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoMainBinding

    override fun onContentInflated() {
        LogManager.write(this@MainActivity, "PicassoMainActivity iniciada")

        binding = PicassoMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    /**
     * Actualiza el aspecto visual de los botones de actividades completadas.
     * Lee el estado de progreso desde SharedPreferences y aplica el fondo correspondiente.
     */
    private fun updateCompletedActivities() {
        LogManager.write(this@MainActivity, "Actualizando actividades completadas en Picasso")

        val prefs = getSharedPreferences("picasso_progress", MODE_PRIVATE)

        if (prefs.getBoolean("color_peace_completed", false)) {
            binding.btnKolorezBakea.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("view_interpret_completed", false)) {
            binding.btnIkusiEtaAsmatu.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        if (prefs.getBoolean("my_message_completed", false)) {
            binding.btnNireMezua.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Mostrar botón de puntuación si la zona está completa
        if (ZoneCompletionActivity.isZoneComplete(this, ZoneConfig.PICASSO)) {
            binding.btnPuntuazioa.visibility = View.VISIBLE
        }
    }

    /**
     * Configura los listeners de los botones del menú principal.
     * Cada botón navega a su actividad correspondiente.
     */
    private fun setupClickListeners() {
        binding.btnKolorezBakea.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a ColorPeaceActivity")
            startActivity(Intent(this, ColorPeaceActivity::class.java))
        }

        binding.btnIkusiEtaAsmatu.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a ViewInterpretActivity")
            startActivity(Intent(this, ViewInterpretActivity::class.java))
        }

        binding.btnNireMezua.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a MyMessageActivity")
            startActivity(Intent(this, MyMessageActivity::class.java))
        }

        binding.btnPuntuazioa.setOnClickListener {
            startActivity(Intent(this, ZoneCompletionActivity::class.java).apply {
                putExtra("zone_prefs_name", ZoneConfig.PICASSO.prefsName)
            })
        }
    }
}