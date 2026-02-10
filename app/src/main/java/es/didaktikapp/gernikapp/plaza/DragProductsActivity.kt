package es.didaktikapp.gernikapp.plaza

import android.content.ClipData
import android.graphics.Outline
import android.media.MediaPlayer
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.plaza.models.Product
import es.didaktikapp.gernikapp.plaza.models.ProductCategory
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch
import androidx.core.content.edit

/**
 * Actividad del juego de arrastrar productos a sus puestos correspondientes en el mercado.
 * El usuario debe clasificar los productos arrastrándolos a la categoría correcta,
 * obteniendo puntuación según los aciertos.
 *
 * @author Arantxa Main
 * @version 1.0
 * @see Product
 * @see ProductCategory
 */
class DragProductsActivity : BaseMenuActivity() {

    /** Grid donde se muestran los productos arrastrables. */
    private lateinit var gridProductos: GridLayout

    /** Botón para volver al menú principal del módulo Plaza. */
    private lateinit var btnBack: Button

    /** Repositorio para gestionar el inicio y finalización de actividades del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión que contiene tokens y el juegoId necesario para la API. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad devuelto por la API al iniciarla. */
    private var actividadProgresoId: String? = null

    /** Lista de productos que el usuario debe clasificar. */
    private val products = mutableListOf<Product>()

    /** Contador de productos correctamente colocados. */
    private var productosColocados = 0

    /** Sonido ambiente del mercado. */
    private var mediaPlayer: MediaPlayer? = null

    /** Sonido reproducido al acertar un producto. */
    private var sonidoAcierto: MediaPlayer? = null

    /** Devuelve el layout asociado a esta actividad. */
    override fun getContentLayoutId() = R.layout.plaza_drag_products

    /**
     * Inicializa la actividad:
     * - Registra inicio en LogManager
     * - Carga vistas y progreso previo
     * - Inicia actividad en API
     * - Crea productos y vistas dinámicas
     * - Configura puestos y scroll
     * - Prepara audio ambiente
     */
    override fun onContentInflated() {
        LogManager.write(this@DragProductsActivity, "DragProductsActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        gridProductos = findViewById(R.id.gridProductos)
        btnBack = findViewById(R.id.btnBack)

        val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)
        if (prefs.getBoolean("drag_products_completed", false)) {
            btnBack.isEnabled = true
        }

        iniciarActividad()
        inicializarProductos()
        crearVistaProductos()
        configurarPuestos()
        configurarScrollView()
        setupButtons()
        inicializarAudio()
    }

    /**
     * Inicializa el audio ambiente y el sonido de acierto.
     */
    private fun inicializarAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.plaza_ambience).apply {
            isLooping = true
            setVolume(0.7f, 0.7f)
        }
        sonidoAcierto = MediaPlayer.create(this, R.raw.plaza_success)
    }

    /** Reanuda el sonido ambiente al volver a la actividad. */
    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    /** Pausa el sonido ambiente al salir de la actividad. */
    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    /** Libera los recursos de audio al destruir la actividad. */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        sonidoAcierto?.release()
        sonidoAcierto = null
    }

    /**
     * Configura el ScrollView para permitir eventos de drag sin interferencias.
     */
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

    /**
     * Inicializa la lista de productos disponibles en el mercado.
     * Cada producto tiene:
     * - ID
     * - Nombre
     * - Imagen
     * - Categoría
     */
    private fun inicializarProductos() {
        // Lácteos
        products.add(Product(1, "Gazta", "Gazta", R.drawable.plaza_gazta, ProductCategory.LACTEOS))
        products.add(Product(2, "Esnea", "Esnea", R.drawable.plaza_esnea, ProductCategory.LACTEOS))
        products.add(Product(3, "Jogurta", "Jogurta", R.drawable.plaza_jogurta, ProductCategory.LACTEOS))

        // Verduras
        products.add(Product(4, "Piperrak", "Piperrak", R.drawable.plaza_piperrak, ProductCategory.VERDURAS))
        products.add(Product(5, "Tipula", "Tipula", R.drawable.plaza_kipula, ProductCategory.VERDURAS))
        products.add(Product(6, "Indabak", "Indabak", R.drawable.plaza_indabak, ProductCategory.VERDURAS))

        // Panadería
        products.add(Product(7, "Ogia", "Ogia", R.drawable.plaza_ogi_otzara, ProductCategory.PANADERIA))
        products.add(Product(8, "Madalenak", "Madalenak", R.drawable.plaza_madalenak, ProductCategory.PANADERIA))
        products.add(Product(9, "Pastela", "Pastela", R.drawable.plaza_pastela, ProductCategory.PANADERIA))

        // Natural
        products.add(Product(10, "Eztia", "Eztia", R.drawable.plaza_eztia, ProductCategory.NATURAL))
        products.add(Product(11, "Polena", "Polena", R.drawable.plaza_polena, ProductCategory.NATURAL))

        // Artesanía
        products.add(Product(12, "Egurrezko Lanak", "Egurrezko Lanak", R.drawable.plaza_egurrezko_esku_lanak, ProductCategory.ARTESANIA))
        products.add(Product(13, "Burdinazko Ontziak", "Burdinazko Ontziak", R.drawable.plaza_buztinezko_ontziak, ProductCategory.ARTESANIA))
        products.add(Product(14, "Zestak", "Zestak", R.drawable.plaza_zestak, ProductCategory.ARTESANIA))

        LogManager.write(this@DragProductsActivity, "Productos inicializados: ${products.size} items")
    }

    /**
     * Crea dinámicamente la vista de cada producto dentro del GridLayout.
     * Cada producto se envuelve en:
     * - Un contenedor exterior con fondo
     * - Un contenedor interior con bordes redondeados
     * - Un ImageView con la imagen del producto
     */
    private fun crearVistaProductos() {
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels

        // Convertir dp a px
        val scrollMargin = (16 * density * 2).toInt()
        val gridPadding = (8 * density * 2).toInt()
        val itemMargin = (4 * density).toInt()

        // Calcular ancho disponible
        val availableWidth = screenWidth - scrollMargin - gridPadding
        val totalMargins = itemMargin * 2 * 4
        val itemSize = (availableWidth - totalMargins) / 4

        products.forEachIndexed { index, producto ->
            // Calcular posición en la cuadrícula
            val row = index / 4
            val col = index % 4

            // Centrar la última fila si tiene menos de 4 elementos
            val isLastRow = index >= 12
            val adjustedCol = if (isLastRow) {
                col + 1
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
                            postDelayed(longPressHandler, 500)
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
                            performClick()
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

    /**
     * Inicia el proceso de drag & drop cuando el usuario hace long press
     * sobre un producto.
     *
     * @param view Contenedor del producto arrastrado
     * @return true si el drag se inició correctamente
     */
    private fun onProductoLongClick(view: View): Boolean {
        // El view es el contenedor exterior, buscar imageContainer -> imageView
        val container = view as FrameLayout
        val imageContainer = container.getChildAt(0) as FrameLayout
        val imageView = imageContainer.getChildAt(0) as AppCompatImageView
        val product = imageView.tag as Product

        LogManager.write(this@DragProductsActivity, "Drag iniciado para producto: ${product.nombreEuskera}")

        val clipData = ClipData.newPlainText("producto", product.id.toString())
        val shadowBuilder = View.DragShadowBuilder(imageView)

        // Iniciar drag and drop
        imageView.startDragAndDrop(clipData, shadowBuilder, container, 0)
        container.visibility = View.INVISIBLE

        // Permitir que el ScrollView no interfiera con el drag
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scrollProductos)
        scrollView.requestDisallowInterceptTouchEvent(true)

        return true
    }

    /**
     * Configura los listeners de drag para cada puesto del mercado.
     * Cada puesto acepta únicamente productos de su categoría.
     */
    private fun configurarPuestos() {
        // Configurar listeners para cada puesto
        val puesto0 = findViewById<LinearLayout>(R.id.puesto0)
        val puesto1 = findViewById<LinearLayout>(R.id.puesto1)
        val puesto2 = findViewById<LinearLayout>(R.id.puesto2)
        val puesto3 = findViewById<LinearLayout>(R.id.puesto3)
        val puesto4 = findViewById<LinearLayout>(R.id.puesto4)

        puesto0.setOnDragListener { v, event -> onPuestoDrag(v, event, ProductCategory.LACTEOS) }
        puesto1.setOnDragListener { v, event -> onPuestoDrag(v, event, ProductCategory.VERDURAS) }
        puesto2.setOnDragListener { v, event -> onPuestoDrag(v, event, ProductCategory.PANADERIA) }
        puesto3.setOnDragListener { v, event -> onPuestoDrag(v, event, ProductCategory.NATURAL) }
        puesto4.setOnDragListener { v, event -> onPuestoDrag(v, event, ProductCategory.ARTESANIA) }
    }

    /**
     * Maneja los eventos de drag & drop sobre un puesto.
     *
     * Comportamiento:
     * - Cambia opacidad al entrar/salir
     * - Valida si el producto pertenece a la categoría correcta
     * - Da feedback visual y sonoro
     * - Marca progreso y completa actividad si corresponde
     */
    private fun onPuestoDrag(view: View, event: DragEvent, categoriaEsperada: ProductCategory): Boolean {
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
                val product = imageView.tag as Product

                if (product.categoria == categoriaEsperada) {
                    LogManager.write(this@DragProductsActivity, "Producto correcto: ${product.nombreEuskera} → $categoriaEsperada")

                    // Respuesta correcta
                    mostrarFeedbackCorrecto(view)
                    container.visibility = View.INVISIBLE
                    productosColocados++

                    if (productosColocados >= products.size) {
                        LogManager.write(this@DragProductsActivity, "Juego completado: ${productosColocados}/${products.size} productos colocados")

                        btnBack.isEnabled = true
                        // Guardar progreso
                        val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)
                        prefs.edit {
                            putBoolean("drag_products_completed", true)
                            putFloat("drag_products_score", 100f)
                        }
                        ZoneCompletionActivity.launchIfComplete(this@DragProductsActivity, ZoneConfig.PLAZA)
                        completarActividad()
                        mostrarMensajeCompletado()
                    }
                } else {
                    LogManager.write(this@DragProductsActivity, "Producto incorrecto: ${product.nombreEuskera} → puesto $categoriaEsperada")

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

    /**
     * Muestra feedback visual y reproduce sonido al acertar un producto.
     */
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

    /**
     * Muestra un diálogo de actividad completada cuando todos los productos
     * han sido colocados correctamente.
     */
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

    /**
     * Muestra feedback visual cuando el producto no corresponde al puesto.
     */
    private fun mostrarFeedbackIncorrecto(view: View) {
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.error))
        view.postDelayed({
            view.setBackgroundResource(R.drawable.plaza_bg_puesto)
        }, 500)
    }

    /**
     * Configura el botón de volver al menú principal del módulo Plaza.
     */
    private fun setupButtons() {
        btnBack.setOnClickListener {
            LogManager.write(this@DragProductsActivity, "Usuario salió de DragProductsActivity")
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Inicia la actividad en la API del juego.
     * Guarda el ID de progreso devuelto para completarla más tarde.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Plaza.ID, Puntos.Plaza.DRAG_PRODUCTS)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@DragProductsActivity, "API iniciarActividad PLAZA_DRAG_PRODUCTS id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("DragProducts", "Error: ${result.message}")
                    LogManager.write(this@DragProductsActivity, "Error iniciarActividad PLAZA_DRAG_PRODUCTS: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa la actividad en la API enviando una puntuación de 100.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    Log.d("DragProducts", "Completado")
                    LogManager.write(this@DragProductsActivity, "API completarActividad PLAZA_DRAG_PRODUCTS puntuación=100")
                }
                is Resource.Error -> {
                    Log.e("DragProducts", "Error: ${result.message}")
                    LogManager.write(this@DragProductsActivity, "Error completarActividad PLAZA_DRAG_PRODUCTS: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}
