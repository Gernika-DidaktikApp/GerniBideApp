package es.didaktikapp.gernikapp.fronton

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.R

class CestaTipActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_cesta_tip)

        val videoView = findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayVideo)

        val uri = "android.resource://${packageName}/${R.raw.fronton_jarduerarako_bideoa}".toUri()
        videoView.setVideoURI(uri)

        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = "Videoa erreproduzitu"
            } else {
                videoView.start()
                btnPlayPause.text = "Videoa gelditu"
            }
        }

        val radioGroup = findViewById<RadioGroup>(R.id.opcionesGroup)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val btnAtzera = findViewById<Button>(R.id.btnAtzera)

        btnConfirmar.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadio = findViewById<RadioButton>(selectedId)
                val seleccion = selectedRadio.text.toString()

                val correcta = listOf("Lankidetza", "Errespetua")
                if (seleccion in correcta) {
                    Toast.makeText(this, "Erantzun Zuzena ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erantzun okerra ❌", Toast.LENGTH_SHORT).show()
                }

                btnAtzera.visibility = View.VISIBLE

            } else {
                Toast.makeText(this, "Hautatu aukera bat", Toast.LENGTH_SHORT).show()
            }
        }

        btnAtzera.setOnClickListener {
            finish()
        }
    }
}