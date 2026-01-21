package es.didaktikapp.gernikapp.plaza

import android.content.Intent
import android.widget.Button
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class MainActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.plaza_main

    override fun onContentInflated() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        contentContainer.findViewById<Button>(R.id.btnVideo).setOnClickListener {
            startActivity(Intent(this, VideoActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnMercado).setOnClickListener {
            startActivity(Intent(this, DragProductsActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnVersos).setOnClickListener {
            startActivity(Intent(this, VerseGameActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnFotos).setOnClickListener {
            startActivity(Intent(this, PhotoMissionActivity::class.java))
        }
    }
}