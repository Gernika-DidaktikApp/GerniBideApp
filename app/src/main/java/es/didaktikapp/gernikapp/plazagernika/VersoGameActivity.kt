package es.didaktikapp.gernikapp.plazagernika

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.plazagernika.models.VersoQuestion

class VersoGameActivity : AppCompatActivity() {

    private lateinit var tvVersoInicial: TextView
    private lateinit var tvProgreso: TextView
    private lateinit var tvAciertos: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbOpcion1: RadioButton
    private lateinit var rbOpcion2: RadioButton
    private lateinit var rbOpcion3: RadioButton
    private lateinit var btnComprobar: Button
    private lateinit var btnSiguiente: Button

    private val preguntas = mutableListOf<VersoQuestion>()
    private var preguntaActual = 0
    private var aciertos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plaza_verso_game)

        inicializarVistas()
        inicializarPreguntas()
        mostrarPregunta()
        setupButtons()
    }

    private fun inicializarVistas() {
        tvVersoInicial = findViewById(R.id.tvVersoInicial)
        tvProgreso = findViewById(R.id.tvProgreso)
        tvAciertos = findViewById(R.id.tvAciertos)
        radioGroup = findViewById(R.id.radioGroupOpciones)
        rbOpcion1 = findViewById(R.id.rbOpcion1)
        rbOpcion2 = findViewById(R.id.rbOpcion2)
        rbOpcion3 = findViewById(R.id.rbOpcion3)
        btnComprobar = findViewById(R.id.btnComprobar)
        btnSiguiente = findViewById(R.id.btnSiguiente)
    }

    private fun inicializarPreguntas() {
        preguntas.add(
            VersoQuestion(
                1,
                "Gernikako plazara\nastelehen goizean...",
                listOf(
                    "jendea biltzen da merkatua egitean",
                    "eskola joaten naiz ikastera",
                    "futbola jolasten dut lagunekin"
                ),
                0
            )
        )
        preguntas.add(
            VersoQuestion(
                2,
                "Produktu ederrak\nbaserritik ekarrita...",
                listOf(
                    "dendan erosten ditut",
                    "plazara saltzen dira",
                    "etxean gordetzen dira"
                ),
                1
            )
        )
        preguntas.add(
            VersoQuestion(
                3,
                "Gazta eta piperrak\neztia eta ogia...",
                listOf(
                    "kalean aurkitzen dira",
                    "merkatuan ikusten dira",
                    "mendian hazten dira"
                ),
                1
            )
        )
    }

    private fun mostrarPregunta() {
        if (preguntaActual < preguntas.size) {
            val pregunta = preguntas[preguntaActual]
            tvVersoInicial.text = pregunta.versoInicial
            rbOpcion1.text = pregunta.opciones[0]
            rbOpcion2.text = pregunta.opciones[1]
            rbOpcion3.text = pregunta.opciones[2]
            tvProgreso.text = "${preguntaActual + 1}/${preguntas.size}"

            radioGroup.clearCheck()
            habilitarOpciones(true)
            btnComprobar.isVisible = true
        }
    }

    private fun setupButtons() {
        btnComprobar.setOnClickListener {
            comprobarRespuesta()
        }

        btnSiguiente.setOnClickListener {
            val intent = Intent(this, FotoMisionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun comprobarRespuesta() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            return // No hay selección
        }

        val pregunta = preguntas[preguntaActual]
        val selectedIndex = when (selectedId) {
            R.id.rbOpcion1 -> 0
            R.id.rbOpcion2 -> 1
            R.id.rbOpcion3 -> 2
            else -> -1
        }

        if (selectedIndex == pregunta.respuestaCorrecta) {
            aciertos++
            tvAciertos.text = "Aciertos: $aciertos/${preguntas.size}"
            mostrarFeedbackCorrecto(selectedId)
        } else {
            mostrarFeedbackIncorrecto(selectedId)
            resaltarRespuestaCorrecta(pregunta.respuestaCorrecta)
        }

        habilitarOpciones(false)
        btnComprobar.isVisible = false

        preguntaActual++
        if (preguntaActual < preguntas.size) {
            // Siguiente pregunta después de un delay
            tvVersoInicial.postDelayed({
                mostrarPregunta()
            }, 2000)
        } else {
            // Fin del juego
            mostrarResultadoFinal()
        }
    }

    private fun mostrarFeedbackCorrecto(selectedId: Int) {
        val radioButton = findViewById<RadioButton>(selectedId)
        radioButton.setBackgroundResource(R.drawable.plaza_bg_correcto)
    }

    private fun mostrarFeedbackIncorrecto(selectedId: Int) {
        val radioButton = findViewById<RadioButton>(selectedId)
        radioButton.setBackgroundResource(R.drawable.plaza_bg_incorrecto)
    }

    private fun resaltarRespuestaCorrecta(correcta: Int) {
        val correctRadioButton = when (correcta) {
            0 -> rbOpcion1
            1 -> rbOpcion2
            2 -> rbOpcion3
            else -> return
        }
        correctRadioButton.setBackgroundResource(R.drawable.plaza_bg_correcto)
    }

    private fun habilitarOpciones(enabled: Boolean) {
        rbOpcion1.isEnabled = enabled
        rbOpcion2.isEnabled = enabled
        rbOpcion3.isEnabled = enabled
        if (enabled) {
            rbOpcion1.setTextColor(ContextCompat.getColor(this, R.color.txtPrincipal))
            rbOpcion2.setTextColor(ContextCompat.getColor(this, R.color.txtPrincipal))
            rbOpcion3.setTextColor(ContextCompat.getColor(this, R.color.txtPrincipal))
            rbOpcion1.setBackgroundResource(R.drawable.plaza_bg_producto_selector)
            rbOpcion2.setBackgroundResource(R.drawable.plaza_bg_producto_selector)
            rbOpcion3.setBackgroundResource(R.drawable.plaza_bg_producto_selector)
        }
    }

    private fun mostrarResultadoFinal() {
        tvAciertos.text = "Aciertos: $aciertos/${preguntas.size}"
        btnSiguiente.isVisible = true
    }
}
