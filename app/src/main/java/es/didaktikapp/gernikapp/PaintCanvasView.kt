package es.didaktikapp.gernikapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream

class PaintCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()
    private var currentPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 30f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = android.graphics.Color.parseColor("#4FC3F7")
        alpha = 180
    }

    var currentColor: Int = android.graphics.Color.parseColor("#4FC3F7")
        set(value) {
            field = value
            currentPaint = Paint().apply {
                isAntiAlias = true
                strokeWidth = 30f
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                color = value
                alpha = 180
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dibujar todos los paths guardados
        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }

        // Dibujar el path actual
        canvas.drawPath(currentPath, currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)
                paths.add(Pair(currentPath, Paint(currentPaint)))
                currentPath = Path()
                invalidate()
            }
        }
        return true
    }

    fun clearCanvas() {
        paths.clear()
        currentPath = Path()
        invalidate()
    }

    /**
     * Genera un bitmap del canvas completo con todos los trazos pintados
     */
    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
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
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)
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
