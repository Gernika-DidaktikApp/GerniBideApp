package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
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
 * @version 1.0
 */
class MainActivity : BaseMenuActivity() {

    companion object {
        private const val TAG = "ArbolMainActivity"
    }

    /** Binding de vista generado para el layout `arbol_main.xml`. */
    private lateinit var binding: ArbolMainBinding

    /**
     * Infla el layout principal del módulo Árbol y configura los listeners de navegación.
     * Se ejecuta automáticamente tras `setContentView()` de la clase base.
     */
    override fun onContentInflated() {
        LogManager.write(this@MainActivity, "ArbolMainActivity iniciada")

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
        LogManager.write(this@MainActivity, "Actualizando actividades completadas del módulo Árbol")

        val prefs = getSharedPreferences("arbol_progress", MODE_PRIVATE)

        // Audio Quiz completado
        val audioQuizCompleted = prefs.getBoolean("audio_quiz_completed", false)
        Log.d(TAG, "audio_quiz_completed = $audioQuizCompleted")
        if (audioQuizCompleted) {
            binding.btnAudioQuiz.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Puzzle completado
        val puzzleCompleted = prefs.getBoolean("puzzle_completed", false)
        Log.d(TAG, "puzzle_completed = $puzzleCompleted")
        if (puzzleCompleted) {
            binding.btnPuzzle.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
        }

        // Árbol Interactivo completado
        val interactiveCompleted = prefs.getBoolean("interactive_completed", false)
        Log.d(TAG, "interactive_completed = $interactiveCompleted")
        if (interactiveCompleted) {
            binding.btnInteractive.background =
                ContextCompat.getDrawable(this, R.drawable.bg_boton_completado)
            Log.d(TAG, "✅ Botón Interactive marcado como completado")
        } else {
            Log.d(TAG, "❌ Botón Interactive NO está completado")
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
            LogManager.write(this@MainActivity, "Navegando a AudioQuizActivity")
            startActivity(Intent(this, AudioQuizActivity::class.java))
        }

        binding.btnPuzzle.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a PuzzleActivity")
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        binding.btnInteractive.setOnClickListener {
            LogManager.write(this@MainActivity, "Navegando a InteractiveActivity")
            startActivity(Intent(this, InteractiveActivity::class.java))
        }

        binding.btnPuntuazioa.setOnClickListener {
            startActivity(Intent(this, ZoneCompletionActivity::class.java).apply {
                putExtra("zone_prefs_name", ZoneConfig.ARBOL.prefsName)
            })
        }
    }
}