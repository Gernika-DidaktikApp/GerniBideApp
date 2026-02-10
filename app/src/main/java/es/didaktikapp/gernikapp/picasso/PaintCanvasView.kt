package es.didaktikapp.gernikapp.picasso

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.utils.Constants
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Custom View para pintar sobre un canvas con soporte de zoom.
 * Permite al usuario dibujar con diferentes colores sobre un área delimitada.
 *
 * Características:
 * - Pintura con trazos suaves usando curvas cuadráticas
 * - Zoom con gestos de pellizco (pinch-to-zoom)
 * - Delimitación de área pintable
 * - Guardado y carga de imágenes
 * - Soporte para múltiples colores con transparencia
 * - Borrador con color gris claro
 *
 * @property paintBitmap Bitmap donde se almacenan los trazos pintados
 * @property bitmapCanvas Canvas del bitmap para dibujar
 * @property paths Lista de pares (Path, Paint) con todos los trazos guardados
 * @property currentPath Path actual siendo dibujado
 * @property currentPaint Paint con la configuración actual de pintura
 * @property currentColor Color actual seleccionado (puede modificarse externamente)
 * @property scaleFactor Factor de escala actual del zoom (1.0 = sin zoom)
 * @property scaleGestureDetector Detector de gestos de zoom
 * @property isEraserMode Indica si está en modo borrador
 * @property paintableLeft Límite izquierdo del área pintable
 * @property paintableTop Límite superior del área pintable
 * @property paintableRight Límite derecho del área pintable
 * @property paintableBottom Límite inferior del área pintable
 *
 * Constantes utilizadas:
 * - Constants.Paint.STROKE_WIDTH: Grosor del trazo
 * - Constants.Paint.ALPHA_VALUE: Transparencia del color (0-255)
 * - Constants.Paint.MIN_ZOOM: Zoom mínimo permitido
 * - Constants.Paint.MAX_ZOOM: Zoom máximo permitido
 *
 * @author Wara Pacheco
 * @version 1.0
 */
class PaintCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Bitmap donde se almacenan todos los trazos pintados por el usuario. */
    private var paintBitmap: Bitmap? = null

    /** Canvas asociado al bitmap, utilizado para dibujar los trazos de forma permanente. */
    private var bitmapCanvas: Canvas? = null

    /** Lista de trazos realizados, cada uno compuesto por un Path y un Paint. */
    private val paths = mutableListOf<Pair<Path, Paint>>()

    /** Path actual que el usuario está dibujando en tiempo real. */
    private var currentPath = Path()

    /** Configuración de pintura activa (color, grosor, estilo). */
    private var currentPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = Constants.Paint.STROKE_WIDTH
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#4FC3F7")
        alpha = Constants.Paint.ALPHA_VALUE
    }

    /** Color actual seleccionado por el usuario para pintar o borrar. */
    var currentColor: Int = Color.parseColor("#4FC3F7")
        set(value) {
            field = value
            val eraserColor = Color.parseColor("#E0E0E0")
            isEraserMode = (value == eraserColor)

            currentPaint = Paint().apply {
                isAntiAlias = true
                strokeWidth = Constants.Paint.STROKE_WIDTH
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND

                // Si es borrador, usar modo CLEAR para borrar el bitmap
                if (isEraserMode) {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                } else {
                    color = value
                    alpha = Constants.Paint.ALPHA_VALUE
                }
            }
        }

    /** Matriz utilizada para aplicar transformaciones de zoom al canvas. */
    private val matrix = Matrix()

    /** Factor de escala actual aplicado al canvas (1.0 = sin zoom). */
    private var scaleFactor = 1f

    /** Detector de gestos para manejar el zoom mediante pellizco. */
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

    /** Última posición X registrada durante el trazado. */
    private var lastTouchX = 0f

    /** Última posición Y registrada durante el trazado. */
    private var lastTouchY = 0f

    /** Indica si el usuario está actualmente dibujando un trazo. */
    private var isPainting = false

    /** Indica si el usuario está utilizando el modo borrador. */
    private var isEraserMode = false

    /** Límite izquierdo del área donde se permite pintar. */
    private var paintableLeft = 0f

    /** Límite superior del área donde se permite pintar. */
    private var paintableTop = 0f

    /** Límite derecho del área donde se permite pintar. */
    private var paintableRight = 0f

    /** Límite inferior del área donde se permite pintar. */
    private var paintableBottom = 0f

    /**
     * Inicializa el bitmap de pintura con las dimensiones del View.
     * Crea un bitmap transparente y establece el área pintable por defecto a el canvas.
     *
     * Condiciones:
     * - Solo se ejecuta si width y height son mayores a 0
     * - Por defecto, toda el área es pintable hasta que se llame a setPaintableBounds()
     */
    private fun initializePaintBitmap() {
        if (width > 0 && height > 0) {
            LogManager.write(context, "PaintCanvasView inicializado: ${width}x${height}")

            paintBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(paintBitmap!!)
            bitmapCanvas?.drawColor(Color.TRANSPARENT)
            // Por defecto, toda el área es pintable
            paintableLeft = 0f
            paintableTop = 0f
            paintableRight = width.toFloat()
            paintableBottom = height.toFloat()
            invalidate()
        }
    }

    /**
     * Define los límites del área donde se puede pintar.
     * Los toques fuera de estos límites no dibujarán.
     *
     * @param left Coordenada X izquierda del área pintable
     * @param top Coordenada Y superior del área pintable
     * @param right Coordenada X derecha del área pintable
     * @param bottom Coordenada Y inferior del área pintable
     */
    fun setPaintableBounds(left: Float, top: Float, right: Float, bottom: Float) {
        LogManager.write(context, "Límites pintables: L=$left T=$top R=$right B=$bottom")

        paintableLeft = left
        paintableTop = top
        paintableRight = right
        paintableBottom = bottom
    }

    /**
     * Callback llamado cuando el tamaño del View cambia.
     * Reinicializa el bitmap de pintura con las nuevas dimensiones.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initializePaintBitmap()
    }

    /**
     * Dibuja el canvas con todos los trazos y aplica las transformaciones de zoom.
     *
     * @param canvas Canvas del View donde se dibuja
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)

        // Dibujar todos los paths guardados
        paintBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // Dibujar el path actual
        if (isEraserMode) {
            // Mostrar el trazo del borrador en blanco para que sea visible
            val previewPaint = Paint().apply {
                isAntiAlias = true
                strokeWidth = Constants.Paint.STROKE_WIDTH
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                color = Color.WHITE
                alpha = 200  // Semi-transparente para que se vea bien
            }
            canvas.drawPath(currentPath, previewPaint)
        } else {
            canvas.drawPath(currentPath, currentPaint)
        }

        canvas.restore()
    }

    /**
     * Maneja eventos táctiles para pintar y hacer zoom.
     *
     * Comportamiento:
     * - Un dedo: Pinta si está dentro del área pintable
     * - Dos dedos o más: Zoom con pellizco (no se puede pintar)
     * - Convierte coordenadas de pantalla a coordenadas del canvas considerando zoom
     * - Valida que el toque esté dentro del área pintable antes de pintar
     *
     * Estados de ACTION:
     * - ACTION_DOWN: Inicia un trazo si está en el área pintable
     * - ACTION_MOVE: Continúa el trazo dibujando curvas suaves
     * - ACTION_UP: Finaliza el trazo y lo guarda en el bitmap
     *
     * @param event Evento táctil
     * @return true si el evento fue manejado
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        // Si hay más de un dedo, es zoom, no pintar
        if (event.pointerCount > 1) {
            isPainting = false
            return true
        }

        // Transformar las coordenadas del toque al espacio del canvas
        val touchX = event.x / scaleFactor
        val touchY = event.y / scaleFactor

        // Verificar si el toque está dentro del área pintable
        val isInBounds = touchX >= paintableLeft && touchX <= paintableRight &&
                         touchY >= paintableTop && touchY <= paintableBottom

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isInBounds) {
                    isPainting = true
                    currentPath.moveTo(touchX, touchY)
                    lastTouchX = touchX
                    lastTouchY = touchY
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPainting) {
                    currentPath.quadTo(
                        lastTouchX,
                        lastTouchY,
                        (touchX + lastTouchX) / 2,
                        (touchY + lastTouchY) / 2
                    )
                    lastTouchX = touchX
                    lastTouchY = touchY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isPainting) {
                    currentPath.lineTo(touchX, touchY)
                    bitmapCanvas?.drawPath(currentPath, currentPaint)
                    paths.add(Pair(Path(currentPath), Paint(currentPaint)))
                    currentPath.reset()
                    isPainting = false
                    invalidate()
                }
            }
        }
        return true
    }

    /**
     * Listener para gestos de zoom (pellizco).
     * Actualiza el factor de escala dentro de los límites MIN_ZOOM y MAX_ZOOM.
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(Constants.Paint.MIN_ZOOM, min(scaleFactor, Constants.Paint.MAX_ZOOM))
            invalidate()
            return true
        }
    }

    /**
     * Limpia el canvas borrando todos los trazos.
     * Resetea el bitmap a transparente y limpia la lista de paths.
     */
    fun clearCanvas() {
        LogManager.write(context, "Canvas limpiado por el usuario")

        paintBitmap?.let {
            bitmapCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            invalidate()
        }
        paths.clear()
        currentPath.reset()
    }

    /**
     * Carga un bitmap previamente guardado en el canvas.
     * Escala el bitmap para que coincida con las dimensiones actuales del canvas.
     *
     * @param bitmap Bitmap a cargar y dibujar en el canvas
     */
    fun loadBitmap(bitmap: Bitmap) {
        if (width > 0 && height > 0) {
            // Escalar el bitmap cargado para que coincida con las dimensiones del canvas
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

            // Dibujar el bitmap cargado en el canvas
            bitmapCanvas?.drawBitmap(scaledBitmap, 0f, 0f, null)
            invalidate()
        }
    }

    /**
     * Genera un bitmap solo del área pintable (donde está la imagen del Guernica)
     * sin transformaciones de zoom/pan.
     *
     * Si hay límites definidos con setPaintableBounds(), recorta al área delimitada.
     * Si no, devuelve el bitmap completo.
     *
     * @return Bitmap del área pintable o del canvas completo
     * @see setPaintableBounds
     */
    fun getBitmap(): Bitmap {
        val sourceBitmap = paintBitmap ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        // Si hay límites definidos, recortar al área pintable
        if (paintableLeft > 0f || paintableTop > 0f ||
            paintableRight < width.toFloat() || paintableBottom < height.toFloat()) {

            val cropWidth = (paintableRight - paintableLeft).toInt()
            val cropHeight = (paintableBottom - paintableTop).toInt()

            if (cropWidth > 0 && cropHeight > 0) {
                return Bitmap.createBitmap(
                    sourceBitmap,
                    paintableLeft.toInt(),
                    paintableTop.toInt(),
                    cropWidth,
                    cropHeight
                )
            }
        }

        // Si no hay límites o son inválidos, devolver el bitmap completo
        return sourceBitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    /**
     * Guarda el canvas actual como imagen PNG en almacenamiento interno.
     * Usa getBitmap() para obtener solo el área pintable.
     *
     * @param context Contexto de la aplicación
     * @param filename Nombre del archivo (por defecto: "guernica_coloreado.png")
     * @return true si se guardó correctamente, false en caso contrario
     * @see getBitmap
     */
    fun saveToInternalStorage(context: Context, filename: String = "guernica_coloreado.png"): Boolean {
        return try {
            LogManager.write(context, "Guardando pintura en $filename")

            val bitmap = getBitmap()
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            LogManager.write(context, "Pintura guardada correctamente")
            true
        } catch (e: Exception) {
            LogManager.write(context, "Error al guardar pintura: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    companion object {
        /**
         * Carga un bitmap guardado desde almacenamiento interno
         * @param context Contexto de la aplicación
         * @param filename Nombre del archivo a cargar
         * @return El bitmap cargado o null si no existe o hay error
         */
        fun loadFromInternalStorage(context: Context, filename: String = Constants.Files.GUERNICA_IMAGE_FILENAME): Bitmap? {
            return try {
                val file = File(context.filesDir, filename)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Verifica si existe una imagen guardada
         * @param context Contexto de la aplicación
         * @param filename Nombre del archivo a verificar
         * @return true si existe, false en caso contrario
         */
        fun hasSavedImage(context: Context, filename: String = Constants.Files.GUERNICA_IMAGE_FILENAME): Boolean {
            val file = File(context.filesDir, filename)
            return file.exists()
        }
    }
}
