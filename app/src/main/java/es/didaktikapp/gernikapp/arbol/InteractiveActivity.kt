package es.didaktikapp.gernikapp.arbol

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
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
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import kotlin.math.hypot

class InteractiveActivity : BaseMenuActivity() {

    private lateinit var treeContainer: FrameLayout
    private lateinit var treeImage: ImageView

    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    private var eventoEstadoId: String? = null

    // Coordenadas calculadas específicamente para los óvalos de arbola_image.jpg
    private val slotPercentages = listOf(
        PointF(50f, 9.5f),  // Superior Central
        PointF(27f, 19f),   // Fila 2 - Izquierda
        PointF(73f, 19f),   // Fila 2 - Derecha
        PointF(19f, 32f),   // Fila 3 - Izquierda
        PointF(50f, 35f),   // Fila 3 - Centro
        PointF(81f, 32f),   // Fila 3 - Derecha
        PointF(20f, 46f),   // Fila 4 - Izquierda
        PointF(50f, 48f),   // Fila 4 - Centro
        PointF(80f, 46f),   // Fila 4 - Derecha
        PointF(32f, 55f),   // Fila 5 - Izquierda
        PointF(68f, 55f),   // Fila 5 - Derecha
        PointF(50f, 60f),   // Fila 6 - Centro Bajo
        PointF(50f, 93f)    // Óvalo Raíces (Césped)
    )

    private val occupiedSlots = mutableMapOf<Int, View?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arbol_interactive)

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        treeContainer = findViewById(R.id.treeContainer)
        treeImage = findViewById(R.id.treeImage)

        // Configurar los botones de valores
        setupWordButtons()

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<View>(R.id.btnFinish).setOnClickListener {
            completarEvento()
            finish()
        }

        iniciarEvento()
    }

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

                // Buscar el primer hueco libre en los óvalos
                val targetSlot = slotPercentages.indices.firstOrNull { occupiedSlots[it] == null }

                if (targetSlot != null) {
                    addValueToTree(text, color, targetSlot)
                }
            }
        }
    }

    private fun addValueToTree(text: String, color: Int, initialSlot: Int) {
        val textView = TextView(this).apply {
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

            // Ancho fijo para encajar en los óvalos de la imagen
            this.layoutParams = FrameLayout.LayoutParams(240, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        treeContainer.addView(textView)

        textView.post {
            moveWordToSlot(textView, initialSlot)
            applyAnimation(textView)
        }

        setupDragLogic(textView, initialSlot)
    }

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

                        for (i in slotPercentages.indices) {
                            val slotPos = getSlotCoords(slotPercentages[i], imgRect)
                            val dist = hypot((v.x + v.width / 2 - slotPos.x), (v.y + v.height / 2 - slotPos.y)).toFloat()

                            // Solo imantar si el slot está libre o es el suyo propio
                            if (dist < minDistance && (occupiedSlots[i] == null || occupiedSlots[i] == v)) {
                                minDistance = dist
                                nearestSlot = i
                            }
                        }

                        // Sensibilidad del imán (Snap)
                        if (nearestSlot != -1 && minDistance < 250) {
                            currentSlot = nearestSlot
                        }

                        moveWordToSlot(v, currentSlot)
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

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            val result = gameRepository.iniciarEvento(juegoId, Actividades.Arbol.ID, Actividades.Arbol.MY_TREE)
            if (result is Resource.Success) {
                eventoEstadoId = result.data.id
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId ?: return
        lifecycleScope.launch {
            gameRepository.completarEvento(estadoId, 100.0)
        }
    }
}