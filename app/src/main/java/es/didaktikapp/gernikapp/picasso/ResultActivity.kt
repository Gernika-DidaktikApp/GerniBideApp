package es.didaktikapp.gernikapp.picasso

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PicassoResultBinding
import es.didaktikapp.gernikapp.utils.BitmapUtils

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: PicassoResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PicassoResultBinding.inflate(layoutInflater)
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
            val combinedBitmap = BitmapUtils.combineBitmapsWithScaling(guernicaBitmap, savedBitmap)

            // Mostrar la imagen combinada
            binding.resultImage.setImageBitmap(combinedBitmap)
        } else {
            // Si no hay imagen guardada, mostrar solo el Guernica
            binding.resultImage.setImageResource(R.drawable.gernika_outlines)
        }
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