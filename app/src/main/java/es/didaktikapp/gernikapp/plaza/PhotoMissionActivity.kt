package es.didaktikapp.gernikapp.plaza

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.plaza.adapters.PhotoMissionAdapter
import es.didaktikapp.gernikapp.plaza.models.EtiquetaFoto
import es.didaktikapp.gernikapp.plaza.models.FotoGaleria
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Activity de la misión fotográfica donde los usuarios toman fotos y las etiquetan.
 */
class PhotoMissionActivity : BaseMenuActivity() {

    private lateinit var btnTomarFoto: Button
    private lateinit var btnIgo: Button
    private lateinit var btnBack: Button
    private lateinit var ivFotoPreview: ImageView
    private lateinit var tvSeleccionarEtiqueta: TextView
    private lateinit var rgEtiquetas: RadioGroup
    private lateinit var rbTradizioa: RadioButton
    private lateinit var rbKomunitatea: RadioButton
    private lateinit var rbBizikidetza: RadioButton
    private lateinit var rvGaleria: RecyclerView
    private lateinit var adapter: PhotoMissionAdapter
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var eventoEstadoId: String? = null

    private val galeriaFotos = mutableListOf<FotoGaleria>()
    private var fotoActual: Bitmap? = null
    private var contadorFotos = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(this, getString(R.string.photo_mission_permiso_denegado), Toast.LENGTH_SHORT).show()
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

    override fun getContentLayoutId() = R.layout.plaza_photo_mission

    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        inicializarVistas()
        iniciarEvento()
        setupRecyclerView()
        setupButtons()
    }

    private fun inicializarVistas() {
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnIgo = findViewById(R.id.btnIgo)
        btnBack = findViewById(R.id.btnBack)
        ivFotoPreview = findViewById(R.id.ivFotoPreview)
        tvSeleccionarEtiqueta = findViewById(R.id.tvSeleccionarEtiqueta)
        rgEtiquetas = findViewById(R.id.rgEtiquetas)
        rbTradizioa = findViewById(R.id.rbTradizioa)
        rbKomunitatea = findViewById(R.id.rbKomunitatea)
        rbBizikidetza = findViewById(R.id.rbBizikidetza)
        rvGaleria = findViewById(R.id.rvGaleria)

        // Check if activity was previously completed
        val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("photo_mission_completed", false)) {
            btnBack.isEnabled = true
        }
    }

    private fun setupRecyclerView() {
        adapter = PhotoMissionAdapter(galeriaFotos)
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

        btnBack.setOnClickListener {
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
            Toast.makeText(this, getString(R.string.photo_mission_seleccionar_etiqueta_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (fotoActual == null) {
            Toast.makeText(this, getString(R.string.photo_mission_no_foto), Toast.LENGTH_SHORT).show()
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

        // Enable back button and save progress
        btnBack.isEnabled = true
        val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("photo_mission_completed", true).apply()
        completarEvento()

        // Resetear vista
        resetearVista()

        Toast.makeText(this, getString(R.string.photo_mission_subida_exito), Toast.LENGTH_SHORT).show()
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

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(juegoId, Actividades.Plaza.ID, Actividades.Plaza.PHOTO_MISSION)) {
                is Resource.Success -> eventoEstadoId = result.data.id
                is Resource.Error -> Log.e("PhotoMission", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(estadoId, 100.0)) {
                is Resource.Success -> Log.d("PhotoMission", "Completado")
                is Resource.Error -> Log.e("PhotoMission", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}
