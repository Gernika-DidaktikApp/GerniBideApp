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
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

class PaintCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paintBitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()

    private var currentPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#4FC3F7")
        alpha = 100 // Semi-transparent para no tapar los trazos
    }

    var currentColor: Int = Color.parseColor("#4FC3F7")
        set(value) {
            field = value
            currentPaint = Paint().apply {
                isAntiAlias = true
                strokeWidth = 8f
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                color = value
                alpha = 100 // Semi-transparent para no tapar los trazos
            }
        }

    // Zoom y pan
    private val matrix = Matrix()
    private var scaleFactor = 1f
    private var translateX = 0f
    private var translateY = 0f

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, ScrollListener())

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPainting = false

    // Límites del área pintable
    private var paintableLeft = 0f
    private var paintableTop = 0f
    private var paintableRight = 0f
    private var paintableBottom = 0f

    private fun initializePaintBitmap() {
        if (width > 0 && height > 0) {
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
     * Define los límites del área donde se puede pintar
     */
    fun setPaintableBounds(left: Float, top: Float, right: Float, bottom: Float) {
        paintableLeft = left
        paintableTop = top
        paintableRight = right
        paintableBottom = bottom
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initializePaintBitmap()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)

        // Dibujar todos los paths guardados
        paintBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        // Dibujar el path actual
        canvas.drawPath(currentPath, currentPaint)

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        // Si hay más de un dedo, es zoom/pan, no pintar
        if (event.pointerCount > 1) {
            isPainting = false
            return true
        }

        gestureDetector.onTouchEvent(event)

        // Transformar las coordenadas del toque al espacio del canvas
        val touchX = (event.x - translateX) / scaleFactor
        val touchY = (event.y - translateY) / scaleFactor

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

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor, 5.0f)) // Limitar zoom entre 0.5x y 5x
            invalidate()
            return true
        }
    }

    private inner class ScrollListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (e2.pointerCount > 1 || !isPainting) {
                translateX -= distanceX
                translateY -= distanceY
                invalidate()
                return true
            }
            return false
        }
    }

    fun clearCanvas() {
        paintBitmap?.let {
            bitmapCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            invalidate()
        }
        paths.clear()
        currentPath.reset()
    }

    /**
     * Carga un bitmap previamente guardado en el canvas
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
     * sin transformaciones de zoom/pan
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
     * Guarda el canvas actual como imagen en almacenamiento interno
     * @param context Contexto de la aplicación
     * @param filename Nombre del archivo (por defecto: "guernica_coloreado.png")
     * @return true si se guardó correctamente, false en caso contrario
     */
    fun saveToInternalStorage(context: Context, filename: String = "guernica_coloreado.png"): Boolean {
        return try {
            val bitmap = getBitmap()
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: Exception) {
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
        fun loadFromInternalStorage(context: Context, filename: String = "guernica_coloreado.png"): Bitmap? {
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
        fun hasSavedImage(context: Context, filename: String = "guernica_coloreado.png"): Boolean {
            val file = File(context.filesDir, filename)
            return file.exists()
        }
    }
}
