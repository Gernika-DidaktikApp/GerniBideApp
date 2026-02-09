package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.content.Context
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch

/**
 * **Juego de Identificación de Sonidos** de la Guerra Civil Española (5 sonidos).
 *
 * **Sonidos y categorías correctas:**
 * | Botón | Sonido | Categoría Correcta |
 * |-------|--------|-------------------|
 * | `btnSound1` | `sirenak` (Sirenas) | **Beldurra (0)** |
 * | `btnSound2` | `bonbak` (Bombas) | **Beldurra (0)** |
 * | `btnSound3` | `haurren_negarrak` (Llantos niños) | **Beldurra (0)** |
 * | `btnSound4` | `arnasa` (Respiración) | **Babesa (1)** |
 * | `btnSound5` | **Silencio** (`-1`) | **Babesa (1)** |
 *
 * **Flujo del juego:**
 * 1. **Pulsar sonido** → Reproduce + **resaltado azul** (`btnPrincipal`)
 * 2. **Elegir categoría** → **Feedback verde/rojo** (1 segundo)
 * 3. **Estrellas**: `⭐ aciertos/5`
 * 4. **Puntuación API**: `aciertos × 100`
 * 5. **Victoria**: 5 sonidos respondidos
 *
 * @author Telmo Castillo
 * @since 2026
 */
class SoundGameActivity : BaseMenuActivity() {

    /** Contador de estrellas (`⭐ X/5`). */
    private lateinit var tvStars: TextView

    /** Pregunta mostrada tras seleccionar sonido. */
    private lateinit var tvQuestion: TextView

    /** Contenedor de botones Beldurra/Babesa. */
    private lateinit var categoryControls: View

    /** Mensaje histórico al completar el juego. */
    private lateinit var tvHistoryMessage: TextView

    /** Botón de retorno (habilitado al completar). */
    private lateinit var btnBack: Button

    /** Layout raíz para feedback visual (verde/rojo). */
    private lateinit var rootLayout: View

    /** Repositorios para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    /** ID del estado del evento activo en la API. */
    private var actividadProgresoId: String? = null

    /** Contador de aciertos (0-5). */
    private var stars = 0

    /** ID del sonido actualmente seleccionado. */
    private var currentSoundId = -1

    /** Total de sonidos del juego. */
    private val totalSounds = 5

    /** Contador de sonidos respondidos. */
    private var answeredSounds = 0

    /** Reproductor de sonidos del juego. */
    private var mediaPlayer: MediaPlayer? = null

    /**
     * **Mapeo sonidos → Categoría correcta** (0=Beldurra, 1=Babesa).
     *
     * **Lógica educativa:**
     * - Sonidos de **miedo/peligro** → Beldurra (0)
     * - Sonidos de **protección/calma** → Babesa (1)
     */
    private val soundCategories = mapOf(
        R.id.btnSound1 to 0, // Sirenas → Beldurra
        R.id.btnSound2 to 0, // Bombas → Beldurra
        R.id.btnSound3 to 0, // Llantos niños → Beldurra
        R.id.btnSound4 to 1, // Respiración → Babesa
        R.id.btnSound5 to 1  // Silencio → Babesa
    )

    /**
     * **Mapeo sonidos → Recursos de audio**. `-1` = **silencio especial**.
     */
    private val soundResources = mapOf(
        R.id.btnSound1 to R.raw.sirenak,
        R.id.btnSound2 to R.raw.bonbak,
        R.id.btnSound3 to R.raw.haurren_negarrak,
        R.id.btnSound4 to R.raw.arnasa,
        R.id.btnSound5 to -1 // Silencio (no reproduce nada)
    )

    /** @return Layout principal del juego de sonidos. */
    override fun getContentLayoutId() = R.layout.bunkers_sound_game

    /**
     * Inicializa el juego completo.
     *
     * **Secuencia de inicialización:**
     * 1. Repositorios + evento API
     * 2. Verificar progreso (`sound_game_completed`)
     * 3. **Configurar 5 botones de sonido**
     * 4. **Configurar 2 botones de categoría**
     */
    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        tvStars = findViewById(R.id.tvStars)
        tvQuestion = findViewById(R.id.tvQuestion)
        categoryControls = findViewById(R.id.categoryControls)
        tvHistoryMessage = findViewById(R.id.tvHistoryMessage)
        btnBack = findViewById(R.id.btnBack)
        rootLayout = findViewById(R.id.soundGameRoot)

        iniciarActividad()

        val prefs = getSharedPreferences("bunkers_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("sound_game_completed", false)) {
            btnBack.isEnabled = true
        }

        setupSoundButtons()
        setupCategoryButtons()

        btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Configura los **5 botones de sonido** (sirenas, bombas, llantos, respiración, silencio).
     *
     * **Al pulsar:**
     * - Reproduce sonido (o silencio)
     * - **Resaltado azul** (`btnPrincipal`)
     * - Muestra pregunta + controles de categoría
     */
    private fun setupSoundButtons() {
        val buttons = listOf(
            R.id.btnSound1, R.id.btnSound2, R.id.btnSound3,
            R.id.btnSound4, R.id.btnSound5
        )

        buttons.forEach { id ->
            findViewById<ImageButton>(id).setOnClickListener {
                currentSoundId = id
                playSound(id)

                // Resaltar sonido activo
                resetSoundButtonColors()
                (it as ImageButton).backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.btnPrincipal)
                )

                tvQuestion.visibility = View.VISIBLE
                categoryControls.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Reproduce sonido seleccionado liberando reproductor anterior.
     *
     * **Casos especiales:**
     * - `resId > 0` → Reproduce audio normal
     * - `resId = -1` → **Silencio** (btnSound5)
     *
     * @param id ID del botón de sonido
     */
    private fun playSound(id: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()

        val resId = soundResources[id] ?: return
        if (resId != -1) {
            mediaPlayer = MediaPlayer.create(this, resId)
            mediaPlayer?.start()
        } else {
            // Silencio - no reproducir nada
            mediaPlayer = null
        }
    }

    /**
     * Resetea colores de botones **solo si están habilitados** (no respondidos).
     * Botones grisados mantienen su estado.
     */
    private fun resetSoundButtonColors() {
        listOf(R.id.btnSound1, R.id.btnSound2, R.id.btnSound3, R.id.btnSound4, R.id.btnSound5).forEach {
            val btn = findViewById<ImageButton>(it)
            if (btn.isEnabled) {
                btn.backgroundTintList = null
            }
        }
    }

    /**
     * Configura **categorías de respuesta**:
     * - `btnBeldurra` → `checkAnswer(0)`
     * - `btnBabesa` → `checkAnswer(1)`
     */
    private fun setupCategoryButtons() {
        findViewById<Button>(R.id.btnBeldurra).setOnClickListener {
            checkAnswer(0) // Beldurra = Miedo
        }
        findViewById<Button>(R.id.btnBabesa).setOnClickListener {
            checkAnswer(1) // Babesa = Protección
        }
    }

    /**
     * **Verifica respuesta** y aplica feedback completo.
     *
     * **Flujo detallado:**
     * 1. Compara con `soundCategories[currentSoundId]`
     * 2. **+1 estrella** si correcto → `⭐ X/5`
     * 3. **Deshabilita** botón sonido + **gris** (`#808080`)
     * 4. **Fondo verde/rojo** 1s (`#8032CD32` / `#80FF0000`)
     * 5. Oculta pregunta hasta siguiente sonido
     * 6. **Victoria** al completar 5 sonidos
     *
     * @param category 0=Beldurra, 1=Babesa
     */
    private fun checkAnswer(category: Int) {
        if (currentSoundId == -1) return

        val correctCategory = soundCategories[currentSoundId]!!
        val isCorrect = category == correctCategory

        if (isCorrect) {
            stars++
            tvStars.text = "⭐ $stars / $totalSounds"
        }

        // Deshabilitar sonido respondido
        val answeredButton = findViewById<ImageButton>(currentSoundId)
        answeredButton.isEnabled = false
        answeredButton.backgroundTintList = ColorStateList.valueOf(
            android.graphics.Color.parseColor("#808080") // Gris 50%
        )
        answeredSounds++

        // Feedback visual de fondo (1000ms)
        val targetColor = if (isCorrect)
            android.graphics.Color.parseColor("#8032CD32") // Verde correcto
        else
            android.graphics.Color.parseColor("#80FF0000") // Rojo incorrecto

        rootLayout.setBackgroundColor(targetColor)
        rootLayout.postDelayed({
            rootLayout.setBackgroundResource(R.drawable.fondo6)
        }, 1000)

        currentSoundId = -1
        resetSoundButtonColors()
        tvQuestion.visibility = View.INVISIBLE
        categoryControls.visibility = View.INVISIBLE

        // Completar juego (5/5 sonidos)
        if (answeredSounds >= totalSounds) {
            tvHistoryMessage.visibility = View.VISIBLE
            btnBack.isEnabled = true

            val score = stars * 100f
            val prefs = getSharedPreferences("bunkers_progress", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("sound_game_completed", true)
                .putFloat("sound_game_score", score)
                .apply()
            ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.BUNKERS)

            completarActividad()
        }
    }

    /**
     * Libera reproductor multimedia al destruir Activity.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Inicia evento **"SOUND_GAME"** del módulo Bunkers en la API.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(
                juegoId, Puntos.Bunkers.ID, Puntos.Bunkers.SOUND_GAME
            )) {
                is Resource.Success -> actividadProgresoId = result.data.id
                is Resource.Error -> Log.e("SoundGame", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa juego con **puntuación ponderada**: `aciertos × 100`.
     *
     * **Ejemplos:**
     * - 5/5 → `500.0`
     * - 3/5 → `300.0`
     * - 1/5 → `100.0`
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, stars * 100.0)) {
                is Resource.Success -> Log.d("SoundGame", "Completado: ${stars * 100}")
                is Resource.Error -> Log.e("SoundGame", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}