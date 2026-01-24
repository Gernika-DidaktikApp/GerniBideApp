package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

/**
 * Activity del quiz de valores de Cesta Punta.
 */
class CestaTipActivity : BaseMenuActivity() {

    override fun getContentLayoutId() = R.layout.fronton_cesta_tip

    override fun onContentInflated() {
        val videoView = findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayVideo)

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

        val radioGroup = findViewById<RadioGroup>(R.id.opcionesGroup)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val prefs = getSharedPreferences("fronton_progress", Context.MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("cesta_tip_completed", false)) {
            btnBack.isEnabled = true
        }

        btnConfirmar.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                // Comparar por ID en vez de texto para que funcione en cualquier idioma
                val correctIds = listOf(R.id.op1, R.id.op2) // Lankidetza y Errespetua
                if (selectedId in correctIds) {
                    Toast.makeText(this, getString(R.string.erantzun_zuzena), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.erantzun_okerra), Toast.LENGTH_SHORT).show()
                }

                // Deshabilitar para que solo se pueda contestar una vez
                btnConfirmar.isEnabled = false
                for (i in 0 until radioGroup.childCount) {
                    radioGroup.getChildAt(i).isEnabled = false
                }

                btnBack.isEnabled = true
                prefs.edit().putBoolean("cesta_tip_completed", true).apply()

            } else {
                Toast.makeText(this, getString(R.string.hautatu_aukera_bat), Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}