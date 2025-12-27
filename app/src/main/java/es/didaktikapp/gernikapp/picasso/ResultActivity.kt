package es.didaktikapp.gernikapp.picasso

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAndDisplayResult()
        setupClickListeners()
    }

    private fun loadAndDisplayResult() {
        // Cargar la imagen guardada
        val savedBitmap = PaintCanvasView.loadFromInternalStorage(this)

        if (savedBitmap != null) {
            // Cargar la imagen del Guernica original
            val guernicaBitmap = BitmapFactory.decodeResource(resources, R.drawable.gernika_outlines)

            // Combinar las dos imágenes
            val combinedBitmap = combineBitmaps(guernicaBitmap, savedBitmap)

            // Mostrar la imagen combinada
            binding.resultImage.setImageBitmap(combinedBitmap)
        } else {
            // Si no hay imagen guardada, mostrar solo el Guernica
            binding.resultImage.setImageResource(R.drawable.gernika_outlines)
        }
    }

    private fun combineBitmaps(background: Bitmap, foreground: Bitmap): Bitmap {
        // Ambas imágenes ahora deberían tener el mismo aspect ratio del Guernica
        // Usar las dimensiones del foreground como base
        val width = foreground.width
        val height = foreground.height

        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)

        // Escalar el background manteniendo el aspect ratio
        val bgWidth = background.width.toFloat()
        val bgHeight = background.height.toFloat()
        val fgWidth = width.toFloat()
        val fgHeight = height.toFloat()

        // Calcular escala manteniendo aspect ratio
        val scaleX = fgWidth / bgWidth
        val scaleY = fgHeight / bgHeight
        val scale = minOf(scaleX, scaleY)

        val scaledWidth = (bgWidth * scale).toInt()
        val scaledHeight = (bgHeight * scale).toInt()

        // Centrar la imagen escalada
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f

        val scaledBackground = Bitmap.createScaledBitmap(background, scaledWidth, scaledHeight, true)

        // Dibujar el background primero
        canvas.drawBitmap(scaledBackground, left, top, null)

        // Dibujar el foreground (painted content) encima
        canvas.drawBitmap(foreground, 0f, 0f, null)

        return combined
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnShare.setOnClickListener {
            // TODO: Implementar compartir imagen
        }
    }
}