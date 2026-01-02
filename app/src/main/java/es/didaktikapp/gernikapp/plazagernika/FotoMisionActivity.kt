package es.didaktikapp.gernikapp.plazagernika

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.adapters.MisionFotoAdapter
import es.didaktikapp.gernikapp.plazagernika.models.MisionFoto

class FotoMisionActivity : AppCompatActivity() {

    private lateinit var rvMisiones: RecyclerView
    private lateinit var btnFinalizar: Button
    private lateinit var adapter: MisionFotoAdapter
    private val misiones = mutableListOf<MisionFoto>()
    private var misionActual: MisionFoto? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null && misionActual != null) {
                misionActual?.completada = true
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "¡Misión completada!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaza_foto_mision)

        rvMisiones = findViewById(R.id.rvMisiones)
        btnFinalizar = findViewById(R.id.btnFinalizar)

        inicializarMisiones()
        setupRecyclerView()
        setupButtons()
    }

    private fun inicializarMisiones() {
        misiones.add(
            MisionFoto(
                1,
                "Puesto de quesos",
                "Encuentra y fotografía un puesto de quesos",
                R.drawable.plaza_gazta
            )
        )
        misiones.add(
            MisionFoto(
                2,
                "Pimientos de Gernika",
                "Encuentra pimientos de Gernika",
                R.drawable.plaza_piperrak
            )
        )
        misiones.add(
            MisionFoto(
                3,
                "Ambiente del mercado",
                "Captura la plaza con gente comprando",
                R.drawable.plaza_postua
            )
        )
    }

    private fun setupRecyclerView() {
        adapter = MisionFotoAdapter(misiones) { mision ->
            misionActual = mision
            verificarPermisoCamara()
        }
        rvMisiones.layoutManager = LinearLayoutManager(this)
        rvMisiones.adapter = adapter
    }

    private fun setupButtons() {
        btnFinalizar.setOnClickListener {
            Toast.makeText(
                this,
                "¡Felicidades! Has completado la actividad",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
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
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }
}
