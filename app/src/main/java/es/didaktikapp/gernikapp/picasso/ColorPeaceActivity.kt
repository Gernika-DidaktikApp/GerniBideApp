package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.databinding.PicassoColorPeaceBinding
import es.didaktikapp.gernikapp.utils.BitmapUtils
import es.didaktikapp.gernikapp.utils.Constants
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import java.io.File

/**
 * Activity para colorear el Guernica de Picasso.
 * Permite al usuario pintar sobre la imagen del Guernica con una paleta de colores.
 * Soporta zoom, pan y guarda el resultado en almacenamiento interno.
 *
 * Modos de funcionamiento:
 * - Modo pintar: Si no existe imagen guardada, muestra el canvas de pintura
 * - Modo vista previa: Si existe imagen guardada, muestra el resultado y permite repintar
 *
 * @property binding ViewBinding del layout picasso_color_peace.xml
 * @property gameRepository Repositorio para gestionar eventos del juego
 * @property tokenManager Gestor de tokens JWT y juegoId
 * @property actividadProgresoId ID del estado del evento actual (puede ser null)
 * @property colorBlue Color azul de la paleta (#4FC3F7)
 * @property colorGreen Color verde de la paleta (#66BB6A)
 * @property colorYellow Color amarillo de la paleta (#FFEB3B)
 * @property colorPink Color rosa de la paleta (#F48FB1)
 * @property colorWhite Color blanco de la paleta (#FFFFFF)
 *
 * Condiciones:
 * - Requiere imagen R.drawable.gernika_outlines en recursos
 * - Usa PaintCanvasView para el canvas de pintura con zoom y pan
 * - Guarda resultado en Constants.Files.GUERNICA_IMAGE_FILENAME
 * - Inicia evento automáticamente con Puntos.Picasso.COLOR_PEACE
 *
 * @see PaintCanvasView
 * @see ResultActivity
 * @author Wara Pacheco
 */
class ColorPeaceActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoColorPeaceBinding
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null

    // Colores disponibles
    private val colorBlue = Color.parseColor("#4FC3F7")
    private val colorGreen = Color.parseColor("#66BB6A")
    private val colorYellow = Color.parseColor("#FFEB3B")
    private val colorPink = Color.parseColor("#F48FB1")
    private val colorWhite = Color.parseColor("#FFFFFF")

    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)
        binding = PicassoColorPeaceBinding.inflate(layoutInflater, contentContainer, true)
        iniciarActividad()
        setupColorListeners()
        setupPaintableBounds()
        checkForSavedPainting()
    }

    /**
     * Verifica si existe una imagen guardada previamente y muestra el modo correspondiente.
     * - Si existe imagen: modo vista previa
     * - Si no existe: modo pintar
     *
     * @see PaintCanvasView.hasSavedImage
     */
    private fun checkForSavedPainting() {
        if (PaintCanvasView.hasSavedImage(this)) {
            // Mostrar modo vista previa
            showPreviewMode()
        } else {
            // Mostrar modo pintar
            showPaintMode()
        }
    }

    /**
     * Muestra el modo vista previa con la imagen guardada.
     * Configura la UI para:
     * - Mostrar la imagen combinada (Guernica + pintura del usuario)
     * - Ocultar controles de pintura
     * - Botón "Repintar" para volver a modo pintar
     * - Botón "Ver resultado" para ir a ResultActivity
     */
    private fun showPreviewMode() {
        // Cargar y mostrar la imagen combinada
        val savedBitmap = PaintCanvasView.loadFromInternalStorage(this)
        if (savedBitmap != null) {
            val guernicaBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.gernika_outlines
            )
            val combinedBitmap = BitmapUtils.combineBitmapsWithScaling(guernicaBitmap, savedBitmap)

            binding.savedImagePreview.setImageBitmap(combinedBitmap)
            binding.savedImagePreview.visibility = View.VISIBLE
        }

        // Ocultar controles de pintura
        binding.colorPalette.visibility = View.GONE
        binding.instructionsText.visibility = View.GONE
        binding.guernicaImage.visibility = View.GONE
        binding.paintCanvas.visibility = View.GONE

        // Configurar botones para modo preview
        binding.clearButton.text = getString(R.string.color_peace_repaint)
        binding.clearButton.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor("#FF9800")
        )
        binding.finishButton.text = getString(R.string.color_peace_view_result)

        binding.clearButton.setOnClickListener {
            // Borrar imagen guardada y volver a modo pintar
            deleteSavedPainting()
            showPaintMode()
        }

        binding.finishButton.setOnClickListener {
            // Ir a ver el resultado
            startActivity(Intent(this, ResultActivity::class.java))
        }
    }

    /**
     * Muestra el modo pintar con el canvas de pintura activo.
     * Configura la UI para:
     * - Mostrar paleta de colores
     * - Mostrar canvas de pintura sobre la imagen del Guernica
     * - Botón "Limpiar" para resetear el canvas
     * - Botón "Finalizar" para guardar y ver resultado
     */
    private fun showPaintMode() {
        // Mostrar controles de pintura
        binding.savedImagePreview.visibility = View.GONE
        binding.colorPalette.visibility = View.VISIBLE
        binding.instructionsText.visibility = View.VISIBLE
        binding.guernicaImage.visibility = View.VISIBLE
        binding.paintCanvas.visibility = View.VISIBLE

        // Restaurar textos y colores de botones
        binding.clearButton.text = getString(R.string.color_peace_clear)
        binding.clearButton.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor("#E57373")
        )
        binding.finishButton.text = getString(R.string.color_peace_finish)
        binding.finishButton.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor("#66BB6A")
        )

        // Configurar botones para modo pintar
        binding.clearButton.setOnClickListener {
            binding.paintCanvas.clearCanvas()
        }

        binding.finishButton.setOnClickListener {
            saveAndFinish()
        }
    }

    /**
     * Elimina la imagen guardada del almacenamiento interno.
     * Permite al usuario empezar de nuevo con una pintura en blanco.
     */
    private fun deleteSavedPainting() {
        val file = File(filesDir, Constants.Files.GUERNICA_IMAGE_FILENAME)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Configura los límites del área pintable basándose en las dimensiones reales de la imagen.
     * Calcula el escalado fitCenter y los márgenes de centrado para delimitar
     * el área donde el usuario puede pintar (solo sobre el Guernica).
     *
     * Proceso:
     * 1. Obtiene dimensiones del ImageView y de la imagen drawable
     * 2. Calcula el factor de escala manteniendo aspect ratio
     * 3. Calcula los límites (left, top, right, bottom) del área escalada
     * 4. Pasa los límites al PaintCanvasView
     */
    private fun setupPaintableBounds() {
        // Esperar a que la vista se dibuje para calcular los límites
        binding.guernicaImage.post {
            val imageView = binding.guernicaImage
            val drawable = imageView.drawable ?: return@post

            // Obtener las dimensiones del ImageView
            val viewWidth = imageView.width.toFloat()
            val viewHeight = imageView.height.toFloat()

            // Obtener las dimensiones reales de la imagen
            val imageWidth = drawable.intrinsicWidth.toFloat()
            val imageHeight = drawable.intrinsicHeight.toFloat()

            // Calcular el ratio de escala (fitCenter mantiene aspect ratio)
            val scaleX = viewWidth / imageWidth
            val scaleY = viewHeight / imageHeight
            val scale = minOf(scaleX, scaleY)

            // Calcular el tamaño escalado de la imagen
            val scaledWidth = imageWidth * scale
            val scaledHeight = imageHeight * scale

            // Calcular los márgenes (centrado)
            val left = (viewWidth - scaledWidth) / 2f
            val top = (viewHeight - scaledHeight) / 2f
            val right = left + scaledWidth
            val bottom = top + scaledHeight

            // Pasar los límites al canvas de pintura
            binding.paintCanvas.setPaintableBounds(left, top, right, bottom)
        }
    }

    /**
     * Configura los listeners de la paleta de colores.
     * Cada botón de color cambia el color actual del canvas y resalta visualmente.
     *
     * Colores disponibles:
     * - Azul (#4FC3F7)
     * - Verde (#66BB6A)
     * - Amarillo (#FFEB3B)
     * - Rosa (#F48FB1)
     * - Blanco (#FFFFFF)
     */
    private fun setupColorListeners() {
        binding.colorBlue.setOnClickListener {
            binding.paintCanvas.currentColor = colorBlue
            highlightSelectedColor(it)
        }

        binding.colorGreen.setOnClickListener {
            binding.paintCanvas.currentColor = colorGreen
            highlightSelectedColor(it)
        }

        binding.colorYellow.setOnClickListener {
            binding.paintCanvas.currentColor = colorYellow
            highlightSelectedColor(it)
        }

        binding.colorPink.setOnClickListener {
            binding.paintCanvas.currentColor = colorPink
            highlightSelectedColor(it)
        }

        binding.colorWhite.setOnClickListener {
            binding.paintCanvas.currentColor = colorWhite
            highlightSelectedColor(it)
        }
    }

    /**
     * Resalta visualmente el color seleccionado aplicando una animación de escala.
     * Resetea la escala de todos los colores a 1.0 y amplía el seleccionado a 1.2.
     *
     * @param selectedView Vista del botón de color seleccionado
     */
    private fun highlightSelectedColor(selectedView: View) {
        // Resetear la escala de todos los colores
        binding.colorBlue.scaleX = 1f
        binding.colorBlue.scaleY = 1f
        binding.colorGreen.scaleX = 1f
        binding.colorGreen.scaleY = 1f
        binding.colorYellow.scaleX = 1f
        binding.colorYellow.scaleY = 1f
        binding.colorPink.scaleX = 1f
        binding.colorPink.scaleY = 1f
        binding.colorWhite.scaleX = 1f
        binding.colorWhite.scaleY = 1f

        // Resaltar el color seleccionado
        selectedView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .start()
    }

    /**
     * Guarda la pintura actual en almacenamiento interno y finaliza la actividad.
     * Si el guardado es exitoso, completa el evento en la API y muestra diálogo de confirmación.
     * Si falla, muestra un mensaje de error.
     *
     * @see PaintCanvasView.saveToInternalStorage
     * @see completarActividad
     */
    private fun saveAndFinish() {
        val saved = binding.paintCanvas.saveToInternalStorage(this)

        if (saved) {
            completarActividad()
            showCompletionDialog()
        } else {
            Toast.makeText(
                this,
                getString(R.string.color_peace_save_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Muestra un diálogo de confirmación al completar la actividad.
     * Al confirmar, navega a ResultActivity para ver el resultado final.
     */
    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.color_peace_dialog_title))
            .setMessage(getString(R.string.color_peace_dialog_message))
            .setPositiveButton(getString(R.string.color_peace_dialog_button)) { dialog, _ ->
                dialog.dismiss()
                // Navegar a ResultActivity para mostrar el resultado
                startActivity(Intent(this, ResultActivity::class.java))
                finish()
            }
            .show()
    }

    /**
     * Inicia el evento en la API del juego.
     * Requiere un juegoId válido del TokenManager.
     * Guarda el actividadProgresoId devuelto por la API para completar el evento después.
     *
     * IDs utilizados:
     * - idActividad: Puntos.Picasso.ID
     * - idEvento: Puntos.Picasso.COLOR_PEACE
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Picasso.ID, Puntos.Picasso.COLOR_PEACE)) {
                is Resource.Success -> actividadProgresoId = result.data.id
                is Resource.Error -> Log.e("ColorPeace", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento en la API del juego.
     * Requiere un actividadProgresoId válido obtenido de iniciarActividad().
     *
     * @see iniciarActividad
     * Puntuación enviada: 100.0 (actividad completada)
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> Log.d("ColorPeace", "Completado")
                is Resource.Error -> Log.e("ColorPeace", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}