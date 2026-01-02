package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.plazagernika.PlazaVideoActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPlazaGernika = findViewById<Button>(R.id.btnPlazaGernika)
        btnPlazaGernika.setOnClickListener {
            val intent = Intent(this, PlazaVideoActivity::class.java)
            startActivity(intent)
        }
    }
}