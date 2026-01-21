package es.didaktikapp.gernikapp.picasso

import android.content.Intent
import android.widget.Button
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class MainActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.picasso_main

    override fun onContentInflated() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        contentContainer.findViewById<Button>(R.id.btnKolorezBakea).setOnClickListener {
            startActivity(Intent(this, ColorPeaceActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnIkusiEtaAsmatu).setOnClickListener {
            startActivity(Intent(this, ViewInterpretActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnNireMezua).setOnClickListener {
            startActivity(Intent(this, MyMessageActivity::class.java))
        }
    }
}