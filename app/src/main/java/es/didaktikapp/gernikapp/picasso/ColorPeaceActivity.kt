package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.utils.BitmapUtils
import es.didaktikapp.gernikapp.utils.Constants
import java.io.File

class ColorPeaceActivity : BaseMenuActivity() {

    private lateinit var savedImagePreview: ImageView
    private lateinit var colorPalette: LinearLayout
    private lateinit var instructionsText: TextView
    private lateinit var guernicaImage: ImageView
    private lateinit var paintCanvas: PaintCanvasView
    private lateinit var clearButton: Button
    private lateinit var finishButton: Button
    private lateinit var colorBlueView: View
    private lateinit var colorGreenView: View
    private lateinit var colorYellowView: View
    private lateinit var colorPinkView: View
    private lateinit var colorWhiteView: View

    private val colorBlue = Color.parseColor("#4FC3F7")
    private val colorGreen = Color.parseColor("#66BB6A")
    private val colorYellow = Color.parseColor("#FFEB3B")
    private val colorPink = Color.parseColor("#F48FB1")
    private val colorWhite = Color.parseColor("#FFFFFF")

    override fun getContentLayoutId(): Int = R.layout.picasso_color_peace

    override fun onContentInflated() {
        initViews()
        setupColorListeners()
        setupPaintableBounds()
        checkForSavedPainting()
    }

    private fun initViews() {
        savedImagePreview = contentContainer.findViewById(R.id.savedImagePreview)
        colorPalette = contentContainer.findViewById(R.id.colorPalette)
        instructionsText = contentContainer.findViewById(R.id.instructionsText)
        guernicaImage = contentContainer.findViewById(R.id.guernicaImage)
        paintCanvas = contentContainer.findViewById(R.id.paintCanvas)
        clearButton = contentContainer.findViewById(R.id.clearButton)
        finishButton = contentContainer.findViewById(R.id.finishButton)
        colorBlueView = contentContainer.findViewById(R.id.colorBlue)
        colorGreenView = contentContainer.findViewById(R.id.colorGreen)
        colorYellowView = contentContainer.findViewById(R.id.colorYellow)
        colorPinkView = contentContainer.findViewById(R.id.colorPink)
        colorWhiteView = contentContainer.findViewById(R.id.colorWhite)
    }

    private fun checkForSavedPainting() {
        if (PaintCanvasView.hasSavedImage(this)) {
            showPreviewMode()
        } else {
            showPaintMode()
        }
    }

    private fun showPreviewMode() {
        val savedBitmap = PaintCanvasView.loadFromInternalStorage(this)
        if (savedBitmap != null) {
            val guernicaBitmap = BitmapFactory.decodeResource(resources, R.drawable.gernika_outlines)
            val combinedBitmap = BitmapUtils.combineBitmapsWithScaling(guernicaBitmap, savedBitmap)
            savedImagePreview.setImageBitmap(combinedBitmap)
            savedImagePreview.visibility = View.VISIBLE
        }

        colorPalette.visibility = View.GONE
        instructionsText.visibility = View.GONE
        guernicaImage.visibility = View.GONE
        paintCanvas.visibility = View.GONE

        clearButton.text = getString(R.string.color_peace_repaint)
        clearButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF9800"))
        finishButton.text = getString(R.string.color_peace_view_result)

        clearButton.setOnClickListener {
            deleteSavedPainting()
            showPaintMode()
        }

        finishButton.setOnClickListener {
            startActivity(Intent(this, ResultActivity::class.java))
        }
    }

    private fun showPaintMode() {
        savedImagePreview.visibility = View.GONE
        colorPalette.visibility = View.VISIBLE
        instructionsText.visibility = View.VISIBLE
        guernicaImage.visibility = View.VISIBLE
        paintCanvas.visibility = View.VISIBLE

        clearButton.text = getString(R.string.color_peace_clear)
        clearButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E57373"))
        finishButton.text = getString(R.string.color_peace_finish)
        finishButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#66BB6A"))

        clearButton.setOnClickListener {
            paintCanvas.clearCanvas()
        }

        finishButton.setOnClickListener {
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
        guernicaImage.post {
            val drawable = guernicaImage.drawable ?: return@post

            val viewWidth = guernicaImage.width.toFloat()
            val viewHeight = guernicaImage.height.toFloat()
            val imageWidth = drawable.intrinsicWidth.toFloat()
            val imageHeight = drawable.intrinsicHeight.toFloat()

            val scaleX = viewWidth / imageWidth
            val scaleY = viewHeight / imageHeight
            val scale = minOf(scaleX, scaleY)

            val scaledWidth = imageWidth * scale
            val scaledHeight = imageHeight * scale

            val left = (viewWidth - scaledWidth) / 2f
            val top = (viewHeight - scaledHeight) / 2f
            val right = left + scaledWidth
            val bottom = top + scaledHeight

            paintCanvas.setPaintableBounds(left, top, right, bottom)
        }
    }

    private fun setupColorListeners() {
        colorBlueView.setOnClickListener {
            paintCanvas.currentColor = colorBlue
            highlightSelectedColor(it)
        }

        colorGreenView.setOnClickListener {
            paintCanvas.currentColor = colorGreen
            highlightSelectedColor(it)
        }

        colorYellowView.setOnClickListener {
            paintCanvas.currentColor = colorYellow
            highlightSelectedColor(it)
        }

        colorPinkView.setOnClickListener {
            paintCanvas.currentColor = colorPink
            highlightSelectedColor(it)
        }

        colorWhiteView.setOnClickListener {
            paintCanvas.currentColor = colorWhite
            highlightSelectedColor(it)
        }
    }

    private fun highlightSelectedColor(selectedView: View) {
        listOf(colorBlueView, colorGreenView, colorYellowView, colorPinkView, colorWhiteView).forEach {
            it.scaleX = 1f
            it.scaleY = 1f
        }

        selectedView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .start()
    }

    private fun saveAndFinish() {
        val saved = paintCanvas.saveToInternalStorage(this)

        if (saved) {
            showCompletionDialog()
        } else {
            Toast.makeText(this, getString(R.string.color_peace_save_error), Toast.LENGTH_LONG).show()
        }
    }

    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.color_peace_dialog_title))
            .setMessage(getString(R.string.color_peace_dialog_message))
            .setPositiveButton(getString(R.string.color_peace_dialog_button)) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, ResultActivity::class.java))
                finish()
            }
            .show()
    }
}