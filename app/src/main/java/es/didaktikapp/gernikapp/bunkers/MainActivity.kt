package es.didaktikapp.gernikapp.bunkers

import android.content.Intent
import android.widget.Button
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class MainActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.bunkers_main

    override fun onContentInflated() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        contentContainer.findViewById<Button>(R.id.btnSoundGame).setOnClickListener {
            startActivity(Intent(this, SoundGameActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnPeaceMural).setOnClickListener {
            startActivity(Intent(this, PeaceMuralActivity::class.java))
        }

        contentContainer.findViewById<Button>(R.id.btnReflection).setOnClickListener {
            startActivity(Intent(this, ReflectionActivity::class.java))
        }
    }
}
