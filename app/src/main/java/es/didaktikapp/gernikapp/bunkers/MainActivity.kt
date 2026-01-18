package es.didaktikapp.gernikapp.bunkers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.MapaActivity
import es.didaktikapp.gernikapp.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bunkers_main)

        val btnSoundGame = findViewById<Button>(R.id.btnSoundGame)
        btnSoundGame.setOnClickListener {
            startActivity(Intent(this, SoundGameActivity::class.java))
        }

        val btnPuzzle = findViewById<Button>(R.id.btnPuzzle)
        btnPuzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        val btnPeaceMural = findViewById<Button>(R.id.btnPeaceMural)
        btnPeaceMural.setOnClickListener {
            startActivity(Intent(this, PeaceMuralActivity::class.java))
        }

        val btnReflection = findViewById<Button>(R.id.btnReflection)
        btnReflection.setOnClickListener {
            startActivity(Intent(this, ReflectionActivity::class.java))
        }

        val btnVolverMapa = findViewById<Button>(R.id.btnVolverMapa)
        btnVolverMapa.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
