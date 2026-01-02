package es.didaktikapp.gernikapp.plazagernika

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
        setupButtons()
    }

    private fun inicializarProductos() {
        productos.add(Producto(1, "Queso Idiazabal", "Gazta", R.drawable.plaza_gazta, CategoriaProducto.LACTEOS))
        productos.add(Producto(2, "Pimientos de Gernika", "Piperrak", R.drawable.plaza_piperrak, CategoriaProducto.VERDURAS))
        productos.add(Producto(3, "Leche", "Esnea", R.drawable.plaza_esnea, CategoriaProducto.LACTEOS))
        productos.add(Producto(4, "Pan artesanal", "Ogia", R.drawable.plaza_pastela, CategoriaProducto.PANADERIA))
        productos.add(Producto(5, "Miel", "Eztia", R.drawable.plaza_eztia, CategoriaProducto.NATURAL))
    }

    private fun crearVistaProductos() {
        productos.forEachIndexed { index, producto ->
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                setImageResource(producto.imagenRes)
                tag = producto
                contentDescription = producto.nombreEuskera
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                setBackgroundResource(R.drawable.plaza_bg_producto)
                setPadding(16, 16, 16, 16)
                setOnLongClickListener { onProductoLongClick(it) }
            }
            gridProductos.addView(imageView)
        }
    }

    private fun onProductoLongClick(view: View): Boolean {
        val producto = view.tag as Producto
        val clipData = ClipData.newPlainText("producto", producto.id.toString())
        val shadowBuilder = View.DragShadowBuilder(view)
        view.startDragAndDrop(clipData, shadowBuilder, view, 0)
        view.visibility = View.INVISIBLE
        return true
    }

    private fun configurarPuestos() {
        // Configurar listeners para cada puesto
        val puesto0 = findViewById<LinearLayout>(R.id.puesto0)
        val puesto1 = findViewById<LinearLayout>(R.id.puesto1)
        val puesto2 = findViewById<LinearLayout>(R.id.puesto2)

        puesto0.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.LACTEOS) }
        puesto1.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.VERDURAS) }
        puesto2.setOnDragListener { v, event -> onPuestoDrag(v, event, CategoriaProducto.PANADERIA) }
    }

    private fun onPuestoDrag(view: View, event: DragEvent, categoriaEsperada: CategoriaProducto): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.base))
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                view.setBackgroundResource(R.drawable.plaza_bg_puesto)
                return true
            }
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as ImageView
                val producto = draggedView.tag as Producto

                if (producto.categoria == categoriaEsperada ||
                    (categoriaEsperada == CategoriaProducto.PANADERIA && producto.categoria == CategoriaProducto.NATURAL)) {
                    // Respuesta correcta
                    mostrarFeedbackCorrecto(view)
                    draggedView.visibility = View.GONE
                    productosColocados++

                    if (productosColocados >= productos.size) {
                        btnSiguiente.isEnabled = true
                    }
                } else {
                    // Respuesta incorrecta
                    mostrarFeedbackIncorrecto(view)
                    draggedView.visibility = View.VISIBLE
                }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                view.setBackgroundResource(R.drawable.plaza_bg_puesto)
                val draggedView = event.localState as ImageView
                if (draggedView.visibility == View.INVISIBLE) {
                    draggedView.visibility = View.VISIBLE
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
