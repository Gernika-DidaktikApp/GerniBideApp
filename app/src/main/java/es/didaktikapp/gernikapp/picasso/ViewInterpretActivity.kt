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
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.databinding.PicassoViewInterpretBinding
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

class ViewInterpretActivity : BaseMenuActivity() {

    private lateinit var binding: PicassoViewInterpretBinding
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager
    private var eventoEstadoId: String? = null
    private var currentQuestionIndex = 0
    private var correctAnswers = 0

    companion object {
        private const val KEY_QUESTION_INDEX = "question_index"
        private const val KEY_CORRECT_ANSWERS = "correct_answers"
        private const val PREFS_NAME = "view_interpret_progress"
        private const val KEY_HAS_SAVED_PROGRESS = "has_saved_progress"
        private const val KEY_TEST_COMPLETED = "test_completed"

        fun hasSavedProgress(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_HAS_SAVED_PROGRESS, false)
        }

        fun isTestCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            return prefs.getBoolean(KEY_TEST_COMPLETED, false)
        }

        fun loadProgress(context: Context): Pair<Int, Int>? {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_HAS_SAVED_PROGRESS, false)) {
                return null
            }
            val questionIndex = prefs.getInt(KEY_QUESTION_INDEX, 0)
            val correctAnswers = prefs.getInt(KEY_CORRECT_ANSWERS, 0)
            return Pair(questionIndex, correctAnswers)
        }

        fun clearProgress(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

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
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)
        binding = PicassoViewInterpretBinding.inflate(layoutInflater, contentContainer, true)
        iniciarEvento()
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

    private fun setupOptionListeners() {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)

        options.forEachIndexed { index, button ->
            button.setOnClickListener {
                checkAnswer(index, button)
            }
        }
    }

    private fun loadQuestion() {
        if (currentQuestionIndex < questions.size) {
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

    private fun getStringByKey(key: String): String {
        val resId = resources.getIdentifier(key, "string", packageName)
        return if (resId != 0) getString(resId) else key
    }

    private fun resetButtons() {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { button ->
            button.isEnabled = true
            button.setBackgroundResource(R.drawable.bg_opcion_normal)
            button.setTextColor(Color.parseColor("#39455F")) // txtPrincipal
        }
    }

    private fun checkAnswer(selectedIndex: Int, selectedButton: Button) {
        val question = questions[currentQuestionIndex]
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

    private fun loadNextQuestion() {
        currentQuestionIndex++
        saveProgress() // Guardar progreso después de cada pregunta

        if (currentQuestionIndex < questions.size) {
            loadQuestion()
        } else {
            showFinalResults()
        }
    }

    override fun onPause() {
        super.onPause()
        // Guardar progreso cuando el usuario sale de la actividad
        if (currentQuestionIndex < questions.size) {
            saveProgress()
        }
    }

    private fun showFinalResults() {
        // Guardar resultado final en lugar de limpiar
        saveProgress(testCompleted = true)

        // Mostrar botón y guardar progreso de actividad completada
        binding.btnBack.visibility = View.VISIBLE
        val progressPrefs = getSharedPreferences("picasso_progress", MODE_PRIVATE)
        progressPrefs.edit().putBoolean("view_interpret_completed", true).apply()

        completarEvento()

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

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(juegoId, Actividades.Picasso.ID, Actividades.Picasso.VIEW_INTERPRET)) {
                is Resource.Success -> eventoEstadoId = result.data.id
                is Resource.Error -> Log.e("ViewInterpret", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(estadoId, correctAnswers * 100.0)) {
                is Resource.Success -> Log.d("ViewInterpret", "Completado: ${correctAnswers * 100}")
                is Resource.Error -> Log.e("ViewInterpret", "Error: ${result.message}")
                is Resource.Loading -> { }
            }
        }
    }
}