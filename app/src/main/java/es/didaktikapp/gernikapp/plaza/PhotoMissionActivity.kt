package es.didaktikapp.gernikapp.plaza

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
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
import es.didaktikapp.gernikapp.data.repository.CloudinaryRepository
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.plaza.adapters.PhotoMissionAdapter
import es.didaktikapp.gernikapp.plaza.models.EtiquetaFoto
import es.didaktikapp.gernikapp.plaza.models.FotoGaleria
import es.didaktikapp.gernikapp.utils.Constants.Puntos
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
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PhotoMissionAdapter
    private lateinit var gameRepository: GameRepository
    private lateinit var cloudinaryRepository: CloudinaryRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null

    private val galeriaFotos = mutableListOf<FotoGaleria>()
    private var fotoActual: Bitmap? = null
    private var contadorFotos = 0
    private var isUploading = false

    companion object {
        private const val TAG = "PhotoMissionActivity"
        private const val PREFS_NAME = "plaza_progress"
        private const val KEY_PHOTO_COMPLETED = "photo_mission_completed"
        private const val KEY_PHOTO_URL = "photo_mission_url"
        private const val KEY_PHOTO_TAG = "photo_mission_tag"
    }

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
        cloudinaryRepository = CloudinaryRepository(this)
        tokenManager = TokenManager(this)

        inicializarVistas()
        iniciarActividad()
        setupRecyclerView()
        setupButtons()
        cargarFotoGuardada()
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
        progressBar = findViewById(R.id.progressBar)

        // Check if activity was previously completed
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_PHOTO_COMPLETED, false)) {
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

        if (isUploading) {
            Toast.makeText(this, "Ya hay una subida en progreso", Toast.LENGTH_SHORT).show()
            return
        }

        val etiqueta = when (selectedId) {
            R.id.rbTradizioa -> EtiquetaFoto.TRADIZIOA
            R.id.rbKomunitatea -> EtiquetaFoto.KOMUNITATEA
            R.id.rbBizikidetza -> EtiquetaFoto.BIZIKIDETZA
            else -> return
        }

        // Subir a Cloudinary usando el repositorio
        lifecycleScope.launch {
            isUploading = true
            mostrarCargando(true)

            when (val result = cloudinaryRepository.subirImagen(fotoActual!!)) {
                is Resource.Success -> {
                    val imageUrl = result.data

                    // Éxito: Añadir foto a la galería local
                    contadorFotos++
                    val nuevaFoto = FotoGaleria(
                        id = contadorFotos,
                        bitmap = fotoActual,
                        etiqueta = etiqueta,
                        url = imageUrl
                    )
                    galeriaFotos.add(0, nuevaFoto)
                    adapter.notifyItemInserted(0)
                    rvGaleria.scrollToPosition(0)

                    // Enable back button and save progress
                    btnBack.isEnabled = true
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    prefs.edit().apply {
                        putBoolean(KEY_PHOTO_COMPLETED, true)
                        putString(KEY_PHOTO_URL, imageUrl)
                        putString(KEY_PHOTO_TAG, etiqueta.name)
                        apply()
                    }

                    // Completar actividad con URL de imagen
                    completarActividad(imageUrl)

                    // Resetear vista
                    resetearVista()

                    Toast.makeText(
                        this@PhotoMissionActivity,
                        getString(R.string.photo_mission_subida_exito),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Error -> {
                    // Error en la subida
                    Log.e(TAG, "❌ Error subiendo imagen: ${result.message}")
                    Toast.makeText(
                        this@PhotoMissionActivity,
                        "Error al subir la imagen: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Loading -> { }
            }

            isUploading = false
            mostrarCargando(false)
        }
    }

    /**
     * Muestra u oculta el indicador de carga durante la subida.
     *
     * @param mostrar true para mostrar, false para ocultar
     */
    private fun mostrarCargando(mostrar: Boolean) {
        if (mostrar) {
            progressBar.visibility = View.VISIBLE
            btnIgo.isEnabled = false
            btnTomarFoto.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnIgo.isEnabled = true
            btnTomarFoto.isEnabled = true
        }
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

    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Plaza.ID, Puntos.Plaza.PHOTO_MISSION)) {
                is Resource.Success -> actividadProgresoId = result.data.id
                is Resource.Error -> Log.e("PhotoMission", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento enviando la puntuación y la URL de la imagen.
     *
     * @param imageUrl URL de la imagen subida a Cloudinary
     */
    private fun completarActividad(imageUrl: String) {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0, imageUrl)) {
                is Resource.Success -> Log.d(TAG, "✅ Evento completado con imagen: $imageUrl")
                is Resource.Error -> Log.e(TAG, "❌ Error completando evento: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Carga la foto guardada previamente desde Cloudinary.
     * Si existe una foto guardada, la muestra en la galería.
     */
    private fun cargarFotoGuardada() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val photoUrl = prefs.getString(KEY_PHOTO_URL, null)
        val photoTag = prefs.getString(KEY_PHOTO_TAG, null)

        if (photoUrl != null && photoTag != null) {
            Log.d(TAG, "📸 Cargando foto guardada: $photoUrl")

            // Cargar imagen desde URL usando Coil
            lifecycleScope.launch {
                try {
                    // Convertir la etiqueta de String a EtiquetaFoto
                    val etiqueta = EtiquetaFoto.valueOf(photoTag)

                    // Crear una FotoGaleria con la imagen cargada
                    contadorFotos++
                    val fotoGuardada = FotoGaleria(
                        id = contadorFotos,
                        bitmap = null, // Se cargará desde URL
                        etiqueta = etiqueta,
                        url = photoUrl // Añadiremos este campo al modelo
                    )

                    galeriaFotos.add(0, fotoGuardada)
                    adapter.notifyItemInserted(0)
                    rvGaleria.scrollToPosition(0)

                    Log.d(TAG, "✅ Foto guardada cargada en galería")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error cargando foto guardada: ${e.message}")
                }
            }
        }
    }
}
