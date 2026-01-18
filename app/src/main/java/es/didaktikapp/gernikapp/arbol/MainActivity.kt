package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.MapaActivity
import es.didaktikapp.gernikapp.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arbol_main)

        val btnInteractive = findViewById<Button>(R.id.btnInteractive)
        btnInteractive.setOnClickListener {
            startActivity(Intent(this, InteractiveActivity::class.java))
        }

        val btnAudioQuiz = findViewById<Button>(R.id.btnAudioQuiz)
        btnAudioQuiz.setOnClickListener {
            startActivity(Intent(this, AudioQuizActivity::class.java))
        }

        val btnPuzzle = findViewById<Button>(R.id.btnPuzzle)
        btnPuzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleActivity::class.java))
        }

        val btnMyTree = findViewById<Button>(R.id.btnMyTree)
        btnMyTree.setOnClickListener {
            startActivity(Intent(this, MyTreeActivity::class.java))
        }

        val btnVolverMapa = findViewById<Button>(R.id.btnVolverMapa)
        btnVolverMapa.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
