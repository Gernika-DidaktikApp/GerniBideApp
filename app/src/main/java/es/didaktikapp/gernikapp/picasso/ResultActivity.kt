package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.graphics.BitmapFactory
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PicassoResultBinding
import es.didaktikapp.gernikapp.utils.BitmapUtils
import es.didaktikapp.gernikapp.utils.Constants
import java.io.File

/**
 * Activity que muestra el resultado final del Guernica coloreado por el usuario.
 * Combina la imagen base (outlines del Guernica) con la pintura del usuario.
 *
 * Características:
 * - Carga la imagen guardada por el usuario
 * - Combina con la imagen original del Guernica
 * - Muestra el resultado final
 * - Opción de compartir (pendiente de implementar)
 *
 * @property binding ViewBinding del layout picasso_result.xml
 *
 * Condiciones:
 * - Requiere que exista imagen guardada por PaintCanvasView
 * - Usa BitmapUtils.combineBitmapsWithScaling para combinar imágenes
 * - Si no hay imagen guardada, muestra solo el Guernica original
 *
 * @see PaintCanvasView
 * @see ColorPeaceActivity
 * @author Wara Pacheco
 */
class ResultActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoResultBinding

    override fun onContentInflated() {
        LogManager.write(this@ResultActivity, "ResultActivity iniciada")

        binding = PicassoResultBinding.inflate(layoutInflater, contentContainer, true)
        loadAndDisplayResult()
        setupClickListeners()
    }

    /**
     * Carga y muestra el resultado final combinando la imagen del Guernica
     * con la pintura del usuario.
     *
     * Proceso:
     * 1. Carga la imagen guardada del usuario
     * 2. Carga la imagen base del Guernica (outlines)
     * 3. Combina ambas imágenes con escalado apropiado
     * 4. Muestra el resultado en ImageView
     * 5. Si no hay imagen guardada, muestra solo el Guernica original
     */
    private fun loadAndDisplayResult() {
        // Cargar la imagen guardada
        val savedBitmap = PaintCanvasView.loadFromInternalStorage(this)

        if (savedBitmap != null) {
            LogManager.write(this@ResultActivity, "Imagen guardada encontrada, combinando con Guernica")

            // Cargar la imagen del Guernica original
            val guernicaBitmap = BitmapFactory.decodeResource(resources, R.drawable.gernika_outlines)
            // Combinar las dos imágenes
            val combinedBitmap = BitmapUtils.combineBitmapsWithScaling(guernicaBitmap, savedBitmap)

            // Mostrar la imagen combinada
            binding.resultImage.setImageBitmap(combinedBitmap)
        } else {
            // Si no hay imagen guardada, mostrar solo el Guernica
            LogManager.write(this@ResultActivity, "No hay imagen guardada, mostrando Guernica original")
            binding.resultImage.setImageResource(R.drawable.gernika_outlines)
        }
    }


    /**
     * Configura los listeners de los botones de la interfaz.
     * - btnBack: Vuelve a MainActivity de Picasso limpiando el stack
     * - btnShare: Funcionalidad pendiente de implementar
     */
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            LogManager.write(this@ResultActivity, "Usuario volvió desde ResultActivity")
            // Navegar a MainActivity de Picasso y limpiar el stack
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        binding.btnShare.setOnClickListener {
            LogManager.write(this@ResultActivity, "Usuario solicitó volver a empezar")
            // Borrar la imagen guardada para permitir volver a pintar
            deleteSavedPainting()
            // Navegar de vuelta a ColorPeaceActivity
            startActivity(Intent(this, ColorPeaceActivity::class.java))
            finish()
        }
    }

    /**
     * Elimina la imagen guardada del almacenamiento interno.
     * Permite al usuario volver a pintar desde cero.
     */
    private fun deleteSavedPainting() {
        val file = File(filesDir, Constants.Files.GUERNICA_IMAGE_FILENAME)
        if (file.exists()) {
            val deleted = file.delete()
            LogManager.write(this@ResultActivity, "Imagen guardada eliminada: $deleted")
        }
    }
}