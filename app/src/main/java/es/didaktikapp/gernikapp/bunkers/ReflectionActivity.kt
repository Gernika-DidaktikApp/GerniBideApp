package com.example.didaktikarbol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReflectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reflection)

        val tvFeedback: TextView = findViewById(R.id.tvFeedback)
        val btnJarraitu: Button = findViewById(R.id.btnJarraitu)

        val emojiButtons = listOf(
            findViewById<View>(R.id.btnBeldurra),
            findViewById<View>(R.id.btnTristura),
            findViewById<View>(R.id.btnLasaitasuna),
            findViewById<View>(R.id.btnItxaropena)
        )

        emojiButtons.forEach { button ->
            button.setOnClickListener {
                tvFeedback.visibility = View.VISIBLE
                btnJarraitu.visibility = View.VISIBLE
                
                // Visual feedback for selection
                emojiButtons.forEach { 
                    it.alpha = 0.5f 
                    it.scaleX = 0.9f
                    it.scaleY = 0.9f
                }
                button.alpha = 1.0f
                button.scaleX = 1.1f
                button.scaleY = 1.1f
            }
        }

        btnJarraitu.setOnClickListener {
            startActivity(Intent(this, PeaceMuralActivity::class.java))
            finish()
        }
    }
}
