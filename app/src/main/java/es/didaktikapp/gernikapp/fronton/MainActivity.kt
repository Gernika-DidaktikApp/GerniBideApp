package es.didaktikapp.gernikapp.fronton

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.MapaActivity
import es.didaktikapp.gernikapp.R

/**
 * Activity principal del módulo del Frontón.
 * Muestra un menú con 5 opciones.
 *
 * @author Erlantz
 * @version 1.0
 * @see AppCompatActivity
 * @see R.layout.fronton_main
 */
class MainActivity : AppCompatActivity() {

    /**
     * Metodo principal del ciclo de vida de la Activity.
     * Inicializa el layout del menú y configura la navegación
     * hacia el resto de pantallas del módulo de frontón.
     *
     * @param savedInstanceState Estado previo de la Activity, si existe
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_main)

        val btnFrontonInfo = findViewById<Button>(R.id.btnFrontonInfo)
        val btnPelota = findViewById<Button>(R.id.btnPelota)
        val btnVideoValores = findViewById<Button>(R.id.btnVideoValores)
        val btnBalioak = findViewById<Button>(R.id.btnBalioak)
        val btnVolverMapa = findViewById<Button>(R.id.btnVolverMapa)

        btnFrontonInfo.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
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