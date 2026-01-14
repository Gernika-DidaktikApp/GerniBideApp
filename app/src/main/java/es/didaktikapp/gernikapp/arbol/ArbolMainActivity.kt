package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.bunkers.SoundGameActivity

class ArbolMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arbol_main)

        // Clear tree and mural values when the application opens
        getSharedPreferences("CollectiveTreePrefs", android.content.Context.MODE_PRIVATE)
            .edit().clear().apply()
        getSharedPreferences("PeaceMuralPrefs", android.content.Context.MODE_PRIVATE)
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