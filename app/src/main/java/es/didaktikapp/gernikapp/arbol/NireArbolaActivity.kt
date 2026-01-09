package com.example.didaktikarbol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class NireArbolaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nire_arbola)

        setupButton(R.id.btnFriendship, R.color.valueFriendship)
        setupButton(R.id.btnFreedom, R.color.valueFreedom)
        setupButton(R.id.btnSolidarity, R.color.valueSolidarity)
        setupButton(R.id.btnRespect, R.color.valueRespect)
        setupButton(R.id.btnPeace, R.color.valuePeace)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupButton(buttonId: Int, colorId: Int) {
        findViewById<Button>(buttonId).setOnClickListener { button ->
            val intent = Intent(this, ArbolInteractivoActivity::class.java).apply {
                putExtra("EXTRA_VALUE_TEXT", (button as Button).text.toString())
                putExtra("EXTRA_VALUE_COLOR", ContextCompat.getColor(this@NireArbolaActivity, colorId))
            }
            startActivity(intent)
        }
    }
}
