package es.didaktikapp.gernikapp.picasso

import android.graphics.BitmapFactory
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.databinding.PicassoResultBinding
import es.didaktikapp.gernikapp.utils.BitmapUtils

class ResultActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoResultBinding

    override fun onContentInflated() {
        binding = PicassoResultBinding.inflate(layoutInflater, contentContainer, true)
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
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnShare.setOnClickListener {
            // TODO: Implementar compartir imagen
        }
    }
}