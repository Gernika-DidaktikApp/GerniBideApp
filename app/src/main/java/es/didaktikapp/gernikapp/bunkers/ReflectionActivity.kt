package es.didaktikapp.gernikapp.bunkers

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class ReflectionActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.bunkers_reflection

    override fun onContentInflated() {
        val tvFeedback: TextView = contentContainer.findViewById(R.id.tvFeedback)
        val btnJarraitu: Button = contentContainer.findViewById(R.id.btnJarraitu)

        val emojiButtons = listOf(
            contentContainer.findViewById<View>(R.id.btnBeldurra),
            contentContainer.findViewById<View>(R.id.btnTristura),
            contentContainer.findViewById<View>(R.id.btnLasaitasuna),
            contentContainer.findViewById<View>(R.id.btnItxaropena)
        )

        emojiButtons.forEach { button ->
            button.setOnClickListener {
                tvFeedback.visibility = View.VISIBLE
                btnJarraitu.visibility = View.VISIBLE

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