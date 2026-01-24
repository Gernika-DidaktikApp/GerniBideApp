package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PicassoColorPeaceBinding
import es.didaktikapp.gernikapp.utils.BitmapUtils
import es.didaktikapp.gernikapp.utils.Constants
import java.io.File

class ColorPeaceActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoColorPeaceBinding

    // Colores disponibles
    private val colorBlue = Color.parseColor("#4FC3F7")
    private val colorGreen = Color.parseColor("#66BB6A")
    private val colorYellow = Color.parseColor("#FFEB3B")
    private val colorPink = Color.parseColor("#F48FB1")
    private val colorWhite = Color.parseColor("#FFFFFF")

    override fun onContentInflated() {
        binding = PicassoColorPeaceBinding.inflate(layoutInflater, contentContainer, true)
        setupColorListeners()
        setupPaintableBounds()
        checkForSavedPainting()
    }

    private fun checkForSavedPainting() {
        if (PaintCanvasView.hasSavedImage(this)) {
            // Mostrar modo vista previa
            showPreviewMode()
        } else {
            // Mostrar modo pintar
            showPaintMode()
        }
    }

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

    private fun deleteSavedPainting() {
        val file = File(filesDir, Constants.Files.GUERNICA_IMAGE_FILENAME)
        if (file.exists()) {
            file.delete()
        }
    }


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

    private fun saveAndFinish() {
        val saved = binding.paintCanvas.saveToInternalStorage(this)

        if (saved) {
            showCompletionDialog()
        } else {
            Toast.makeText(
                this,
                getString(R.string.color_peace_save_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
}