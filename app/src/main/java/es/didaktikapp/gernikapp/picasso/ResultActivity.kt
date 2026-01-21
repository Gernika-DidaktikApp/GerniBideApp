package es.didaktikapp.gernikapp.picasso

import android.graphics.BitmapFactory
import android.widget.Button
import android.widget.ImageView
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.utils.BitmapUtils

class ResultActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.picasso_result

    override fun onContentInflated() {
        loadAndDisplayResult()
        setupClickListeners()
    }

    private fun loadAndDisplayResult() {
        val resultImage = contentContainer.findViewById<ImageView>(R.id.resultImage)
        val savedBitmap = PaintCanvasView.loadFromInternalStorage(this)

        if (savedBitmap != null) {
            val guernicaBitmap = BitmapFactory.decodeResource(resources, R.drawable.gernika_outlines)
            val combinedBitmap = BitmapUtils.combineBitmapsWithScaling(guernicaBitmap, savedBitmap)
            resultImage.setImageBitmap(combinedBitmap)
        } else {
            resultImage.setImageResource(R.drawable.gernika_outlines)
        }
    }

    private fun setupClickListeners() {
        contentContainer.findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }

        contentContainer.findViewById<Button>(R.id.btnShare).setOnClickListener {
            // TODO: Implementar compartir imagen
        }
    }
}