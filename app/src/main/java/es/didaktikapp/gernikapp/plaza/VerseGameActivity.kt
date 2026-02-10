package es.didaktikapp.gernikapp.plaza

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
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.plaza.models.VerseQuestion
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch

/**
 * Actividad del juego de completar versos tradicionales vascos (bertsoak).
 * Presenta versos con opciones de respuesta múltiple y el usuario debe
 * seleccionar la continuación correcta de cada verso.
 *
 * @author Arantxa Main
 * @version 1.0
 * @see VerseQuestion
 */
class VerseGameActivity : BaseMenuActivity() {

    /** Texto del verso inicial mostrado al usuario. */
    private lateinit var tvVersoInicial: TextView

    /** Grupo de opciones de respuesta. */
    private lateinit var radioGroup: RadioGroup

    /** Primera opción de respuesta. */
    private lateinit var rbOpcion1: RadioButton

    /** Segunda opción de respuesta. */
    private lateinit var rbOpcion2: RadioButton

    /** Tercera opción de respuesta. */
    private lateinit var rbOpcion3: RadioButton

    /** Botón para comprobar la respuesta seleccionada. */
    private lateinit var btnComprobar: Button

    /** Botón para volver al menú principal del módulo Plaza. */
    private lateinit var btnBack: Button

    /** Repositorio para gestionar el progreso de la actividad en la API. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión que contiene tokens y el juegoId necesario para la API. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad devuelto por la API. */
    private var actividadProgresoId: String? = null

    /** Lista de preguntas del juego. */
    private val preguntas = mutableListOf<VerseQuestion>()

    /** Índice de la pregunta actual. */
    private var preguntaActual = 0

    /** Número de respuestas correctas acumuladas. */
    private var aciertos = 0

    /** Devuelve el layout asociado a esta actividad. */
    override fun getContentLayoutId() = R.layout.plaza_verse_game

    /**
     * Inicializa la actividad:
     * - Registra inicio en LogManager
     * - Inicializa vistas y preguntas
     * - Inicia actividad en la API
     * - Muestra la primera pregunta
     * - Configura listeners de botones
     */
    override fun onContentInflated() {
        LogManager.write(this@VerseGameActivity, "VerseGameActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        inicializarVistas()
        inicializarPreguntas()
        iniciarActividad()
        mostrarPregunta()
        setupButtons()
    }

    /**
     * Inicializa todas las vistas del layout.
     */
    private fun inicializarVistas() {
        tvVersoInicial = findViewById(R.id.tvVersoInicial)
        radioGroup = findViewById(R.id.radioGroupOpciones)
        rbOpcion1 = findViewById(R.id.rbOpcion1)
        rbOpcion2 = findViewById(R.id.rbOpcion2)
        rbOpcion3 = findViewById(R.id.rbOpcion3)
        btnComprobar = findViewById(R.id.btnComprobar)
        btnBack = findViewById(R.id.btnBack)

        // Check if activity was previously completed
        val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)
        if (prefs.getBoolean("verse_game_completed", false)) {
            btnBack.isEnabled = true
        }
    }

    /**
     * Inicializa la lista de preguntas del juego.
     * Cada pregunta contiene:
     * - Verso inicial
     * - Tres opciones
     * - Índice de la respuesta correcta
     */
    private fun inicializarPreguntas() {
        preguntas.add(
            VerseQuestion(
                1,
                getString(R.string.jendea_etorri_da),
                listOf(
                    getString(R.string.barazkiak_erostera),
                    getString(R.string.gerrikoa_jantzi),
                    getString(R.string.baina_ni_trizturaz)
                ),
                0
            )
        )
    }

    /**
     * Muestra la pregunta actual en pantalla:
     * - Verso inicial
     * - Opciones de respuesta
     * - Restablece el estado visual
     */
    private fun mostrarPregunta() {
        if (preguntaActual < preguntas.size) {
            val pregunta = preguntas[preguntaActual]
            tvVersoInicial.text = pregunta.versoInicial
            rbOpcion1.text = pregunta.opciones[0]
            rbOpcion2.text = pregunta.opciones[1]
            rbOpcion3.text = pregunta.opciones[2]

            radioGroup.clearCheck()
            habilitarOpciones(true)
            btnComprobar.isVisible = true
        }
    }

    /**
     * Configura los listeners de los botones:
     * - Comprobar respuesta
     * - Volver al menú Plaza
     */
    private fun setupButtons() {
        btnComprobar.setOnClickListener {
            comprobarRespuesta()
        }

        btnBack.setOnClickListener {
            LogManager.write(this@VerseGameActivity, "Usuario salió de VerseGameActivity")
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Comprueba la respuesta seleccionada por el usuario.
     */
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

        LogManager.write( this@VerseGameActivity, "Respuesta seleccionada: $selectedIndex (correcta=${pregunta.respuestaCorrecta})" )

        if (selectedIndex == pregunta.respuestaCorrecta) {
            LogManager.write(this@VerseGameActivity, "Respuesta correcta")
            aciertos++
            mostrarFeedbackCorrecto(selectedId)
        } else {
            LogManager.write(this@VerseGameActivity, "Respuesta incorrecta")
            mostrarFeedbackIncorrecto(selectedId)
            resaltarRespuestaCorrecta(pregunta.respuestaCorrecta)
        }

        habilitarOpciones(false)
        btnComprobar.isVisible = false

        LogManager.write(this@VerseGameActivity, "Avanzando a la siguiente pregunta")

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

    /**
     * Muestra feedback visual para una respuesta correcta.
     */
    private fun mostrarFeedbackCorrecto(selectedId: Int) {
        val radioButton = findViewById<RadioButton>(selectedId)
        radioButton.setBackgroundResource(R.drawable.plaza_bg_correct)
        Toast.makeText(this, getString(R.string.verse_game_oso_ondo), Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra feedback visual para una respuesta incorrecta.
     */
    private fun mostrarFeedbackIncorrecto(selectedId: Int) {
        val radioButton = findViewById<RadioButton>(selectedId)
        radioButton.setBackgroundResource(R.drawable.plaza_bg_incorrect)
    }

    /**
     * Resalta visualmente la opción correcta después de un error.
     */
    private fun resaltarRespuestaCorrecta(correcta: Int) {
        val correctRadioButton = when (correcta) {
            0 -> rbOpcion1
            1 -> rbOpcion2
            2 -> rbOpcion3
            else -> return
        }
        correctRadioButton.setBackgroundResource(R.drawable.plaza_bg_correct)
    }

    /**
     * Habilita o deshabilita las opciones de respuesta.
     */
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


    /**
     * Muestra el resultado final del juego:
     * - Guarda progreso en SharedPreferences
     * - Envía puntuación a la API
     * - Desbloquea la zona Plaza si corresponde
     */
    private fun mostrarResultadoFinal() {
        LogManager.write( this@VerseGameActivity, "Juego completado con $aciertos aciertos de ${preguntas.size}" )
        btnBack.isEnabled = true
        completarActividad()

        // Save progress
        val score = aciertos * 100f
        val prefs = getSharedPreferences("plaza_progress", MODE_PRIVATE)
        prefs.edit {
            putBoolean("verse_game_completed", true)
            putFloat("verse_game_score", score)
        }
        ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.PLAZA)
    }

    /**
     * Inicia la actividad en la API del juego.
     * Guarda el ID de progreso devuelto.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Plaza.ID, Puntos.Plaza.VERSE_GAME)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@VerseGameActivity, "API iniciarActividad PLAZA_VERSE_GAME id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("VerseGame", "Error: ${result.message}")
                    LogManager.write(this@VerseGameActivity, "Error iniciarActividad PLAZA_VERSE_GAME: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa la actividad en la API enviando la puntuación final.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, (aciertos * 100).toDouble())) {
                is Resource.Success -> {
                    Log.d("VerseGame", "Completado")
                    LogManager.write(this@VerseGameActivity, "API completarActividad PLAZA_VERSE_GAME puntuación=${aciertos * 100}")
                }
                is Resource.Error -> {
                    Log.e("VerseGame", "Error: ${result.message}")
                    LogManager.write(this@VerseGameActivity, "Error completarActividad PLAZA_VERSE_GAME: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}
