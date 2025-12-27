package es.didaktikapp.gernikapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.databinding.ActivityColorPeaceBinding

class ColorPeaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityColorPeaceBinding

    // Colores disponibles
    private val colorBlue = Color.parseColor("#4FC3F7")
    private val colorGreen = Color.parseColor("#66BB6A")
    private val colorYellow = Color.parseColor("#FFEB3B")
    private val colorPink = Color.parseColor("#F48FB1")
    private val colorWhite = Color.parseColor("#FFFFFF")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityColorPeaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupColorListeners()
        setupButtonListeners()
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

    private fun setupButtonListeners() {
        binding.clearButton.setOnClickListener {
            binding.paintCanvas.clearCanvas()
        }

        binding.finishButton.setOnClickListener {
            saveAndFinish()
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
                finish()
            }
            .show()
    }
}