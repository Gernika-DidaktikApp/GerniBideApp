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
import es.didaktikapp.gernikapp.plaza.models.FotoRespuestaContenido
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import androidx.core.content.edit

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
                                                                                                                                                                                                                                                                                                                                                                                                                                                                            private lateinit var rvGaleria: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: PhotoMissionAdapter
    private lateinit var gameRepository: GameRepository
    private lateinit var cloudinaryRepository: CloudinaryRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null
    private var actividadEstado: String? = null // "en_progreso" o "completado"

    private val galeriaFotos = mutableListOf<FotoGaleria>()
    private var fotoActual: Bitmap? = null
    private var contadorFotos = 0
    private var isUploading = false

    companion object {
        private const val TAG = "PhotoMissionActivity"
        private const val PREFS_NAME = "plaza_photo_mission"
        private const val KEY_ACTIVIDAD_PROGRESO_ID = "actividad_progreso_id"
    }

    // Moshi para parsear JSON de forma segura
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

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
                LogManager.write(this@PhotoMissionActivity, "Foto tomada correctamente")
                fotoActual = imageBitmap
                mostrarVistaPrevia(imageBitmap)
            }
        }
    }

    override fun getContentLayoutId() = R.layout.plaza_photo_mission

    override fun onContentInflated() {
        LogManager.write(this@PhotoMissionActivity, "PhotoMissionActivity iniciada")

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
            LogManager.write(this@PhotoMissionActivity, "Usuario salió de PhotoMissionActivity")
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
            Toast.makeText(this, getString(R.string.photo_mission_upload_in_progress), Toast.LENGTH_SHORT).show()
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

            LogManager.write(this@PhotoMissionActivity, "Iniciando subida a Cloudinary…")

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
                    LogManager.write(this@PhotoMissionActivity, "Error subiendo imagen: ${result.message}")

                    // Error en la subida
                    Log.e(TAG, "❌ Error subiendo imagen: ${result.message}")
                    Toast.makeText(
                        this@PhotoMissionActivity,
                        getString(R.string.photo_mission_error_subir, result.message ?: ""),
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
        // Liberar memoria del Bitmap antes de establecerlo a null
        fotoActual?.recycle()
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
            // Primero verificar si ya existe un progreso guardado
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val progresoIdGuardado = prefs.getString(KEY_ACTIVIDAD_PROGRESO_ID, null)

            if (progresoIdGuardado != null) {
                // Ya existe un progreso, intentar obtenerlo
                Log.d(TAG, "📂 Progreso existente encontrado: $progresoIdGuardado")
                when (val result = gameRepository.getActividadProgreso(progresoIdGuardado)) {
                    is Resource.Success -> {
                        actividadProgresoId = result.data.id
                        actividadEstado = result.data.estado // Guardar el estado
                        Log.d(TAG, "✅ Progreso cargado - ID: ${result.data.id}, Estado: ${result.data.estado}")
                        Log.d(TAG, "🔍 Tiene respuesta_contenido: ${!result.data.respuestaContenido.isNullOrEmpty()}")

                        // Si está completado, cargar la foto
                        if (result.data.estaCompletado()) {
                            Log.d(TAG, "✅ La actividad está completada, cargando foto...")
                            cargarFotoDesdeApi(result.data)
                        } else {
                            Log.d(TAG, "ℹ️ La actividad está en progreso, no hay foto que cargar")
                        }
                    }
                    is Resource.Error -> {
                        // El progreso guardado no existe o es inválido, crear uno nuevo
                        Log.w(TAG, "⚠️ Progreso guardado inválido, creando nuevo: ${result.message}")
                        crearNuevoProgreso(juegoId)
                    }
                    is Resource.Loading -> { }
                }
            } else {
                // No existe progreso guardado, crear uno nuevo
                Log.d(TAG, "🆕 No hay progreso guardado, creando nuevo")
                crearNuevoProgreso(juegoId)
            }
        }
    }

    /**
     * Crea un nuevo progreso de actividad y lo guarda.
     */
    private suspend fun crearNuevoProgreso(juegoId: String) {
        when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Plaza.ID, Puntos.Plaza.PHOTO_MISSION)) {
            is Resource.Success -> {
                actividadProgresoId = result.data.id
                actividadEstado = result.data.estado // Guardar el estado (debería ser "en_progreso")

                // Guardar el ID del progreso
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                prefs.edit { putString(KEY_ACTIVIDAD_PROGRESO_ID, result.data.id) }

                LogManager.write(this@PhotoMissionActivity, "Nuevo progreso creado: ${result.data.id}")
                Log.d(TAG, "✅ Nuevo progreso creado: ${result.data.id}, Estado: ${result.data.estado}")

                // Si la actividad ya está completada (no debería), cargar la foto
                if (result.data.estaCompletado()) {
                    cargarFotoDesdeApi(result.data)
                }
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Error al crear nuevo progreso: ${result.message}")
            }
            is Resource.Loading -> { }
        }
    }

    /**
     * Completa o actualiza la actividad con los datos de la foto en formato JSON.
     * Usa el endpoint correcto según el estado actual:
     * - "en_progreso": PUT /completar (marca como completada)
     * - "completado": PUT / (actualiza respuesta_contenido)
     *
     * @param respuestaJson JSON con URL de Cloudinary y etiqueta: {"url":"...", "etiqueta":"..."}
     */
    private fun completarActividad(respuestaJson: String) {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            // Usar el endpoint correcto según el estado actual
            val result = when (actividadEstado) {
                "completado" -> {
                    // Actividad ya completada: usar endpoint de actualización
                    Log.d(TAG, "🔄 Actividad ya completada, actualizando respuesta_contenido...")
                    gameRepository.actualizarActividad(
                        progresoId = estadoId,
                        respuestaContenido = respuestaJson
                    )
                }
                else -> {
                    // Actividad en progreso: usar endpoint de completar
                    LogManager.write(this@PhotoMissionActivity, "Completando actividad por primera vez…")
                    Log.d(TAG, "🏁 Completando actividad por primera vez...")
                    gameRepository.completarActividad(estadoId, 100.0, respuestaJson)
                }
            }

            when (result) {
                is Resource.Success -> {
                    // Actualizar el estado local
                    actividadEstado = result.data.estado
                    LogManager.write(this@PhotoMissionActivity, "Actividad guardada correctamente: $respuestaJson")
                    Log.d(TAG, "✅ Actividad guardada exitosamente: $respuestaJson")
                    Log.d(TAG, "✅ Nuevo estado: ${result.data.estado}")
                }
                is Resource.Error -> {
                    LogManager.write(this@PhotoMissionActivity, "Error guardando actividad: ${result.message}")
                    Log.e(TAG, "❌ Error guardando actividad: ${result.message}")
                    Toast.makeText(
                        this@PhotoMissionActivity,
                        getString(R.string.photo_mission_error_guardar, result.message ?: ""),
                        Toast.LENGTH_LONG
                    ).show()
                }
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
        Log.d(TAG, "🔍 cargarFotoDesdeApi() - Estado: ${progreso.estado}, Progreso ID: ${progreso.id}")

        val respuestaContenido = progreso.respuestaContenido
        Log.d(TAG, "🔍 respuesta_contenido: $respuestaContenido")

        if (respuestaContenido.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ No hay foto guardada en la API (respuesta_contenido es null o vacío)")
            return
        }

        try {
            // Parsear JSON usando Moshi (más seguro que regex)
            val jsonAdapter = moshi.adapter(FotoRespuestaContenido::class.java)
            val fotoRespuesta = jsonAdapter.fromJson(respuestaContenido)

            if (fotoRespuesta != null) {
                Log.d(TAG, "🔍 URL extraída: ${fotoRespuesta.url}")
                Log.d(TAG, "🔍 Etiqueta extraída: ${fotoRespuesta.etiqueta}")

                val etiqueta = EtiquetaFoto.valueOf(fotoRespuesta.etiqueta)

                Log.d(TAG, "📸 Cargando foto desde API: ${fotoRespuesta.url}")

                // Crear FotoGaleria con la imagen desde URL
                contadorFotos++
                val fotoGuardada = FotoGaleria(
                    id = contadorFotos,
                    bitmap = null, // Se cargará desde URL en el adapter
                    etiqueta = etiqueta,
                    url = fotoRespuesta.url
                )

                galeriaFotos.add(0, fotoGuardada)
                adapter.notifyItemInserted(0)
                rvGaleria.scrollToPosition(0)

                // Habilitar botón de retroceso si la actividad está completada
                btnBack.isEnabled = true

                Log.d(TAG, "✅ Foto cargada desde API correctamente (total fotos: ${galeriaFotos.size})")
            } else {
                Log.w(TAG, "⚠️ Error parseando JSON: respuesta null")
                Log.w(TAG, "⚠️ Contenido recibido: $respuestaContenido")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "❌ Error: Etiqueta inválida - ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parseando foto desde API: ${e.message}", e)
        }
    }
}
