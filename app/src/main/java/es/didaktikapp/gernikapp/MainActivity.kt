package es.didaktikapp.gernikapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.bunkers.ArbolActivity
import es.didaktikapp.gernikapp.bunkers.SoundGameActivity
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Clear tree and mural values when the application opens
        getSharedPreferences("CollectiveTreePrefs", MODE_PRIVATE)
            .edit().clear().apply()
        getSharedPreferences("PeaceMuralPrefs", MODE_PRIVATE)
            .edit().clear().apply()

        val btnArbol = findViewById<Button>(R.id.btnArbol)
        val btnBunkers = findViewById<Button>(R.id.btnBunkers)

        btnArbol.setOnClickListener {
            val intent = Intent(this, ArbolActivity::class.java)
            startActivity(intent)
        }

        btnBunkers.setOnClickListener {
            val intent = Intent(this, SoundGameActivity::class.java)
            startActivity(intent)
        }
    }
}
