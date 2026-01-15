package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R

import android.graphics.PointF
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.random.Random

class ArbolInteractivoActivity : AppCompatActivity() {

    private lateinit var treeContainer: FrameLayout
    private lateinit var treeImage: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false

    // Slots positions as percentages (X, Y) relative to the image bounds
    private val slotPercentages = listOf(
        PointF(50f, 10f),   // Top
        PointF(32f, 18f),   // Row 2 L
        PointF(68f, 18f),   // Row 2 R
        PointF(50f, 26f),   // Row 3 C
        PointF(24f, 34f),   // Row 4 L
        PointF(76f, 34f),   // Row 4 R
        PointF(50f, 42f),   // Row 5 C
        PointF(32f, 56f),   // Row 6 L
        PointF(68f, 56f),   // Row 6 R
        PointF(44f, 64f),   // Row 7 L
        PointF(56f, 64f),   // Row 7 R
        PointF(50f, 93f)    // Ground
    )

    private val occupiedSlots = mutableMapOf<Int, View?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arbol_interactivo)

        treeContainer = findViewById(R.id.treeContainer)
        treeImage = findViewById(R.id.treeImage)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btnFinish).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnMute).setOnClickListener {
            toggleMute(it as ImageButton)
        }

        setupMusic()
        setupWordButtons()

        // Handle intent entry (legacy/direct from previous screen)
        val text = intent.getStringExtra("EXTRA_VALUE_TEXT") ?: ""
        if (text.isNotEmpty()) {
            val color = intent.getIntExtra("EXTRA_VALUE_COLOR", ContextCompat.getColor(this, R.color.valuePeace))
            treeContainer.post {
                autoPlaceWord(text, color)
            }
        }
    }

    private fun setupMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.genikako_arbola)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun toggleMute(btn: ImageButton) {
        if (isMuted) {
            mediaPlayer?.setVolume(1.0f, 1.0f)
            btn.setImageResource(R.drawable.ic_pause)
        } else {
            mediaPlayer?.setVolume(0.0f, 0.0f)
            btn.setImageResource(R.drawable.ic_play)
        }
        isMuted = !isMuted
    }

    private fun setupWordButtons() {
        val wordButtons = listOf(
            Triple(R.id.btnAmistadVal, R.string.valor_amistad, R.color.valueFriendship),
            Triple(R.id.btnLibertadVal, R.string.valor_libertad, R.color.valueFreedom),
            Triple(R.id.btnSolidaridadVal, R.string.valor_solidaridad, R.color.valueSolidarity),
            Triple(R.id.btnRespetoVal, R.string.valor_respeto, R.color.valueRespect),
            Triple(R.id.btnPazVal, R.string.valor_paz, R.color.valuePeace)
        )

        wordButtons.forEach { (btnId, textRes, colorRes) ->
            findViewById<Button>(btnId).setOnClickListener {
                val text = getString(textRes)
                val color = ContextCompat.getColor(this, colorRes)
                autoPlaceWord(text, color)
            }
        }
    }

    private fun autoPlaceWord(text: String, color: Int) {
        // Find first empty slot
        var targetSlot = -1
        for (i in slotPercentages.indices) {
            if (occupiedSlots[i] == null) {
                targetSlot = i
                break
            }
        }

        // If all full, maybe replace or just don't add. Let's cycle or just use slot 0 for now.
        if (targetSlot == -1) targetSlot = Random.nextInt(slotPercentages.size)

        addValueToTree(text, color, targetSlot)
    }

    private fun addValueToTree(text: String, color: Int, initialSlot: Int) {
        val textView = TextView(this).apply {
            this.text = text
            this.setTextColor(color)
            
            // Si la palabra es "adiskidetasuna", la hacemos un poco más pequeña aún
            val isAdiskidetasuna = text.equals(getString(R.string.valor_amistad), ignoreCase = true) || 
                                 text.equals("adiskidetasuna", ignoreCase = true)
            this.textSize = if (isAdiskidetasuna) 11f else 14f //tamaño adiskidetasuna
            
            this.setTypeface(null, android.graphics.Typeface.BOLD)
            this.gravity = android.view.Gravity.CENTER
            
            // Fondo blanco con esquinas redondeadas
            val shape = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(android.graphics.Color.WHITE)
                cornerRadius = 20f // Esquinas redondeadas
            }
            this.background = shape
            
            this.setPadding(15, 8, 15, 8)
            // Fixed width to fit in ovals
            this.layoutParams = FrameLayout.LayoutParams(220, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        treeContainer.addView(textView)

        // Position it after adding so it has dimensions
        textView.post {
            moveWordToSlot(textView, initialSlot)
            applyAnimation(textView)
        }

        textView.setOnTouchListener(object : View.OnTouchListener {
            private var dX = 0f
            private var dY = 0f
            private var currentSlot: Int = initialSlot

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        view.bringToFront()
                        // Release current slot
                        if (occupiedSlots[currentSlot] == view) {
                            occupiedSlots.remove(currentSlot)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        view.x = event.rawX + dX
                        view.y = event.rawY + dY
                    }
                    MotionEvent.ACTION_UP -> {
                        // Snap to nearest slot
                        val imgRect = getImageViewRect(treeImage)
                        var nearestSlot = -1
                        var minDistance = Float.MAX_VALUE

                        for (i in slotPercentages.indices) {
                            val slotPos = getSlotCoords(slotPercentages[i], imgRect)
                            val dist = Math.hypot((view.x + view.width/2 - slotPos.x).toDouble(), 
                                                  (view.y + view.height/2 - slotPos.y).toDouble()).toFloat()
                            if (dist < minDistance) {
                                minDistance = dist
                                nearestSlot = i
                            }
                        }

                        if (nearestSlot != -1 && minDistance < 300) {
                            // If slot is occupied by someone else, they swap or just stay
                            val existing = occupiedSlots[nearestSlot]
                            if (existing != null && existing != view) {
                                // For now, just overlap or let them stay. 
                                // Better: stay in slot.
                            }
                            currentSlot = nearestSlot
                            occupiedSlots[currentSlot] = view
                            moveWordToSlot(view, currentSlot)
                        } else {
                            // No near slot, snap back to where it was or stay?
                            // Let's stay but re-assign to old slot if empty
                            if (occupiedSlots[currentSlot] == null) {
                                occupiedSlots[currentSlot] = view
                                moveWordToSlot(view, currentSlot)
                            } else {
                                // Find any free slot
                                autoPlaceWord(text, color)
                                treeContainer.removeView(view)
                            }
                        }
                    }
                }
                return true
            }
        })
    }

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

    private fun getSlotCoords(percent: PointF, imgRect: Rect): PointF {
        val x = imgRect.left + (percent.x / 100f) * imgRect.width()
        val y = imgRect.top + (percent.y / 100f) * imgRect.height()
        return PointF(x, y)
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

    private fun applyAnimation(view: View) {
        val animSet = AnimationSet(true)
        val scale = ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, view.width / 2f, view.height / 2f)
        scale.duration = 500
        val fade = AlphaAnimation(0f, 1f)
        fade.duration = 500
        animSet.addAnimation(scale)
        animSet.addAnimation(fade)
        view.startAnimation(animSet)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
