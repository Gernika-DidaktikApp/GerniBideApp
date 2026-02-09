package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager

/**
 * Activity de **Reflexión Emocional** donde el usuario selecciona **1 emoción** de 4 opciones.
 *
 * **Emociones disponibles:**
 * | ID Botón | Emoción |
 * |----------|---------|
 * | `btnBeldurra` | Beldurra (Miedo) |
 * | `btnTristura` | Tristura (Tristeza) |
 * | `btnLasaitasuna` | Lasaitasuna (Calma) |
 * | `btnItxaropena` | Itxaropena (Esperanza) |
 *
 * **Mecánica simple:**
 * 1. Usuario selecciona **UNA emoción**
 * 2. **Feedback visual**: Botón seleccionado crece (1.1x) + resto se atenúan (0.5f alpha, 0.9x)
 * 3. Se muestra `tvFeedback`
 * 4. Se marca como `reflection_completed = true`
 * 5. **Progreso 100%** inmediato en API
 *
 * **Diseño UX:**
 * - **No requiere completar todas** → Cualquier selección cuenta como completado
 * - **Feedback inmediato** visual y persistente
 * - **Estado restaurado** si ya estaba completado
 *
 * @author Telmo Castillo
 * @since 2026
 */
class ReflectionActivity : BaseMenuActivity() {

    /** Repositorios para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    /** Identificador del estado del evento activo en la API. */
    private var actividadProgresoId: String? = null

    /** @return Layout principal de la actividad de reflexión. */
    override fun getContentLayoutId() = R.layout.bunkers_reflection

    /**
     * Configura la actividad de reflexión emocional completa.
     *
     * **Secuencia de inicialización:**
     * 1. Inicializa repositorios y evento API
     * 2. Verifica progreso previo (`reflection_completed`)
     * 3. Configura los **4 botones de emociones**
     * 4. Feedback visual al seleccionar
     * 5. Habilita `btnBack` al completar
     */
    override fun onContentInflated() {
        LogManager.write(this@ReflectionActivity, "ReflectionActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        iniciarActividad()

        val tvFeedback: TextView = findViewById(R.id.tvFeedback)
        val btnBack: Button = findViewById(R.id.btnBack)
        val prefs = getSharedPreferences("bunkers_progress", MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón de retorno
        if (prefs.getBoolean("reflection_completed", false)) {
            btnBack.isEnabled = true
        }

        /**
         * **4 Emociones para seleccionar:**
         * - `btnBeldurra` → Miedo
         * - `btnTristura` → Tristeza
         * - `btnLasaitasuna` → Calma
         * - `btnItxaropena` → Esperanza
         */
        val emojiButtons = listOf(
            findViewById<View>(R.id.btnBeldurra),
            findViewById(R.id.btnTristura),
            findViewById(R.id.btnLasaitasuna),
            findViewById(R.id.btnItxaropena)
        )

        emojiButtons.forEach { button ->
            button.setOnClickListener {
                LogManager.write(this@ReflectionActivity, "Emoción seleccionada: ${button.resources.getResourceEntryName(button.id)}")

                // Mostrar feedback textual
                tvFeedback.visibility = View.VISIBLE

                // Marcar como completada y habilitar botón
                btnBack.isEnabled = true
                prefs.edit {
                    putBoolean("reflection_completed", true)
                    putFloat("reflection_score", 100f)
                }
                ZoneCompletionActivity.launchIfComplete(this@ReflectionActivity, ZoneConfig.BUNKERS)
                completarActividad()

                /**
                 * **Feedback visual dinámico:**
                 * ✅ **Seleccionado**: alpha=1.0f, scale=1.1x
                 * ❌ **Resto**: alpha=0.5f, scale=0.9x
                 */
                emojiButtons.forEach {
                    it.alpha = 0.5f
                    it.scaleX = 0.9f
                    it.scaleY = 0.9f
                }
                button.alpha = 1.0f
                button.scaleX = 1.1f
                button.scaleY = 1.1f
            }
        }

        btnBack.setOnClickListener {
            LogManager.write(this@ReflectionActivity, "Usuario salió de ReflectionActivity")
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Inicia el evento **"REFLECTION"** del módulo Bunkers en la API.
     * Se ejecuta automáticamente al cargar la actividad.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(
                juegoId,
                Puntos.Bunkers.ID,
                Puntos.Bunkers.REFLECTION
            )) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@ReflectionActivity, "API iniciarActividad BUNKERS_REFLECTION id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("Reflection", "Error: ${result.message}")
                    LogManager.write(this@ReflectionActivity, "Error iniciarActividad BUNKERS_REFLECTION: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento con **puntuación 100%** inmediatamente al seleccionar cualquier emoción.
     * **No requiere completar todas las opciones.**
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    Log.d("Reflection", "Completado")
                    LogManager.write(this@ReflectionActivity, "API completarActividad BUNKERS_REFLECTION puntuación=100")
                }
                is Resource.Error -> {
                    Log.e("Reflection", "Error: ${result.message}")
                    LogManager.write(this@ReflectionActivity, "Error completarActividad BUNKERS_REFLECTION: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}