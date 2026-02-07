package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Activity de puzzle de **6 piezas** (2x3) donde el usuario arma la imagen `arbola_eta_batzar_etxea`.
 *
 * **Mecánica de juego:**
 * 1. Las 6 piezas se generan cortando el bitmap original
 * 2. **Drag & Drop** → Piezas se posicionan aleatoriamente
 * 3. **Snap automático** → Si está cerca del target (<150px), se fija con sonido
 * 4. **Victoria** → Todas las piezas colocadas → Guarda progreso + API
 *
 * **Características técnicas:**
 * - **Bordes blancos** (`puzzle_piece_border`) durante arrastre
 * - **Sonido de éxito** (`TONE_PROP_BEEP`) al colocar correctamente
 * - **Guía semitransparente** que se atenúa al ganar
 * - Progreso persistente en `SharedPreferences`
 *
 * @author Telmo Castillo
 * @since 2026
 */
class PuzzleActivity : BaseMenuActivity() {

    /** Contenedor principal donde se arrastran las piezas del puzzle. */
    private lateinit var puzzleContainer: FrameLayout

    /** Mensaje de victoria mostrado al completar el puzzle. */
    private lateinit var tvVictory: TextView

    /** Botón de retorno, habilitado solo al completar o si ya estaba completado. */
    private lateinit var btnBack: Button

    /** Imagen guía de referencia que muestra dónde van las piezas. */
    private lateinit var guideImage: ImageView

    /** Repositorios para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    /** Identificador del estado del evento activo en la API. */
    private var actividadProgresoId: String? = null

    /** Dimensiones del puzzle: 2 filas × 3 columnas = 6 piezas. */
    private val rows = 2
    private val cols = 3
    private val totalPieces = rows * cols

    /** Contador de piezas correctamente colocadas. */
    private var piecesPlaced = 0

    /**
     * Data class que representa una pieza del puzzle.
     *
     * @property imageView View de la pieza arrastrable
     * @property targetX Coordenada X destino absoluta
     * @property targetY Coordenada Y destino absoluta
     */
    private data class PuzzlePiece(
        val imageView: ImageView,
        val targetX: Float,
        val targetY: Float
    )

    /** Lista de todas las piezas del puzzle. */
    private val piecesList = mutableListOf<PuzzlePiece>()

    /** @return Layout principal del puzzle. */
    override fun getContentLayoutId() = R.layout.arbol_puzzle

    /**
     * Inicializa repositorios, UI y lógica del puzzle.
     *
     * **Secuencia de inicialización:**
     * 1. Configura GameRepository y TokenManager
     * 2. Inicia evento en la API
     * 3. Configura botón de retorno
     * 4. Verifica progreso previo
     * 5. **Espera a que `guideImage` se mida** → `setupPuzzle()`
     */
    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        puzzleContainer = findViewById(R.id.puzzleContainer)
        tvVictory = findViewById(R.id.tvVictory)
        btnBack = findViewById(R.id.btnBack)
        guideImage = findViewById(R.id.guideImage)

        iniciarActividad()

        btnBack.setOnClickListener {
            finish()
        }

        // Si ya estaba completado, mostrar botón activo
        val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("puzzle_completed", false)) {
            btnBack.visibility = View.VISIBLE
            btnBack.isEnabled = true
        }

        // Esperar a que guideImage tenga dimensiones reales
        guideImage.post {
            setupPuzzle()
        }
    }

    /**
     * **Core del puzzle**: Corta el bitmap en 6 piezas y configura drag & drop.
     *
     * **Proceso:**
     * 1. Carga `R.drawable.arbola_eta_batzar_etxea`
     * 2. Calcula dimensiones reales de `guideImage` (considerando escalado)
     * 3. **Corta bitmap** en 2x3 piezas con `Bitmap.createBitmap()`
     * 4. Posiciona cada pieza **aleatoriamente**
     * 5. Configura `OnTouchListener` para cada pieza
     */
    private fun setupPuzzle() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.arbola_eta_batzar_etxea)

        val imageRect = getImageViewRect(guideImage)
        val pieceWidth = imageRect.width() / cols
        val pieceHeight = imageRect.height() / rows

        val bitmapPieceWidth = originalBitmap.width / cols
        val bitmapPieceHeight = originalBitmap.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Manejo preciso de bordes para evitar pérdida de píxeles
                val srcX = c * bitmapPieceWidth
                val srcY = r * bitmapPieceHeight
                val w = if (c == cols - 1) originalBitmap.width - srcX else bitmapPieceWidth
                val h = if (r == rows - 1) originalBitmap.height - srcY else bitmapPieceHeight

                // Cortar pieza del bitmap original
                val pieceBitmap = Bitmap.createBitmap(originalBitmap, srcX, srcY, w, h)

                val iv = ImageView(this).apply {
                    setImageBitmap(pieceBitmap)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = FrameLayout.LayoutParams(pieceWidth, pieceHeight)
                    // Borde blanco durante arrastre
                    setBackgroundResource(R.drawable.puzzle_piece_border)
                    setPadding(5, 5, 5, 5)
                }

                // Calcular posición destino absoluta
                val targetX = imageRect.left + (c * pieceWidth)
                val targetY = imageRect.top + (r * pieceHeight)

                val piece = PuzzlePiece(iv, targetX.toFloat(), targetY.toFloat())

                // Posición inicial aleatoria
                iv.x = Random.nextInt(0, (puzzleContainer.width - pieceWidth).coerceAtLeast(1)).toFloat()
                iv.y = Random.nextInt(0, (puzzleContainer.height - pieceHeight).coerceAtLeast(1)).toFloat()

                setupDragListener(piece)
                piecesList.add(piece)
                puzzleContainer.addView(iv)
            }
        }
    }

    /**
     * Configura **drag & drop** para una pieza específica.
     *
     * **Lógica de snap:**
     * - Distancia < **150px** → Se fija en posición destino
     * - Remueve borde y padding al colocar
     * - Desactiva touch listener
     * - Reproduce sonido de éxito
     *
     * @param piece Pieza del puzzle
     */
    private fun setupDragListener(piece: PuzzlePiece) {
        piece.imageView.setOnTouchListener(object : View.OnTouchListener {
            private var dX = 0f
            private var dY = 0f
            private var isPlaced = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (isPlaced) return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = v.x - event.rawX
                        dY = v.y - event.rawY
                        v.bringToFront()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        v.x = event.rawX + dX
                        v.y = event.rawY + dY
                    }
                    MotionEvent.ACTION_UP -> {
                        val distance = Math.sqrt(
                            Math.pow((v.x - piece.targetX).toDouble(), 2.0) +
                                    Math.pow((v.y - piece.targetY).toDouble(), 2.0)
                        )

                        // SNAP: Si está dentro del radio de 150px
                        if (distance < 150) {
                            v.x = piece.targetX
                            v.y = piece.targetY
                            isPlaced = true
                            v.background = null // Quitar borde al colocar
                            v.setPadding(0, 0, 0, 0)
                            v.setOnTouchListener(null)
                            playSuccessSound()
                            checkVictory()
                        }
                    }
                }
                return true
            }
        })
    }

    /**
     * Reproduce **sonido de éxito** (`TONE_PROP_BEEP` de 150ms) al colocar pieza correctamente.
     * Usa `ToneGenerator` en canal `STREAM_ALARM`.
     */
    private fun playSuccessSound() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Verifica si el puzzle está completo.
     *
     * **Al ganar:**
     * - Muestra `tvVictory`
     * - Habilita `btnBack`
     * - Atenúa `guideImage` (alpha = 0.5f)
     * - Guarda `puzzle_completed = true`
     * - Completa evento API (100%)
     */
    private fun checkVictory() {
        piecesPlaced++
        if (piecesPlaced == totalPieces) {
            tvVictory.visibility = View.VISIBLE
            btnBack.visibility = View.VISIBLE
            btnBack.isEnabled = true
            guideImage.alpha = 0.5f

            // Persistir progreso local
            val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("puzzle_completed", true).apply()

            completarActividad()
        }
    }

    /**
     * Calcula el **rectángulo efectivo** de `guideImage` considerando escalado y márgenes.
     * Esencial para calcular posiciones destino precisas de las piezas.
     *
     * @param imageView ImageView guía
     * @return Rect con coordenadas reales de dibujo
     */
    private fun getImageViewRect(imageView: ImageView): Rect {
        val rect = Rect()
        val drawable = imageView.drawable ?: return rect

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        val containerWidth = imageView.width
        val containerHeight = imageView.height

        val scale: Float
        var xOffset = 0f
        var yOffset = 0f

        if (containerWidth * imageHeight > containerHeight * imageWidth) {
            scale = containerHeight.toFloat() / imageHeight.toFloat()
            xOffset = (containerWidth - imageWidth * scale) / 2
        } else {
            scale = containerWidth.toFloat() / imageWidth.toFloat()
            yOffset = (containerHeight - imageHeight * scale) / 2
        }

        rect.left = xOffset.toInt()
        rect.top = yOffset.toInt()
        rect.right = (xOffset + imageWidth * scale).toInt()
        rect.bottom = (yOffset + imageHeight * scale).toInt()

        return rect
    }

    /**
     * Inicia el evento "PUZZLE" en la API al cargar la actividad.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId()

        if (juegoId == null) {
            Log.e("Puzzle", "No hay juegoId guardado")
            return
        }

        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(
                idJuego = juegoId,
                idPunto = Puntos.Arbol.ID,
                idActividad = Puntos.Arbol.PUZZLE
            )) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    Log.d("Puzzle", "Evento iniciado: $actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("Puzzle", "Error al iniciar evento: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento en la API con **puntuación 100%**.
     * Se ejecuta automáticamente al completar todas las piezas.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId

        if (estadoId == null) {
            Log.e("Puzzle", "No hay actividadProgresoId guardado")
            return
        }

        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(
                progresoId = estadoId,
                puntuacion = 100.0 // Puzzle completado = 100%
            )) {
                is Resource.Success -> {
                    Log.d("Puzzle", "Evento completado con puntuación: 100")
                }
                is Resource.Error -> {
                    Log.e("Puzzle", "Error al completar evento: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}