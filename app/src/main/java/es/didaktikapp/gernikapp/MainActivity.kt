package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.plazagernika.PlazaVideoActivity
import es.didaktikapp.gernikapp.plazagernika.ArrastrProductosActivity
import es.didaktikapp.gernikapp.plazagernika.VersoGameActivity
import es.didaktikapp.gernikapp.plazagernika.FotoMisionActivity

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
            val intent = Intent(this, ArrastrProductosActivity::class.java)
            startActivity(intent)
        }

        val btnVersos = findViewById<Button>(R.id.btnVersos)
        btnVersos.setOnClickListener {
            val intent = Intent(this, VersoGameActivity::class.java)
            startActivity(intent)
        }

        val btnFotos = findViewById<Button>(R.id.btnFotos)
        btnFotos.setOnClickListener {
            val intent = Intent(this, FotoMisionActivity::class.java)
            startActivity(intent)
        }
    }
}