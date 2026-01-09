package es.didaktikapp.gernikapp.plazagernika

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.R

class PlazaIndexActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaza_index)

        val btnVideo = findViewById<Button>(R.id.btnVideo)
        btnVideo.setOnClickListener {
            val intent = Intent(this, PlazaVideoActivity::class.java)
            startActivity(intent)
        }

        val btnMercado = findViewById<Button>(R.id.btnMercado)
        btnMercado.setOnClickListener {
            val intent = Intent(this, DragProductsActivity::class.java)
            startActivity(intent)
        }

        val btnVersos = findViewById<Button>(R.id.btnVersos)
        btnVersos.setOnClickListener {
            val intent = Intent(this, VerseGameActivity::class.java)
            startActivity(intent)
        }

        val btnFotos = findViewById<Button>(R.id.btnFotos)
        btnFotos.setOnClickListener {
            val intent = Intent(this, PhotoMissionActivity::class.java)
            startActivity(intent)
        }
    }
}