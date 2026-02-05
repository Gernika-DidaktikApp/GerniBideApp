package es.didaktikapp.gernikapp.plaza

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.plaza.models.VerseQuestion
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Activity del juego de completar versos tradicionales.
 */
class VerseGameActivity : BaseMenuActivity() {

    private lateinit var tvVersoInicial: TextView
    private lateinit var tvProgreso: TextView
    private lateinit var tvAciertos: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbOpcion1: RadioButton
    private lateinit var rbOpcion2: RadioButton
    private lateinit var rbOpcion3: RadioButton
    private lateinit var btnComprobar: Button
    private lateinit var btnBack: Button
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var eventoEstadoId: String? = null

    private val preguntas = mutableListOf<VerseQuestion>()
    private var preguntaActual = 0
    private var aciertos = 0

    override fun getContentLayoutId() = R.layout.plaza_verse_game

    override fun onContentInflated() {
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        inicializarVistas()
        inicializarPreguntas()
        iniciarEvento()
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
        btnBack = findViewById(R.id.btnBack)

        // Check if activity was previously completed
        val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("verse_game_completed", false)) {
            btnBack.isEnabled = true
        }
    }

    private fun inicializarPreguntas() {
        preguntas.add(
            VerseQuestion(
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
            VerseQuestion(
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
            VerseQuestion(
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
            tvProgreso.text = getString(R.string.verse_game_progress, preguntaActual + 1, preguntas.size)

            radioGroup.clearCheck()
            habilitarOpciones(true)
            btnComprobar.isVisible = true
        }
    }

    private fun setupButtons() {
        btnComprobar.setOnClickListener {
            comprobarRespuesta()
        }

        btnBack.setOnClickListener {
            finish()
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
            tvAciertos.text = getString(R.string.verse_game_aciertos, aciertos, preguntas.size)
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
        radioButton.setBackgroundResource(R.drawable.plaza_bg_correct)
        Toast.makeText(this, getString(R.string.verse_game_oso_ondo), Toast.LENGTH_SHORT).show()
    }

    private fun mostrarFeedbackIncorrecto(selectedId: Int) {
        val radioButton = findViewById<RadioButton>(selectedId)
        radioButton.setBackgroundResource(R.drawable.plaza_bg_incorrect)
    }

    private fun resaltarRespuestaCorrecta(correcta: Int) {
        val correctRadioButton = when (correcta) {
            0 -> rbOpcion1
            1 -> rbOpcion2
            2 -> rbOpcion3
            else -> return
        }
        correctRadioButton.setBackgroundResource(R.drawable.plaza_bg_correct)
    }

    private fun habilitarOpciones(enabled: Boolean) {
        rbOpcion1.isEnabled = enabled
        rbOpcion2.isEnabled = enabled
        rbOpcion3.isEnabled = enabled
        if (enabled) {
            rbOpcion1.setTextColor(ContextCompat.getColor(this, R.color.txtPrincipal))
            rbOpcion2.setTextColor(ContextCompat.getColor(this, R.color.txtPrincipal))
            rbOpcion3.setTextColor(ContextCompat.getColor(this, R.color.txtPrincipal))
            rbOpcion1.setBackgroundResource(R.drawable.plaza_bg_product_selector)
            rbOpcion2.setBackgroundResource(R.drawable.plaza_bg_product_selector)
            rbOpcion3.setBackgroundResource(R.drawable.plaza_bg_product_selector)
        }
    }

    private fun mostrarResultadoFinal() {
        tvAciertos.text = getString(R.string.verse_game_aciertos, aciertos, preguntas.size)
        btnBack.isEnabled = true
        completarEvento()

        // Save progress
        val prefs = getSharedPreferences("plaza_progress", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("verse_game_completed", true) }
    }

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(juegoId, Actividades.Plaza.ID, Actividades.Plaza.VERSE_GAME)) {
                is Resource.Success -> eventoEstadoId = result.data.id
                is Resource.Error -> Log.e("VerseGame", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(estadoId, (aciertos * 100).toDouble())) {
                is Resource.Success -> Log.d("VerseGame", "Completado")
                is Resource.Error -> Log.e("VerseGame", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}
