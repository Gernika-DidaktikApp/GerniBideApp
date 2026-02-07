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
        setupRecyclerView()
        setupButtons()
        iniciarActividad() // Carga la foto automáticamente si existe
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

                    // Enable back button
                    btnBack.isEnabled = true

                    // Completar actividad con URL de imagen y etiqueta en formato JSON
                    val respuestaJson = """{"url":"$imageUrl","etiqueta":"${etiqueta.name}"}"""
                    completarActividad(respuestaJson)

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
                is Resource.Success -> {
                    actividadProgresoId = result.data.id

                    // Si la actividad ya está completada, cargar la foto
                    if (result.data.estaCompletado()) {
                        cargarFotoDesdeApi(result.data)
                    }
                }
                is Resource.Error -> Log.e(TAG, "❌ Error iniciar actividad: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa la actividad enviando la puntuación y los datos de la foto en formato JSON.
     *
     * @param respuestaJson JSON con URL de Cloudinary y etiqueta: {"url":"...", "etiqueta":"..."}
     */
    private fun completarActividad(respuestaJson: String) {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0, respuestaJson)) {
                is Resource.Success -> Log.d(TAG, "✅ Actividad completada: $respuestaJson")
                is Resource.Error -> Log.e(TAG, "❌ Error completando actividad: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Carga la foto desde la API de Gernika.
     * Parsea el JSON de respuesta_contenido para obtener la URL y etiqueta.
     *
     * @param progreso Respuesta de la API con los datos del progreso
     */
    private fun cargarFotoDesdeApi(progreso: es.didaktikapp.gernikapp.data.models.ActividadProgresoResponse) {
        val respuestaContenido = progreso.respuestaContenido

        if (respuestaContenido.isNullOrEmpty()) {
            Log.d(TAG, "⚠️ No hay foto guardada en la API")
            return
        }

        try {
            // Parsear JSON: {"url":"https://...", "etiqueta":"TRADIZIOA"}
            val jsonRegex = """"url":"([^"]+)","etiqueta":"([^"]+)"""".toRegex()
            val matchResult = jsonRegex.find(respuestaContenido)

            if (matchResult != null) {
                val (photoUrl, etiquetaName) = matchResult.destructured
                val etiqueta = EtiquetaFoto.valueOf(etiquetaName)

                Log.d(TAG, "📸 Cargando foto desde API: $photoUrl")

                // Crear FotoGaleria con la imagen desde URL
                contadorFotos++
                val fotoGuardada = FotoGaleria(
                    id = contadorFotos,
                    bitmap = null, // Se cargará desde URL en el adapter
                    etiqueta = etiqueta,
                    url = photoUrl
                )

                galeriaFotos.add(0, fotoGuardada)
                adapter.notifyItemInserted(0)
                rvGaleria.scrollToPosition(0)

                // Habilitar botón de retroceso si la actividad está completada
                btnBack.isEnabled = true

                Log.d(TAG, "✅ Foto cargada desde API correctamente")
            } else {
                Log.w(TAG, "⚠️ Formato de respuesta_contenido no reconocido: $respuestaContenido")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parseando foto desde API: ${e.message}", e)
        }
    }
}
