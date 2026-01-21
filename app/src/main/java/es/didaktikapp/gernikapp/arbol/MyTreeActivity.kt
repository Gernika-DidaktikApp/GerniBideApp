package es.didaktikapp.gernikapp.arbol

import android.content.Intent
import android.widget.Button
import androidx.core.content.ContextCompat
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class MyTreeActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.arbol_my_tree

    override fun onContentInflated() {
        setupButton(R.id.btnFriendship, R.color.valueFriendship)
        setupButton(R.id.btnFreedom, R.color.valueFreedom)
        setupButton(R.id.btnSolidarity, R.color.valueSolidarity)
        setupButton(R.id.btnRespect, R.color.valueRespect)
        setupButton(R.id.btnPeace, R.color.valuePeace)
    }

    private fun setupButton(buttonId: Int, colorId: Int) {
        contentContainer.findViewById<Button>(buttonId).setOnClickListener { button ->
            val intent = Intent(this, InteractiveActivity::class.java).apply {
                putExtra("EXTRA_VALUE_TEXT", (button as Button).text.toString())
                putExtra("EXTRA_VALUE_COLOR", ContextCompat.getColor(this@MyTreeActivity, colorId))
            }
            startActivity(intent)
        }
    }
}