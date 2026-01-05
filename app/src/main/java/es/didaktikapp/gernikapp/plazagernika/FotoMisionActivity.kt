package es.didaktikapp.gernikapp.plazagernika

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.adapters.MisionFotoAdapter
import es.didaktikapp.gernikapp.plazagernika.models.EtiquetaFoto
import es.didaktikapp.gernikapp.plazagernika.models.FotoGaleria

class FotoMisionActivity : AppCompatActivity() {

    private lateinit var btnTomarFoto: Button
    private lateinit var btnIgo: Button
    private lateinit var btnFinalizar: Button
    private lateinit var ivFotoPreview: ImageView
    private lateinit var tvSeleccionarEtiqueta: TextView
    private lateinit var rgEtiquetas: RadioGroup
    private lateinit var rbTradizioa: RadioButton
    private lateinit var rbKomunitatea: RadioButton
    private lateinit var rbBizikidetza: RadioButton
    private lateinit var rvGaleria: RecyclerView
    private lateinit var adapter: MisionFotoAdapter

    private val galeriaFotos = mutableListOf<FotoGaleria>()
    private var fotoActual: Bitmap? = null
    private var contadorFotos = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(this, "Kameraren baimena ukatuta", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                fotoActual = imageBitmap
                mostrarVistaPrevia(imageBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaza_foto_mision)

        inicializarVistas()
        setupRecyclerView()
        setupButtons()
    }

    private fun inicializarVistas() {
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnIgo = findViewById(R.id.btnIgo)
        btnFinalizar = findViewById(R.id.btnFinalizar)
        ivFotoPreview = findViewById(R.id.ivFotoPreview)
        tvSeleccionarEtiqueta = findViewById(R.id.tvSeleccionarEtiqueta)
        rgEtiquetas = findViewById(R.id.rgEtiquetas)
        rbTradizioa = findViewById(R.id.rbTradizioa)
        rbKomunitatea = findViewById(R.id.rbKomunitatea)
        rbBizikidetza = findViewById(R.id.rbBizikidetza)
        rvGaleria = findViewById(R.id.rvGaleria)
    }

    private fun setupRecyclerView() {
        adapter = MisionFotoAdapter(galeriaFotos)
        rvGaleria.layoutManager = GridLayoutManager(this, 2)
        rvGaleria.adapter = adapter
    }

    private fun setupButtons() {
        btnTomarFoto.setOnClickListener {
            verificarPermisoCamara()
        }

        btnIgo.setOnClickListener {
            subirFoto()
        }

        btnFinalizar.setOnClickListener {
            Toast.makeText(
                this,
                "Zorionak! Jarduera osatu duzu",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun mostrarVistaPrevia(bitmap: Bitmap) {
        ivFotoPreview.setImageBitmap(bitmap)
        ivFotoPreview.visibility = View.VISIBLE
        btnTomarFoto.visibility = View.GONE
        tvSeleccionarEtiqueta.visibility = View.VISIBLE
        rgEtiquetas.visibility = View.VISIBLE
        btnIgo.visibility = View.VISIBLE
    }

    private fun subirFoto() {
        val selectedId = rgEtiquetas.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "Mesedez, aukeratu etiketa bat", Toast.LENGTH_SHORT).show()
            return
        }

        if (fotoActual == null) {
            Toast.makeText(this, "Ez dago argazkirik", Toast.LENGTH_SHORT).show()
            return
        }

        val etiqueta = when (selectedId) {
            R.id.rbTradizioa -> EtiquetaFoto.TRADIZIOA
            R.id.rbKomunitatea -> EtiquetaFoto.KOMUNITATEA
            R.id.rbBizikidetza -> EtiquetaFoto.BIZIKIDETZA
            else -> return
        }

        // Añadir foto a la galería
        contadorFotos++
        val nuevaFoto = FotoGaleria(
            id = contadorFotos,
            bitmap = fotoActual!!,
            etiqueta = etiqueta
        )
        galeriaFotos.add(0, nuevaFoto) // Añadir al inicio
        adapter.notifyItemInserted(0)
        rvGaleria.scrollToPosition(0)

        // Resetear vista
        resetearVista()

        Toast.makeText(this, "Argazkia igo da galerira!", Toast.LENGTH_SHORT).show()
    }

    private fun resetearVista() {
        fotoActual = null
        ivFotoPreview.visibility = View.GONE
        tvSeleccionarEtiqueta.visibility = View.GONE
        rgEtiquetas.visibility = View.GONE
        rgEtiquetas.clearCheck()
        btnIgo.visibility = View.GONE
        btnTomarFoto.visibility = View.VISIBLE
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirCamara() {
        val takePictureIntent = android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }
}
