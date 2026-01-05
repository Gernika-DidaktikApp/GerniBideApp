package es.didaktikapp.gernikapp.plazagernika

import android.content.ClipData
import android.content.Intent
import android.graphics.Outline
import android.media.MediaPlayer
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.models.CategoriaProducto
import es.didaktikapp.gernikapp.plazagernika.models.Producto

class ArrastrProductosActivity : AppCompatActivity() {

    private lateinit var gridProductos: GridLayout
    private lateinit var btnSiguiente: Button
    private val productos = mutableListOf<Producto>()
    private var productosColocados = 0
    private var mediaPlayer: MediaPlayer? = null
    private var sonidoAcierto: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaza_arrastr_productos)

        gridProductos = findViewById(R.id.gridProductos)
        btnSiguiente = findViewById(R.id.btnSiguiente)

        inicializarProductos()
        crearVistaProductos()
        configurarPuestos()
        configurarScrollView()
        setupButtons()
        inicializarAudio()
    }

    private fun inicializarAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.plaza_ambiente).apply {
            isLooping = true
            setVolume(0.7f, 0.7f)
        }
        sonidoAcierto = MediaPlayer.create(this, R.raw.plaza_acierto)
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        sonidoAcierto?.release()
        sonidoAcierto = null
    }

    private fun configurarScrollView() {
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollProductos)

        // Permitir que los eventos de drag pasen a través del ScrollView
        scrollView.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED,
                DragEvent.ACTION_DRAG_ENTERED,
                DragEvent.ACTION_DRAG_EXITED,
                DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }
    }

    private fun inicializarProductos() {
        // Lácteos
        productos.add(Producto(1, "Gazta", "Gazta", R.drawable.plaza_gazta, CategoriaProducto.LACTEOS))
        productos.add(Producto(2, "Esnea", "Esnea", R.drawable.plaza_esnea, CategoriaProducto.LACTEOS))
        productos.add(Producto(3, "Jogurta", "Jogurta", R.drawable.plaza_jogurta, CategoriaProducto.LACTEOS))

        // Verduras
        productos.add(Producto(4, "Piperrak", "Piperrak", R.drawable.plaza_piperrak, CategoriaProducto.VERDURAS))
        productos.add(Producto(5, "Tipula", "Tipula", R.drawable.plaza_kipula, CategoriaProducto.VERDURAS))
        productos.add(Producto(6, "Indabak", "Indabak", R.drawable.plaza_indabak, CategoriaProducto.VERDURAS))

        // Panadería
        productos.add(Producto(7, "Ogia", "Ogia", R.drawable.plaza_ogi_otzara, CategoriaProducto.PANADERIA))
        productos.add(Producto(8, "Madalenak", "Madalenak", R.drawable.plaza_madalenak, CategoriaProducto.PANADERIA))
        productos.add(Producto(9, "Pastela", "Pastela", R.drawable.plaza_pastela, CategoriaProducto.PANADERIA))

        // Natural
        productos.add(Producto(10, "Eztia", "Eztia", R.drawable.plaza_eztia, CategoriaProducto.NATURAL))
        productos.add(Producto(11, "Polena", "Polena", R.drawable.plaza_polena, CategoriaProducto.NATURAL))

        // Artesanía
        productos.add(Producto(12, "Egurrezko Lanak", "Egurrezko Lanak", R.drawable.plaza_egurrezko_esku_lanak, CategoriaProducto.ARTESANIA))
        productos.add(Producto(13, "Burdinazko Ontziak", "Burdinazko Ontziak", R.drawable.plaza_buztinezko_ontziak, CategoriaProducto.ARTESANIA))
        productos.add(Producto(14, "Zestak", "Zestak", R.drawable.plaza_zestak, CategoriaProducto.ARTESANIA))
    }

    private fun crearVistaProductos() {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels

        // Convertir dp a px
        val scrollMargin = (16 * density * 2).toInt() // 16dp a cada lado del ScrollView
        val gridPadding = (8 * density * 2).toInt() // 8dp de padding del GridLayout
        val itemMargin = (4 * density).toInt() // 4dp de margen por item

        // Calcular ancho disponible
        val availableWidth = screenWidth - scrollMargin - gridPadding
        val totalMargins = itemMargin * 2 * 4 // 4dp * 2 lados * 4 columnas
        val itemSize = (availableWidth - totalMargins) / 4

        productos.forEachIndexed { index, producto ->
            // Calcular posición en la cuadrícula
            val row = index / 4
            val col = index % 4

            // Centrar la última fila si tiene menos de 4 elementos
            val isLastRow = index >= 12 // Los últimos 2 productos (índices 12 y 13)
            val adjustedCol = if (isLastRow) {
                col + 1 // Desplazar 1 columna a la derecha para centrar
            } else {
                col
            }

            // Crear contenedor exterior con el background
            val container = FrameLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = itemSize
                    height = itemSize
                    columnSpec = GridLayout.spec(adjustedCol)
                    rowSpec = GridLayout.spec(row)
                    setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
                }
                setBackgroundResource(R.drawable.plaza_bg_producto)
            }

            // Crear contenedor interior para la imagen con bordes redondeados
            val imageContainer = FrameLayout(this).apply {
                val marginPx = (5 * resources.displayMetrics.density).toInt()
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
                clipToOutline = true
                outlineProvider = object : android.view.ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, 3f * resources.displayMetrics.density)
                    }
                }
            }

            // Crear ImageView dentro del contenedor interior
            val imageView = object : AppCompatImageView(this) {
                override fun performClick(): Boolean {
                    super.performClick()
                    return true
                }
            }.apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageResource(producto.imagenRes)
                tag = producto
                contentDescription = producto.nombreEuskera
                scaleType = ImageView.ScaleType.CENTER_CROP
                isClickable = true
                isFocusable = true
                isLongClickable = true

                // Usar OnTouchListener para manejar el long press manualmente
                var longPressHandler: Runnable? = null
                setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            // Cancelar el scroll del ScrollView
                            parent.parent.parent.requestDisallowInterceptTouchEvent(true)

                            // Iniciar temporizador para long press
                            longPressHandler = Runnable {
                                onProductoLongClick(container)
                            }
                            postDelayed(longPressHandler, 500) // 500ms para long press
                            false
                        }
                        android.view.MotionEvent.ACTION_MOVE -> {
                            // Si el usuario mueve el dedo, cancelar long press
                            false
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            // Permitir que el ScrollView intercepte de nuevo
                            parent.parent.parent.requestDisallowInterceptTouchEvent(false)
                            // Cancelar el temporizador
                            longPressHandler?.let { removeCallbacks(it) }
                            performClick() // Para accesibilidad
                            false
                        }
                        else -> false
                    }
                }
            }

            imageContainer.addView(imageView)
            container.addView(imageContainer)
            gridProductos.addView(container)
        }
    }

    private fun onProductoLongClick(view: View): Boolean {
        // El view es el contenedor exterior, buscar imageContainer -> imageView
        val container = view as FrameLayout
        val imageContainer = container.getChildAt(0) as FrameLayout
        val imageView = imageContainer.getChildAt(0) as AppCompatImageView
        val producto = imageView.tag as Producto

        val clipData = ClipData.newPlainText("producto", producto.id.toString())
        val shadowBuilder = View.DragShadowBuilder(imageView)

        // Iniciar drag and drop
        imageView.startDragAndDrop(clipData, shadowBuilder, container, 0)
        container.visibility = View.INVISIBLE

        // Permitir que el ScrollView no interfiera con el drag
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollProductos)
        scrollView.requestDisallowInterceptTouchEvent(true)

        return true
    }

    private fun configurarPuestos() {
        // Configurar listeners para cada puesto
        val puesto0 = findViewById<LinearLayout>(R.id.puesto0)
        val puesto1 = findViewById<LinearLayout>(R.id.puesto1)
        val puesto2 = findViewById<LinearLayout>(R.id.puesto2)
        val puesto3 = findViewById<LinearLayout>(R.id.puesto3)
        val puesto4 = findViewById<LinearLayout>(R.id.puesto4)

        puesto0.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.LACTEOS) }
        puesto1.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.VERDURAS) }
        puesto2.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.PANADERIA) }
        puesto3.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.NATURAL) }
        puesto4.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.ARTESANIA) }
    }

    private fun onPuestoDrag(view: View, event: DragEvent, categoriaEsperada: CategoriaProducto): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.alpha = 0.7f
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.alpha = 1.0f
                return true
            }
            DragEvent.ACTION_DROP -> {
                view.alpha = 1.0f
                val container = event.localState as FrameLayout
                val imageContainer = container.getChildAt(0) as FrameLayout
                val imageView = imageContainer.getChildAt(0) as AppCompatImageView
                val producto = imageView.tag as Producto

                if (producto.categoria == categoriaEsperada) {
                    // Respuesta correcta
                    mostrarFeedbackCorrecto(view)
                    container.visibility = View.INVISIBLE // Mantener el espacio en el grid
                    productosColocados++

                    if (productosColocados >= productos.size) {
                        btnSiguiente.isEnabled = true
                        mostrarMensajeCompletado()
                    }
                } else {
                    // Respuesta incorrecta
                    mostrarFeedbackIncorrecto(view)
                    container.visibility = View.VISIBLE
                }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                view.alpha = 1.0f
                if (!event.result) {
                    // No se hizo drop en ningún puesto válido
                    val container = event.localState as FrameLayout
                    if (container.visibility == View.INVISIBLE) {
                        container.visibility = View.VISIBLE
                    }
                }
                return true
            }
            else -> return false
        }
    }

    private fun mostrarFeedbackCorrecto(view: View) {
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.correcto))
        view.postDelayed({
            view.setBackgroundResource(R.drawable.plaza_bg_puesto)
        }, 500)

        // Reproducir sonido de acierto
        sonidoAcierto?.apply {
            if (isPlaying) {
                seekTo(0)
            }
            start()
        }
    }

    private fun mostrarMensajeCompletado() {
        // Mostrar el diálogo después de un pequeño delay para que se vea el último acierto
        gridProductos.postDelayed({
            AlertDialog.Builder(this)
                .setTitle(R.string.completado_titulo)
                .setMessage(R.string.completado_mensaje)
                .setPositiveButton(R.string.jarraitu) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }, 800)
    }

    private fun mostrarFeedbackIncorrecto(view: View) {
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
        view.postDelayed({
            view.setBackgroundResource(R.drawable.plaza_bg_puesto)
        }, 500)
    }

    private fun setupButtons() {
        btnSiguiente.setOnClickListener {
            val intent = Intent(this, VersoGameActivity::class.java)
            startActivity(intent)
        }
    }
}
