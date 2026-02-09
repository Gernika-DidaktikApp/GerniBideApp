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

class SpriteAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var spriteSheet: Bitmap? = null
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val srcRect = Rect()
    private val dstRect = Rect()

    private val columns = 6
    private val rows = 6
    private val totalFrames = 36
    private var currentFrame = 0

    private var frameWidth = 0
    private var frameHeight = 0

    private val handler = Handler(Looper.getMainLooper())
    private val frameDelay = 80L

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

    fun startAnimation() {
        handler.removeCallbacks(animRunnable)
        currentFrame = 0
        handler.post(animRunnable)
    }

    fun stopAnimation() {
        handler.removeCallbacks(animRunnable)
    }

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(animRunnable)
    }
}
