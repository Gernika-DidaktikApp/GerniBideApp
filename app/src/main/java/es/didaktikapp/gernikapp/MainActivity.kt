package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.plazagernika.PlazaVideoActivity
import es.didaktikapp.gernikapp.plazagernika.DragProductsActivity
import es.didaktikapp.gernikapp.plazagernika.VerseGameActivity
import es.didaktikapp.gernikapp.plazagernika.PhotoMissionActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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