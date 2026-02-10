package es.didaktikapp.gernikapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import es.didaktikapp.gernikapp.R

/**
 * Vista personalizada que reproduce una animación a partir de una hoja de sprites (sprite sheet).
 * Recorre los fotogramas de la imagen en secuencia para crear la ilusión de movimiento
 * de la mascota de la aplicación.
 *
 * @author Arantxa Main
 * @version 1.0
 * @param context Contexto de la aplicación.
 * @param attrs Atributos XML opcionales.
 * @param defStyleAttr Estilo por defecto opcional.
 */
class SpriteAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Bitmap que contiene el sprite sheet completo. */
    private var spriteSheet: Bitmap? = null

    /** Paint optimizado para dibujar bitmaps suavizados. */
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    /** Rectángulo que define el área del fotograma dentro del sprite sheet. */
    private val srcRect = Rect()

    /** Rectángulo destino donde se dibuja el fotograma escalado al tamaño del View. */
    private val dstRect = Rect()

    /** Número de columnas del sprite sheet. */
    private val columns = 6

    /** Número de filas del sprite sheet. */
    private val rows = 6

    /** Número total de fotogramas de la animación. */
    private val totalFrames = 36

    /** Índice del fotograma actual. */
    private var currentFrame = 0

    /** Ancho de cada fotograma dentro del sprite sheet. */
    private var frameWidth = 0

    /** Alto de cada fotograma dentro del sprite sheet. */
    private var frameHeight = 0

    /** Handler que gestiona la actualización periódica de la animación. */
    private val handler = Handler(Looper.getMainLooper())

    /** Tiempo entre fotogramas (en milisegundos). */
    private val frameDelay = 80L

    /**
     * Runnable que avanza al siguiente fotograma y vuelve a ejecutarse
     * después del tiempo indicado en [frameDelay].
     */
    private val animRunnable = object : Runnable {
        override fun run() {
            currentFrame = (currentFrame + 1) % totalFrames
            invalidate()
            handler.postDelayed(this, frameDelay)
        }
    }

    init {
        loadSpriteSheet()
    }

    /**
     * Carga el sprite sheet desde recursos y calcula el tamaño de cada fotograma.
     */
    private fun loadSpriteSheet() {
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.mascota_sprite_sheet, options)
        spriteSheet?.let {
            frameWidth = it.width / columns
            frameHeight = it.height / rows
        }
    }

    /**
     * Inicia la animación desde el primer fotograma.
     * Reinicia el índice y programa el runnable.
     */
    fun startAnimation() {
        handler.removeCallbacks(animRunnable)
        currentFrame = 0
        handler.post(animRunnable)
    }

    /**
     * Detiene la animación cancelando el runnable.
     */
    fun stopAnimation() {
        handler.removeCallbacks(animRunnable)
    }

    /**
     * Dibuja el fotograma actual en el canvas.
     * Calcula la fila y columna correspondientes dentro del sprite sheet.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val sheet = spriteSheet ?: return

        val col = currentFrame % columns
        val row = currentFrame / columns

        srcRect.set(
            col * frameWidth,
            row * frameHeight,
            (col + 1) * frameWidth,
            (row + 1) * frameHeight
        )
        dstRect.set(0, 0, width, height)

        canvas.drawBitmap(sheet, srcRect, dstRect, paint)
    }

    /**
     * Limpia callbacks pendientes cuando la vista se elimina de la ventana.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(animRunnable)
    }
}
