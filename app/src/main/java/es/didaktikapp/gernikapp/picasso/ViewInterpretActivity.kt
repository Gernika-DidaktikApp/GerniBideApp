package es.didaktikapp.gernikapp.picasso

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.LogManager
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.databinding.PicassoViewInterpretBinding
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import kotlinx.coroutines.launch

/**
 * Activity de quiz de interpretación de elementos del Guernica.
 * Presenta 6 preguntas sobre los elementos de la obra con 4 opciones cada una.
 * Guarda el progreso y permite continuar desde donde se quedó el usuario.
 *
 * Características:
 * - 6 preguntas con imágenes de elementos del Guernica
 * - Sistema de guardado automático de progreso
 * - Permite continuar test interrumpido o empezar de nuevo
 * - Muestra resultados con sistema de estrellas (⭐) según aciertos
 * - Retroalimentación visual de respuestas correctas/incorrectas
 *
 * @property binding ViewBinding del layout picasso_view_interpret.xml
 * @property gameRepository Repositorio para gestionar eventos del juego
 * @property tokenManager Gestor de tokens JWT y juegoId
 * @property actividadProgresoId ID del estado del evento actual (puede ser null)
 * @property currentQuestionIndex Índice de la pregunta actual (0-5)
 * @property correctAnswers Número de respuestas correctas acumuladas
 *
 * Condiciones:
 * - Requiere SharedPreferences "view_interpret_progress" para guardar progreso
 * - Requiere SharedPreferences "picasso_progress" para marcar actividad completada
 * - Las preguntas y opciones se obtienen de strings.xml por idioma
 * - Las imágenes deben existir en drawable: picasso_zaldia, picasso_ama_haurrarekin, etc.
 * - Sistema de puntuación: correctAnswers * 100.0 enviado a la API
 *
 * @see Question
 * @author Wara Pacheco
 */
class ViewInterpretActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoViewInterpretBinding
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var actividadProgresoId: String? = null
    private var currentQuestionIndex = 0
    private var correctAnswers = 0

    companion object {
        private const val KEY_QUESTION_INDEX = "question_index"
        private const val KEY_CORRECT_ANSWERS = "correct_answers"
        private const val PREFS_NAME = "view_interpret_progress"
        private const val KEY_HAS_SAVED_PROGRESS = "has_saved_progress"
        private const val KEY_TEST_COMPLETED = "test_completed"

        /**
         * Verifica si existe progreso guardado del quiz.
         *
         * @param context Contexto de la aplicación
         * @return true si hay progreso guardado, false en caso contrario
         */
        fun hasSavedProgress(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_HAS_SAVED_PROGRESS, false)
        }

        /**
         * Verifica si el test fue completado anteriormente.
         *
         * @param context Contexto de la aplicación
         * @return true si el test está completado, false en caso contrario
         */
        fun isTestCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_TEST_COMPLETED, false)
        }

        /**
         * Carga el progreso guardado del quiz.
         *
         * @param context Contexto de la aplicación
         * @return Pair con (índice de pregunta, respuestas correctas) o null si no hay progreso
         */
        fun loadProgress(context: Context): Pair<Int, Int>? {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_HAS_SAVED_PROGRESS, false)) {
                return null
            }
            val questionIndex = prefs.getInt(KEY_QUESTION_INDEX, 0)
            val correctAnswers = prefs.getInt(KEY_CORRECT_ANSWERS, 0)
            return Pair(questionIndex, correctAnswers)
        }

        /**
         * Limpia todo el progreso guardado del quiz.
         *
         * @param context Contexto de la aplicación
         */
        fun clearProgress(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

    /**
     * Clase de datos que representa una pregunta del quiz.
     *
     * @property imageName Nombre del recurso drawable de la imagen del elemento (sin extensión)
     * @property questionKey Key del string resource para la pregunta
     * @property optionsKeys Lista de 4 keys de string resources para las opciones
     * @property correctAnswerIndex Índice de la respuesta correcta (0-3)
     * @property feedbackKey Key del string resource para el feedback (no usado actualmente)
     */
    private data class Question(
        val imageName: String,
        val questionKey: String,
        val optionsKeys: List<String>,
        val correctAnswerIndex: Int,
        val feedbackKey: String
    )

    private val questions = listOf(
        Question("picasso_zaldia", "view_interpret_q1", listOf("view_interpret_q1_opt1", "view_interpret_q1_opt2", "view_interpret_q1_opt3", "view_interpret_q1_opt4"), 1, "view_interpret_q1_feedback"),
        Question("picasso_ama_haurrarekin", "view_interpret_q2", listOf("view_interpret_q2_opt1", "view_interpret_q2_opt2", "view_interpret_q2_opt3", "view_interpret_q2_opt4"), 1, "view_interpret_q2_feedback"),
        Question("picasso_argia", "view_interpret_q3", listOf("view_interpret_q3_opt1", "view_interpret_q3_opt2", "view_interpret_q3_opt3", "view_interpret_q3_opt4"), 1, "view_interpret_q3_feedback"),
        Question("picasso_zezena", "view_interpret_q4", listOf("view_interpret_q4_opt1", "view_interpret_q4_opt2", "view_interpret_q4_opt3", "view_interpret_q4_opt4"), 2, "view_interpret_q4_feedback"),
        Question("picasso_oihua", "view_interpret_q5", listOf("view_interpret_q5_opt1", "view_interpret_q5_opt2", "view_interpret_q5_opt3", "view_interpret_q5_opt4"), 2, "view_interpret_q5_feedback"),
        Question("picasso_gerlaria", "view_interpret_q6", listOf("view_interpret_q6_opt1", "view_interpret_q6_opt2", "view_interpret_q6_opt3", "view_interpret_q6_opt4"), 1, "view_interpret_q6_feedback")
    )

    override fun onContentInflated() {
        LogManager.write(this@ViewInterpretActivity, "ViewInterpretActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)
        binding = PicassoViewInterpretBinding.inflate(layoutInflater, contentContainer, true)
        iniciarActividad()
        setupOptionListeners()
        binding.nextButton.setOnClickListener {
            loadNextQuestion()
        }
        setupBackButton()
        checkForSavedProgress()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_QUESTION_INDEX, currentQuestionIndex)
        outState.putInt(KEY_CORRECT_ANSWERS, correctAnswers)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentQuestionIndex = savedInstanceState.getInt(KEY_QUESTION_INDEX, 0)
        correctAnswers = savedInstanceState.getInt(KEY_CORRECT_ANSWERS, 0)
    }

    /**
     * Configura el botón de retroceso.
     * Solo se muestra si la actividad ya fue completada anteriormente.
     */
    private fun setupBackButton() {
        val progressPrefs = getSharedPreferences("picasso_progress", MODE_PRIVATE)

        // Si ya estaba completada, mostrar botón
        if (progressPrefs.getBoolean("view_interpret_completed", false)) {
            binding.btnBack.visibility = View.VISIBLE
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Verifica si existe progreso guardado y muestra el diálogo correspondiente.
     *
     * Casos:
     * - Test completado: Muestra resultado anterior con opción de repetir o salir
     * - Test en progreso: Permite continuar desde donde lo dejó o empezar de nuevo
     * - Sin progreso: Carga la primera pregunta directamente
     */
    private fun checkForSavedProgress() {
        if (hasSavedProgress(this)) {
            if (isTestCompleted(this)) {
                // Test completado anteriormente - mostrar calificación
                val progress = loadProgress(this)
                val previousScore = progress?.second ?: 0
                val stars = when (previousScore) {
                    6 -> "⭐⭐⭐⭐⭐"
                    5 -> "⭐⭐⭐⭐"
                    4 -> "⭐⭐⭐"
                    3 -> "⭐⭐"
                    else -> "⭐"
                }

                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.view_interpret_previous_result_title))
                    .setMessage(getString(R.string.view_interpret_previous_result_message, previousScore, questions.size, stars))
                    .setPositiveButton(getString(R.string.view_interpret_redo_test)) { dialog, _ ->
                        dialog.dismiss()
                        clearProgress(this)
                        currentQuestionIndex = 0
                        correctAnswers = 0
                        loadQuestion()
                    }
                    .setNegativeButton(getString(R.string.view_interpret_exit)) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // Test en progreso - mostrar diálogo para continuar o empezar de nuevo
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.view_interpret_load_title))
                    .setMessage(getString(R.string.view_interpret_load_message))
                    .setPositiveButton(getString(R.string.view_interpret_load_continue)) { dialog, _ ->
                        dialog.dismiss()
                        loadSavedProgress()
                    }
                    .setNegativeButton(getString(R.string.view_interpret_load_start_new)) { dialog, _ ->
                        dialog.dismiss()
                        clearProgress(this)
                        loadQuestion()
                    }
                    .setCancelable(false)
                    .show()
            }
        } else {
            loadQuestion()
        }
    }

    /**
     * Carga el progreso guardado y restaura el estado del quiz.
     * Si no hay progreso, inicia desde la primera pregunta.
     */
    private fun loadSavedProgress() {
        val progress = loadProgress(this)
        if (progress != null) {
            currentQuestionIndex = progress.first
            correctAnswers = progress.second
            loadQuestion()
        } else {
            loadQuestion()
        }
    }

    /**
     * Guarda el progreso actual del quiz en SharedPreferences.
     *
     * @param testCompleted true si el test está completado, false si está en progreso
     */
    private fun saveProgress(testCompleted: Boolean = false) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_HAS_SAVED_PROGRESS, true)
            putBoolean(KEY_TEST_COMPLETED, testCompleted)
            putInt(KEY_QUESTION_INDEX, currentQuestionIndex)
            putInt(KEY_CORRECT_ANSWERS, correctAnswers)
            apply()
        }
    }

    /**
     * Configura los listeners de los 4 botones de opciones.
     * Cada botón llama a checkAnswer() con su índice correspondiente.
     */
    private fun setupOptionListeners() {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        options.forEachIndexed { index, button ->
            button.setOnClickListener {
                checkAnswer(index, button)
            }
        }
    }

    /**
     * Carga la pregunta actual en la interfaz.
     *
     * Proceso:
     * 1. Actualiza el indicador de progreso (ej: "3/6")
     * 2. Carga la imagen del elemento del Guernica desde drawable
     * 3. Carga la pregunta y las 4 opciones desde strings.xml
     * 4. Resetea el estado visual de los botones
     * 5. Oculta el feedback y botón "Siguiente"
     */
    private fun loadQuestion() {
        if (currentQuestionIndex < questions.size) {
            LogManager.write( this@ViewInterpretActivity, "Cargando pregunta ${currentQuestionIndex + 1} de ${questions.size}" )

            val question = questions[currentQuestionIndex]

            // Actualizar UI
            binding.progressText.text = "${currentQuestionIndex + 1}/${questions.size}"

            // Cargar imagen como recurso drawable
            val imageResId = resources.getIdentifier(
                question.imageName,
                "drawable",
                packageName
            )
            if (imageResId != 0) {
                binding.elementImage.setImageResource(imageResId)
            }

            // Configurar pregunta y opciones desde strings
            binding.questionText.text = getStringByKey(question.questionKey)
            binding.option1.text = getStringByKey(question.optionsKeys[0])
            binding.option2.text = getStringByKey(question.optionsKeys[1])
            binding.option3.text = getStringByKey(question.optionsKeys[2])
            binding.option4.text = getStringByKey(question.optionsKeys[3])

            // Resetear estado de botones
            resetButtons()

            // Ocultar feedback y botón siguiente
            binding.feedbackText.visibility = View.GONE
            binding.nextButton.visibility = View.GONE
        }
    }

    /**
     * Obtiene un string de recursos usando su key.
     * Si no encuentra el recurso, devuelve la key como fallback.
     *
     * @param key Key del string resource (ej: "view_interpret_q1")
     * @return El string traducido o la key si no existe el recurso
     */
    private fun getStringByKey(key: String): String {
        val resId = resources.getIdentifier(key, "string", packageName)
        return if (resId != 0) getString(resId) else key
    }

    /**
     * Resetea el estado visual de los 4 botones de opciones.
     * Los habilita y restaura el fondo y color de texto por defecto.
     */
    private fun resetButtons() {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { button ->
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.bg_opcion_normal)
            button.setTextColor(Color.parseColor("#39455F")) // txtPrincipal
        }
    }

    /**
     * Verifica si la respuesta seleccionada es correcta y aplica retroalimentación visual.
     *
     * Comportamiento:
     * - Deshabilita todos los botones
     * - Si es correcta: fondo verde (#2E7D32), incrementa correctAnswers
     * - Si es incorrecta: fondo rojo (#D84315), resalta también la correcta en verde
     * - Muestra el botón "Siguiente" para continuar
     *
     * @param selectedIndex Índice de la opción seleccionada (0-3)
     * @param selectedButton Botón que fue presionado
     */
    private fun checkAnswer(selectedIndex: Int, selectedButton: Button) {
        val question = questions[currentQuestionIndex]

        LogManager.write( this@ViewInterpretActivity, "Respuesta seleccionada: $selectedIndex (correcta=${question.correctAnswerIndex})" )

        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        // Deshabilitar todos los botones
        options.forEach { it.isEnabled = false }

        if (selectedIndex == question.correctAnswerIndex) {
            // Respuesta correcta
            selectedButton.setBackgroundResource(R.drawable.bg_opcion_correcta)
            selectedButton.setTextColor(Color.parseColor("#2E7D32"))
            correctAnswers++
        } else {
            // Respuesta incorrecta
            selectedButton.setBackgroundResource(R.drawable.bg_opcion_incorrecta)
            selectedButton.setTextColor(Color.parseColor("#D84315"))

            // Mostrar la respuesta correcta
            options[question.correctAnswerIndex].setBackgroundResource(R.drawable.bg_opcion_correcta)
            options[question.correctAnswerIndex].setTextColor(Color.parseColor("#2E7D32"))
        }

        // Mostrar botón siguiente
        binding.nextButton.visibility = View.VISIBLE
    }

    /**
     * Avanza a la siguiente pregunta o muestra los resultados finales.
     * Guarda el progreso después de cada pregunta.
     *
     * Flujo:
     * - Incrementa currentQuestionIndex
     * - Si hay más preguntas: carga la siguiente
     * - Si no hay más: muestra resultados finales
     */
    private fun loadNextQuestion() {
        LogManager.write( this@ViewInterpretActivity, "Avanzando a la siguiente pregunta (actual=$currentQuestionIndex)" )

        currentQuestionIndex++
        saveProgress() // Guardar progreso después de cada pregunta

        if (currentQuestionIndex < questions.size) {
            loadQuestion()
        } else {
            showFinalResults()
        }
    }

    /**
     * Guarda el progreso automáticamente cuando el usuario sale de la actividad.
     * Solo guarda si el test no está completado.
     */
    override fun onPause() {
        super.onPause()
        // Guardar progreso cuando el usuario sale de la actividad
        if (currentQuestionIndex < questions.size) {
            saveProgress()
        }
    }

    /**
     * Muestra los resultados finales del quiz con sistema de estrellas.
     *
     * Sistema de puntuación:
     * - 6 aciertos: ⭐⭐⭐⭐⭐
     * - 5 aciertos: ⭐⭐⭐⭐
     * - 4 aciertos: ⭐⭐⭐
     * - 3 aciertos: ⭐⭐
     * - 0-2 aciertos: ⭐
     *
     * Acciones:
     * - Marca el test como completado
     * - Habilita el botón de retroceso
     * - Guarda progreso en "picasso_progress"
     * - Completa el evento en la API con puntuación
     */
    private fun showFinalResults() {
        LogManager.write( this@ViewInterpretActivity, "Test completado con $correctAnswers aciertos" )

        // Guardar resultado final en lugar de limpiar
        saveProgress(testCompleted = true)

        // Mostrar botón y guardar progreso de actividad completada
        binding.btnBack.visibility = View.VISIBLE
        val score = correctAnswers * 100f
        val progressPrefs = getSharedPreferences("picasso_progress", MODE_PRIVATE)
        progressPrefs.edit()
            .putBoolean("view_interpret_completed", true)
            .putFloat("view_interpret_score", score)
            .apply()
        ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.PICASSO)

        completarActividad()

        val stars = when (correctAnswers) {
            6 -> "⭐⭐⭐⭐⭐"
            5 -> "⭐⭐⭐⭐"
            4 -> "⭐⭐⭐"
            3 -> "⭐⭐"
            else -> "⭐"
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.view_interpret_results_title))
            .setMessage(getString(R.string.view_interpret_results_message, correctAnswers, questions.size, stars))
            .setPositiveButton(getString(R.string.view_interpret_results_finish)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Inicia el evento en la API del juego.
     * Requiere un juegoId válido del TokenManager.
     * Guarda el actividadProgresoId devuelto por la API para completar el evento después.
     *
     * IDs utilizados:
     * - idActividad: Puntos.Picasso.ID
     * - idEvento: Puntos.Picasso.VIEW_INTERPRET
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Picasso.ID, Puntos.Picasso.VIEW_INTERPRET)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@ViewInterpretActivity, "API iniciarActividad VIEW_INTERPRET id=$actividadProgresoId")
                }
                is Resource.Error -> {
                    Log.e("ViewInterpret", "Error: ${result.message}")
                    LogManager.write(this@ViewInterpretActivity, "Error iniciarActividad VIEW_INTERPRET: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Completa el evento en la API del juego.
     * Requiere un actividadProgresoId válido obtenido de iniciarActividad().
     *
     * @see iniciarActividad
     * Puntuación enviada: correctAnswers * 100.0 (0-600 puntos)
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, correctAnswers * 100.0)) {
                is Resource.Success -> {
                    Log.d("ViewInterpret", "Completado: ${correctAnswers * 100}")
                    LogManager.write(this@ViewInterpretActivity, "API completarActividad VIEW_INTERPRET puntuación=${correctAnswers * 100}")
                }
                is Resource.Error -> {
                    Log.e("ViewInterpret", "Error: ${result.message}")
                    LogManager.write(this@ViewInterpretActivity, "Error completarActividad VIEW_INTERPRET: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}