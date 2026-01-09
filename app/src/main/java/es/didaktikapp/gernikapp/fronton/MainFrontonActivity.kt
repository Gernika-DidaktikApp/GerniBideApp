package es.didaktikapp.gernikapp.fronton

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.MapaActivity
import es.didaktikapp.gernikapp.R

class MainFrontonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_main_fronton)

        val btnFrontonInfo = findViewById<Button>(R.id.btnFrontonInfo)
        val btnPelota = findViewById<Button>(R.id.btnPelota)
        val btnVideoValores = findViewById<Button>(R.id.btnVideoValores)
        val btnBalioak = findViewById<Button>(R.id.btnBalioak)
        val btnVolverMapa = findViewById<Button>(R.id.btnVolverMapa)

        btnFrontonInfo.setOnClickListener {
            startActivity(Intent(this, FrontonInfoActivity::class.java))
        }

        btnPelota.setOnClickListener {
            startActivity(Intent(this, DancingBallActivity::class.java))
        }

        btnVideoValores.setOnClickListener {
            startActivity(Intent(this, CestaTipActivity::class.java))
        }

        btnBalioak.setOnClickListener {
            startActivity(Intent(this, ValuesGroupActivity::class.java))
        }

        btnVolverMapa.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}