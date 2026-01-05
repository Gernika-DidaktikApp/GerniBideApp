package es.didaktikapp.gernikapp.plazagernika

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.models.CategoriaProducto
import es.didaktikapp.gernikapp.plazagernika.models.Producto

class ArrastrProductosActivity : AppCompatActivity() {

    private lateinit var gridProductos: GridLayout
    private lateinit var btnSiguiente: Button
    private val productos = mutableListOf<Producto>()
    private var productosColocados = 0

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
            val imageView = ImageView(this).apply {
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

                layoutParams = GridLayout.LayoutParams().apply {
                    width = itemSize
                    height = itemSize
                    columnSpec = GridLayout.spec(adjustedCol)
                    rowSpec = GridLayout.spec(row)
                    setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
                }
                setImageResource(producto.imagenRes)
                tag = producto
                contentDescription = producto.nombreEuskera
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundResource(R.drawable.plaza_bg_producto)
                setPadding(8, 8, 8, 8)
                isClickable = true
                isFocusable = true
                isLongClickable = true

                // Usar OnTouchListener para manejar el long press manualmente
                var longPressHandler: Runnable? = null
                setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            Log.d("ArrastrProductos", "Touch DOWN en ${producto.nombreEuskera}")
                            // Cancelar el scroll del ScrollView
                            parent.requestDisallowInterceptTouchEvent(true)

                            // Iniciar temporizador para long press
                            longPressHandler = Runnable {
                                Log.d("ArrastrProductos", "Long press detectado!")
                                onProductoLongClick(v)
                            }
                            postDelayed(longPressHandler, 500) // 500ms para long press
                            false
                        }
                        android.view.MotionEvent.ACTION_MOVE -> {
                            // Si el usuario mueve el dedo, cancelar long press
                            false
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            Log.d("ArrastrProductos", "Touch UP/CANCEL")
                            // Permitir que el ScrollView intercepte de nuevo
                            parent.requestDisallowInterceptTouchEvent(false)
                            // Cancelar el temporizador
                            longPressHandler?.let { removeCallbacks(it) }
                            false
                        }
                        else -> false
                    }
                }
            }
            gridProductos.addView(imageView)
        }
    }

    private fun onProductoLongClick(view: View): Boolean {
        val producto = view.tag as Producto
        Log.d("ArrastrProductos", "Long click detectado en: ${producto.nombreEuskera}")
        Toast.makeText(this, "Arrastrando ${producto.nombreEuskera}", Toast.LENGTH_SHORT).show()

        val clipData = ClipData.newPlainText("producto", producto.id.toString())
        val shadowBuilder = View.DragShadowBuilder(view)

        // Iniciar drag and drop
        view.startDragAndDrop(clipData, shadowBuilder, view, 0)
        view.visibility = View.INVISIBLE

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
                Log.d("ArrastrProductos", "DRAG_STARTED en puesto $categoriaEsperada")
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d("ArrastrProductos", "DRAG_ENTERED en puesto $categoriaEsperada")
                view.alpha = 0.7f
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d("ArrastrProductos", "DRAG_EXITED de puesto $categoriaEsperada")
                view.alpha = 1.0f
                return true
            }
            DragEvent.ACTION_DROP -> {
                Log.d("ArrastrProductos", "DROP en puesto $categoriaEsperada")
                view.alpha = 1.0f
                val draggedView = event.localState as ImageView
                val producto = draggedView.tag as Producto

                if (producto.categoria == categoriaEsperada) {
                    // Respuesta correcta
                    Log.d("ArrastrProductos", "Correcto! ${producto.nombreEuskera} -> $categoriaEsperada")
                    Toast.makeText(this, "Oso ondo!", Toast.LENGTH_SHORT).show()
                    mostrarFeedbackCorrecto(view)
                    draggedView.visibility = View.GONE
                    productosColocados++

                    if (productosColocados >= productos.size) {
                        btnSiguiente.isEnabled = true
                    }
                } else {
                    // Respuesta incorrecta
                    Log.d("ArrastrProductos", "Incorrecto! ${producto.nombreEuskera} -> $categoriaEsperada (esperaba ${producto.categoria})")
                    Toast.makeText(this, "Ez da zuzena", Toast.LENGTH_SHORT).show()
                    mostrarFeedbackIncorrecto(view)
                    draggedView.visibility = View.VISIBLE
                }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("ArrastrProductos", "DRAG_ENDED, result: ${event.result}")
                view.alpha = 1.0f
                if (!event.result) {
                    // No se hizo drop en ningún puesto válido
                    val draggedView = event.localState as ImageView
                    if (draggedView.visibility != View.GONE) {
                        draggedView.visibility = View.VISIBLE
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
