package es.didaktikapp.gernikapp.arbol

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import kotlin.math.hypot
import androidx.core.content.edit


/**
 * Activity interactiva donde el usuario construye su "árbol de valores" arrastrando palabras
 * a posiciones específicas del árbol ilustrado.
 *
 * **Funcionalidades principales:**
 * - **Drag & Drop**: Las palabras de valores se pueden arrastrar y "imantan" a 13 posiciones predefinidas.
 * - **Sistema de coordenadas**: Usa porcentajes precisos para ubicar elementos en la imagen del árbol.
 * - **Animaciones**: Efectos de escala y fade-in al colocar elementos.
 * - **Gestión de eventos**: Integra con la API del juego para seguimiento del progreso.
 *
 * El usuario puede arrastrar valores como "Amistad", "Libertad", "Solidaridad", etc., y soltarlos
 * en los óvalos blancos de la imagen del árbol. El sistema detecta automáticamente el hueco más cercano.
 *
 * @author Telmo Castillo
 * @version 1.0
 */
class InteractiveActivity : BaseMenuActivity() {

    companion object {
        private const val TAG = "InteractiveActivity"
    }

    /** Contenedor principal donde se posicionan las palabras arrastrables. */
    private lateinit var treeContainer: FrameLayout

    /** Imagen base del árbol que sirve de referencia visual. */
    private lateinit var treeImage: ImageView

    /** Repositorio para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de tokens y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** ID del estado del evento activo en la API. */
    private var actividadProgresoId: String? = null

    /**
     * Coordenadas porcentuales exactas de los 14 óvalos decorativos en `arbola_image.jpg`.
     * * **Estructura del árbol (de arriba a abajo):**
     * 1.  Cúspide: Superior Central (50%, 9.5%)
     * 2.  Eje Central Superior: Justo debajo de la cúspide (50%, 22.5%)
     * 3-4. Fila 2 (Lateral): Izquierda (27%, 19%) | Derecha (73%, 19%)
     * 5-7. Fila 3 (Ancha): Izquierda (19%, 32%) | Centro (50%, 35%) | Derecha (81%, 32%)
     * 8-10. Fila 4 (Media): Izquierda (20%, 46%) | Centro (50%, 48%) | Derecha (80%, 46%)
     * 11-12. Fila 5 (Baja): Izquierda (32%, 55%) | Derecha (68%, 55%)
     * 13. Base de la Copa: Centro Bajo sobre el tronco (50%, 60%)
     * 14. Raíz: Óvalo independiente en el césped (50%, 93%)
     */
    private val slotPercentages = listOf(
        PointF(50f, 9.5f),  // 1. Superior Central (Punta)
        PointF(50f, 22.5f), // 2. NUEVO: Central Alto (Justo debajo del 1)
        PointF(27f, 19f),   // 3. Fila 2 - Izquierda
        PointF(73f, 19f),   // 4. Fila 2 - Derecha
        PointF(19f, 32f),   // 5. Fila 3 - Izquierda
        PointF(50f, 35f),   // 6. Fila 3 - Centro
        PointF(81f, 32f),   // 7. Fila 3 - Derecha
        PointF(20f, 46f),   // 8. Fila 4 - Izquierda
        PointF(50f, 48f),   // 9. Fila 4 - Centro
        PointF(80f, 46f),   // 10. Fila 4 - Derecha
        PointF(32f, 55f),   // 11. Fila 5 - Izquierda
        PointF(68f, 55f),   // 12. Fila 5 - Derecha
        PointF(50f, 60f),   // 13. Fila 6 - Centro Bajo
        PointF(50f, 93f)    // 14. Óvalo Raíces (Césped)
    )

    /** Mapa que rastrea qué View ocupa cada slot (null = libre). */
    private val occupiedSlots = mutableMapOf<Int, View?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arbol_interactive)

        LogManager.write(this@InteractiveActivity, "InteractiveActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        treeContainer = findViewById(R.id.treeContainer)
        treeImage = findViewById(R.id.treeImage)

        // Ocultar botón de finalizar hasta que todas las piezas estén colocadas
        findViewById<View>(R.id.btnFinish).visibility = View.GONE

        // Configurar botones de valores arrastrables
        setupWordButtons()

        // Botones de navegación
        findViewById<View>(R.id.btnBack).setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.btnFinish).setOnClickListener {
            LogManager.write(this@InteractiveActivity, "Actividad Árbol finalizada por el usuario")
            val prefs = getSharedPreferences("arbol_progress", MODE_PRIVATE)
            prefs.edit {
                putBoolean("interactive_completed", true)
                putFloat("interactive_score", 100f)
            }
            Log.d(TAG, "✅ Guardado: interactive_completed = true, interactive_score = 100.0")
            ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.ARBOL)
            completarActividad()
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Iniciar seguimiento del evento
        iniciarActividad()
    }

    /**
     * Configura los 5 botones de valores arrastrables (Amistad, Libertad, Solidaridad, Respeto, Paz).
     * Cada botón crea una TextView con diseño específico y activa su lógica de drag & drop.
     */
    private fun setupWordButtons() {
        val wordButtons = listOf(
            Triple(R.id.btnAmistadVal, R.string.value_friendship, R.color.valueFriendship),
            Triple(R.id.btnLibertadVal, R.string.value_freedom, R.color.valueFreedom),
            Triple(R.id.btnSolidaridadVal, R.string.value_solidarity, R.color.valueSolidarity),
            Triple(R.id.btnRespetoVal, R.string.value_respect, R.color.valueRespect),
            Triple(R.id.btnPazVal, R.string.value_peace, R.color.valuePeace)
        )

        wordButtons.forEach { (btnId, textRes, colorRes) ->
            findViewById<Button>(btnId).setOnClickListener {
                val text = getString(textRes)
                val color = ContextCompat.getColor(this, colorRes)

                // Buscar primer slot libre
                val targetSlot = slotPercentages.indices.firstOrNull { occupiedSlots[it] == null }

                if (targetSlot != null) {
                    addValueToTree(text, color, targetSlot)
                    checkAllValuesPlaced()
                }
            }
        }
    }

    /**
     * Crea una TextView con el valor y la posiciona en el árbol.
     *
     * @param text Texto del valor (ej: "Amistad")
     * @param color Color distintivo del valor
     * @param initialSlot Índice del slot inicial donde aparecerá
     */
    private fun addValueToTree(text: String, color: Int, initialSlot: Int) {
        val textView = TextView(this).apply {
            LogManager.write(this@InteractiveActivity, "Valor añadido al árbol: $text (slot $initialSlot)")
            this.text = text
            this.setTextColor(color)
            this.textSize = 13f
            this.setTypeface(null, Typeface.BOLD)
            this.gravity = Gravity.CENTER
            this.setPadding(20, 10, 20, 10)

            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(android.graphics.Color.WHITE)
                cornerRadius = 35f
                setStroke(3, color)
            }
            this.background = shape

            // Ancho fijo optimizado para los óvalos de la imagen
            this.layoutParams = FrameLayout.LayoutParams(240, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        treeContainer.addView(textView)

        textView.post {
            moveWordToSlot(textView, initialSlot)
            applyAnimation(textView)
        }

        setupDragLogic(textView, initialSlot)
    }

    /**
     * Configura la lógica completa de drag & drop para una TextView de valor.
     *
     * **Comportamiento:**
     * - `ACTION_DOWN`: Prepara arrastre y libera slot anterior
     * - `ACTION_MOVE`: Sigue el dedo del usuario
     * - `ACTION_UP`: Detecta slot más cercano y aplica "snap"
     *
     * @param view TextView arrastrable
     * @param startSlot Slot inicial
     */
    private fun setupDragLogic(view: View, startSlot: Int) {
        view.setOnTouchListener(object : View.OnTouchListener {
            private var dX = 0f
            private var dY = 0f
            private var currentSlot: Int = startSlot

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = v.x - event.rawX
                        dY = v.y - event.rawY
                        v.bringToFront()
                        occupiedSlots.remove(currentSlot)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        v.x = event.rawX + dX
                        v.y = event.rawY + dY
                    }
                    MotionEvent.ACTION_UP -> {
                        val imgRect = getImageViewRect(treeImage)
                        var nearestSlot = -1
                        var minDistance = Float.MAX_VALUE

                        // Buscar slot más cercano disponible
                        for (i in slotPercentages.indices) {
                            val slotPos = getSlotCoords(slotPercentages[i], imgRect)
                            val dist = hypot((v.x + v.width / 2 - slotPos.x), (v.y + v.height / 2 - slotPos.y))

                            // Solo considerar slots libres o el propio
                            if (dist < minDistance && (occupiedSlots[i] == null || occupiedSlots[i] == v)) {
                                minDistance = dist
                                nearestSlot = i
                            }
                        }

                        // Aplicar efecto imán si está dentro del rango (250px)
                        if (nearestSlot != -1 && minDistance < 250) {
                            currentSlot = nearestSlot
                        }

                        moveWordToSlot(v, currentSlot)

                        LogManager.write( this@InteractiveActivity, "Valor colocado en slot $currentSlot" )
                        checkAllValuesPlaced()
                    }
                }
                return true
            }
        })
    }

    /**
     * Mueve una TextView al slot especificado con animación suave.
     *
     * @param view TextView a reposicionar
     * @param slotIndex Índice del slot destino
     */
    private fun moveWordToSlot(view: View, slotIndex: Int) {
        val imgRect = getImageViewRect(treeImage)
        val coords = getSlotCoords(slotPercentages[slotIndex], imgRect)

        view.animate()
            .x(coords.x - view.width / 2f)
            .y(coords.y - view.height / 2f)
            .setDuration(300)
            .start()

        occupiedSlots[slotIndex] = view
    }

    /**
     * Convierte coordenadas porcentuales en coordenadas absolutas de pantalla.
     *
     * @param percent Coordenadas en porcentaje (0-100%)
     * @param imgRect Rectángulo de la imagen del árbol
     * @return Coordenadas absolutas en píxeles
     */
    private fun getSlotCoords(percent: PointF, imgRect: Rect): PointF {
        val x = imgRect.left + (percent.x / 100f) * imgRect.width()
        val y = imgRect.top + (percent.y / 100f) * imgRect.height()
        return PointF(x, y)
    }

    /**
     * Calcula el rectángulo efectivo de la imagen considerando escalado y márgenes.
     *
     * @param imageView ImageView del árbol
     * @return Rectángulo con coordenadas reales de dibujo
     */
    private fun getImageViewRect(imageView: ImageView): Rect {
        val rect = Rect()
        val drawable = imageView.drawable ?: return rect

        val imgW = drawable.intrinsicWidth.toFloat()
        val imgH = drawable.intrinsicHeight.toFloat()
        val viewW = imageView.width.toFloat()
        val viewH = imageView.height.toFloat()

        val scale = if (viewW / imgW < viewH / imgH) viewW / imgW else viewH / imgH

        val actualW = imgW * scale
        val actualH = imgH * scale

        rect.left = ((viewW - actualW) / 2).toInt()
        rect.top = ((viewH - actualH) / 2).toInt()
        rect.right = (rect.left + actualW).toInt()
        rect.bottom = (rect.top + actualH).toInt()

        return rect
    }

    /**
     * Aplica animación de entrada: escala desde 70% + fade-in.
     *
     * @param view View a animar
     */
    private fun applyAnimation(view: View) {
        val animSet = AnimationSet(true)
        val scale = ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, view.width / 2f, view.height / 2f)
        scale.duration = 400
        val fade = AlphaAnimation(0f, 1f)
        fade.duration = 400
        animSet.addAnimation(scale)
        animSet.addAnimation(fade)
        view.startAnimation(animSet)
    }

    /**
     * Verifica si los 5 valores han sido colocados en el árbol.
     * Si todos están colocados, muestra el botón de finalizar.
     */
    private fun checkAllValuesPlaced() {
        val placedCount = occupiedSlots.count { it.value != null }

        if (placedCount == 5) {
            Log.d(TAG, "✅ Todos los 5 valores colocados en el árbol")
            LogManager.write(this@InteractiveActivity, "Todos los valores del árbol han sido colocados")

            // Mostrar botón de finalizar
            findViewById<View>(R.id.btnFinish).visibility = View.VISIBLE

            Toast.makeText(
                this,
                getString(R.string.my_tree_complete),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Inicia el evento "Mi Árbol" en la API al cargar la actividad.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            val result = gameRepository.iniciarActividad(juegoId, Puntos.Arbol.ID, Puntos.Arbol.MY_TREE)
            if (result is Resource.Success) {
                actividadProgresoId = result.data.id
                LogManager.write(this@InteractiveActivity, "API iniciarActividad ARBOL_MY_TREE id=$actividadProgresoId")
            }
            if (result is Resource.Error) {
                LogManager.write(this@InteractiveActivity, "Error iniciarActividad ARBOL_MY_TREE: ${result.message}")
            }
        }
    }

    /**
     * Completa el evento asignando puntuación máxima (100.0).
     * Se ejecuta al pulsar "Finalizar".
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            gameRepository.completarActividad(estadoId, 100.0)
        }
    }
}