package es.didaktikapp.gernikapp.arbol

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.databinding.ArbolMainBinding
import es.didaktikapp.gernikapp.utils.ZoneConfig

/**
 * Activity principal del módulo "Árbol" que actúa como **menú de navegación**.
 *
 * **Funcionalidades:**
 * - Muestra los 3 botones principales: Audio Quiz, Puzzle, Árbol Interactivo
 * - **Indicadores visuales** de progreso: Botones cambian a `bg_boton_completado` cuando se completan
 * - Persistencia local mediante `SharedPreferences` (`arbol_progress`)
 * - Navegación directa a las 3 sub-actividades del módulo
 *
 * **Flujo del usuario:**
 * 1. Entra al menú → Ve estado actual de actividades completadas
 * 2. Pulsa botón → Navega a la actividad seleccionada
 * 3. Completa actividad → Vuelve al menú con botón marcado como completado
 *
 * @author Telmo Castillo
 * @since 2026
 */
class MainActivity : BaseMenuActivity() {

    /** Binding de vista generado para el layout `arbol_main.xml`. */
    private lateinit var binding: ArbolMainBinding

    /**
     * Infla el layout principal del módulo Árbol y configura los listeners de navegación.
     * Se ejecuta automáticamente tras `setContentView()` de la clase base.
     */
    override fun onContentInflated() {
        binding = ArbolMainBinding.inflate(layoutInflater, contentContainer, true)
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
     * Lee el progreso desde `SharedPreferences` y aplica el drawable de "completado"
     * a los botones correspondientes.
     *
     * **Claves de SharedPreferences:**
     * - `audio_quiz_completed`
     * - `puzzle_completed`
     * - `interactive_completed`
     *
     * **Drawable aplicado:** `R.drawable.bg_boton_completado`
     */
    private fun updateCompletedActivities() {
        val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)

        // Audio Quiz completado
        if (prefs.getBoolean("audio_quiz_completed", false)) {
            binding.btnAudioQuiz.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Puzzle completado
        if (prefs.getBoolean("puzzle_completed", false)) {
            binding.btnPuzzle.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Árbol Interactivo completado
        if (prefs.getBoolean("interactive_completed", false)) {
            binding.btnInteractive.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Mostrar botón de puntuación si la zona está completa
        if (ZoneCompletionActivity.isZoneComplete(this, ZoneConfig.ARBOL)) {
            binding.btnPuntuazioa.visibility = View.VISIBLE
        }
    }

    /**
     * Configura los `OnClickListener` de los 3 botones principales.
     *
     * **Navegación:**
     * - `btnAudioQuiz` → `AudioQuizActivity`
     * - `btnPuzzle` → `PuzzleActivity`
     * - `btnInteractive` → `InteractiveActivity`
     */
    private fun setupClickListeners() {
        binding.btnAudioQuiz.setOnClickListener {
            startActivity(Intent(this, AudioQuizActivity::class.java))
        }

        binding.btnPuzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        binding.btnInteractive.setOnClickListener {
            startActivity(Intent(this, InteractiveActivity::class.java))
        }

        binding.btnPuntuazioa.setOnClickListener {
            startActivity(Intent(this, ZoneCompletionActivity::class.java).apply {
                putExtra("zone_prefs_name", ZoneConfig.ARBOL.prefsName)
            })
        }
    }
}