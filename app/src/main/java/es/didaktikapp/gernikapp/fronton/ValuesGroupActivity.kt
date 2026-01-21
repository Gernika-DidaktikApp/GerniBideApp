package es.didaktikapp.gernikapp.fronton

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toColorInt
import com.google.android.flexbox.FlexboxLayout
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class ValuesGroupActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.fronton_values_group

    override fun onContentInflated() {
        val input = contentContainer.findViewById<EditText>(R.id.inputValor)
        val btnAnadir = contentContainer.findViewById<Button>(R.id.btnAnadir)
        val btnFinalizar = contentContainer.findViewById<Button>(R.id.btnFinalizar)
        val container = contentContainer.findViewById<FlexboxLayout>(R.id.valoresContainer)
        val mensajeFinal = contentContainer.findViewById<TextView>(R.id.mensajeFinal)
        val btnVolver = contentContainer.findViewById<Button>(R.id.btnVolver)

        btnAnadir.setOnClickListener {
            val texto = input.text.toString().trim()

            if (texto.isEmpty()) {
                Toast.makeText(this, getString(R.string.gehitu_aurretik_sartu_balioa), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bubble = TextView(this).apply {
                text = texto
                setPadding(40, 24, 40, 24)
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER

                val colores = listOf(
                    "#3F51B5", "#009688", "#FF5722", "#9C27B0", "#FFC107",
                    "#4CAF50", "#E91E63", "#2196F3", "#FF9800", "#795548"
                )

                background = AppCompatResources.getDrawable(
                    this@ValuesGroupActivity,
                    R.drawable.fronton_bubble_background
                )
                background?.setTint(colores.random().toColorInt())

                val params = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(16, 16, 16, 16)
                layoutParams = params
            }

            container.addView(bubble, container.childCount - 1)
            input.text.clear()
        }

        btnVolver.setOnClickListener {
            finish()
        }

        btnFinalizar.setOnClickListener {
            mensajeFinal.visibility = View.VISIBLE
            btnVolver.visibility = View.VISIBLE
        }
    }
}