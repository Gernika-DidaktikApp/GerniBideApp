package com.example.didaktikarbol

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class PuzzleActivity : AppCompatActivity() {

    private lateinit var puzzleContainer: FrameLayout
    private lateinit var tvVictory: TextView
    private lateinit var btnNext: Button
    private lateinit var guideImage: ImageView

    private val rows = 2
    private val cols = 3
    private val totalPieces = rows * cols
    private var piecesPlaced = 0

    private data class PuzzlePiece(
        val imageView: ImageView,
        val targetX: Float,
        val targetY: Float
    )

    private val piecesList = mutableListOf<PuzzlePiece>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        puzzleContainer = findViewById(R.id.puzzleContainer)
        tvVictory = findViewById(R.id.tvVictory)
        btnNext = findViewById(R.id.btnNext)
        guideImage = findViewById(R.id.guideImage)

        btnNext.setOnClickListener {
            startActivity(Intent(this, NireArbolaActivity::class.java))
            finish()
        }

        // Delay setup until guideImage is measured to get correct target positions
        guideImage.post {
            setupPuzzle()
        }
    }

    private fun setupPuzzle() {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.arbola_eta_batzar_etxea)
        
        // Calculate the actual displayed size of the guide image
        val imageRect = getImageViewRect(guideImage)
        val pieceWidth = imageRect.width() / cols
        val pieceHeight = imageRect.height() / rows

        val bitmapPieceWidth = originalBitmap.width / cols
        val bitmapPieceHeight = originalBitmap.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Ensure we don't skip pixels due to rounding
                val srcX = c * bitmapPieceWidth
                val srcY = r * bitmapPieceHeight
                val w = if (c == cols - 1) originalBitmap.width - srcX else bitmapPieceWidth
                val h = if (r == rows - 1) originalBitmap.height - srcY else bitmapPieceHeight

                // Slice bitmap
                val pieceBitmap = Bitmap.createBitmap(originalBitmap, srcX, srcY, w, h)

                val iv = ImageView(this).apply {
                    setImageBitmap(pieceBitmap)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = FrameLayout.LayoutParams(pieceWidth, pieceHeight)
                    // Add white border
                    setBackgroundResource(R.drawable.puzzle_piece_border)
                    setPadding(5, 5, 5, 5)
                }

                // Calculate target position relative to parent
                val targetX = imageRect.left + (c * pieceWidth)
                val targetY = imageRect.top + (r * pieceHeight)

                val piece = PuzzlePiece(iv, targetX.toFloat(), targetY.toFloat())
                
                // Randomize initial position
                iv.x = Random.nextInt(0, (puzzleContainer.width - pieceWidth).coerceAtLeast(1)).toFloat()
                iv.y = Random.nextInt(0, (puzzleContainer.height - pieceHeight).coerceAtLeast(1)).toFloat()

                setupDragListener(piece)
                piecesList.add(piece)
                puzzleContainer.addView(iv)
            }
        }
    }

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

                        if (distance < 150) {
                            v.x = piece.targetX
                            v.y = piece.targetY
                            isPlaced = true
                            v.background = null // Remove border when placed
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

    private fun playSuccessSound() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkVictory() {
        piecesPlaced++
        if (piecesPlaced == totalPieces) {
            tvVictory.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
            guideImage.alpha = 0.5f // Reveal image slightly more
        }
    }

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
}
