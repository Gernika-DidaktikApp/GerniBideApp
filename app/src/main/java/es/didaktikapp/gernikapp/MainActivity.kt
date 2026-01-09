package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.arbol.ArbolActivity
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnMap = findViewById<Button>(R.id.btnMap)
        val btnArbol = findViewById<Button>(R.id.btnArbol)

        btnMap.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
        }

        btnArbol.setOnClickListener {
            val intent = Intent(this, ArbolActivity::class.java)
            startActivity(intent)
        }
    }
}
