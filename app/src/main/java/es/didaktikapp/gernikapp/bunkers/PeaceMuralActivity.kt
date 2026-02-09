package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Activity del **Mural de la Paz** donde los usuarios añaden palabras positivas al mural.
 *
 * **Mecánica de juego:**
 * 1. **Fondo musical** continuo (`babeslekuak_bideoaren_audioa`)
 * 2. **4 palabras fijas**: Itxaropena, Elkarbizitza, Laguntza, Adiskidetasuna
 * 3. **Efecto visual**: Texto aparece con **animación escala + fade-in**
 * 4. **Persistencia**: Posiciones guardadas en `PeaceMuralPrefs` (`mural_data`)
 * 5. **Progreso**: Se marca como completado al añadir **cualquier palabra**
 *
 * **Características:**
 * - **Control de volumen** (mute/unmute) con `ImageButton`
 * - **Estado restaurado** al reiniciar la Activity
 * - **Integración API** completa (iniciar/completar evento)
 *
 * @author Telmo Castillo
 * @since 2026
 */
class PeaceMuralActivity : BaseMenuActivity() {

    /** Contenedor principal donde se posicionan las palabras del mural. */
    private lateinit var muralContainer: FrameLayout

    /** Mensaje final de felicitación mostrado al completar. */
    private lateinit var tvFinalCongrats: TextView

    /** Botón de retorno, habilitado solo al completar el mural. */
    private lateinit var btnBack: Button

    /** SharedPreferences para datos del mural (texto|x|y). */
    private val sharedPrefs by lazy { getSharedPreferences("PeaceMuralPrefs", MODE_PRIVATE) }

    /** SharedPreferences para progreso del módulo Bunkers. */
    private val progressPrefs by lazy { getSharedPreferences("bunkers_progress", MODE_PRIVATE) }

    /** Reproductor de música de fondo en bucle continuo. */
    private var mediaPlayer: MediaPlayer? = null

    /** Repositorios para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    /** Identificador del estado del evento activo en la API. */
    private var actividadProgresoId: String? = null

    /** @return Layout principal del mural de la paz. */
    override fun getContentLayoutId() = R.layout.bunkers_peace_mural

    /**
     * Inicializa la actividad completa:
     *
     * **Secuencia:**
     * 1. Repositorios (GameRepository, TokenManager)
     * 2. Iniciar evento API
     * 3. Verificar progreso previo
     * 4. **Cargar mural guardado** (`loadMural()`)
     * 5. Configurar botones + música
     */
    override fun onContentInflated() {
        LogManager.write(this@PeaceMuralActivity, "PeaceMuralActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        muralContainer = findViewById(R.id.muralContainer)
        tvFinalCongrats = findViewById(R.id.tvFinalCongrats)
        btnBack = findViewById(R.id.btnBack)

        iniciarActividad()

        // Si ya estaba completada, habilitar botón
        if (progressPrefs.getBoolean("peace_mural_completed", false)) {
            btnBack.isEnabled = true
        }

        loadMural()
        setupWordButtons()

        btnBack.setOnClickListener {
            LogManager.write(this@PeaceMuralActivity, "Usuario salió de PeaceMuralActivity")
            mediaPlayer?.stop()
            mediaPlayer?.release()
            finish()
        }
    }

    /**
     * Configura los **4 botones de palabras positivas** del mural.
     *
     * **Palabras disponibles:**
     * | ID Botón | Texto |
     * |----------|-------|
     * | `btnItxaropenaVal` | Itxaropena |
     * | `btnElkarbizitzaVal` | Elkarbizitza |
     * | `btnLaguntzaVal` | Laguntza |
     * | `btnAdiskidetasunaVal` | Adiskidetasuna |
     *
     * **Al pulsar cualquiera:**
     * - Añade palabra al mural con animación
     * - Marca como `peace_mural_completed = true`
     * - Habilita `btnBack`
     * - Completa evento API (100%)
     */
    private fun setupWordButtons() {
        val buttons = listOf(
            R.id.btnItxaropenaVal,
            R.id.btnElkarbizitzaVal,
            R.id.btnLaguntzaVal,
            R.id.btnAdiskidetasunaVal
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                val text = (it as Button).text.toString()

                LogManager.write(this@PeaceMuralActivity, "Palabra añadida al mural: $text")

                addWordToMural(text, isNew = true)
                tvFinalCongrats.visibility = View.VISIBLE

                // Marcar como completada
                btnBack.isEnabled = true
                progressPrefs.edit()
                    .putBoolean("peace_mural_completed", true)
                    .putFloat("peace_mural_score", 100f)
                    .apply()
                ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.BUNKERS)
                completarActividad()
            }
        }
    }

    /**
     * Añade una palabra al mural en posición aleatoria o específica.
     *
     * **Estilo aleatorio:**
     * - Tamaño: 20-35sp
     * - Color: `Random.nextInt()`
     * - Fuente: **Negrita**
     * - Posición: Aleatoria dentro de límites seguros
     *
     * @param text Texto de la palabra
     * @param x Coordenada X (null = aleatoria)
     * @param y Coordenada Y (null = aleatoria)
     * @param isNew Si es nueva, aplica animación y la guarda
     */
    private fun addWordToMural(text: String, x: Float? = null, y: Float? = null, isNew: Boolean = false) {
        val textView = TextView(this).apply {
            this.text = text
            this.textSize = Random.nextInt(20, 35).toFloat()
            this.setTextColor(Random.nextInt())
            this.setTypeface(null, android.graphics.Typeface.BOLD)
        }

        muralContainer.post {
            val finalX = x ?: Random.nextInt(50, (muralContainer.width - 200).coerceAtLeast(51)).toFloat()
            val finalY = y ?: Random.nextInt(50, (muralContainer.height - 100).coerceAtLeast(51)).toFloat()

            textView.x = finalX
            textView.y = finalY

            muralContainer.addView(textView)

            if (isNew) {
                applyAnimation(textView)
                saveWord(text, finalX, finalY)
            }
        }
    }

    /**
     * **Animación de entrada**: Escala (0.5→1.2) + Fade-in (0→1) en 500ms.
     *
     * @param view TextView a animar
     */
    private fun applyAnimation(view: View) {
        val animSet = AnimationSet(true)

        val scale = ScaleAnimation(0.5f, 1.2f, 0.5f, 1.2f, view.width / 2f, view.height / 2f)
        scale.duration = 500

        val fade = AlphaAnimation(0f, 1f)
        fade.duration = 500

        animSet.addAnimation(scale)
        animSet.addAnimation(fade)
        view.startAnimation(animSet)
    }

    /**
     * **Persiste palabra** en formato `texto|x|y` separado por `;`.
     * Ejemplo: `"Itxaropena|150.5|200.3;Elkarbizitza|300.2|450.1"`
     *
     * @param text Texto de la palabra
     * @param x Posición X absoluta
     * @param y Posición Y absoluta
     */
    private fun saveWord(text: String, x: Float, y: Float) {
        LogManager.write(this@PeaceMuralActivity, "Guardando palabra '$text' en posición ($x, $y)")

        val currentData = sharedPrefs.getString("mural_data", "") ?: ""
        val newData = if (currentData.isEmpty()) "$text|$x|$y" else "$currentData;$text|$x|$y"
        sharedPrefs.edit().putString("mural_data", newData).apply()
    }

    /**
     * **Restaura mural** desde `PeaceMuralPrefs` sin animaciones.
     * Parsea formato `texto|x|y` y recrea TextViews en las posiciones guardadas.
     */
    private fun loadMural() {
        val currentData = sharedPrefs.getString("mural_data", "") ?: ""

        LogManager.write(this@PeaceMuralActivity, "Cargando mural guardado: ${currentData.length} caracteres")

        if (currentData.isNotEmpty()) {
            currentData.split(";").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size == 3) {
                    addWordToMural(parts[0], parts[1].toFloat(), parts[2].toFloat(), isNew = false)
                }
            }
        }
    }

    /**
     * Libera recursos del reproductor multimedia al destruir la Activity.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Inicia el evento **"PEACE_MURAL"** del módulo Bunkers en la API.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Bunkers.ID, Puntos.Bunkers.PEACE_MURAL)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@PeaceMuralActivity, "API iniciarActividad BUNKERS_PEACE_MURAL id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("PeaceMural", "Error: ${result.message}")
                    LogManager.write(this@PeaceMuralActivity, "Error iniciarActividad BUNKERS_PEACE_MURAL: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento con **puntuación 100%** al añadir cualquier palabra.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    Log.d("PeaceMural", "Completado")
                    LogManager.write(this@PeaceMuralActivity, "API completarActividad BUNKERS_PEACE_MURAL puntuación=100")
                }
                is Resource.Error -> {
                    Log.e("PeaceMural", "Error: ${result.message}")
                    LogManager.write(this@PeaceMuralActivity, "Error completarActividad BUNKERS_PEACE_MURAL: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}