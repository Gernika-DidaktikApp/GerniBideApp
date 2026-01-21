package es.didaktikapp.gernikapp.fronton

import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class CestaTipActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.fronton_cesta_tip

    override fun onContentInflated() {
        val videoView = contentContainer.findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = contentContainer.findViewById<Button>(R.id.btnPlayVideo)

        val uri = "android.resource://${packageName}/${R.raw.fronton_jarduerarako_bideoa}".toUri()
        videoView.setVideoURI(uri)

        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = getString(R.string.videoa_erreproduzitu)
            } else {
                videoView.start()
                btnPlayPause.text = getString(R.string.videoa_gelditu)
            }
        }

        val radioGroup = contentContainer.findViewById<RadioGroup>(R.id.opcionesGroup)
        val btnConfirmar = contentContainer.findViewById<Button>(R.id.btnConfirmar)
        val btnAtzera = contentContainer.findViewById<Button>(R.id.btnAtzera)

        btnConfirmar.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadio = contentContainer.findViewById<RadioButton>(selectedId)
                val seleccion = selectedRadio.text.toString()

                val correcta = listOf("Lankidetza", "Errespetua")
                if (seleccion in correcta) {
                    Toast.makeText(this, getString(R.string.erantzun_zuzena), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.erantzun_okerra), Toast.LENGTH_SHORT).show()
                }

                btnAtzera.visibility = View.VISIBLE

            } else {
                Toast.makeText(this, getString(R.string.hautatu_aukera_bat), Toast.LENGTH_SHORT).show()
            }
        }

        btnAtzera.setOnClickListener {
            finish()
        }
    }
}