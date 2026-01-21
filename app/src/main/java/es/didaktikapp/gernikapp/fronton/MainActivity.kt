package es.didaktikapp.gernikapp.fronton

import android.content.Intent
import android.widget.Button
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class MainActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.fronton_main

    override fun onContentInflated() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        contentContainer.findViewById<Button>(R.id.btnFrontonInfo).setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnPelota).setOnClickListener {
            startActivity(Intent(this, DancingBallActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnVideoValores).setOnClickListener {
            startActivity(Intent(this, CestaTipActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnBalioak).setOnClickListener {
            startActivity(Intent(this, ValuesGroupActivity::class.java))
        }
    }
}