package es.didaktikapp.gernikapp.bunkers

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.databinding.BunkersMainBinding
import es.didaktikapp.gernikapp.utils.ZoneConfig

/**
 * Activity principal del módulo **"Bunkers"** que actúa como **menú de navegación**.
 *
 * **Actividades del módulo:**
 * 1. **Mural de la Paz** (`PeaceMuralActivity`)
 * 2. **Reflexión** (`ReflectionActivity`)
 * 3. **Juego de Sonidos** (`SoundGameActivity`)
 *
 * **Funcionalidades:**
 * - **Indicadores visuales** de progreso: Botones cambian a `bg_boton_completado`
 * - Persistencia local mediante `SharedPreferences` (`bunkers_progress`)
 * - **Actualización dinámica** en `onResume()` al volver de sub-actividades
 * - Navegación directa a las 3 actividades del módulo
 *
 * **Flujo del usuario:**
 * 1. Entra al menú → Ve estado actual de actividades completadas
 * 2. Pulsa botón → Navega a actividad seleccionada
 * 3. Completa actividad → Vuelve con botón marcado como completado
 *
 * @author Telmo Castillo
 * @version 1.0
 */
class MainActivity : BaseMenuActivity() {

    /** Binding de vista generado para el layout `bunkers_main.xml`. */
    private lateinit var binding: BunkersMainBinding

    /**
     * Infla el layout principal del módulo Bunkers y configura los listeners de navegación.
     * Se ejecuta automáticamente tras `setContentView()` de la clase base.
     */
    override fun onContentInflated() {
        LogManager.write(this@MainActivity, "BunkersMainActivity iniciada")

        binding = BunkersMainBinding.inflate(layoutInflater, contentContainer, true)
        setupClickListeners()
    }

    /**
     * Actualiza visualmente el estado de las actividades cada vez que la Activity recupera el foco.
     *
     * **Se ejecuta en:**
     * - Primera carga del menú
     * - Retorno desde cualquier sub-actividad completada
     */
    override fun onResume() {
        super.onResume()
        updateCompletedActivities()
    }

    /**
     * Lee el progreso desde `SharedPreferences` (`bunkers_progress`) y aplica el drawable de "completado"
     * a los botones correspondientes.
     *
     * **Claves de SharedPreferences:**
     * | Actividad | Clave SP |
     * |-----------|----------|
     * | Mural de la Paz | `peace_mural_completed` |
     * | Reflexión | `reflection_completed` |
     * | Juego de Sonidos | `sound_game_completed` |
     *
     * **Drawable aplicado:** `R.drawable.bg_boton_completado`
     */
    private fun updateCompletedActivities() {
        LogManager.write(this@MainActivity, "Actualizando actividades completadas del módulo Bunkers")

        val prefs = getSharedPreferences("bunkers_progress", MODE_PRIVATE)

        // Vídeo completado
        if (prefs.getBoolean("video_bunker_completed", false)) {
            binding.btnVideoBunker.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Mural de la Paz completado
        if (prefs.getBoolean("peace_mural_completed", false)) {
            binding.btnPeaceMural.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Reflexión completada
        if (prefs.getBoolean("reflection_completed", false)) {
            binding.btnReflection.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Juego de Sonidos completado
        if (prefs.getBoolean("sound_game_completed", false)) {
            binding.btnSoundGame.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Mostrar botón de puntuación si la zona está completa
        if (ZoneCompletionActivity.isZoneComplete(this, ZoneConfig.BUNKERS)) {
            binding.btnPuntuazioa.visibility = View.VISIBLE
        }
    }

    /**
     * Configura los `OnClickListener` de los 3 botones principales del módulo Bunkers.
     *
     * **Mapeo de navegación:**
     * | Botón | Activity destino |
     * |-------|------------------|
     * | `btnSoundGame` | `SoundGameActivity` |
     * | `btnPeaceMural` | `PeaceMuralActivity` |
     * | `btnReflection` | `ReflectionActivity` |
     */
    private fun setupClickListeners() {
        binding.btnVideoBunker.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a VideoBunkerActivity")
            startActivity(Intent(this, VideoBunkerActivity::class.java))
        }

        binding.btnSoundGame.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a SoundGameActivity")
            startActivity(Intent(this, SoundGameActivity::class.java))
        }

        binding.btnPeaceMural.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a PeaceMuralActivity")
            startActivity(Intent(this, PeaceMuralActivity::class.java))
        }

        binding.btnReflection.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a ReflectionActivity")
            startActivity(Intent(this, ReflectionActivity::class.java))
        }

        binding.btnPuntuazioa.setOnClickListener {
            startActivity(Intent(this, ZoneCompletionActivity::class.java).apply {
                putExtra("zone_prefs_name", ZoneConfig.BUNKERS.prefsName)
            })
        }
    }
}